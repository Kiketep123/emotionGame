package roguelike_emotions.visual;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import roguelike_emotions.mainMechanics.EmotionInstance;

public class VisualStyle {
	public final Color hpColor = new Color(0.85f, 0.2f, 0.25f, 1f);
	public final Color screenTint = new Color(0f, 0f, 0f, 0f);

	public Color colorOf(EmotionInstance emo) {
		if (emo == null)
			return new Color(0.7f, 0.7f, 0.7f, 1f);
		// 1) Si tu EmotionInstance tiene color

		if (emo.getColor() != null)
			return new Color().set(Integer.valueOf(emo.getColor()));
		// 2) Si registraste en EmotionEffectVisualRegistry:
		try {
			if (emo.getColor() != null && !emo.getColor().isEmpty())
				return Color.valueOf(emo.getColor());
		} catch (Throwable ignored) {
		}

		// 3) Fallback determinista
		int h = emo.toString().hashCode();
		Random r = new Random(h);
		float rc = 0.5f + r.nextFloat() * 0.5f, gc = 0.5f + r.nextFloat() * 0.5f, bc = 0.5f + r.nextFloat() * 0.5f;
		return new Color(rc, gc, bc, 1f);
	}
}
