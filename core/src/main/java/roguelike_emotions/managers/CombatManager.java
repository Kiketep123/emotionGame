package roguelike_emotions.managers;

import java.util.List;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.vfx.DamageEvent;
import roguelike_emotions.vfx.HealEvent;
import roguelike_emotions.vfx.TurnStepEvent;
import roguelike_emotions.vfx.VisBus;

/**
 * 🎮 CombatManager v2.0 - CON SISTEMA DE EXPERIENCIA
 */
public class CombatManager {

	private static final int PLAYER_VIEW_ID = 1;
	private final CombatLogger logger;

	public CombatManager() {
		this.logger = CombatLogger.get();
	}

	// ==================== TURNO SIMPLE ====================
	public CombatResult executeTurn(Player player, Enemy enemy, PlayerAction action) {
		if (!validateCombatants(player, enemy)) {
			return createInvalidResult();
		}

		int enemyViewId = computeEnemyViewId(enemy);
		StringBuilder summary = new StringBuilder();

		// FASE 1: Acción del jugador
		executePlayerPhase(player, enemy, action, enemyViewId, summary);

		// FASE 2: Efectos de turno
		executeTurnEffectsPhase(player, enemy, summary);

		// FASE 3: Contraataque del enemigo
		if (enemy.isAlive()) {
			executeEnemyPhase(player, enemy, enemyViewId, summary);
		}

		// 🆕 FASE 4: Experiencia post-turno
		grantTurnExperience(player, enemy);

		return buildResult(player, enemy, summary.toString());
	}

	// ==================== TURNO MÚLTIPLE ====================
	public CombatResult executeRound(Player player, List<Enemy> enemies, PlayerAction action, Enemy target) {
		if (!validateCombatants(player, target)) {
			return createInvalidResult();
		}

		if (enemies == null || enemies.isEmpty()) {
			return new CombatResult(player.isAlive(), false, 0, 0, "Sistema: No hay enemigos.");
		}

		StringBuilder summary = new StringBuilder();
		int targetViewId = computeEnemyViewId(target);

		// FASE 1: Player actúa
		VisBus.post(new TurnStepEvent(PLAYER_VIEW_ID, "PLAYER"));
		executePlayerPhase(player, target, action, targetViewId, summary);

		// FASE 2: Efectos de turno
		executeTurnEffectsPhase(player, target, summary);

		// FASE 3: Target contraataca
		if (target.isAlive()) {
			VisBus.post(new TurnStepEvent(targetViewId, "ENEMY"));
			executeEnemyPhase(player, target, targetViewId, summary);
		}

		// FASE 4: Resto de enemigos
		for (Enemy enemy : enemies) {
			if (enemy == null || !enemy.isAlive() || enemy == target) {
				continue;
			}

			int viewId = computeEnemyViewId(enemy);
			VisBus.post(new TurnStepEvent(viewId, "ENEMY"));
			executeEnemyPhase(player, enemy, viewId, summary);

			if (!player.isAlive()) {
				break;
			}
		}

		// 🆕 FASE 5: Experiencia post-round
		grantTurnExperience(player, target);

		return buildResult(player, target, summary.toString());
	}

	// ==================== FASES DE COMBATE ====================

	private void executePlayerPhase(Player player, Enemy enemy, PlayerAction action, int enemyViewId,
			StringBuilder summary) {
		int healthBefore = enemy.getHealth();

		switch (action) {
		case ATTACK:
			player.attack(enemy);
			int damage = healthBefore - enemy.getHealth();

			if (damage > 0) {
				VisBus.post(new DamageEvent(PLAYER_VIEW_ID, enemyViewId, damage, "PLAYER_ATTACK"));

				// 🆕 Experiencia por daño causado
				grantDamageExperience(player, damage);
			}
			break;

		case DEFEND:
			player.defender(10);
			// 🆕 Pequeña XP por defender
			grantActionExperience(player, 3);
			break;

		case USE_EMOTION:
			List<EmotionInstance> emotions = player.getEmocionesActivas();
			if (emotions != null && !emotions.isEmpty()) {
				EmotionInstance emotion = emotions.get(0);
				int playerHealthBefore = player.getHealth();
				player.usarEmocion(emotion);

				int healing = player.getHealth() - playerHealthBefore;
				int damageTaken = Math.max(0, playerHealthBefore - player.getHealth());

				if (damageTaken > 0) {
					VisBus.post(new DamageEvent(enemyViewId, PLAYER_VIEW_ID, damageTaken, "ENEMY_ATTACK"));
				}

				if (healing > 0) {
					VisBus.post(new HealEvent(PLAYER_VIEW_ID, PLAYER_VIEW_ID, healing, "HEAL"));
					// 🆕 XP por curación
					grantHealingExperience(player, healing);
				}

				// 🆕 XP por usar emoción
				grantActionExperience(player, 5);
			} else {
				logger.log("[Jugador] No tiene emociones activas");
			}
			break;
		}
	}

	private void executeTurnEffectsPhase(Player player, Enemy enemy, StringBuilder summary) {
		int playerHealthBefore = player.getHealth();
		int enemyHealthBefore = enemy.getHealth();

		player.tickTurnoEmocional();
		enemy.tickTurnoEmocional(player);

		int playerDelta = player.getHealth() - playerHealthBefore;
		int enemyDelta = enemyHealthBefore - enemy.getHealth();

		if (playerDelta > 0) {
			VisBus.post(new HealEvent(PLAYER_VIEW_ID, PLAYER_VIEW_ID, playerDelta, "HOT"));
		} else if (playerDelta < 0) {
			VisBus.post(new DamageEvent(computeEnemyViewId(enemy), PLAYER_VIEW_ID, -playerDelta, "DOT"));
		}

		if (enemyDelta > 0) {
			VisBus.post(new DamageEvent(PLAYER_VIEW_ID, computeEnemyViewId(enemy), enemyDelta, "DOT"));
		}
	}

	private void executeEnemyPhase(Player player, Enemy enemy, int enemyViewId, StringBuilder summary) {
		int playerHealthBefore = player.getHealth();

		enemy.atacar(player);

		int damage = playerHealthBefore - player.getHealth();
		if (damage > 0) {
			VisBus.post(new DamageEvent(enemyViewId, PLAYER_VIEW_ID, damage, "ENEMY_ATTACK"));
		}
	}

	// ==================== 🆕 SISTEMA DE EXPERIENCIA ====================

	/**
	 * 🆕 Otorga XP a todas las emociones por sobrevivir el turno
	 */
	private void grantTurnExperience(Player player, Enemy enemy) {
		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(5); // XP base por turno
		}
	}

	/**
	 * 🆕 Otorga XP por daño causado
	 */
	public void grantDamageExperience(Player player, int damage) {
		int expGain = Math.min(15, damage / 2);

		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(expGain);
		}
	}

	/**
	 * 🆕 Otorga XP por curación
	 */
	public void grantHealingExperience(Player player, int healing) {
		int expGain = Math.min(10, healing / 3);

		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(expGain);
		}
	}

	/**
	 * 🆕 Otorga XP por acción específica
	 */
	public void grantActionExperience(Player player, int amount) {
		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(amount);
		}
	}

	/**
	 * 🆕 Otorga XP masiva al derrotar un enemigo
	 */
	public void onEnemyDefeated(Player player, Enemy enemy) {
		int expReward = 30; // Base

		// Bonus por tipo de enemigo
		if (enemy.getNombre().contains("Jefe") || enemy.getNombre().contains("Boss")) {
			expReward = 100;
			logger.log("🏆 ¡JEFE DERROTADO! +100 XP para todas las emociones");
		} else if (enemy.getNombre().contains("Elite")) {
			expReward = 50;
			logger.log("⚔️ Elite derrotado! +50 XP para todas las emociones");
		}

		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(expReward);
		}

		logger.log("✨ Todas las emociones ganaron " + expReward + " XP");
	}

	/**
	 * 🆕 Otorga XP por sobrevivir con poca vida
	 */
	public void onNearDeathSurvival(Player player) {
		if (player.getHealth() <= player.getMaxHealth() * 0.2) {
			logger.log("💀 ¡Sobreviviste al borde de la muerte! +50 XP");

			for (EmotionInstance emotion : player.getEmocionesActivas()) {
				emotion.gainExperience(50);
			}
		}
	}

	/**
	 * 🆕 Otorga XP crítica por daño crítico
	 */
	public void onCriticalHit(Player player, int damage) {
		logger.log("💥 ¡CRÍTICO! +" + (damage / 4) + " XP");

		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(damage / 4);
		}
	}

	// ==================== HELPERS ====================

	private boolean validateCombatants(Player player, Enemy enemy) {
		if (player == null || enemy == null) {
			logger.log("Sistema: Jugador o enemigo nulo");
			return false;
		}

		if (!player.isAlive() || !enemy.isAlive()) {
			logger.log("Sistema: Combatiente muerto al inicio del turno");
			return false;
		}

		return true;
	}

	private CombatResult buildResult(Player player, Enemy enemy, String summary) {
		if (!player.isAlive()) {
			logger.log("Fin: El jugador ha muerto");
			return new CombatResult(false, enemy.isAlive(), 0, 0, summary);
		}

		if (!enemy.isAlive()) {
			logger.log("Fin: " + enemy.getNombre() + " ha sido derrotado");
			// 🆕 Otorgar XP por victoria
			onEnemyDefeated(player, enemy);
			return new CombatResult(true, false, 0, 0, summary);
		}

		// 🆕 Check supervivencia crítica
		onNearDeathSurvival(player);

		return new CombatResult(player.isAlive(), enemy.isAlive(), 0, 0, summary);
	}

	private CombatResult createInvalidResult() {
		return new CombatResult(false, false, 0, 0, "Combate inválido");
	}

	private int computeEnemyViewId(Enemy enemy) {
		List<Enemy> enemies = GameManager.getInstance().getEnemies();
		if (enemies == null)
			return 100;
		int index = enemies.indexOf(enemy);
		return 100 + Math.max(0, index);
	}

	// ==================== ENUMS Y RECORDS ====================

	public enum PlayerAction {
		ATTACK, DEFEND, USE_EMOTION;

		public static PlayerAction fromString(String action) {
			if (action == null || action.isBlank()) {
				return ATTACK;
			}

			String normalized = action.trim().toUpperCase();
			return switch (normalized) {
			case "ATTACK", "ATACAR", "ATAQUE" -> ATTACK;
			case "DEFEND", "DEFENDER", "DEFENSA" -> DEFEND;
			case "USE_EMOTION", "EMOTION", "USAREMOCION" -> USE_EMOTION;
			default -> ATTACK;
			};
		}
	}

	public record CombatResult(boolean playerAlive, boolean enemyAlive, int damageToEnemy, int damageToPlayer,
			String summary) {
	}
}
