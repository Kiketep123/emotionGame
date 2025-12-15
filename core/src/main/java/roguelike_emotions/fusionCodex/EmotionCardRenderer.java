package roguelike_emotions.fusionCodex;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import roguelike_emotions.mainMechanics.EmotionInstance;

public class EmotionCardRenderer {

	public static Drawable makeCardIconBg(EmotionInstance emotion, Color emotionColor) {
		int size = 60;
		Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
		int center = size / 2;
		int radius = 26;

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int dx = x - center;
				int dy = y - center;
				float dist = (float) Math.sqrt(dx * dx + dy * dy);

				if (dist <= radius) {
					float t = dist / radius;
					float r = 0.05f + (emotionColor.r * 0.15f * (1f - t));
					float g = 0.06f + (emotionColor.g * 0.15f * (1f - t));
					float b = 0.09f + (emotionColor.b * 0.20f * (1f - t));
					pm.setColor(r, g, b, 1f);
					pm.drawPixel(x, y);
				}
			}
		}

		pm.setColor(emotionColor.r, emotionColor.g, emotionColor.b, 0.8f);
		pm.drawCircle(center, center, radius);
		pm.setColor(emotionColor.r, emotionColor.g, emotionColor.b, 0.5f);
		pm.drawCircle(center, center, radius - 1);

		Texture t = new Texture(pm);
		pm.dispose();
		return new TextureRegionDrawable(t);
	}

	public static Drawable makeCardGradient(EmotionInstance emotion, Color emotionColor, boolean highlighted) {
		int w = 100, h = 80;
		Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

		for (int y = 0; y < h; y++) {
			float t = y / (float) h;
			float baseR = 0.06f + (emotionColor.r * 0.08f * (1f - t));
			float baseG = 0.08f + (emotionColor.g * 0.08f * (1f - t));
			float baseB = 0.12f + (emotionColor.b * 0.12f * (1f - t));

			if (highlighted) {
				baseR += 0.03f;
				baseG += 0.03f;
				baseB += 0.04f;
			}

			pm.setColor(baseR, baseG, baseB, 0.98f);
			pm.drawLine(0, y, w - 1, y);
		}

		if (highlighted) {
			Color glowColor = new Color(emotionColor);
			for (int i = 3; i >= 0; i--) {
				glowColor.a = (3 - i) * 0.15f;
				pm.setColor(glowColor);
				pm.drawRectangle(i, i, w - i * 2, h - i * 2);
			}
		}

		Color borderColor = new Color(emotionColor);
		borderColor.a = highlighted ? 0.9f : 0.5f;
		pm.setColor(borderColor);
		pm.drawRectangle(1, 1, w - 2, h - 2);
		if (highlighted) {
			borderColor.a = 0.7f;
			pm.setColor(borderColor);
			pm.drawRectangle(2, 2, w - 4, h - 4);
		}

		Texture t = new Texture(pm);
		pm.dispose();
		return new TextureRegionDrawable(t);
	}

	public static Drawable makeCardGradientCompatible(EmotionInstance emotion, Color emotionColor, boolean compatible) {
		Color indicatorColor = compatible ? new Color(0.2f, 0.8f, 0.3f, 1f) : new Color(0.8f, 0.2f, 0.2f, 1f);

		int w = 100, h = 80;
		Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

		for (int y = 0; y < h; y++) {
			float t = y / (float) h;
			float baseR = 0.06f + (emotionColor.r * 0.08f * (1f - t));
			float baseG = 0.08f + (emotionColor.g * 0.08f * (1f - t));
			float baseB = 0.12f + (emotionColor.b * 0.12f * (1f - t));

			baseR += indicatorColor.r * 0.03f;
			baseG += indicatorColor.g * 0.03f;
			baseB += indicatorColor.b * 0.03f;

			pm.setColor(baseR, baseG, baseB, 0.98f);
			pm.drawLine(0, y, w - 1, y);
		}

		pm.setColor(indicatorColor.r, indicatorColor.g, indicatorColor.b, 0.4f);
		pm.drawRectangle(0, 0, w, h);
		pm.drawRectangle(1, 1, w - 2, h - 2);

		Texture t = new Texture(pm);
		pm.dispose();
		return new TextureRegionDrawable(t);
	}
}
