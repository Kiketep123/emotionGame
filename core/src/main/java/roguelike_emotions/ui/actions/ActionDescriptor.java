package roguelike_emotions.ui.actions;

	// Describe an action that can be performed by the player.
public record ActionDescriptor(
        String id,          // "ATTACK", "DEFEND", "USE_EMOTION" ...
        String label,       // "Atacar"
        String hotkey,      // "NUM_1"
        boolean requiresTarget
) {}
