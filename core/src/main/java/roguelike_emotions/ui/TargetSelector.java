package roguelike_emotions.ui;

import java.util.List;

import roguelike_emotions.characters.Enemy;

/** Encapsula la lógica de navegación y elección de objetivos vivos. */
public final class TargetSelector {
	private TargetSelector() {
	}

	/** Clamp + salto sobre cadáveres si procede. */
	public static int moveIndex(List<Enemy> es, int currentIndex, int delta) {
		if (es == null || es.isEmpty())
			return 0;
		int n = es.size();
		int idx = Math.max(0, Math.min(n - 1, currentIndex + delta));
		for (int guard = 0; guard < n; guard++) {
			Enemy e = es.get(idx);
			if (e != null && e.isAlive())
				break;
			idx = (idx + (delta >= 0 ? +1 : -1) + n) % n;
		}
		return idx;
	}

	/** Devuelve un enemigo vivo cercano al índice, o null. */
	public static Enemy pickAlive(List<Enemy> es, int desiredIndex) {
		if (es == null || es.isEmpty())
			return null;
		int n = es.size();
		int idx = Math.max(0, Math.min(n - 1, desiredIndex));
		if (es.get(idx) != null && es.get(idx).isAlive())
			return es.get(idx);
		for (int d = 1; d < n; d++) {
			int r = (idx + d) % n;
			if (es.get(r) != null && es.get(r).isAlive())
				return es.get(r);
			int l = (idx - d + n) % n;
			if (es.get(l) != null && es.get(l).isAlive())
				return es.get(l);
		}
		return null;
	}
}
