package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.MultiEmotionSynergyManager;
import roguelike_emotions.utils.SynergyEffect;

public class Enemy {
	private String nombre;
	private int baseDamage;
	private int baseDefense;
	private double baseSpeed;
	private EnemyRole role;
	private int health;
	private EmotionInstance estadoEmocionalActual;
	private List<EmotionInstance> emocionesActivas = new ArrayList<>();
	private List<EffectDetail> efectosActivos = new ArrayList<>();
	private List<OverTimeHeal> healOverTimeEffects = new ArrayList<>();
	private Map<String, Buff> activeBuffs = new HashMap<>();
	private Map<String, Debuff> activeDebuffs = new HashMap<>();
	private static EmotionDominanceMatrix dominanceMatrix;
	// Cooldown para la habilidad activa
    private boolean canUseActive = true;
    private int cooldownTurns = 0;

    // Flag para pasiva ya aplicada (solo una vez)
    private boolean pasivasAplicadas = false;

	public static void setDominanceMatrix(EmotionDominanceMatrix m) {
		dominanceMatrix = m;
	}

	public Enemy(String nombre, int maxHealth, int baseDamage, int baseDefense, double baseSpeed, EnemyRole role) {
		this.nombre = nombre;
		this.health = maxHealth;
		this.baseDamage = baseDamage;
		this.baseDefense = baseDefense;
		this.baseSpeed = baseSpeed;
		this.role = role;
	}



	public void recibirDanyo(int cantidad) {
		int neto;
		int defensaBase = getDefensa();
		if (getDefensa() > 0) {
			if (cantidad <= defensaBase) {
				setDefensaBase(defensaBase - cantidad);

				neto = 0;
			} else {
				neto = cantidad - defensaBase;
				setDefensaBase(0);
			}
		} else {
			neto = cantidad;
		}

		health = Math.max(0, health - neto);

		CombatLogger.get().log("[Enemy] Recibe "+neto+" de daño. Vida restante: "+health+", Defensa restante: "+getDefensa());
	}

	private void setDefensaBase(int defensa) {
		this.baseDefense = defensa;
	}

	public void tickTurnoEmocional(Player jugador) {

		// ————————————————————————————————
		// 0) Limpiar cualquier "buff de sinergia" que pueda quedar de turno anterior.
		// Así evitamos acumular multiplicadores permanentemente.
		activeBuffs.remove("damageBoost");
		activeBuffs.remove("defenseBoost");
		activeBuffs.remove("speedBoost");
		// 1) Efectos data-driven
		Iterator<EffectDetail> itE = efectosActivos.iterator();
		while (itE.hasNext()) {
			EffectDetail ed = itE.next();
			if (Math.random() < ed.getProbabilidad()) {
				ed.aplicarA(this);
				ed.aplicarA(jugador);
			}
			ed.reducirDuracion(1);
			if (ed.haExpirado())
				itE.remove();
		}

		// 2) Heal-over-time
		Iterator<OverTimeHeal> itH = healOverTimeEffects.iterator();
		while (itH.hasNext()) {
			OverTimeHeal hot = itH.next();
			health = Math.min(health + hot.getAmount(), health);
			hot.reducirDuracion();
			if (hot.getRemainingTurns() <= 0)
				itH.remove();
		}

		// 3) Buffs
		Iterator<Buff> itB = activeBuffs.values().iterator();
		while (itB.hasNext()) {
			Buff b = itB.next();
			b.reducirDuracion();
			if (b.getRemainingTurns() <= 0) {
				CombatLogger.get().log("[Enemy " + nombre + "] Buff " + b.getType() + " expirado");
				itB.remove();
			}
		}

		// 4) Debuffs
		Iterator<Debuff> itD = activeDebuffs.values().iterator();
		while (itD.hasNext()) {
			Debuff d = itD.next();
			d.reducirDuracion();
			if (d.getRemainingTurns() <= 0) {
				CombatLogger.get().log("[Enemy " + nombre + "] Debuff " + d.getType() + " expirado");
				itD.remove();
			}
		}

		// 5) Aplica la pasiva de rol (tu método aplicarPasivas)
        aplicarPasivasUnaSolaVez(jugador);

		// 6) Bonos por fusiones activas según rol
		int numFusiones = emocionesActivas.size();
		this.baseDefense = (int) (this.baseDefense * role.fuseDefenseBonus(numFusiones));
		this.baseDamage = (int) (this.baseDamage * role.fuseDamageBonus(numFusiones));
		// Para SUPPORT, podríamos curir un porcentaje:
		if (role == EnemyRole.SUPPORT && numFusiones > 0) {
			int heal = (int) ((this.baseDamage) * role.fuseHealBonus(numFusiones) * 0.2);
			CombatLogger.get().log("[Support Bonus] "+nombre+" se cura "+heal+")");
			this.health = Math.min(this.health + heal, /* maxHealth si lo guardas */ Integer.MAX_VALUE);
		}
		// Permitir usar activa de nuevo en el próximo turno
		canUseActive = true;

		// ————————————————————————————————
		// 6) Sinergias contextuales entre emociones activas
		// En lugar de mutar danyoBase, defensaBase o velBase de forma permanente,
		// aqui creamos Buffs temporales de 1 turno con los multiplicadores de sinergía.
		List<EmotionType> tiposActivos = new ArrayList<>();
		for (EmotionInstance ei : emocionesActivas) {
			tiposActivos.add(ei.getTipoBase());
		}

		List<SynergyEffect> sinergias = MultiEmotionSynergyManager.getSynergies(tiposActivos);
		for (SynergyEffect se : sinergias) {
			// 6a) Buff temporal de daño
			if (se.getDamageMultiplier() != 1.0) {
				activeBuffs.put("damageBoost", new Buff("damageBoost", se.getDamageMultiplier(), 1));
			}

			// 6b) Buff temporal de defensa
			if (se.getDefenseMultiplier() != 1.0) {
				activeBuffs.put("defenseBoost", new Buff("defenseBoost", se.getDefenseMultiplier(), 1));
			}

			// 6c) Buff temporal de velocidad
			if (se.getSpeedMultiplier() != 1.0) {
				activeBuffs.put("speedBoost", new Buff("speedBoost", se.getSpeedMultiplier(), 1));
			}

			// 6d) Curación continua adicional (HoT)
			if (se.getHotAmount() > 0 && se.getHotTurns() > 0) {
				healOverTimeEffects.add(new OverTimeHeal(se.getHotAmount(), se.getHotTurns()));
			}

			// 6e) Veneno adicional (genera un EffectDetail que aplica "intoxicar" vía JSON)
			if (se.getPoisonAmount() > 0 && se.getPoisonTurns() > 0) {
				EffectDetail poisonDetail = new EffectDetail(
						/* tipo */ roguelike_emotions.mainMechanics.EmotionEffect.VENENO,
						/* intensidad */ se.getPoisonAmount(), /* probabilidad */1.0,
						/* duración */ se.getPoisonTurns());
				efectosActivos.add(poisonDetail);
			}

			// 6f) Stun adicional (genera un EffectDetail que aplica
			// "applyDebuff(\"stun\",1)" vía JSON)
			if (se.getStunTurns() > 0) {
				EffectDetail stunDetail = new EffectDetail(
						/* tipo */ roguelike_emotions.mainMechanics.EmotionEffect.REBOTE, /* intensidad */ 0.0,
						/* probabilidad */1.0, /* duración */ se.getStunTurns());
				efectosActivos.add(stunDetail);
			}

			// 6g) Buff genérico extra (ejemplo: "defenseBoost")
			if (se.getBuffType() != null && se.getBuffTurns() > 0) {
				activeBuffs.put(se.getBuffType(),
						new Buff(se.getBuffType(), se.getBuffMultiplier(), se.getBuffTurns()));
			}

			// 6h) Debuff genérico extra (ejemplo: "silence", "slow", etc.)
			if (se.getDebuffType() != null && se.getDebuffTurns() > 0) {
				activeDebuffs.put(se.getDebuffType(),
						new Debuff(se.getDebuffType(), se.getBuffMultiplier(), se.getDebuffTurns()));
			}
		}

	     if (!canUseActive) {
	            cooldownTurns--;
	            if (cooldownTurns <= 0) {
	                canUseActive = true;
	                CombatLogger.get().log("[Enemy "+nombre+"] Habilidad activa lista nuevamente%n");
	            }
	        }
	}

	public void atacar(Player jugador) {
		int dmg = getDamageAgainst(jugador);
		Attack atk = new Attack();
		efectosActivos.forEach(atk::addEffect);
		atk.applyToPlayer(jugador, dmg);
		CombatLogger.get().log("[Enemy "+nombre+"] ataca e inflige "+dmg+" daño.");

		// habilidad activa de rol
		usarActiva(jugador);
	}

	/**
	 * Calcula daño contra el jugador según dominancia emocional
	 */
	public int getDamageAgainst(Player jugador) {
		double factor = 1.0;
		if (estadoEmocionalActual != null && dominanceMatrix != null) {
			EmotionType tE = estadoEmocionalActual.getTipoBase();
			double suma = 0;
			for (EmotionInstance ej : jugador.getEmocionesActivas()) {
				EmotionType tJ = ej.getTipoBase();
				double pEJ = dominanceMatrix.getPeso(tE, tJ);
				double pJE = dominanceMatrix.getPeso(tJ, tE);
				suma += (pEJ + pJE) / 2;
			}
			if (!jugador.getEmocionesActivas().isEmpty()) {
				factor = suma / jugador.getEmocionesActivas().size();
			}
		}
		return (int) Math.max(0, baseDamage * factor);
	}

	/**
	 * Crea un Attack, le añade sus EffectDetail si fuera el caso, y lo aplica al
	 * jugador (daño + efectos + tickDuracion en Attack).
	 */

	public void aplicarPasivas(Player jugador) {
		int fusions = emocionesActivas.size();

		// 1) Bono general de stats por número de fusiones
		this.baseDefense = (int) (baseDefense * role.fuseDefenseBonus(fusions));
		this.baseDamage = (int) (baseDamage * role.fuseDamageBonus(fusions));

		// 2) Bono “curación” específico de SUPPORT
		if (role == EnemyRole.SUPPORT && fusions > 0) {
			// aplicamos un % de curación sobre maxHealth
			int heal = (int) (getVida() * (role.fuseHealBonus(fusions) - 1.0));
			heal = Math.max(1, heal);
			health = Math.min(getVida(), health + heal);
			CombatLogger.get().log("[Support Pasiva] "+nombre+" se cura "+heal+" puntos (Numero de fusiones: "+fusions+")");
		}

		// 3) Adicional: chequeos de baseEmotion para roles “temáticos”
		EmotionType base = estadoEmocionalActual.getTipoBase();
		switch (role) {
		case TANK:
			if (base == EmotionType.CALMA) {
				// refuerzo extra si está tranquilo
				int extraDef = (int) (baseDefense * 0.10);
				baseDefense += extraDef;
				CombatLogger.get().log("[Tank Pasiva] " + nombre + " gana:"+extraDef+" defensa por CALMA");
			}
			break;
		case DPS:
			if (base == EmotionType.IRA || base == EmotionType.RABIA) {
				int extraDmg = (int) (baseDamage * 0.15);
				baseDamage += extraDmg;
				CombatLogger.get().log("[DPS Pasiva] "+nombre+" gana "+extraDmg+" daño extra por IRA/RABIA%n");
			}
			break;
		case SUPPORT:
			if (base == EmotionType.ALEGRIA) {
				int extraHeal = (int) (jugador.getVida() * 0.10);
				jugador.curar(extraHeal);
				CombatLogger.get().log("[Support Pasiva] "+nombre+" cura al jugador "+extraHeal+" extra por ALEGRIA%n");
			}
			// además reduce 1 turno de debuffs del jugador
			jugador.reduceirDebuffs(1);
			break;
		}
	}

	/** ————————————— Activa de rol ————————————— */
	public void usarActiva(Player jugador) {
		if (!canUseActive)
			return;

		CombatLogger.get().log("[Enemy "+nombre+"] activa habilidad "+role.getActiveSkill());
		switch (role) {
		case TANK:
			// Provocar: obliga al jugador a atacar a este enemigo
			jugador.applyTaunt(1,this);
			break;
		case DPS:
			// Golpe Crítico: daño x2 + stun
			int crit = baseDamage * 2;
			jugador.recibirDanyo(crit);
			jugador.applyDebuff("Stun", 1, /* duración */1);
			break;
		case SUPPORT:
			// Vínculo Emocional: hereda la mitad de los buffs del enemigo
			jugador.inheritBuffsFrom(this, /* ratio */0.5);
			break;
		}

		canUseActive = false;
	}

	 /** Aplica pasivas de rol (solo una vez, cuando se setea la emoción base o en su primer tick) **/
    private void aplicarPasivasUnaSolaVez(Player jugador) {
        if (pasivasAplicadas || estadoEmocionalActual == null) return;

        EmotionType tipoBase = estadoEmocionalActual.getTipoBase();
        switch (role) {
            case TANK:
                // +10% defensa si emoción base es CALMA
                if (tipoBase == EmotionType.CALMA) {
                    baseDefense = (int) (baseDefense * 1.10);
                    CombatLogger.get().log("[Enemy "+nombre+"] Pasiva TANK: +10%% defensa por CALMA%n");
                }
                break;

            case DPS:
                // +15% daño si emoción dominante es IRA o RABIA
                if (tipoBase == EmotionType.IRA || tipoBase == EmotionType.RABIA) {
                    baseDamage = (int) (baseDamage * 1.15);
                    CombatLogger.get().log("[Enemy "+nombre+"] Pasiva DPS: +15%% daño por IRA/RABIA%n");
                }
                break;

            case SUPPORT:
                // +10% curación inicial si emoción base es ALEGRIA
                if (tipoBase == EmotionType.ALEGRIA) {
                    int heal = (int) (health * 0.10);
                    health = Math.min(health + heal, health);
                    CombatLogger.get().log("[Enemy "+nombre+"] Pasiva SUPPORT: cura +10%% vida por ALEGRÍA%n");
                }
                break;
        }
        pasivasAplicadas = true;
    }

	// Al inicio de cada turno del enemigo, recargas su activa:
	public void onTurnStart() {
		canUseActive = true;
	}

	public void setRole(EnemyRole r) {
		this.role = r;
	}

	public EnemyRole getRole() {
		return role;
	}

	public EmotionInstance getEstadoEmocionalActual() {
		return estadoEmocionalActual;
	}

	public void setEstadoEmocionalActual(EmotionInstance estadoEmocionalActual) {
		this.estadoEmocionalActual = estadoEmocionalActual;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setBaseDamage(int baseDamage) {
		this.baseDamage = baseDamage;
	}

	public void setBaseDefense(int baseDefense) {
		this.baseDefense = baseDefense;
	}

	public void setBaseSpeed(double baseSpeed) {
		this.baseSpeed = baseSpeed;
	}

	public void setEmocionesActivas(List<EmotionInstance> emocionesActivas) {
		this.emocionesActivas = emocionesActivas;
	}

	public static EmotionDominanceMatrix getDominanceMatrix() {
		return dominanceMatrix;
	}

	public int getDanyo() {
        double dmg = baseDamage;
        // Buff de tipo "damageBoost"
        Buff b = activeBuffs.get("damageBoost");
        if (b != null) {
            dmg *= b.getMultiplier();
        }
        return (int) Math.max(1, dmg);
    }

    /** Devuelve la defensa real del jugador en este turno (aplica buffs). */
    public int getDefensa() {
        double def = baseDefense;
        // Buff de tipo "defenseBoost"
        Buff b = activeBuffs.get("defenseBoost");
        if (b != null) {
            def *= b.getMultiplier();
        }
        return (int) Math.max(0, def);
    }

    /** Devuelve la velocidad real del jugador en este turno (aplica buffs). */
    public double getVelocidad() {
        double vel = baseSpeed;
        // Buff de tipo "speedBoost"
        Buff b = activeBuffs.get("speedBoost");
        if (b != null) {
            vel *= b.getMultiplier();
        }
        return Math.max(0.1, vel);
    }


	public void setEstadoEmocional(EmotionInstance e) {
		this.estadoEmocionalActual = e;
	}

	public EmotionInstance getEstadoEmocional() {
		return estadoEmocionalActual;
	}

	public void añadirEmocion(EmotionInstance e) {
		emocionesActivas.add(e);
	}

	public List<EmotionInstance> getEmocionesActivas() {
		return Collections.unmodifiableList(emocionesActivas);
	}

	public String getNombre() {
		return nombre;
	}

	public boolean isAlive() {
		return health > 0;
	}

	public int getVida() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}
	public List<EffectDetail> getEfectosActivos() {
		return efectosActivos;
	}

	public void setEfectosActivos(List<EffectDetail> efectosActivos) {
		this.efectosActivos = efectosActivos;
	}

	public List<OverTimeHeal> getHealOverTimeEffects() {
		return healOverTimeEffects;
	}

	public void setHealOverTimeEffects(List<OverTimeHeal> healOverTimeEffects) {
		this.healOverTimeEffects = healOverTimeEffects;
	}

	public Map<String, Buff> getActiveBuffs() {
		return activeBuffs;
	}

	public void setActiveBuffs(Map<String, Buff> activeBuffs) {
		this.activeBuffs = activeBuffs;
	}

	public Map<String, Debuff> getActiveDebuffs() {
		return activeDebuffs;
	}

	public void setActiveDebuffs(Map<String, Debuff> activeDebuffs) {
		this.activeDebuffs = activeDebuffs;
	}
}
