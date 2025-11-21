package roguelike_emotions.ui.actions;

import java.util.Locale;

/**
 * VO de la capa UI: representa la acción elegida sin filtrar enums del dominio.
 */
public enum ActionOption {
	ATTACK("atacar"), DEFEND("defender"), USE_EMOTION("usaremocion");

	private final String label;

	ActionOption(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	/** Label UI -> opción UI (robusto a may/min). */
	public static ActionOption fromLabel(String label) {
		if (label == null)
			return ATTACK;
		String s = label.trim().toLowerCase(Locale.ROOT);
		for (var opt : values())
			if (opt.label.equals(s))
				return opt;
		return ATTACK;
	}
}
