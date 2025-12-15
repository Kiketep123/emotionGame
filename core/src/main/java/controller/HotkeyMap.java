package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Input;

import roguelike_emotions.ui.actions.ActionDescriptor;
import roguelike_emotions.ui.actions.ActionId;

/**
 * Mapea teclas (keycode) a ActionId basándose en la configuración JSON.
 */
public final class HotkeyMap {
    private final Map<Integer, ActionId> byKey = new HashMap<>();
    private int configHash = 0;

    /**
     * Resuelve un keycode a su ActionId correspondiente.
     * @return ActionId o null si no hay mapeo.
     */
    public ActionId resolve(int keycode) {
        return byKey.get(keycode);
    }

    /**
     * Reconstruye el mapa solo si la configuración cambió.
     */
    public void rebuildIfChanged(List<ActionDescriptor> actions) {
        int newHash = calculateHash(actions);
        if (newHash == configHash) return;

        byKey.clear();
        if (actions != null) {
            for (ActionDescriptor action : actions) {
                mapHotkey(action);
            }
        }
        configHash = newHash;
    }

    // ========== Helpers privados ==========

    private int calculateHash(List<ActionDescriptor> actions) {
        int hash = 1;
        if (actions != null) {
            for (ActionDescriptor a : actions) {
                hash = 31 * hash + (a.id() != null ? a.id().hashCode() : 0);
                hash = 31 * hash + (a.hotkey() != null ? a.hotkey().hashCode() : 0);
            }
        }
        return hash;
    }

    private void mapHotkey(ActionDescriptor action) {
        if (action.hotkey() == null || action.hotkey().isBlank()) return;

        try {
            int keycode = Input.Keys.valueOf(action.hotkey().trim());
            ActionId actionId = ActionId.fromLabel(action.id());
            byKey.put(keycode, actionId);
        } catch (IllegalArgumentException e) {
            System.err.println("HotkeyMap: Hotkey inválida '" + action.hotkey() + "' para acción '" + action.id() + "'");
        }
    }
}
