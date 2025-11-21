package roguelike_emotions.managers;

import java.util.List;
import java.util.Locale;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.vfx.BuffAppliedEvent;
import roguelike_emotions.vfx.DamageEvent;
import roguelike_emotions.vfx.HealEvent;
import roguelike_emotions.vfx.TurnStepEvent;
import roguelike_emotions.vfx.VisBus;

/**
 * Gestor especializado en combate - Responsabilidad única: ejecutar turnos de
 * combate
 */
class CombatManager {

	private static final int PLAYER_VIEW_ID = 1;
	private static final int DEFENSE_BONUS = 25;

	private final CombatLogger logger;

	public CombatManager() {
		this.logger = CombatLogger.get();
	}

	/**
	 * Ejecuta un turno completo de combate
	 */
	public CombatResult executeTurn(Player player, Enemy enemy, PlayerAction action) {
		if (!validateCombatants(player, enemy)) {
			return createInvalidResult();
		}

		int enemyViewId = computeEnemyViewId(enemy);

		// Fase 1: Acción del jugador
		CombatPhaseResult playerPhase = executePlayerAction(player, enemy, action, enemyViewId);

		// Fase 2: Efectos de turno (DoT, HoT, etc.)
		CombatPhaseResult tickPhase = executeTurnEffects(player, enemy, enemyViewId);

		// Fase 3: Contraataque enemigo (si sobrevive)
		CombatPhaseResult enemyPhase = executeEnemyAction(player, enemy, enemyViewId);

		// Construir resultado final
		return buildCombatResult(player, enemy, playerPhase, tickPhase, enemyPhase);
	}

	private boolean validateCombatants(Player player, Enemy enemy) {
		if (player == null || enemy == null) {
			logger.log("[Sistema] Jugador o enemigo nulo");
			return false;
		}

		if (!player.isAlive() || !enemy.isAlive()) {
			logger.log("[Sistema] Combatiente muerto al inicio del turno");
			return false;
		}

		return true;
	}

	private CombatPhaseResult executePlayerAction(Player player, Enemy enemy, PlayerAction action, int enemyViewId) {
		return switch (action) {
		case ATTACK -> executePlayerAttack(player, enemy, enemyViewId);
		case DEFEND -> executePlayerDefend(player);
		case USE_EMOTION -> executePlayerEmotion(player, enemy, enemyViewId);
		};
	}

	private CombatPhaseResult executePlayerAttack(Player player, Enemy enemy, int enemyViewId) {
		int healthBefore = getHealth(enemy);
		player.attack(enemy);
		int damage = calculateDamage(healthBefore, getHealth(enemy));

		if (damage > 0) {
			VisBus.post(new DamageEvent(PLAYER_VIEW_ID, enemyViewId, damage, "PLAYER_ATTACK"));
		}

		String message = String.format("[Jugador] Ataca a %s infligiendo %d de daño.Defensa actual: %c", getName(enemy),
				damage, enemy.getDefensa());
		logger.log(message);

		return new CombatPhaseResult(0, damage, message);
	}

	private CombatPhaseResult executePlayerDefend(Player player) {
		player.defender(DEFENSE_BONUS);

		VisBus.post(new BuffAppliedEvent(PLAYER_VIEW_ID, "DEFENSE", 1, 1));

		String message = String.format("[Jugador] Se defiende (+%d defensa)", DEFENSE_BONUS);
		logger.log(message);

		return new CombatPhaseResult(0, 0, message);
	}

	private CombatPhaseResult executePlayerEmotion(Player player, Enemy enemy, int enemyViewId) {
		List<EmotionInstance> activeEmotions = player.getEmocionesActivas();

		if (activeEmotions == null || activeEmotions.isEmpty()) {
			String message = "[Jugador] No tiene emociones activas";
			logger.log(message);
			return new CombatPhaseResult(0, 0, message);
		}

		EmotionInstance emotion = activeEmotions.get(0);

		int playerHealthBefore = getHealth(player);
		int enemyHealthBefore = getHealth(enemy);

		player.usarEmocion(emotion);

		int healing = calculateHealing(playerHealthBefore, getHealth(player));
		int damage = calculateDamage(enemyHealthBefore, getHealth(enemy));

		// Emitir eventos visuales
		if (healing > 0) {
			VisBus.post(new HealEvent(PLAYER_VIEW_ID, PLAYER_VIEW_ID, healing, emotionToTag(emotion, "HEAL")));
		}

		if (damage > 0) {
			VisBus.post(new DamageEvent(PLAYER_VIEW_ID, enemyViewId, damage, emotionToTag(emotion, "EMO")));
		}

		String message = buildEmotionMessage(emotion, healing, damage);
		logger.log(message);

		return new CombatPhaseResult(healing, damage, message);
	}

	private CombatPhaseResult executeTurnEffects(Player player, Enemy enemy, int enemyViewId) {
		int playerHealthBefore = getHealth(player);
		int enemyHealthBefore = getHealth(enemy);

		player.tickTurnoEmocional();
		enemy.tickTurnoEmocional(player);

		int playerDelta = getHealth(player) - playerHealthBefore;
		int enemyDamage = Math.max(0, enemyHealthBefore - getHealth(enemy));

		// Efectos sobre el jugador
		if (playerDelta > 0) {
			VisBus.post(new HealEvent(PLAYER_VIEW_ID, PLAYER_VIEW_ID, playerDelta, "HOT"));
		} else if (playerDelta < 0) {
			VisBus.post(new DamageEvent(enemyViewId, PLAYER_VIEW_ID, -playerDelta, "DOT"));
		}

		// Efectos sobre el enemigo
		if (enemyDamage > 0) {
			VisBus.post(new DamageEvent(PLAYER_VIEW_ID, enemyViewId, enemyDamage, "DOT"));
		}

		String message = buildTickMessage(playerDelta, enemyDamage);
		if (!message.isEmpty()) {
			logger.log(message);
		}

		return new CombatPhaseResult(Math.max(0, playerDelta), enemyDamage, message);
	}

	private CombatPhaseResult executeEnemyAction(Player player, Enemy enemy, int enemyViewId) {
		String message = "";
		if (!enemy.isAlive()) {
			return new CombatPhaseResult(0, 0, "");
		}

		int playerHealthBefore = getHealth(player);
		enemy.atacar(player);
		int damage = calculateDamage(playerHealthBefore, getHealth(player));

		if (damage > 0) {
			VisBus.post(new DamageEvent(enemyViewId, PLAYER_VIEW_ID, damage, "ENEMY_ATTACK"));
			message = String.format("[%s] Ataca al jugador infligiendo %d de daño", getName(enemy), damage);
			logger.log(message);
		}

		return new CombatPhaseResult(0, damage, message);
	}

	private CombatResult buildCombatResult(Player player, Enemy enemy, CombatPhaseResult playerPhase,
			CombatPhaseResult tickPhase, CombatPhaseResult enemyPhase) {
		int totalDamageToEnemy = playerPhase.damageDealt + tickPhase.damageDealt;
		int totalDamageToPlayer = enemyPhase.damageDealt;

		StringBuilder summary = new StringBuilder();
		summary.append(playerPhase.message);
		if (!tickPhase.message.isEmpty()) {
			summary.append("\n").append(tickPhase.message);
		}
		if (!enemyPhase.message.isEmpty()) {
			summary.append("\n").append(enemyPhase.message);
		}

		// Log final del turno
		if (!player.isAlive()) {
			logger.log("[Fin] El jugador ha muerto");
			System.exit(0); // Termina el juego
		} else if (!enemy.isAlive()) {
			logger.log("[Fin] " + getName(enemy) + " ha sido derrotado");
			// System.exit(0); // Termina el juego

		}

		return new CombatResult(player.isAlive(), enemy.isAlive(), totalDamageToEnemy, totalDamageToPlayer,
				summary.toString());
	}

	// ========== Métodos auxiliares ==========

	private int computeEnemyViewId(Enemy enemy) {
		List<Enemy> enemies = GameManager.getInstance().getEnemies();
		if (enemies == null)
			return 100;

		int index = enemies.indexOf(enemy);
		return 100 + Math.max(0, index);
	}

	private int getHealth(Player player) {
		try {
			return player.getHealth();
		} catch (Exception e) {
			logger.log("[Error] No se pudo obtener vida del jugador: " + e.getMessage());
			return 0;
		}
	}

	private int getHealth(Enemy enemy) {
		try {
			return enemy.getHealth();
		} catch (Exception e) {
			logger.log("[Error] No se pudo obtener vida del enemigo: " + e.getMessage());
			return 0;
		}
	}

	private String getName(Enemy enemy) {
		try {
			String name = enemy.getNombre();
			return (name != null && !name.isEmpty()) ? name : "Enemigo";
		} catch (Exception e) {
			return "Enemigo";
		}
	}

	private String getName(EmotionInstance emotion) {
		try {
			String name = emotion.toString();
			return (name != null && !name.isEmpty()) ? name : "Emoción";
		} catch (Exception e) {
			return "Emoción";
		}
	}

	private int calculateDamage(int healthBefore, int healthAfter) {
		return Math.max(0, healthBefore - healthAfter);
	}

	private int calculateHealing(int healthBefore, int healthAfter) {
		return Math.max(0, healthAfter - healthBefore);
	}

	private String emotionToTag(EmotionInstance emotion, String fallback) {
		try {
			String name = emotion.toString();
			if (name == null || name.isBlank()) {
				return fallback;
			}

			String normalized = name.toUpperCase(Locale.ROOT);

			if (normalized.contains("FUEGO") || normalized.contains("FIRE")) {
				return "FIRE";
			}
			if (normalized.contains("VENENO") || normalized.contains("POISON")) {
				return "POISON";
			}
			if (normalized.contains("HIELO") || normalized.contains("ICE")) {
				return "ICE";
			}
			if (normalized.contains("CUR") || normalized.contains("HEAL")) {
				return "HEAL";
			}

			return normalized;
		} catch (Exception e) {
			return fallback;
		}
	}

	private String buildEmotionMessage(EmotionInstance emotion, int healing, int damage) {
		StringBuilder msg = new StringBuilder("[Jugador] Usa emoción (");
		msg.append(getName(emotion)).append(")");

		if (healing > 0) {
			msg.append(" - Cura ").append(healing).append(" HP");
		}
		if (damage > 0) {
			if (healing > 0)
				msg.append(",");
			msg.append(" Inflige ").append(damage).append(" de daño");
		}

		return msg.toString();
	}

	private String buildTickMessage(int playerDelta, int enemyDamage) {
		StringBuilder msg = new StringBuilder();

		if (playerDelta > 0) {
			msg.append("[Efectos] Jugador regenera ").append(playerDelta).append(" HP");
		} else if (playerDelta < 0) {
			msg.append("[Efectos] Jugador pierde ").append(-playerDelta).append(" HP");
		}

		if (enemyDamage > 0) {
			if (!msg.isEmpty())
				msg.append(" | ");
			msg.append("Enemigo pierde ").append(enemyDamage).append(" HP");
		}

		return msg.toString();
	}

	private CombatResult createInvalidResult() {
		return new CombatResult(false, false, 0, 0, "Combate inválido");
	}

	/**
	 * Resultado interno de una fase de combate
	 */
	private static class CombatPhaseResult {
		final int damageDealt;
		final String message;
		final int healing;

		CombatPhaseResult(int healing, int damageDealt, String message) {
			this.damageDealt = damageDealt;
			this.message = message;
			this.healing = healing;
		}
	}

	public CombatResult executeRound(Player player, List<Enemy> enemies, PlayerAction action, Enemy target) {
	    if (!validateCombatants(player, target)) {
	        return createInvalidResult();
	    }
	    if (enemies == null || enemies.isEmpty()) {
	        return new CombatResult(player.isAlive(), false, 0, 0, "[Sistema] No hay enemigos.");
	    }

	    CombatState state = new CombatState(player, enemies);
	    StringBuilder summary = new StringBuilder();

	    executePlayerPhase(player, target, action, summary);
	    executeTurnEffectsPhase(player, target, summary);
	    executeTargetCounterPhase(player, target, summary);
	    executeRemainingEnemiesPhase(player, enemies, target, summary);

	    return state.buildResult(player, enemies, summary.toString());
	}

	private void executePlayerPhase(Player player, Enemy target, PlayerAction action, StringBuilder summary) {
	    int targetViewId = computeEnemyViewId(target);
	    VisBus.post(new TurnStepEvent(PLAYER_VIEW_ID, "PLAYER"));

	    CombatPhaseResult result = executePlayerAction(player, target, action, targetViewId);
	    appendMessage(summary, result.message);
	}

	private void executeTurnEffectsPhase(Player player, Enemy target, StringBuilder summary) {
	    int targetViewId = computeEnemyViewId(target);
	    CombatPhaseResult result = executeTurnEffects(player, target, targetViewId);
	    appendMessage(summary, result.message);
	}

	private void executeTargetCounterPhase(Player player, Enemy target, StringBuilder summary) {
	    if (!isAlive(target)) {
	        return;
	    }

	    int targetViewId = computeEnemyViewId(target);
	    VisBus.post(new TurnStepEvent(targetViewId, "ENEMY"));

	    CombatPhaseResult result = executeEnemyAction(player, target, targetViewId);
	    appendMessage(summary, result.message);
	}

	private void executeRemainingEnemiesPhase(Player player, List<Enemy> enemies, Enemy target, StringBuilder summary) {
	    for (Enemy enemy : enemies) {
	        if (!shouldProcessEnemy(enemy, target, player)) {
	            continue;
	        }

	        executeEnemyTurn(player, enemy, summary);

	        if (!player.isAlive()) {
	            break;
	        }
	    }
	}

	private boolean shouldProcessEnemy(Enemy enemy, Enemy target, Player player) {
	    return enemy != null && enemy.isAlive() && enemy != target;
	}

	private void executeEnemyTurn(Player player, Enemy enemy, StringBuilder summary) {
	    int viewId = computeEnemyViewId(enemy);
	    VisBus.post(new TurnStepEvent(viewId, "ENEMY"));

	    CombatPhaseResult result = executeEnemyAction(player, enemy, viewId);
	    appendMessage(summary, result.message);
	}

	private void appendMessage(StringBuilder summary, String message) {
	    if (message.isEmpty()) {
	        return;
	    }

	    if (!summary.isEmpty()) {
	        summary.append('\n');
	    }
	    summary.append(message);
	}

	private boolean isAlive(Enemy enemy) {
	    return enemy != null && enemy.isAlive();
	}

	// Clase interna para encapsular el estado del combate
	private  class CombatState {
	    private final int initialPlayerHealth;
	    private final int[] initialEnemyHealth;

	    CombatState(Player player, List<Enemy> enemies) {
	        this.initialPlayerHealth = getHealth(player);
	        this.initialEnemyHealth = captureEnemyHealth(enemies);
	    }

	    private int[] captureEnemyHealth(List<Enemy> enemies) {
	        int[] health = new int[enemies.size()];
	        for (int i = 0; i < enemies.size(); i++) {
	            Enemy enemy = enemies.get(i);
	            health[i] = (enemy != null) ? getHealth(enemy) : 0;
	        }
	        return health;
	    }

	    CombatResult buildResult(Player player, List<Enemy> enemies, String summary) {
	        int playerDamage = calculatePlayerDamage(player);
	        int enemyDamage = calculateTotalEnemyDamage(enemies);
	        boolean anyEnemyAlive = checkAnyEnemyAlive(enemies);

	        return new CombatResult(
	            player.isAlive(),
	            anyEnemyAlive,
	            enemyDamage,
	            playerDamage,
	            summary
	        );
	    }

	    private int calculatePlayerDamage(Player player) {
	        int currentHealth = getHealth(player);
	        return Math.max(0, initialPlayerHealth - currentHealth);
	    }

	    private int calculateTotalEnemyDamage(List<Enemy> enemies) {
	        int totalDamage = 0;
	        for (int i = 0; i < enemies.size(); i++) {
	            totalDamage += calculateEnemyDamage(enemies.get(i), i);
	        }
	        return totalDamage;
	    }

	    private int calculateEnemyDamage(Enemy enemy, int index) {
	        int currentHealth = (enemy != null) ? getHealth(enemy) : 0;
	        return Math.max(0, initialEnemyHealth[index] - currentHealth);
	    }

	    private boolean checkAnyEnemyAlive(List<Enemy> enemies) {
	        for (Enemy enemy : enemies) {
	            if (enemy != null && enemy.isAlive()) {
	                return true;
	            }
	        }
	        return false;
	    }
	}

}