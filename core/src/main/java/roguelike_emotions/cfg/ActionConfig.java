package roguelike_emotions.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import roguelike_emotions.ui.actions.ActionDescriptor;
import roguelike_emotions.ui.actions.ActionTheme;

/**
 * Configuración de acciones cargada desde JSON.
 */
public final class ActionConfig {
	private final List<ActionDescriptor> actions;
	private final ActionTheme theme;

	private ActionConfig(List<ActionDescriptor> actions, ActionTheme theme) {
		this.actions = actions;
		this.theme = theme;
	}

	public List<ActionDescriptor> actions() {
		return actions;
	}

	public ActionTheme theme() {
		return theme;
	}

	/**
	 * Carga la configuración desde un archivo JSON.
	 */
	public static ActionConfig load(FileHandle file) {
		try {
			JsonValue root = new JsonReader().parse(file);
			ActionTheme theme = loadTheme(root.get("theme"));
			List<ActionDescriptor> actions = loadActions(root.get("actions"));
			return new ActionConfig(Collections.unmodifiableList(actions), theme);
		} catch (Exception e) {
			System.err.println("Error cargando ActionConfig: " + e.getMessage());
			return new ActionConfig(Collections.emptyList(), ActionTheme.defaults());
		}
	}

	// ========== Helpers privados ==========

	private static ActionTheme loadTheme(JsonValue themeJson) {
		if (themeJson == null)
			return ActionTheme.defaults();

		return new ActionTheme(themeJson.getFloat("pillHeight", 40f), themeJson.getFloat("gap", 12f),
				themeJson.getFloat("padding", 16f), themeJson.getFloat("minPillWidth", 120f));
	}

	private static List<ActionDescriptor> loadActions(JsonValue actionsArray) {
		List<ActionDescriptor> list = new ArrayList<>();

		if (actionsArray == null)
			return list;

		for (JsonValue actionJson = actionsArray.child; actionJson != null; actionJson = actionJson.next) {
			ActionDescriptor descriptor = parseAction(actionJson);
			if (descriptor != null) {
				list.add(descriptor);
			}
		}

		return list;
	}

	private static ActionDescriptor parseAction(JsonValue actionJson) {
		try {
			String id = actionJson.getString("id").trim();
			String label = actionJson.getString("label", id);
			String hotkey = actionJson.getString("hotkey", null);
			boolean requiresTarget = actionJson.getBoolean("requiresTarget", false);

			return new ActionDescriptor(id, label, hotkey, requiresTarget);
		} catch (Exception e) {
			System.err.println("Error parseando acción: " + e.getMessage());
			return null;
		}
	}
}
