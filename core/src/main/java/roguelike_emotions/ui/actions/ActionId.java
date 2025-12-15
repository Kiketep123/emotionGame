package roguelike_emotions.ui.actions;

import java.util.Locale;

/**
 * Identificador de acción desde el JSON. Mapea los IDs del actions.json a las
 * opciones del juego.
 */
public enum ActionId {
	ATTACK, DEFEND, USE_EMOTION, ITEM, FLEE;

	/**
	 * Convierte el ID del JSON a ActionId.
	 */
	public static ActionId fromLabel(String label) {
		if (label == null)
			return ATTACK;

		String normalized = label.trim().toUpperCase(Locale.ROOT);
		try {
			return ActionId.valueOf(normalized);
		} catch (IllegalArgumentException e) {
			System.err.println("ActionId: Label desconocido '" + label + "', usando ATTACK por defecto");
			return ATTACK;
		}
	}

	/**
	 * Convierte ActionId a ActionOption para la UI.
	 */
	public ActionOption toOption() {
		return ActionOption.valueOf(this.name());
	}
}
