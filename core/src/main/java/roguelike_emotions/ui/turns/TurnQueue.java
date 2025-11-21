package roguelike_emotions.ui.turns;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Estado de la pista de turnos en UI (inmutable hacia fuera). */
public final class TurnQueue {
	private final List<TurnToken> tokens = new ArrayList<>();
	private int cursor = 0; // índice del actor actual

	public List<TurnToken> tokens() {
		return tokens;
	}

	public int cursor() {
		return cursor;
	}

	public TurnToken current() {
		return tokens.isEmpty() ? null : tokens.get(Math.max(0, Math.min(cursor, tokens.size() - 1)));
	}

	/** Reconstruye desde proveedor conservando el cursor si es posible. */
	public void rebuild(TurnOrderProvider provider) {
		int currentId = current() != null ? current().viewId() : -1;
		tokens.clear();
		if (provider != null)
			tokens.addAll(Objects.requireNonNullElse(provider.snapshot(), List.of()));
		// reposiciona cursor al actor con el mismo id si sigue existiendo
		int newIndex = 0;
		if (currentId != -1) {
			for (int i = 0; i < tokens.size(); i++)
				if (tokens.get(i).viewId() == currentId) {
					newIndex = i;
					break;
				}
		}
		cursor = Math.max(0, Math.min(newIndex, Math.max(0, tokens.size() - 1)));
	}

	public void setCursor(int idx) {
		cursor = clamp(idx);
	}

	public void advance() {
		cursor = clamp(cursor + 1);
	}

	public void reset() {
		cursor = 0;
	}

	private int clamp(int i) {
		return tokens.isEmpty() ? 0 : Math.max(0, Math.min(i, tokens.size() - 1));
	}
}
