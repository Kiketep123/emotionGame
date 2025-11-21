package roguelike_emotions.ui.turns;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.managers.GameManager;

import java.util.ArrayList;
import java.util.List;

public final class GameTurnOrderProvider implements TurnOrderProvider {
	private final GameManager gm;
	private List<TurnToken> lastStable = List.of();

	public GameTurnOrderProvider(GameManager gm) {
		this.gm = gm;
	}

	@Override
	public List<TurnToken> snapshot() {
		final List<TurnToken> out = new ArrayList<>(8);
		try {
			Player p = gm.getPlayer();
			if (p != null)
				out.add(new TurnToken(1, "Tú", true, safeAlive(p)));

			List<Enemy> es = gm.getEnemies();
			if (es != null) {
				for (int i = 0; i < es.size(); i++) {
					Enemy e = es.get(i);
					if (e == null)
						continue;
					out.add(new TurnToken(100 + i, safeName(e, "Enemigo " + (i + 1)), false, safeAlive(e)));
				}
			}
		} catch (Throwable ignored) {
		}

		if (out.size() >= 2) {
			lastStable = List.copyOf(out);
			return out;
		}
		// si aún no están listos los enemigos, no vacíes la UI
		return lastStable.isEmpty() ? out : lastStable;
	}

	private static boolean safeAlive(Player p) {
		try {
			return p.isAlive();
		} catch (Throwable t) {
			return true;
		}
	}

	private static boolean safeAlive(Enemy e) {
		try {
			return e.isAlive();
		} catch (Throwable t) {
			return true;
		}
	}

	private static String safeName(Enemy e, String def) {
		try {
			var n = e.getNombre();
			return (n != null && !n.isBlank()) ? n : def;
		} catch (Throwable t) {
			return def;
		}
	}
}
