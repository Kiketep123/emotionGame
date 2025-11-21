package roguelike_emotions.ui.turns;

import java.util.List;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.ui.TargetSelector;
import roguelike_emotions.ui.actions.ActionOption;
import roguelike_emotions.vfx.Director;

/**
 * Capa de aplicación (UI): valida estado, llama a la Facade y no pisa
 * cinematics. No conoce detalles del CombatManager (Strategy oculta tras la
 * Facade).
 */
public final class TurnOrchestrator {

	/**
	 * Intenta ejecutar el turno (acción del jugador -> ticks -> respuesta enemiga)
	 * si la escena está libre.
	 */
	public static boolean tryExecute(GameManager gm, Director director, Player player, List<Enemy> enemies,
			ActionOption action, int desiredIndex) {
		if (director != null && director.isBusy())
			return false;
		if (gm == null || player == null || enemies == null || enemies.isEmpty())
			return false;

		Enemy target = TargetSelector.pickAlive(enemies, desiredIndex);
		if (target == null)
			return false;

		// RONDA completa: jugador -> ticks -> contra del objetivo -> resto de enemigos
		gm.executeCombatRound(player, enemies, action.label(), target);
		return true;
	}

	private TurnOrchestrator() {
	}
}
