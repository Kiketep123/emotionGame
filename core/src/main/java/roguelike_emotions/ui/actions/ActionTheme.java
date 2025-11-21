package roguelike_emotions.ui.actions;

public record ActionTheme(float pillHeight, float gap, float padding, float minPillWidth) {
	public static ActionTheme defaults() {
		return new ActionTheme(40f, 12f, 16f, 120f);
	}
}
