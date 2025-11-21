package roguelike_emotions.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import roguelike_emotions.ui.actions.ActionDescriptor;
import roguelike_emotions.ui.actions.ActionTheme;

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

	public static ActionConfig load(FileHandle fh) {
		JsonValue root = new JsonReader().parse(fh);
		JsonValue th = root.get("theme");
		ActionTheme theme = (th == null) ? ActionTheme.defaults()
				: new ActionTheme(th.getFloat("pillHeight", 40f), th.getFloat("gap", 12f), th.getFloat("padding", 16f),
						th.getFloat("minPillWidth", 120f));

		List<ActionDescriptor> list = new ArrayList<>(8);
		JsonValue arr = root.get("actions");
		if (arr != null) {
			for (JsonValue a = arr.child; a != null; a = a.next) {
				String id = a.getString("id").trim();
				String label = a.getString("label", id);
				String hotkey = a.getString("hotkey", null);
				boolean req = a.getBoolean("requiresTarget", false);
				list.add(new ActionDescriptor(id, label, hotkey, req));
			}
		}
		return new ActionConfig(Collections.unmodifiableList(list), theme);
	}
}
