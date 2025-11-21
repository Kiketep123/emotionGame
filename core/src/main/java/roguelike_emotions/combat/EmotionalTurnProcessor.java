package roguelike_emotions.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.MultiEmotionSynergyManager;
import roguelike_emotions.utils.SynergyEffect;

/**
 * Procesador unificado de turnos emocionales. Funciona tanto para Player vs
 * Enemy como Enemy vs Player.
 */
public class EmotionalTurnProcessor {

	// ==================== CONSTANTES ====================

	private static final String DAMAGE_BOOST = "damageBoost";
	private static final String DEFENSE_BOOST = "defenseBoost";
	private static final String SPEED_BOOST = "speedBoost";

	// ==================== MÉTODO PRINCIPAL ====================

	/**
	 * Procesa un turno completo para cualquier CombatEntity.
	 *
	 * @param actor  La entidad que realiza su turno
	 * @param target El objetivo del turno (puede recibir efectos)
	 */
	public static void processTurn(CombatEntity actor, CombatEntity target) {
		if (!actor.canAct()) {
			logEntityCantAct(actor);
			return;
		}

		logTurnStart(actor);

		// Fase 1: Limpieza de buffs temporales
		cleanupTemporaryBuffs(actor);

		// Fase 2: Procesamiento de efectos activos
		processActiveEffects(actor, target);

		// Fase 3: Procesamiento de efectos temporales
		processTemporalEffects(actor);

		// Fase 4: Procesamiento de buffs y debuffs
		processBuffsAndDebuffs(actor);

		// Fase 5: Aplicación de pasivas (si aplica)
		applyPassiveAbilities(actor);

		// Fase 6: Bonificaciones por fusiones
		applyFusionBonuses(actor);

		// Fase 7: Aplicación de sinergias emocionales
		applySynergies(actor);

		// Fase 8: Gestión de cooldowns
		updateAbilityCooldowns(actor);

		logTurnEnd(actor);
	}

	// ==================== FASE 1: LIMPIEZA ====================

	private static void cleanupTemporaryBuffs(CombatEntity entity) {
		entity.getActiveBuffs().remove(DAMAGE_BOOST);
		entity.getActiveBuffs().remove(DEFENSE_BOOST);
		entity.getActiveBuffs().remove(SPEED_BOOST);
	}

	// ==================== FASE 2: EFECTOS ACTIVOS ====================

	private static void processActiveEffects(CombatEntity actor, CombatEntity target) {
		Iterator<EffectDetail> iterator = actor.getEfectosActivos().iterator();

		while (iterator.hasNext()) {
			EffectDetail effect = iterator.next();

			if (shouldApplyEffect(effect)) {
				applyEffectToTargets(effect, actor, target);
			}

			effect.reducirDuracion(1);

			if (effect.haExpirado()) {
				logEffectExpired(actor, effect);
				iterator.remove();
			}
		}
	}

	private static boolean shouldApplyEffect(EffectDetail effect) {
		return Math.random() < effect.getProbabilidad();
	}

	private static void applyEffectToTargets(EffectDetail effect, CombatEntity actor, CombatEntity target) {
		effect.aplicarA(actor);
		effect.aplicarA(target);
	}

	// ==================== FASE 3: EFECTOS TEMPORALES ====================

	private static void processTemporalEffects(CombatEntity entity) {
		processHealOverTime(entity);
	}

	private static void processHealOverTime(CombatEntity entity) {
		Iterator<OverTimeHeal> iterator = entity.getHealOverTimeEffects().iterator();

		while (iterator.hasNext()) {
			OverTimeHeal hot = iterator.next();

			int healAmount = hot.getAmount();
			entity.heal(healAmount);

			logHealApplied(entity, healAmount);

			hot.reducirDuracion();

			if (hot.getRemainingTurns() <= 0) {
				iterator.remove();
			}
		}
	}

	// ==================== FASE 4: BUFFS Y DEBUFFS ====================

	private static void processBuffsAndDebuffs(CombatEntity entity) {
		processBuffs(entity);
		processDebuffs(entity);
	}

	private static void processBuffs(CombatEntity entity) {
		Iterator<Buff> iterator = entity.getActiveBuffs().values().iterator();

		while (iterator.hasNext()) {
			Buff buff = iterator.next();
			buff.reducirDuracion();

			if (buff.getRemainingTurns() <= 0) {
				logBuffExpired(entity, buff);
				iterator.remove();
			}
		}
	}

	private static void processDebuffs(CombatEntity entity) {
		Iterator<Debuff> iterator = entity.getActiveDebuffs().values().iterator();

		while (iterator.hasNext()) {
			Debuff debuff = iterator.next();
			debuff.reducirDuracion();

			if (debuff.getRemainingTurns() <= 0) {
				logDebuffExpired(entity, debuff);
				iterator.remove();
			}
		}
	}

	// ==================== FASE 5: PASIVAS ====================

	private static void applyPassiveAbilities(CombatEntity entity) {
		// Las pasivas dependen del tipo de entidad
		if (entity instanceof PassiveAbilityHolder p) {
			p.aplicarPasivas();
		}
	}

	// ==================== FASE 6: BONIFICACIONES POR FUSIÓN ====================

	private static void applyFusionBonuses(CombatEntity entity) {
		int fusionCount = entity.getEmocionesActivas().size();

		if (fusionCount < 2) {
			return; // Sin bonos con menos de 2 emociones
		}

		// Bonificaciones escalables
		double fusionMultiplier = 1.0 + (fusionCount - 1) * 0.05; // +5% por emoción extra

		int newDamage = (int) (entity.getBaseDamage() * fusionMultiplier);
		int newDefense = (int) (entity.getBaseDefense() * fusionMultiplier);

		entity.setBaseDamage(newDamage);
		entity.setBaseDefense(newDefense);

		logFusionBonus(entity, fusionCount, fusionMultiplier);
	}

	// ==================== FASE 7: SINERGIAS EMOCIONALES ====================

	private static void applySynergies(CombatEntity entity) {
		List<EmotionType> activeTypes = extractActiveEmotionTypes(entity);

		if (activeTypes.size() < 2) {
			return;
		}

		List<SynergyEffect> synergies = MultiEmotionSynergyManager.getSynergies(activeTypes);

		if (synergies.isEmpty()) {
			return;
		}

		logActiveSynergies(entity, synergies);

		for (SynergyEffect synergy : synergies) {
			applySingleSynergy(entity, synergy);
		}
	}

	private static List<EmotionType> extractActiveEmotionTypes(CombatEntity entity) {
		List<EmotionType> types = new ArrayList<>();

		for (EmotionInstance emotion : entity.getEmocionesActivas()) {
			types.add(emotion.getTipoBase());
		}

		return types;
	}

	private static void applySingleSynergy(CombatEntity entity, SynergyEffect synergy) {
		applyStatBuffs(entity, synergy);
		applyHealOverTime(entity, synergy);
		applyPoisonEffect(entity, synergy);
		applyStunEffect(entity, synergy);
		applyGenericBuffs(entity, synergy);
		applyGenericDebuffs(entity, synergy);
	}

	private static void applyStatBuffs(CombatEntity entity, SynergyEffect synergy) {
		Map<String, Buff> buffs = entity.getActiveBuffs();

		if (synergy.getDamageMultiplier() != 1.0) {
			buffs.put(DAMAGE_BOOST, new Buff(DAMAGE_BOOST, synergy.getDamageMultiplier(), 1));
		}

		if (synergy.getDefenseMultiplier() != 1.0) {
			buffs.put(DEFENSE_BOOST, new Buff(DEFENSE_BOOST, synergy.getDefenseMultiplier(), 1));
		}

		if (synergy.getSpeedMultiplier() != 1.0) {
			buffs.put(SPEED_BOOST, new Buff(SPEED_BOOST, synergy.getSpeedMultiplier(), 1));
		}
	}

	private static void applyHealOverTime(CombatEntity entity, SynergyEffect synergy) {
		if (synergy.getHotAmount() > 0 && synergy.getHotTurns() > 0) {
			entity.getHealOverTimeEffects().add(new OverTimeHeal(synergy.getHotAmount(), synergy.getHotTurns()));

			logSynergyHoT(entity, synergy);
		}
	}

	private static void applyPoisonEffect(CombatEntity entity, SynergyEffect synergy) {
		if (synergy.getPoisonAmount() > 0 && synergy.getPoisonTurns() > 0) {
			EffectDetail poisonEffect = new EffectDetail(EmotionEffect.VENENO, synergy.getPoisonAmount(), 1.0,
					synergy.getPoisonTurns());

			entity.getEfectosActivos().add(poisonEffect);
			logSynergyPoison(entity, synergy);
		}
	}

	private static void applyStunEffect(CombatEntity entity, SynergyEffect synergy) {
		if (synergy.getStunTurns() > 0) {
			EffectDetail stunEffect = new EffectDetail(EmotionEffect.STUN, 0.0, 1.0, synergy.getStunTurns());

			entity.getEfectosActivos().add(stunEffect);
			logSynergyStun(entity, synergy);
		}
	}

	private static void applyGenericBuffs(CombatEntity entity, SynergyEffect synergy) {
		if (synergy.getBuffType() != null && synergy.getBuffTurns() > 0) {
			Buff buff = new Buff(synergy.getBuffType(), synergy.getBuffMultiplier(), synergy.getBuffTurns());

			entity.getActiveBuffs().put(synergy.getBuffType(), buff);
			logSynergyBuff(entity, synergy);
		}
	}

	private static void applyGenericDebuffs(CombatEntity entity, SynergyEffect synergy) {
		if (synergy.getDebuffType() != null && synergy.getDebuffTurns() > 0) {
			Debuff debuff = new Debuff(synergy.getDebuffType(), synergy.getBuffMultiplier(), synergy.getDebuffTurns());

			entity.getActiveDebuffs().put(synergy.getDebuffType(), debuff);
			logSynergyDebuff(entity, synergy);
		}
	}

	// ==================== FASE 8: COOLDOWNS ====================

	private static void updateAbilityCooldowns(CombatEntity entity) {
		if (entity.canUseActive()) {
			return;
		}

		int remainingCooldown = entity.getCooldownTurns() - 1;
		entity.setCooldownTurns(remainingCooldown);

		if (remainingCooldown <= 0) {
			entity.setCanUseActive(true);
			logAbilityReady(entity);
		}
	}

	// ==================== CÁLCULO DE DAÑO UNIFICADO ====================

	/**
	 * Calcula el daño final considerando todos los modificadores activos.
	 */
	public static int calculateFinalDamage(CombatEntity attacker, CombatEntity defender) {
		int baseDamage = attacker.getBaseDamage();

		// Aplicar buff de daño del atacante
		Buff damageBoost = attacker.getActiveBuffs().get(DAMAGE_BOOST);
		if (damageBoost != null) {
			baseDamage = (int) (baseDamage * damageBoost.getMultiplier());
		}

		// Calcular defensa del defensor
		int defense = defender.getBaseDefense();
		Buff defenseBoost = defender.getActiveBuffs().get(DEFENSE_BOOST);
		if (defenseBoost != null) {
			defense = (int) (defense * defenseBoost.getMultiplier());
		}

		// Fórmula de daño: Ataque - (Defensa / 2)
		return Math.max(1, baseDamage - defense / 2);

	}

	/**
	 * Realiza un ataque completo de una entidad a otra.
	 */
	public static AttackResult performAttack(CombatEntity attacker, CombatEntity defender) {
		if (!attacker.canAct()) {
			return AttackResult.failed("El atacante no puede actuar");
		}

		if (!defender.isAlive()) {
			return AttackResult.failed("El defensor ya está derrotado");
		}

		int damage = calculateFinalDamage(attacker, defender);
		int actualDamage = defender.takeDamage(damage);

		logAttack(attacker, defender, actualDamage);

		return AttackResult.success(actualDamage, defender.isAlive());
	}

	// ==================== RESULTADO DE ATAQUE ====================

	public static class AttackResult {
		private final boolean success;
		private final int damageDealt;
		private final boolean targetAlive;
		private final String message;

		private AttackResult(boolean success, int damage, boolean alive, String msg) {
			this.success = success;
			this.damageDealt = damage;
			this.targetAlive = alive;
			this.message = msg;
		}

		public static AttackResult success(int damage, boolean alive) {
			return new AttackResult(true, damage, alive, "Ataque exitoso");
		}

		public static AttackResult failed(String reason) {
			return new AttackResult(false, 0, true, reason);
		}

		public boolean isSuccess() {
			return success;
		}

		public int getDamageDealt() {
			return damageDealt;
		}

		public boolean isTargetAlive() {
			return targetAlive;
		}

		public String getMessage() {
			return message;
		}
	}

	// ==================== LOGGING ====================

	private static void logTurnStart(CombatEntity entity) {
		CombatLogger.get().log(String.format("[%s %s] Inicia turno", entity.getEntityType(), entity.getNombre()));
	}

	private static void logTurnEnd(CombatEntity entity) {
		CombatLogger.get().log(String.format("[%s %s] Finaliza turno (HP: %d/%d)", entity.getEntityType(),
				entity.getNombre(), entity.getHealth(), entity.getMaxHealth()));
	}

	private static void logEntityCantAct(CombatEntity entity) {
		CombatLogger.get()
				.log(String.format("[%s %s] No puede actuar este turno", entity.getEntityType(), entity.getNombre()));
	}

	private static void logEffectExpired(CombatEntity entity, EffectDetail effect) {
		CombatLogger.get().log(String.format("[%s %s] Efecto '%s' expirado", entity.getEntityType(), entity.getNombre(),
				effect.getTipo()));
	}

	private static void logHealApplied(CombatEntity entity, int amount) {
		CombatLogger.get().log(String.format("[HoT] %s recupera %d HP", entity.getNombre(), amount));
	}

	private static void logBuffExpired(CombatEntity entity, Buff buff) {
		CombatLogger.get().log(String.format("[%s %s] Buff '%s' expirado", entity.getEntityType(), entity.getNombre(),
				buff.getType()));
	}

	private static void logDebuffExpired(CombatEntity entity, Debuff debuff) {
		CombatLogger.get().log(String.format("[%s %s] Debuff '%s' expirado", entity.getEntityType(), entity.getNombre(),
				debuff.getType()));
	}

	private static void logFusionBonus(CombatEntity entity, int count, double multiplier) {
		CombatLogger.get().log(String.format("[Fusion Bonus] %s tiene %d emociones activas (x%.2f)", entity.getNombre(),
				count, multiplier));
	}

	private static void logActiveSynergies(CombatEntity entity, List<SynergyEffect> synergies) {
		CombatLogger.get().log(String.format("[%s %s] %d sinergia(s) activa(s)", entity.getEntityType(),
				entity.getNombre(), synergies.size()));

		for (SynergyEffect synergy : synergies) {
			CombatLogger.get().log("  ⚡ " + synergy.getName());
		}
	}

	private static void logSynergyHoT(CombatEntity entity, SynergyEffect synergy) {
		CombatLogger.get().log(String.format("[Synergy: %s] %s recibe curación continua: %d HP x %d turnos",
				synergy.getName(), entity.getNombre(), synergy.getHotAmount(), synergy.getHotTurns()));
	}

	private static void logSynergyPoison(CombatEntity entity, SynergyEffect synergy) {
		CombatLogger.get().log(String.format("[Synergy: %s] %s aplica veneno: %d daño x %d turnos", synergy.getName(),
				entity.getNombre(), synergy.getPoisonAmount(), synergy.getPoisonTurns()));
	}

	private static void logSynergyStun(CombatEntity entity, SynergyEffect synergy) {
		CombatLogger.get().log(String.format("[Synergy: %s] %s puede aturdir por %d turno(s)", synergy.getName(),
				entity.getNombre(), synergy.getStunTurns()));
	}

	private static void logSynergyBuff(CombatEntity entity, SynergyEffect synergy) {
		CombatLogger.get()
				.log(String.format("[Synergy: %s] %s recibe buff '%s' (x%.2f) por %d turnos", synergy.getName(),
						entity.getNombre(), synergy.getBuffType(), synergy.getBuffMultiplier(),
						synergy.getBuffTurns()));
	}

	private static void logSynergyDebuff(CombatEntity entity, SynergyEffect synergy) {
		CombatLogger.get().log(String.format("[Synergy: %s] %s aplica debuff '%s' por %d turnos", synergy.getName(),
				entity.getNombre(), synergy.getDebuffType(), synergy.getDebuffTurns()));
	}

	private static void logAbilityReady(CombatEntity entity) {
		CombatLogger.get()
				.log(String.format("[%s %s] Habilidad activa lista", entity.getEntityType(), entity.getNombre()));
	}

	private static void logAttack(CombatEntity attacker, CombatEntity defender, int damage) {
		CombatLogger.get().log(String.format("💥 %s ataca a %s por %d de daño (HP: %d → %d)", attacker.getNombre(),
				defender.getNombre(), damage, defender.getHealth() + damage, defender.getHealth()));
	}
}