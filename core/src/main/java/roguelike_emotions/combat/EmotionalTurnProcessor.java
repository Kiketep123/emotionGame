package roguelike_emotions.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import roguelike_emotions.characters.Player;
import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.mainMechanics.SentientEmotion;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.MultiEmotionSynergyManager;
import roguelike_emotions.utils.SynergyEffect;

/**
 * 🧠 EmotionalTurnProcessor v3.0 - REFACTORIZADO
 * 
 * Procesador centralizado de turnos con lógica optimizada: - Sin aplicaciones
 * duplicadas de efectos - Control de estado para evitar re-procesamiento -
 * Separación clara de responsabilidades
 * 
 * @author Tu Nombre
 * @version 3.0
 */
public class EmotionalTurnProcessor {

	// ==================== CONSTANTES ====================

	private static final String DAMAGE_BOOST = "damageBoost";
	private static final String DEFENSE_BOOST = "defenseBoost";
	private static final String SPEED_BOOST = "speedBoost";
	private static final String FUSION_DAMAGE = "fusionDamage";
	private static final String FUSION_DEFENSE = "fusionDefense";

	private static final Random rand = new Random();

	// ==================== FLAGS DE ESTADO (Evitar duplicados) ====================

	private static final ThreadLocal<Set<String>> processedThisTurn = ThreadLocal.withInitial(HashSet::new);

	// ==================== PROCESAMIENTO PRINCIPAL ====================

	/**
	 * Procesa el turno completo de una entidad.
	 * 
	 * @param actor  Entidad que actúa
	 * @param target Objetivo (puede ser null para procesamiento auto)
	 */
	public static void processTurn(CombatEntity actor, CombatEntity target) {
		if (!actor.canAct()) {
			logEntityCantAct(actor);
			return;
		}

		// Limpiar flags de turno anterior
		processedThisTurn.get().clear();

		logTurnStart(actor);

		// FASE 1: Procesar emociones (XP, despertar, efectos directos)
		if (actor instanceof Player) {
			processPlayerEmotions((Player) actor);
		}

		// FASE 2: Efectos activos (DoT, buffs temporales, etc.)
		processActiveEffects(actor);

		// FASE 3: Curación continua
		processHealOverTime(actor);

		// FASE 4: Expiración de buffs/debuffs
		processBuffsAndDebuffs(actor);

		// FASE 5: Pasivas de emociones sentientes
		if (actor instanceof Player) {
			applyPassiveAbilities((Player) actor);
		}

		// FASE 6: Bonificaciones por cantidad de emociones
		if (actor instanceof Player) {
			applyFusionBonuses((Player) actor);
		}

		// FASE 7: Sinergias emocionales
		if (actor instanceof Player) {
			applySynergies((Player) actor);
		}

		// FASE 8: Reducir cooldowns
		updateAbilityCooldowns(actor);

		logTurnEnd(actor);
	}

	// ==================== FASE 1: PROCESAMIENTO DE EMOCIONES ====================

	/**
	 * Procesa cada emoción del jugador: - Gana experiencia - Intenta despertar (si
	 * es normal) - Tick de sentientes (si ya despertó)
	 * 
	 * ❌ NO APLICA EFECTOS AQUÍ (se hace en processActiveEffects)
	 */
	private static void processPlayerEmotions(Player player) {
		List<EmotionInstance> emotionsToReplace = new ArrayList<>();
		List<SentientEmotion> newSentients = new ArrayList<>();

		for (EmotionInstance emotion : player.getEmocionesActivas()) {

			// 1. Incrementar contador de uso
			emotion.incrementUsageCount();

			// 2. Ganar experiencia base
			int baseExp = calculateExperienceGain(emotion);
			emotion.gainExperience(baseExp);

			// 3. Intentar despertar (solo emociones normales)
			if (!(emotion instanceof SentientEmotion)) {
				if (checkAndProcessAwakening(emotion, player)) {
					SentientEmotion sentient = SentientEmotion.fromEmotionInstance(emotion);
					sentient.setInitialExperience(emotion.getUsageCount(), emotion.getExperiencePoints());

					emotionsToReplace.add(emotion);
					newSentients.add(sentient);

					announceAwakening(emotion, sentient);
				}
			} else {
				// Ya es sentiente, procesar lógica especial
				SentientEmotion sentient = (SentientEmotion) emotion;
				sentient.onTurnTick(player);

				// Incrementar edad
				sentient.incrementAge();

				// Aumentar hambre gradualmente
				sentient.incrementHunger(5);
			}
		}

		// Reemplazar emociones que despertaron
		for (int i = 0; i < emotionsToReplace.size(); i++) {
			player.replaceEmotion(emotionsToReplace.get(i), newSentients.get(i));
		}
	}

	/**
	 * Calcula XP ganada basándose en intensidad de efectos
	 */
	private static int calculateExperienceGain(EmotionInstance emotion) {
		int baseExp = 10;

		for (EffectDetail efecto : emotion.getEfectos()) {
			if (efecto.getIntensidad() > 1.5) {
				baseExp += 5;
			}
			if (efecto.getIntensidad() > 2.0) {
				baseExp += 10;
			}
		}

		return baseExp;
	}

	/**
	 * Verifica si una emoción puede despertar y ejecuta el proceso
	 */
	private static boolean checkAndProcessAwakening(EmotionInstance emotion, Player player) {
		// Mostrar progreso si está cerca
		int progress = emotion.getAwakeningProgress();

		if (progress >= 70 && progress < 100 && rand.nextInt(100) < 15) {
			CombatLogger.get().log("💭 " + emotion.getNombre() + " parece diferente... (" + progress + "%)");
		}

		// Verificar si puede despertar
		if (!emotion.canAwaken()) {
			return false;
		}

		// Intentar despertar
		if (emotion.tryAwaken()) {
			return true;
		} else {
			// Falló pero está cerca
			if (rand.nextInt(100) < 25) {
				CombatLogger.get().log("✨ " + emotion.getNombre() + " está al borde del despertar... ("
						+ emotion.getAwakeningChance() + "%)");
			}
			return false;
		}
	}

	/**
	 * Anuncia el despertar con estilo
	 */
	private static void announceAwakening(EmotionInstance base, SentientEmotion sentient) {
		CombatLogger.get().log("");
		CombatLogger.get().log("═══════════════════════════════════════");
		CombatLogger.get().log("🧠 ¡DESPERTAR DE CONSCIENCIA! 🧠");
		CombatLogger.get().log("");
		CombatLogger.get().log("  " + base.getNombre() + " ha cobrado VIDA PROPIA");
		CombatLogger.get().log("  Personalidad: " + sentient.getPersonality().name());
		CombatLogger.get().log("  XP acumulada: " + base.getExperiencePoints());
		CombatLogger.get().log("  " + getAwakeningMessage(sentient));
		CombatLogger.get().log("═══════════════════════════════════════");
		CombatLogger.get().log("");
	}

	/**
	 * Mensaje según personalidad
	 */
	private static String getAwakeningMessage(SentientEmotion sentient) {
		return switch (sentient.getPersonality()) {
		case REBELDE -> "\"No me controlarás fácilmente...\"";
		case SUMISA -> "\"Estoy aquí para ayudarte.\"";
		case GLOTONA -> "\"Tengo... HAMBRE.\"";
		case PARASÍTICA -> "\"Tu energía... la necesito.\"";
		case SIMBIONTE -> "\"Juntos somos más fuertes.\"";
		case VOLÁTIL -> "\"¡No sé qué siento!\"";
		case SABIA -> "\"Veo cosas que tú no ves...\"";
		};
	}

	// ==================== FASE 2: EFECTOS ACTIVOS ====================

	/**
	 * Procesa efectos activos (DoT, debuffs temporales, etc.) ✅ AQUÍ es donde se
	 * aplican los efectos de las emociones
	 */
	private static void processActiveEffects(CombatEntity actor) {
		Iterator<EffectDetail> iterator = actor.getEfectosActivos().iterator();
		Set<String> processedEffects = new HashSet<>();

		while (iterator.hasNext()) {
			EffectDetail effect = iterator.next();
			String effectKey = effect.getTipo().name();

			// Evitar aplicar el mismo tipo de efecto múltiples veces
			if (processedEffects.contains(effectKey)) {
				continue;
			}

			// Aplicar efecto si la probabilidad lo permite
			if (shouldApplyEffect(effect)) {
				effect.aplicarA(actor);
				processedEffects.add(effectKey);
			}

			// Reducir duración
			effect.reducirDuracion(1);

			// Eliminar si expiró
			if (effect.haExpirado()) {
				logEffectExpired(actor, effect);
				iterator.remove();
			}
		}
	}

	private static boolean shouldApplyEffect(EffectDetail effect) {
		return Math.random() < effect.getProbabilidad();
	}

	// ==================== FASE 3: CURACIÓN CONTINUA ====================

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

	private static void applyPassiveAbilities(Player player) {
		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			if (emotion instanceof SentientEmotion) {
				SentientEmotion sentient = (SentientEmotion) emotion;

				// Aplicar pasiva según personalidad
				switch (sentient.getPersonality()) {
				case SIMBIONTE:
					// Curación pasiva pequeña
					if (player.getHealth() < player.getMaxHealth() && rand.nextInt(100) < 20) {
						int heal = 5;
						player.heal(heal);
						CombatLogger.get().log("🌿 " + sentient.getNombre() + " te cura " + heal + " HP (Simbionte)");
					}
					break;

				case SABIA:
					// Reducción de cooldowns
					if (player.getCooldownTurns() > 0 && rand.nextInt(100) < 15) {
						player.setCooldownTurns(player.getCooldownTurns() - 1);
						CombatLogger.get().log("📚 " + sentient.getNombre() + " reduce tu cooldown (Sabia)");
					}
					break;

				case GLOTONA:
					// Necesita comida o pierde lealtad
					if (sentient.getHunger() > 80) {
						sentient.setLoyalty(sentient.getLoyalty() - 2);
						CombatLogger.get().log("😠 " + sentient.getNombre() + " pierde lealtad por hambre");
					}
					break;

				default:
					// Otras personalidades no tienen pasiva cada turno
					break;
				}
			}
		}
	}

	// ==================== FASE 6: BONIFICACIONES POR FUSIÓN ====================

	/**
	 * Aplica buffs escalables por cantidad de emociones activas. ✅ OPTIMIZADO: Solo
	 * crea buffs si no existen
	 */
	private static void applyFusionBonuses(Player player) {
		int fusionCount = player.getEmocionesActivas().size();

		if (fusionCount < 2) {
			return;
		}

		double fusionMultiplier = 1.0 + (fusionCount - 1) * 0.05;
		Map<String, Buff> buffs = player.getActiveBuffs();

		// ✅ ARREGLADO: Solo crear si no existe o si expiró
		if (!buffs.containsKey(FUSION_DAMAGE)) {
			buffs.put(FUSION_DAMAGE, new Buff(FUSION_DAMAGE, fusionMultiplier, 2));
			logFusionBonus(player, fusionCount, fusionMultiplier);
		} else {
			// Renovar duración si ya existe
			buffs.get(FUSION_DAMAGE).aumentarDuracion(fusionCount);
		}

		if (!buffs.containsKey(FUSION_DEFENSE)) {
			buffs.put(FUSION_DEFENSE, new Buff(FUSION_DEFENSE, fusionMultiplier, 2));
		} else {
			buffs.get(FUSION_DEFENSE).aumentarDuracion(fusionCount);
		}
	}

	// ==================== FASE 7: SINERGIAS ====================

	/**
	 * Aplica sinergias emocionales. ✅ OPTIMIZADO: Verifica si ya está activa antes
	 * de reaplicar
	 */
	private static void applySynergies(Player player) {
		List<EmotionType> activeTypes = extractActiveEmotionTypes(player);

		if (activeTypes.size() < 2) {
			return;
		}

		List<SynergyEffect> synergies = MultiEmotionSynergyManager.getSynergies(activeTypes);

		if (synergies.isEmpty()) {
			return;
		}

		// ✅ OPTIMIZADO: Solo logear una vez
		if (!processedThisTurn.get().contains("synergies_logged")) {
			logActiveSynergies(player, synergies);
			processedThisTurn.get().add("synergies_logged");
		}

		for (SynergyEffect synergy : synergies) {
			String synergyKey = "synergy_" + synergy.getName();

			// ✅ Evitar reaplicar la misma sinergia este turno
			if (!processedThisTurn.get().contains(synergyKey)) {
				applySingleSynergy(player, synergy);
				processedThisTurn.get().add(synergyKey);
			}
		}
	}

	private static List<EmotionType> extractActiveEmotionTypes(Player player) {
		List<EmotionType> types = new ArrayList<>();
		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			types.add(emotion.getTipoBase());
		}
		return types;
	}

	private static void applySingleSynergy(Player player, SynergyEffect synergy) {
		applyStatBuffs(player, synergy);
		applyHealOverTime(player, synergy);
		applyPoisonEffect(player, synergy);
		applyStunEffect(player, synergy);
		applyGenericBuffs(player, synergy);
		applyGenericDebuffs(player, synergy);
	}

	private static void applyStatBuffs(CombatEntity entity, SynergyEffect synergy) {
		Map<String, Buff> buffs = entity.getActiveBuffs();

		if (synergy.getDamageMultiplier() != 1.0) {
			buffs.put(DAMAGE_BOOST, new Buff(DAMAGE_BOOST, synergy.getDamageMultiplier(), 2));
		}

		if (synergy.getDefenseMultiplier() != 1.0) {
			buffs.put(DEFENSE_BOOST, new Buff(DEFENSE_BOOST, synergy.getDefenseMultiplier(), 2));
		}

		if (synergy.getSpeedMultiplier() != 1.0) {
			buffs.put(SPEED_BOOST, new Buff(SPEED_BOOST, synergy.getSpeedMultiplier(), 2));
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

	// ==================== CÁLCULO DE DAÑO ====================

	public static int calculateFinalDamage(CombatEntity attacker, CombatEntity defender) {
		int baseDamage = attacker.getBaseDamage();

		// Aplicar buff de daño
		Buff damageBoost = attacker.getActiveBuffs().get(DAMAGE_BOOST);
		if (damageBoost != null) {
			baseDamage = (int) (baseDamage * damageBoost.getMultiplier());
		}

		// Aplicar buff de fusión
		Buff fusionDamage = attacker.getActiveBuffs().get(FUSION_DAMAGE);
		if (fusionDamage != null) {
			baseDamage = (int) (baseDamage * fusionDamage.getMultiplier());
		}

		// Calcular defensa
		int defense = defender.getBaseDefense();

		Buff defenseBoost = defender.getActiveBuffs().get(DEFENSE_BOOST);
		if (defenseBoost != null) {
			defense = (int) (defense * defenseBoost.getMultiplier());
		}

		Buff fusionDefense = defender.getActiveBuffs().get(FUSION_DEFENSE);
		if (fusionDefense != null) {
			defense = (int) (defense * fusionDefense.getMultiplier());
		}

		return Math.max(1, baseDamage - defense);
	}

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

	// ==================== ATTACK RESULT ====================

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
		CombatLogger.get().log(
				String.format("[Fusion Bonus] %s tiene %d emociones (x%.2f)", entity.getNombre(), count, multiplier));
	}

	private static void logActiveSynergies(CombatEntity entity, List<SynergyEffect> synergies) {
		CombatLogger.get().log(String.format("[%s %s] %d sinergia(s) activa(s)", entity.getEntityType(),
				entity.getNombre(), synergies.size()));
		for (SynergyEffect synergy : synergies) {
			CombatLogger.get().log("  ⚡ " + synergy.getName());
		}
	}

	private static void logSynergyHoT(CombatEntity entity, SynergyEffect synergy) {
		CombatLogger.get().log(String.format("[Synergy: %s] %s recibe HoT: %d HP x %d turnos", synergy.getName(),
				entity.getNombre(), synergy.getHotAmount(), synergy.getHotTurns()));
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
