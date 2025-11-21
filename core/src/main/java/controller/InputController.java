package controller;

import java.util.List;
import java.util.function.Supplier;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.ui.TargetSelector;
import roguelike_emotions.ui.actions.ActionId;
import roguelike_emotions.ui.actions.ActionOption;

/**
 * Orquesta el input de la UI en términos de comandos pequeños y testeables. No
 * conoce libGDX fuera del mapeo de keycodes, ni al dominio.
 */
public final class InputController extends InputAdapter {
	private ActionOption selectedAction = ActionOption.ATTACK;
	private int selectedEnemyIndex = 0;
	private final java.util.function.Supplier<List<Enemy>> enemiesProvider;
	private final Runnable onConfirm;
	private final HotkeyMap hotkeys;
	private final java.util.function.Consumer<ActionId> onAction;

	/**
	 * @param enemiesProvider acceso perezoso a la lista de enemigos en pantalla.
	 * @param onConfirm       callback a ejecutar cuando se confirma la acción
	 *                        (ENTER).
	 */
	//TODO inyectar hotkeys y onAction
	public InputController(Supplier<List<Enemy>> enemiesProvider, Runnable onConfirm) {
		this.enemiesProvider = enemiesProvider;
		this.onConfirm = onConfirm;
		this.hotkeys = new HotkeyMap();
		this.onAction = null;
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

	@Override
	public boolean keyDown(int keycode) {

		ActionId id = hotkeys.resolve(keycode);
		if (id != null) {
			onAction.accept(id);
			return true;
		}
		switch (keycode) {
		case Input.Keys.NUM_1 -> selectedAction = ActionOption.ATTACK;
		case Input.Keys.NUM_2 -> selectedAction = ActionOption.DEFEND;
		case Input.Keys.NUM_3 -> selectedAction = ActionOption.USE_EMOTION;
		case Input.Keys.LEFT -> {
			List<Enemy> es = enemiesProvider.get();
			selectedEnemyIndex = TargetSelector.moveIndex(es, selectedEnemyIndex, -1);
		}
		case Input.Keys.RIGHT -> {
			List<Enemy> es = enemiesProvider.get();
			selectedEnemyIndex = TargetSelector.moveIndex(es, selectedEnemyIndex, +1);
		}
		case Input.Keys.ENTER -> onConfirm.run();
		}
		return true;
	}
}
