package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roguelike_emotions.combat.CombatEntity;
import roguelike_emotions.combat.EmotionalTurnProcessor;
import roguelike_emotions.combat.PassiveAbilityHolder;
import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.utils.CombatLogger;

public class Enemy implements CombatEntity, PassiveAbilityHolder {

	private String nombre;
	private int baseDamage;
	private int baseDefense;
	private double baseSpeed;
	private EnemyRole role;
	private int health;
	private int maxHealth;
	private EmotionInstance estadoEmocionalActual;

	private List<EmotionInstance> emocionesActivas = new ArrayList<>();
	private List<EffectDetail> efectosActivos = new ArrayList<>();
	private List<OverTimeHeal> healOverTimeEffects = new ArrayList<>();
	private Map<String, Buff> activeBuffs = new HashMap<>();
	private Map<String, Debuff> activeDebuffs = new HashMap<>();

	private static EmotionDominanceMatrix dominanceMatrix;
	private int cooldownTurns = 0;
	private boolean pasivasAplicadas = false;

	private static final String DAMAGE_BOOST = "damageBoost";
	private static final String DEFENSE_BOOST = "defenseBoost";
	private static final String SPEED_BOOST = "speedBoost";

	// CONSTRUCTOR ARREGLADO
	public Enemy(String nombre, int maxHealth, int baseDamage, int baseDefense, double baseSpeed, EnemyRole role) {
		this.nombre = nombre;
		this.maxHealth = maxHealth;
		this.health = maxHealth;
		this.baseDamage = baseDamage; // ✅ USA EL PARÁMETRO
		this.baseDefense = baseDefense;
		this.baseSpeed = baseSpeed;
		this.role = role;
	}

	@Override
	public String getNombre() {
		return nombre;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.ENEMY;
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public void setHealth(int health) {
		this.health = Math.max(0, Math.min(health, maxHealth));
	}

	@Override
	public int getMaxHealth() {
		return maxHealth;
	}

	@Override
	public int getBaseDamage() {
		return baseDamage;
	}

	@Override
	public void setBaseDamage(int damage) {
		this.baseDamage = Math.max(0, damage);
	}

	@Override
	public int getBaseDefense() {
		return baseDefense;
	}

	@Override
	public void setBaseDefense(int defense) {
		this.baseDefense = Math.max(0, defense);
	}

	@Override
	public int getSpeed() {
		return (int) baseSpeed;
	}

	@Override
	public void setSpeed(int speed) {
		this.baseSpeed = Math.max(1, speed);
	}

	@Override
	public List<EmotionInstance> getEmocionesActivas() {
		return emocionesActivas;
	}

	@Override
	public void addEmocion(EmotionInstance emotion) {
		if (!emocionesActivas.contains(emotion))
			emocionesActivas.add(emotion);
	}

	@Override
	public void removeEmocion(EmotionInstance emotion) {
		emocionesActivas.remove(emotion);
	}

	@Override
	public void clearEmociones() {
		emocionesActivas.clear();
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
		return health > 0;
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
		int actualDamage = Math.min(amount, health);
		health -= actualDamage;
		return actualDamage;
	}

	@Override
	public void heal(int amount) {
		health = Math.min(health + amount, maxHealth);
	}

	@Override
	public boolean canUseActive() {
		return pasivasAplicadas;
	}

	@Override
	public void setCanUseActive(boolean canUse) {
		this.pasivasAplicadas = canUse;
	}

	@Override
	public int getCooldownTurns() {
		return cooldownTurns;
	}

	@Override
	public void setCooldownTurns(int turns) {
		this.cooldownTurns = Math.max(0, turns);
	}

	public void tickTurnoEmocional(Player player) {
		EmotionalTurnProcessor.processTurn(this, player);
	}

	@Override
	public void aplicarPasivas() {
		if (pasivasAplicadas || estadoEmocionalActual == null)
			return;
		EmotionType tipoBase = estadoEmocionalActual.getTipoBase();
		switch (role) {
		case TANK:
			if (tipoBase == EmotionType.CALMA) {
				baseDefense = (int) (baseDefense * 1.10);
				CombatLogger.get().log("[Enemy " + nombre + "] 🛡️ Pasiva TANK: +10% defensa");
			}
			break;
		case DPS:
			if (tipoBase == EmotionType.IRA || tipoBase == EmotionType.RABIA) {
				baseDamage = (int) (baseDamage * 1.15);
				CombatLogger.get().log("[Enemy " + nombre + "] ⚔️ Pasiva DPS: +15% daño");
			}
			break;
		case SUPPORT:
			if (tipoBase == EmotionType.ALEGRIA) {
				int heal = (int) (maxHealth * 0.10);
				health = Math.min(health + heal, maxHealth);
				CombatLogger.get().log("[Enemy " + nombre + "] 💚 Pasiva SUPPORT: +10% HP");
			}
			break;
		}
		pasivasAplicadas = true;
	}

	public void atacar(Player jugador) {
		if (!canAct()) {
			CombatLogger.get().log("[Enemy " + nombre + "] No puede actuar - Stunned");
			return;
		}

		int baseDamage = getDanyo();
		boolean isCritical = Math.random() < 0.12;
		if (isCritical)
			baseDamage = (int) (baseDamage * 1.5);

		int variance = (int) (baseDamage * 0.20);
		int finalDamage = baseDamage + (int) (Math.random() * variance * 2 - variance);
		finalDamage = Math.max(1, finalDamage);

		Attack atk = new Attack();
		efectosActivos.forEach(atk::addEffect);
		atk.applyToPlayer(jugador, finalDamage);

		StringBuilder feedback = new StringBuilder("👿 ").append(nombre).append(" ataca → ");
		if (isCritical)
			feedback.append("💥 Crítico! ");
		feedback.append(finalDamage).append(" de daño");
		CombatLogger.get().log(feedback.toString());

		usarActiva(jugador);
	}

	public void usarActiva(Player jugador) {
		if (!pasivasAplicadas || cooldownTurns > 0)
			return;
		CombatLogger.get().log("[Enemy " + nombre + "] 🌟 Activa habilidad: " + role.getActiveSkill());
		switch (role) {
		case TANK:
			CombatLogger.get().log("    🎯 Provocación!");
			jugador.applyTaunt(2, this);
			break;
		case DPS:
			int critDamage = (int) (baseDamage * 2.5);
			jugador.recibirDanyo(critDamage);
			CombatLogger.get().log("    💀 Golpe Devastador! " + critDamage + " dmg");
			break;
		case SUPPORT:
			jugador.inheritBuffsFrom(this, 0.3);
			int selfHeal = (int) (maxHealth * 0.15);
			health = Math.min(health + selfHeal, maxHealth);
			CombatLogger.get().log("    🔗 Vínculo + " + selfHeal + " HP");
			break;
		}
		pasivasAplicadas = false;
		cooldownTurns = 3;
	}

	public void recibirDanyo(int cantidad) {
		int defensaActual = getDefensa();
		int neto = Math.max(1, cantidad - defensaActual);
		health = Math.max(0, health - neto);
		System.out.printf("[Enemy] %s recibe %d de daño (bruto: %d, defensa: %d) → HP: %d/%d%n", nombre, neto, cantidad,
				defensaActual, health, maxHealth);
	}

	public int getDanyo() {
		double dmg = baseDamage;
		Buff b = activeBuffs.get(DAMAGE_BOOST);
		if (b != null)
			dmg *= b.getMultiplier();
		return (int) Math.max(1, dmg);
	}

	public int getDefensa() {
		double def = baseDefense;
		Buff b = activeBuffs.get(DEFENSE_BOOST);
		if (b != null)
			def *= b.getMultiplier();
		return (int) Math.max(0, def);
	}

	public double getVelocidad() {
		double vel = baseSpeed;
		Buff b = activeBuffs.get(SPEED_BOOST);
		if (b != null)
			vel *= b.getMultiplier();
		return Math.max(0.1, vel);
	}

	public EnemyRole getRole() {
		return role;
	}

	public void setRole(EnemyRole role) {
		this.role = role;
	}

	public EmotionInstance getEstadoEmocionalActual() {
		return estadoEmocionalActual;
	}

	public void setEstadoEmocionalActual(EmotionInstance emotion) {
		this.estadoEmocionalActual = emotion;
		this.pasivasAplicadas = false;
	}

	public void setEstadoEmocional(EmotionInstance e) {
		setEstadoEmocionalActual(e);
	}

	public void addEmotion(EmotionInstance e) {
		addEmocion(e);
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = Math.max(1, maxHealth);
		this.health = Math.min(this.health, this.maxHealth);
	}

	public void setBaseSpeed(double baseSpeed) {
		this.baseSpeed = Math.max(0.1, baseSpeed);
	}

	public void setEmocionesActivas(List<EmotionInstance> emociones) {
		this.emocionesActivas = emociones;
	}

	public void setEfectosActivos(List<EffectDetail> efectos) {
		this.efectosActivos = efectos;
	}

	public void setHealOverTimeEffects(List<OverTimeHeal> effects) {
		this.healOverTimeEffects = effects;
	}

	public void setActiveBuffs(Map<String, Buff> buffs) {
		this.activeBuffs = buffs;
	}

	public void setActiveDebuffs(Map<String, Debuff> debuffs) {
		this.activeDebuffs = debuffs;
	}

	public boolean isPasivasAplicadas() {
		return pasivasAplicadas;
	}

	public void setPasivasAplicadas(boolean aplicadas) {
		this.pasivasAplicadas = aplicadas;
	}

	public void onTurnStart() {
		pasivasAplicadas = true;
	}

	public static void setDominanceMatrix(EmotionDominanceMatrix m) {
		dominanceMatrix = m;
	}

	public static EmotionDominanceMatrix getDominanceMatrix() {
		return dominanceMatrix;
	}
}
