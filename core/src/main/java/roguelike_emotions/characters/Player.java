package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roguelike_emotions.effects.AbstractTimedEffect;
import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.map.EmotionNode;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.MultiEmotionSynergyManager;
import roguelike_emotions.utils.SynergyEffect;

public class Player implements Cloneable {
	// --- Stats base ---
	private int vida = 100;
	private int veneno = 0;
	private int defensaBase = 50;
	private int danyoBase = 15;
	private double velBase = 10;

	// --- Emociones activas ---
	private List<EmotionInstance> emocionesActivas = new ArrayList<>();
	private EmotionNode nodoMentalActual;

	// --- Buffs y Debuffs ---
	private Map<String, Buff> activeBuffs = new HashMap<>();
	private Map<String, Debuff> activeDebuffs = new HashMap<>();

	// --- Heal-over-time (HoT) effects ---
	private List<OverTimeHeal> healOverTimeEffects = new ArrayList<>();
	private List<EffectDetail> efectosActivos = new ArrayList<>();
	private EmotionInstanceFactory emotionFactory = new EmotionInstanceFactory();
	/** Enemigo que nos ha provocado (taunt) */

	private Enemy tauntSource;
	private int tauntTurnsRemaining = 0;

	// --- Manejo de emociones ---
	public void añadirEmocion(EmotionInstance e) {
		if (e != null && !emocionesActivas.contains(e)) {
			emocionesActivas.add(e);
			for (EffectDetail ed : e.getEfectos()) {
				efectosActivos.add(EffectDetail.fromConfig(ed.getTipo()));
			}
		}
	}

	public void setDefensaBase(int defensaBase) {
		this.defensaBase = defensaBase;
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

	public List<OverTimeHeal> getHealOverTimeEffects() {
		return healOverTimeEffects;
	}

	public void setHealOverTimeEffects(List<OverTimeHeal> healOverTimeEffects) {
		this.healOverTimeEffects = healOverTimeEffects;
	}

	public List<EffectDetail> getEfectosActivos() {
		return efectosActivos;
	}

	public void eliminarEmocion(EmotionInstance e) {
		emocionesActivas.remove(e);
	}

	public List<EmotionInstance> getEmocionesActivas() {
		return Collections.unmodifiableList(emocionesActivas);
	}

	public void modifySpeed(double multiplier, int duration) {
		velBase = Math.max(0.1, velBase * multiplier);
		CombatLogger.get().log("[Player] Velocidad ajustada " + multiplier + "→" + velBase);
	}

	// --- Llamadas de los EffectDetail via reflection ---
	public void curar(int cantidad) {
		vida += cantidad;
		CombatLogger.get().log("[Player] +" + cantidad + " vida → " + vida);
	}

	public void intoxicar(int nivel) {
		veneno += nivel;
		CombatLogger.get().log("[Player] +" + nivel + " veneno → " + veneno);
	}

	public void defender(int nivel) {
		defensaBase += nivel;
		CombatLogger.get().log("[Player] +" + nivel + " defensa → " + defensaBase);
	}

	public void applyBuff(String type, double multiplier, int duration) {
		activeBuffs.put(type, new Buff(type, multiplier, duration));
		CombatLogger.get()
				.log("[Player] Buff " + type + "  con mutilipicador " + multiplier + " por " + duration + " turnos");
	}

	public void applyDebuff(String type, double multiplier, int duration) {
		activeDebuffs.put(type, new Debuff(type, multiplier, duration));
		CombatLogger.get().log("[Player] Debuff " + type + " por " + duration + " turnos");
	}

	public void applyHealOverTime(int amount, int turns) {
		healOverTimeEffects.add(new OverTimeHeal(amount, turns));
		CombatLogger.get().log("[Player] Curación continua " + amount + " por " + turns + " turnos");
	}

	// --- Tick de todos los efectos cada turno ---
	public void tickTurnoEmocional() {
		// 1) Efectos data-driven
		Iterator<EffectDetail> eit = efectosActivos.iterator();
		while (eit.hasNext()) {
			EffectDetail ed = eit.next();
			if (Math.random() < ed.getProbabilidad()) {
				ed.aplicarA(this);
			}
			ed.reducirDuracion(1);
			if (ed.haExpirado()) {
				eit.remove();
				CombatLogger.get().log("[Player] Efecto " + ed.getTipo() + " expirado");
			}
		}

		// 2) Heal-over-time
		Iterator<OverTimeHeal> hotIt = healOverTimeEffects.iterator();
		while (hotIt.hasNext()) {
			OverTimeHeal hot = hotIt.next();
			vida += hot.getAmount();
			CombatLogger.get().log("[Player] HoT +" + hot.getAmount() + " vida → " + vida);
			// Aquí usamos reduceTurns() y getRemainingTurns()
			hot.reducirDuracion();
			if (hot.getRemainingTurns() <= 0) {
				hotIt.remove();
			}
		}

		// 3) Buffs
		tickTurnBasedEffects(activeBuffs, "Buff");

		// 4) Debuffs
		tickTurnBasedEffects(activeDebuffs, "Debuff");

		// 5) Veneno
		if (veneno > 0) {
			vida -= veneno;
			CombatLogger.get().log("[Player] Veneno -" + veneno + " vida → " + vida);
		}
		// 6) Taunt
		if (tauntSource != null) {
			tauntTurnsRemaining--;
			if (tauntTurnsRemaining <= 0 || !tauntSource.isAlive()) {
				CombatLogger.get().log("[Player] Ya no estás bajo Provocación.");
				tauntSource = null;
				tauntTurnsRemaining = 0;
			}
		}

		// ————————————————————————————————
		// 7) Sinergias contextuales entre emociones activas
		// En lugar de mutar danyoBase, defensaBase o velBase de forma permanente,
		// aqui creamos Buffs temporales de 1 turno con los multiplicadores de sinergía.
		List<EmotionType> tiposActivos = new ArrayList<>();
		for (EmotionInstance ei : emocionesActivas) {
			tiposActivos.add(ei.getTipoBase());
		}

		List<SynergyEffect> sinergias = MultiEmotionSynergyManager.getSynergies(tiposActivos);
		for (SynergyEffect se : sinergias) {
			// 7a) Buff temporal de daño
			if (se.getDamageMultiplier() != 1.0) {
				activeBuffs.put("damageBoost", new Buff("damageBoost", se.getDamageMultiplier(), 1));
			}

			// 7b) Buff temporal de defensa
			if (se.getDefenseMultiplier() != 1.0) {
				activeBuffs.put("defenseBoost", new Buff("defenseBoost", se.getDefenseMultiplier(), 1));
			}

			// 7c) Buff temporal de velocidad
			if (se.getSpeedMultiplier() != 1.0) {
				activeBuffs.put("speedBoost", new Buff("speedBoost", se.getSpeedMultiplier(), 1));
			}

			// 7d) Curación continua adicional (HoT)
			if (se.getHotAmount() > 0 && se.getHotTurns() > 0) {
				healOverTimeEffects.add(new OverTimeHeal(se.getHotAmount(), se.getHotTurns()));
			}

			// 7e) Veneno adicional (genera un EffectDetail que aplica "intoxicar" vía JSON)
			if (se.getPoisonAmount() > 0 && se.getPoisonTurns() > 0) {
				EffectDetail poisonDetail = new EffectDetail(
						/* tipo */ roguelike_emotions.mainMechanics.EmotionEffect.VENENO,
						/* intensidad */ se.getPoisonAmount(), /* probabilidad */1.0,
						/* duración */ se.getPoisonTurns());
				efectosActivos.add(poisonDetail);
			}

			// 7f) Stun adicional (genera un EffectDetail que aplica
			// "applyDebuff(\"stun\",1)" vía JSON)
			if (se.getStunTurns() > 0) {
				EffectDetail stunDetail = new EffectDetail(
						/* tipo */ roguelike_emotions.mainMechanics.EmotionEffect.REBOTE, /* intensidad */ 0.0,
						/* probabilidad */1.0, /* duración */ se.getStunTurns());
				efectosActivos.add(stunDetail);
			}

			// 7g) Buff genérico extra (ejemplo: "defenseBoost")
			if (se.getBuffType() != null && se.getBuffTurns() > 0) {
				activeBuffs.put(se.getBuffType(),
						new Buff(se.getBuffType(), se.getBuffMultiplier(), se.getBuffTurns()));
			}

			// 7h) Debuff genérico extra (ejemplo: "silence", "slow", etc.)
			if (se.getDebuffType() != null && se.getDebuffTurns() > 0) {
				activeDebuffs.put(se.getDebuffType(),
						new Debuff(se.getDebuffType(), se.getBuffMultiplier(), se.getDebuffTurns()));
			}
		}

	}

	private <T extends AbstractTimedEffect> void tickTurnBasedEffects(Map<String, T> map, String label) {
		Iterator<T> it = map.values().iterator();
		while (it.hasNext()) {
			T effect = it.next();
			// resta un turno y actualiza internamente
			if (effect.reducirDuracion()) {
				it.remove();
				CombatLogger.get().log("[Player] " + label + " " + effect.getNombre() + " expirado");
			}
		}
	}

	public void reduceirDebuffs(int turnos) {
		Iterator<Debuff> it = activeDebuffs.values().iterator();
		while (it.hasNext()) {
			Debuff d = it.next();
			if (d.reducirDuracion()) {
				it.remove();
				CombatLogger.get().log("[Player] Debuff " + d.getType() + " expirado tras taunt.");
			}
		}
	}

	/**
	 * Queda marcado este enemigo como quien provocó (taunt) al jugador.
	 */
	public void setTauntSource(Enemy enemigo) {
		this.tauntSource = enemigo;
		CombatLogger.get().log("[Player] Ha sido provocado por " + enemigo.getNombre());
	}

	public Enemy getTauntSource() {
		return tauntSource;
	}

	/**
	 * Hereda un porcentaje de los "buffs" del enemigo. Aquí los interpretamos como
	 * sus efectos emotivos activos.
	 */
	// --- Heredar buffs de un enemigo (ratio: 0.5 = hereda 50% del multiplicador)
	// ---
	public void inheritBuffsFrom(Enemy fuente, double ratio) {
		// Por cada buff del enemigo, copiamos al jugador con multiplicador*ratio y
		// duración igual
		for (Map.Entry<String, ?> en : fuente.getActiveBuffs().entrySet()) {
			String type = en.getKey();
			Buff buffEne = (Buff) en.getValue();
			double mult = buffEne.getMultiplier();
			int turns = buffEne.getRemainingTurns();
			applyBuff(type, mult * ratio, turns);
		}
		CombatLogger.get().log("[Player] Hereda buffs de " + fuente.getNombre() + "(ratio " + ratio + ")");
	}

	// --- Getters de stats finales ---
	public int getDanyo() {
		double dmg = danyoBase;
		Buff b = activeBuffs.get("damageBoost");
		if (b != null)
			dmg *= b.getMultiplier();
		return (int) Math.max(1, dmg);
	}

	public int getDefensa() {
		double def = defensaBase;
		Buff b = activeBuffs.get("defenseBoost");
		if (b != null)
			def *= b.getMultiplier();
		return (int) Math.max(0, def);
	}

	public double getVelocidad() {
		double vel = velBase;
		// Buff de tipo "speedBoost"
		Buff b = activeBuffs.get("speedBoost");
		if (b != null) {
			vel *= b.getMultiplier();
		}
		return Math.max(0.1, vel);
	}

	public int getVida() {
		return vida;
	}

	public int getVeneno() {
		return veneno;
	}

	public boolean isAlive() {
		return vida > 0;
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

		vida = Math.max(0, vida - neto);

		CombatLogger.get().log(
				"[Player] Recibe " + neto + " de daño. Vida restante: " + vida + " Defensa restante: " + getDefensa());
	}

	// --- Método de ataque (se llama desde la UI o GameLoop) ---
	public void attack(Enemy enemigo) {
		// Forzar taunt: si tauntSource != null, solo puedo atacar a ese enemigo
		if (tauntSource != null && enemigo != tauntSource) {
			CombatLogger.get().log("[Player] Taunt activo: debes atacar a " + tauntSource.getNombre() + ".");
			return;
		}

		int damage = getDanyo();
		CombatLogger.get().log("[Player] Ataca a "+enemigo.getNombre()+" haciendo "+damage+" daño base.");

		// Crear Attack, trasladar mis datos data-driven (EffectDetail) al ataque
		Attack atk = new Attack();
		for (EffectDetail ed : efectosActivos) {
			// Creamos copia manual (no usamos clone())
			EffectDetail copia = new EffectDetail(ed.getTipo(), ed.getIntensidad(), ed.getProbabilidad(),
					ed.getRemainingTurns());
			atk.addEffect(copia);
		}
		atk.applyToEnemy(enemigo, damage);
	}

	// --- Reset completo del estado del jugador (al reiniciar oleada o juego) ---
	public void resetState() {
		vida = 100;
		veneno = 0;
		defensaBase = 50;
		danyoBase = 10;
		velBase = 10.0;
		emocionesActivas.clear();
		efectosActivos.clear();
		activeBuffs.clear();
		activeDebuffs.clear();
		healOverTimeEffects.clear();
		tauntSource = null;
		tauntTurnsRemaining = 0;
		EmotionInstance emocionInicial = emotionFactory.generarProcedural();
		añadirEmocion(emocionInicial);
	}

	/**
	 * Aplica un debuff de tipo "taunt" que fuerza al atacante a centrarse en este
	 * turno.
	 *
	 * @param turns número de turnos que dura el taunt
	 */
	// --- Taunt-related ---
	public void applyTaunt(int duration, Enemy source) {
		this.tauntSource = source;
		this.tauntTurnsRemaining = duration;
		CombatLogger.get().log("[Player] Queda taunteado por " + duration + " turnos (de " + source.getNombre() + ")");
	}

	@Override
	public Player clone() {
		try {
			Player copia = (Player) super.clone();
			// Si tienes listas mutables, clónalas también:
			copia.emocionesActivas = new ArrayList<>(this.emocionesActivas);
			copia.efectosActivos = new ArrayList<>(this.efectosActivos);
			copia.activeBuffs = new HashMap<>(this.activeBuffs);
			copia.activeDebuffs = new HashMap<>(this.activeDebuffs);
			copia.healOverTimeEffects = new ArrayList<>(this.healOverTimeEffects);
			return copia;
		} catch (CloneNotSupportedException ex) {
			throw new AssertionError("No debería ocurrir");
		}
	}

	public EmotionNode getNodoMentalActual() {
		return nodoMentalActual;
	}

	public void setNodoMentalActual(EmotionNode nodo) {
		this.nodoMentalActual = nodo;
	}

	public void usarEmocion(EmotionInstance emocion) {
		if (!emocionesActivas.contains(emocion)) {
			CombatLogger.get().log("[Player] Emoción no disponible: " + emocion.getNombre());
			return;
		}

		for (EffectDetail ed : emocion.getEfectos()) {
			ed.aplicarA(this);
			efectosActivos.add(
					new EffectDetail(ed.getTipo(), ed.getIntensidad(), ed.getProbabilidad(), ed.getRemainingTurns()));
			CombatLogger.get().log("[Player] Aplica efecto de emoción: " + ed.getNombre());
		}
	}
}
