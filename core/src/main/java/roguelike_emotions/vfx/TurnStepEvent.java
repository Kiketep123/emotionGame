package roguelike_emotions.vfx;

public final class TurnStepEvent implements VisEvent {
	private final int actorViewId; // 1 = player, 100+i = enemigo i
	private final String label; // "PLAYER" | "ENEMY"

	public TurnStepEvent(int actorViewId, String label) {
		this.actorViewId = actorViewId;
		this.label = label;
	}

	public int actorViewId() {
		return actorViewId;
	}

	public String label() {
		return label;
	}
}
