package roguelike_emotions.ui.actions;

import roguelike_emotions.characters.Enemy;

public interface ActionExecutor {
	  /** Ejecuta la acción; target puede ser null si no se requiere. */
	  void execute(ActionId id, Enemy targetOrNull);
	}
