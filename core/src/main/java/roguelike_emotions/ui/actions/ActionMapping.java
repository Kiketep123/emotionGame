package roguelike_emotions.ui.actions;


public final class ActionMapping {
	private ActionMapping() {
	}

	public static PlayerAction toPlayerAction(String idOrLabel) {
		if (idOrLabel == null)
			return PlayerAction.ATTACK;
		String s = idOrLabel.trim().toUpperCase();
		return switch (s) {
		case "ATTACK", "ATACAR" -> PlayerAction.ATTACK;
		case "DEFEND", "DEFENDER" -> PlayerAction.DEFEND;
		case "USE_EMOTION", "USAR_EMOCION" -> PlayerAction.USE_EMOTION;
		// añade aquí si luego metes ITEM / FLEE:
		// case "ITEM", "OBJETO" -> PlayerAction.ITEM;
		// case "FLEE", "HUIR" -> PlayerAction.FLEE;
		default -> PlayerAction.ATTACK;
		};
	}
}
