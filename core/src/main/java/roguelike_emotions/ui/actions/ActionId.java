package roguelike_emotions.ui.actions;

public enum ActionId {
	ATTACK, DEFEND, USE_EMOTION, ITEM, FLEE;

	/** Útil si vienes de etiquetas UI. */
	public static ActionId fromLabel(String label) {
		if (label == null)
			return ATTACK;
		String s = label.trim().toUpperCase();
		return switch (s) {
		case "ATACAR", "ATTACK" -> ATTACK;
		case "DEFENDER", "DEFEND" -> DEFEND;
		case "USAR EMOCION", "USAR_EMOCION", "USE_EMOTION" -> USE_EMOTION;
		case "OBJETO", "ITEM" -> ITEM;
		case "HUIR", "FLEE" -> FLEE;
		default -> ATTACK;
		};
	}
}
