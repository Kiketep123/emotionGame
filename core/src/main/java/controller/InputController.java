package controller;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.ui.TargetSelector;
import roguelike_emotions.ui.actions.ActionId;
import roguelike_emotions.ui.actions.ActionOption;

/**
 * Orquesta el input de la UI en términos de comandos pequeños y testeables.
 * No conoce dominio, solo emite ActionId / mueve target.
 */
public final class InputController extends InputAdapter {

    private ActionOption selectedAction = ActionOption.ATTACK;
    private int selectedEnemyIndex = 0;
    private boolean enabled = true;

    private final Supplier<List<Enemy>> enemiesProvider;
    private final Runnable onConfirm;
    private final HotkeyMap hotkeys;
    private final Consumer<ActionId> onAction;

    /**
     * @param enemiesProvider acceso perezoso a la lista de enemigos en pantalla.
     * @param hotkeys         mapa de hotkeys data-driven (desde ActionDescriptor).
     * @param onAction        callback para cuando se pulsa una hotkey válida.
     * @param onConfirm       callback al confirmar acción (ENTER).
     */
    public InputController(
            Supplier<List<Enemy>> enemiesProvider,
            HotkeyMap hotkeys,
            Consumer<ActionId> onAction,
            Runnable onConfirm
    ) {
        this.enemiesProvider = enemiesProvider;
        this.hotkeys = hotkeys;
        this.onAction = onAction;
        this.onConfirm = onConfirm;
    }

    // ========== Getters/Setters ==========

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ActionOption getSelectedAction() {
        return selectedAction;
    }

    public int getSelectedEnemyIndex() {
        return selectedEnemyIndex;
    }

    public void setSelectedEnemyIndex(int idx) {
        this.selectedEnemyIndex = Math.max(0, idx);
    }

    /** Permite que la pantalla ajuste acción desde fuera (por ejemplo click UI). */
    public void setSelectedAction(ActionOption opt) {
        if (opt != null) {
            this.selectedAction = opt;
        }
    }

    // ========== InputProcessor ==========

    @Override
    public boolean keyDown(int keycode) {
        if (!enabled) return false;

        // 1) Hotkeys data-driven
        ActionId id = hotkeys.resolve(keycode);
        if (id != null) {
            onAction.accept(id);
            return true;
        }

        // 2) Navegación / confirmación
        switch (keycode) {
            case Input.Keys.LEFT:
                List<Enemy> enemiesLeft = enemiesProvider.get();
                selectedEnemyIndex = TargetSelector.moveIndex(enemiesLeft, selectedEnemyIndex, -1);
                return true;

            case Input.Keys.RIGHT:
                List<Enemy> enemiesRight = enemiesProvider.get();
                selectedEnemyIndex = TargetSelector.moveIndex(enemiesRight, selectedEnemyIndex, +1);
                return true;

            case Input.Keys.ENTER:
                onConfirm.run();
                return true;

            default:
                return false;
        }
    }
}
