package roguelike_emotions.ui.actions;

import java.util.Locale;

/**
 * VO de la capa UI: representa la acción elegida sin filtrar enums del dominio.
 */
public enum ActionOption {
    ATTACK("atacar"),
    DEFEND("defender"),
    USE_EMOTION("usaremocion"),
    ITEM("objeto"),
    FLEE("huir");

    private final String label;

    ActionOption(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * Convierte desde el ID del JSON (ej: "ATTACK") a ActionOption.
     * Este es el método clave que faltaba.
     */
    public static ActionOption fromId(String id) {
        if (id == null) return ATTACK;

        String normalized = id.trim().toUpperCase(Locale.ROOT);
        try {
            return ActionOption.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            System.err.println("ActionOption: ID desconocido '" + id + "', usando ATTACK por defecto");
            return ATTACK;
        }
    }

    /**
     * Convierte desde el label UI (ej: "atacar") a ActionOption.
     * Usado para conversión desde UI.
     */
    public static ActionOption fromLabel(String label) {
        if (label == null) return ATTACK;

        String normalized = label.trim().toLowerCase(Locale.ROOT);
        for (ActionOption opt : values()) {
            if (opt.label.equals(normalized)) {
                return opt;
            }
        }

        System.err.println("ActionOption: Label desconocido '" + label + "', usando ATTACK por defecto");
        return ATTACK;
    }
}
