package roguelike_emotions.utils;

import roguelike_emotions.graphics.Vec2;

/**
 * Utilidad de layout para colocar enemigos en 1 o 2 filas dentro de la banda
 * derecha. Garantiza que quedan dentro de [left..right] con padding.
 */
public final class LayoutUtil {
	private LayoutUtil() {
	}

	public static Vec2 enemySlot(int i, int n, float worldW, float worldH) {
		float pad = 48f;
		float left = worldW * 0.58f;
		float right = worldW - pad;

		int rows = (n <= 4) ? 1 : 2;
		int perRow = (int) Math.ceil(n / (float) rows);
		int row = i / perRow;
		int col = i % perRow;

		float yTop = worldH * 0.60f;
		float yBot = worldH * 0.40f;
		float y = (rows == 1) ? (worldH * 0.50f) : (row == 0 ? yTop : yBot);

		float usable = Math.max(1f, right - left);
		float x;
		if (perRow == 1) {
			x = (left + right) * 0.5f;
		} else {
			float step = usable / (perRow - 1);
			x = left + col * step;
		}
		// clamp final
		x = Math.max(left, Math.min(right, x));
		return new Vec2(x, y);
	}
}
