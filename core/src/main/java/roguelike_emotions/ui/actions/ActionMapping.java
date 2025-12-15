package roguelike_emotions.ui.actions;

/**
 * Mapper de compatibilidad: convierte ids/labels UI a ActionId del dominio UI.
 * (PlayerAction ya no existe en el proyecto).
 */
public final class ActionMapping {
    private ActionMapping() {}

    public static ActionId toActionId(String idOrLabel) {
        return ActionId.fromLabel(idOrLabel);
    }
}
