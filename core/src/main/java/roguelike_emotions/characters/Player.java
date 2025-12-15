package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roguelike_emotions.combat.CombatEntity;
import roguelike_emotions.combat.EmotionalTurnProcessor;
import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;
import roguelike_emotions.mainMechanics.SentientEmotion;
import roguelike_emotions.map.EmotionNode;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.vfx.ComboMaxEvent; // ✅ NUEVO
import roguelike_emotions.vfx.VisBus; // ✅ NUEVO

public class Player implements CombatEntity, Cloneable {

	private int vida = 100;
	private int maxVida = 100;
	private int defensaBase = 8;
	private int danyoBase = 20;
	private double velBase = 10;

	private List<EmotionInstance> emocionesActivas = new ArrayList<>();
	private EmotionNode nodoMentalActual;
	private Map<String, Buff> activeBuffs = new HashMap<>();
	private Map<String, Debuff> activeDebuffs = new HashMap<>();
	private List<OverTimeHeal> healOverTimeEffects = new ArrayList<>();
	private List<EffectDetail> efectosActivos = new ArrayList<>();
	private EmotionInstanceFactory emotionFactory = new EmotionInstanceFactory();

	private Enemy tauntSource;
	private int tauntTurnsRemaining = 0;
	private boolean canUseAbility = true;
	private int abilityCooldown = 0;
	private int comboStreak = 0;

	// ==================== INTERFACE METHODS ====================

	@Override
	public String getNombre() {
		return "Player";
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.PLAYER;
	}

	@Override
	public int getHealth() {
		return vida;
	}

	@Override
	public void setHealth(int health) {
		this.vida = Math.max(0, Math.min(health, maxVida));
	}

	@Override
	public int getMaxHealth() {
		return maxVida;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxVida = Math.max(1, maxHealth);
		this.vida = Math.min(this.vida, this.maxVida);
	}

	@Override
	public int getBaseDamage() {
		return danyoBase;
	}

	@Override
	public void setBaseDamage(int damage) {
		this.danyoBase = Math.max(0, damage);
	}

	@Override
	public int getBaseDefense() {
		return defensaBase;
	}

	@Override
	public void setBaseDefense(int defense) {
		this.defensaBase = Math.max(0, defense);
	}

	@Override
	public int getSpeed() {
		return (int) velBase;
	}

	@Override
	public void setSpeed(int speed) {
		this.velBase = Math.max(1, speed);
	}

	@Override
	public List<EmotionInstance> getEmocionesActivas() {
		return emocionesActivas;
	}

	@Override
	public void addEmocion(EmotionInstance emotion) {
		añadirEmocion(emotion);
	}

	@Override
	public void removeEmocion(EmotionInstance emotion) {
		eliminarEmocion(emotion);
	}

	@Override
	public void clearEmociones() {
		emocionesActivas.clear();
		efectosActivos.clear();
	}

	@Override
	public List<EffectDetail> getEfectosActivos() {
		return efectosActivos;
	}

	@Override
	public List<OverTimeHeal> getHealOverTimeEffects() {
		return healOverTimeEffects;
	}

	@Override
	public Map<String, Buff> getActiveBuffs() {
		return activeBuffs;
	}

	@Override
	public Map<String, Debuff> getActiveDebuffs() {
		return activeDebuffs;
	}

	@Override
	public boolean isAlive() {
		return vida > 0;
	}

	@Override
	public boolean isStunned() {
		return activeDebuffs.containsKey("stun") || activeDebuffs.containsKey("Stun")
				|| activeDebuffs.containsKey("aturdimiento");
	}

	@Override
	public boolean canAct() {
		return isAlive() && !isStunned();
	}

	@Override
	public int takeDamage(int amount) {
		int actualDamage = Math.min(amount, vida);
		vida -= actualDamage;
		return actualDamage;
	}

	@Override
	public void heal(int amount) {
		vida = Math.min(vida + amount, maxVida);
	}

	@Override
	public boolean canUseActive() {
		return canUseAbility;
	}

	@Override
	public void setCanUseActive(boolean canUse) {
		this.canUseAbility = canUse;
	}

	@Override
	public int getCooldownTurns() {
		return abilityCooldown;
	}

	@Override
	public void setCooldownTurns(int turns) {
		this.abilityCooldown = Math.max(0, turns);
	}

	// ==================== COMBAT METHODS ====================

	public void attack(Enemy enemigo) {
		if (!canAct()) {
			CombatLogger.get().log("[Player] No puede actuar - Está Stunned");
			comboStreak = 0;
			return;
		}

		if (tauntSource != null && enemigo != tauntSource) {
			CombatLogger.get().log("[Player] Taunt activo: debes atacar a " + tauntSource.getNombre());
			return;
		}

		// Calcular daño base y crítico
		int baseDamage = getDanyo();
		boolean isCritical = Math.random() < 0.25;
		if (isCritical) {
			baseDamage = (int) (baseDamage * 1.8);
		}

		// Incrementar combo y detectar x3
		int previousCombo = comboStreak;
		comboStreak = Math.min(comboStreak + 1, 3);

		// Trigger visual cuando alcanzas x3 por primera vez
		if (previousCombo < 3 && comboStreak == 3) {
			VisBus.post(new ComboMaxEvent(1)); // 1 = PLAYER_VIEW_ID
		}

		// Aplicar multiplicador de combo
		double comboMultiplier = 1.0 + (comboStreak - 1) * 0.05;
		baseDamage = (int) (baseDamage * comboMultiplier);

		// Varianza de daño
		int variance = (int) (baseDamage * 0.15);
		int finalDamage = baseDamage + (int) (Math.random() * variance * 2 - variance);
		finalDamage = Math.max(1, finalDamage);

		// Feedback mejorado para combo x3
		StringBuilder feedback = new StringBuilder("⚔️ Player ataca → ");
		if (isCritical) {
			feedback.append("💥 ¡CRÍTICO! ");
		}
		if (comboStreak == 3) {
			feedback.append("✨🔥 ¡COMBO MÁXIMO x3! 🔥✨ ");
		} else if (comboStreak >= 1) {
			feedback.append("🔥 Combo x").append(comboStreak).append("! ");
		}
		feedback.append(finalDamage).append(" de daño");
		CombatLogger.get().log(feedback.toString());

		// Aplicar el daño con efectos activos
		Attack atk = new Attack();
		for (EffectDetail ed : efectosActivos) {
			EffectDetail copia = new EffectDetail(ed.getTipo(), ed.getIntensidad(), ed.getProbabilidad(),
					ed.getRemainingTurns());
			atk.addEffect(copia);
		}

		atk.applyToEnemy(enemigo, finalDamage);

		// Resetear combo si el enemigo muere
		if (!enemigo.isAlive()) {
			comboStreak = 0;
		}
	}

	public void recibirDanyo(int cantidad) {
		int defensaActual = getDefensa();
		int neto = cantidad - defensaActual;

		// Si la defensa bloquea todo, no hay daño ni perdida de combo
		if (neto <= 0) {
			System.out.printf(
					"[Player] 🛡️ ¡Bloqueado completamente! (bruto: %d, defensa: %d) → HP: %d/%d | Combo: x%d%n",
					cantidad, defensaActual, vida, maxVida, comboStreak);
			return;
		}

		// Daño real penetra armadura → combo se rompe
		vida = Math.max(0, vida - neto);
		comboStreak = 0;

		System.out.printf("[Player] ⚠ Combo roto! Recibe %d de daño (bruto: %d, defensa: %d) → HP: %d/%d%n", neto,
				cantidad, defensaActual, vida, maxVida);
	}

	public int getDanyo() {
		double dmg = danyoBase;

		// Buff normal de daño
		Buff b = activeBuffs.get("damageBoost");
		if (b != null)
			dmg *= b.getMultiplier();

		// Buff de fusión
		Buff fusion = activeBuffs.get("fusionDamage");
		if (fusion != null)
			dmg *= fusion.getMultiplier();

		return (int) Math.max(1, dmg);
	}

	public int getDefensa() {
		double def = defensaBase;

		// Buff normal de defensa
		Buff b = activeBuffs.get("defenseBoost");
		if (b != null)
			def *= b.getMultiplier();

		// Buff de fusión
		Buff fusion = activeBuffs.get("fusionDefense");
		if (fusion != null)
			def *= fusion.getMultiplier();

		return (int) Math.max(0, def);
	}

	public double getVelocidad() {
		double vel = velBase;
		Buff b = activeBuffs.get("speedBoost");
		if (b != null)
			vel *= b.getMultiplier();
		return Math.max(0.1, vel);
	}

	// ==================== EMOTION & EFFECT METHODS ====================

	public void añadirEmocion(EmotionInstance e) {
		if (e != null && !emocionesActivas.contains(e)) {
			emocionesActivas.add(e);
			for (EffectDetail ed : e.getEfectos()) {
				efectosActivos.add(EffectDetail.fromConfig(ed.getTipo()));
			}
		}
	}

	public void eliminarEmocion(EmotionInstance e) {
		emocionesActivas.remove(e);
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

	public void curar(int cantidad) {
		vida = Math.min(vida + cantidad, maxVida);
		CombatLogger.get().log("[Player] +" + cantidad + " vida → " + vida);
	}

	public void defender(int nivel) {
		applyBuff("defenseBoost", 1.0 + (nivel / (double) defensaBase), 2);
		CombatLogger.get().log("[Player] Buff de defensa activado (+" + nivel + ") por 2 turnos");
	}

	public void modifySpeed(double multiplier) {
		velBase = Math.max(0.1, velBase * multiplier);
		CombatLogger.get().log("[Player] Velocidad ajustada " + multiplier + " → " + velBase);
	}

	public void applyBuff(String type, double multiplier, int duration) {
		activeBuffs.put(type, new Buff(type, multiplier, duration));
		CombatLogger.get().log("[Player] Buff " + type + " x" + multiplier + " por " + duration + " turnos");
	}

	public void applyDebuff(String type, double multiplier, int duration) {
		activeDebuffs.put(type, new Debuff(type, multiplier, duration));
		CombatLogger.get().log("[Player] Debuff " + type + " por " + duration + " turnos");
	}

	public void applyHealOverTime(int amount, int turns) {
		healOverTimeEffects.add(new OverTimeHeal(amount, turns));
		CombatLogger.get().log("[Player] Curación continua " + amount + " por " + turns + " turnos");
	}

	// ==================== AÑADIR AL FINAL DE LA CLASE ====================

	/**
	 * ✅ NUEVO: Tick de emociones sentientes cada turno
	 */
	public void tickSentientEmotions() {
		for (EmotionInstance e : emocionesActivas) {
			if (e instanceof SentientEmotion) {
				SentientEmotion sentient = (SentientEmotion) e;
				sentient.onTurnTick(this);

				// Diálogos contextuales
				String dialogue = sentient.getDialogue(true, false);
				if (dialogue != null) {
					CombatLogger.get().log(dialogue);
				}
			}
		}
	}

	public void replaceEmotion(EmotionInstance oldEmotion, SentientEmotion newEmotion) {
		int index = emocionesActivas.indexOf(oldEmotion);

		if (index != -1) {
			emocionesActivas.set(index, newEmotion);
			CombatLogger.get()
					.log("✨ " + oldEmotion.getNombre() + " ha evolucionado a " + newEmotion.getNombre() + "!");
		} else {
			CombatLogger.get().log("⚠ Error: No se pudo reemplazar la emoción");
		}
	}

	/**
	 * Obtiene una emoción por nombre (para buscar y reemplazar)
	 */
	public EmotionInstance getEmotionByName(String nombre) {
		return emocionesActivas.stream().filter(e -> e.getNombre().equals(nombre)).findFirst().orElse(null);
	}

	// ==================== TURN PROCESSING ====================

	public void tickTurnoEmocional() {
		EmotionalTurnProcessor.processTurn(this, null);

		if (tauntSource != null) {
			tauntTurnsRemaining--;
			if (tauntTurnsRemaining <= 0 || !tauntSource.isAlive()) {
				CombatLogger.get().log("[Player] Ya no estás bajo Provocación");
				tauntSource = null;
				tauntTurnsRemaining = 0;
			}
		}
	}

	public void reduceirDebuffs() {
		Iterator<Debuff> it = activeDebuffs.values().iterator();
		while (it.hasNext()) {
			Debuff d = it.next();
			if (d.reducirDuracion()) {
				it.remove();
				CombatLogger.get().log("[Player] Debuff " + d.getType() + " expirado");
			}
		}
	}

	// ==================== TAUNT & BUFF MECHANICS ====================

	public void applyTaunt(int duration, Enemy source) {
		this.tauntSource = source;
		this.tauntTurnsRemaining = duration;
	}

	public void setTauntSource(Enemy enemigo) {
		this.tauntSource = enemigo;
		CombatLogger.get().log("[Player] Ha sido provocado por " + enemigo.getNombre());
	}

	public Enemy getTauntSource() {
		return tauntSource;
	}

	public void inheritBuffsFrom(Enemy fuente, double ratio) {
		for (Map.Entry<String, Buff> entry : fuente.getActiveBuffs().entrySet()) {
			String type = entry.getKey();
			Buff buffEnemigo = entry.getValue();
			applyBuff(type, buffEnemigo.getMultiplier() * ratio, buffEnemigo.getRemainingTurns());
		}
		CombatLogger.get().log("[Player] Hereda buffs de " + fuente.getNombre() + " (ratio " + ratio + ")");
	}

	// ==================== GETTERS & SETTERS ====================

	public EmotionNode getNodoMentalActual() {
		return nodoMentalActual;
	}

	public void setNodoMentalActual(EmotionNode nodo) {
		this.nodoMentalActual = nodo;
	}

	public int getComboStreak() {
		return comboStreak;
	}

	// ==================== STATE RESET ====================

	public void resetState() {
		vida = 100;
		maxVida = 100;
		defensaBase = 8;
		danyoBase = 18;
		velBase = 10.0;
		comboStreak = 0;

		emocionesActivas.clear();
		efectosActivos.clear();
		activeBuffs.clear();
		activeDebuffs.clear();
		healOverTimeEffects.clear();

		tauntSource = null;
		tauntTurnsRemaining = 0;
		canUseAbility = true;
		abilityCooldown = 0;

		EmotionInstance emocionInicial = emotionFactory.generarProcedural();
		añadirEmocion(emocionInicial);
	}

	public void resetCombatState() {
		comboStreak = 0;
		efectosActivos.clear();
		activeBuffs.clear();
		activeDebuffs.clear();
		healOverTimeEffects.clear();
		tauntSource = null;
		tauntTurnsRemaining = 0;
		canUseAbility = true;
		abilityCooldown = 0;
	}

	@Override
	public Player clone() {
		try {
			Player copia = (Player) super.clone();
			copia.emocionesActivas = new ArrayList<>(this.emocionesActivas);
			copia.efectosActivos = new ArrayList<>(this.efectosActivos);
			copia.activeBuffs = new HashMap<>(this.activeBuffs);
			copia.activeDebuffs = new HashMap<>(this.activeDebuffs);
			copia.healOverTimeEffects = new ArrayList<>(this.healOverTimeEffects);
			return copia;
		} catch (CloneNotSupportedException ex) {
			throw new AssertionError("No debería ocurrir", ex);
		}
	}
}
