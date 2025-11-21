package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Input;

import roguelike_emotions.ui.actions.ActionDescriptor;

public final class HotkeyMap {
	private final Map<Integer, String> byKey = new HashMap<>();
	private int hash = 0;

	public String resolveId(int keycode) {
		return byKey.get(keycode);
	}

	public void rebuildIfChanged(List<ActionDescriptor> actions) {
		int h = 1;
		if (actions != null) {
			for (ActionDescriptor a : actions) {
				h = 31 * h + (a.id() != null ? a.id().hashCode() : 0);
				h = 31 * h + (a.hotkey() != null ? a.hotkey().hashCode() : 0);
			}
		}
		if (h == hash)
			return;

		byKey.clear();
		if (actions != null) {
			for (ActionDescriptor a : actions) {
				if (a.hotkey() == null || a.hotkey().isBlank())
					continue;
				try {
					byKey.put(Input.Keys.valueOf(a.hotkey().trim()), a.id());
				} catch (Throwable ignored) {
				}
			}
		}
		hash = h;
	}
}
