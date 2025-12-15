package roguelike_emotions.ui;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.mainMechanics.EmotionType;

/**
 * Registro centralizado de iconos.
 *
 * Convención assets (core/assets): - icons/effects/<spriteId normalizado>.png
 * (spriteId viene de EmotionEffectVisualRegistry) -
 * icons/emotions/<emotionType>.png - icons/small/<name>.png
 *
 * Devuelve null si no existe el archivo y cachea para no recargar texturas.
 */
public final class IconRegistry {

	private static final Map<String, TextureRegion> REGION_CACHE = new HashMap<>();
	private static final Map<String, Drawable> DRAWABLE_CACHE = new HashMap<>();

	private IconRegistry() {
	}

	// ---------------- public API ----------------

	/** Icono de efecto basado en spriteId del registry visual REAL. */
	public static TextureRegion effectRegion(EmotionEffect effect) {
		if (effect == null)
			return null;

		EffectVisualData data = EmotionEffectVisualRegistry.getVisualData(effect);
		if (data == null)
			return null;

		String spriteId = data.getSpriteId(); // <- existe en tu EffectVisualData
		if (spriteId == null)
			return null;

		String file = normalizeSpriteId(spriteId);
		return region("icons/effects/" + file + ".png");
	}

	public static Drawable effectDrawable(EmotionEffect effect) {
		if (effect == null)
			return null;
		String key = "effect:" + effect.name();
		if (DRAWABLE_CACHE.containsKey(key))
			return DRAWABLE_CACHE.get(key);

		TextureRegion r = effectRegion(effect);
		Drawable d = (r != null) ? new TextureRegionDrawable(r) : null;
		DRAWABLE_CACHE.put(key, d);
		return d;
	}

	/** Icono de emoción por tipo base. */
	public static TextureRegion emotionRegion(EmotionType type) {
		if (type == null)
			return null;
		return region("icons/emotions/" + type.name().toLowerCase() + ".png");
	}

	public static Drawable emotionDrawable(EmotionType type) {
		if (type == null)
			return null;
		String key = "emotion:" + type.name();
		if (DRAWABLE_CACHE.containsKey(key))
			return DRAWABLE_CACHE.get(key);

		TextureRegion r = emotionRegion(type);
		Drawable d = (r != null) ? new TextureRegionDrawable(r) : null;
		DRAWABLE_CACHE.put(key, d);
		return d;
	}

	/** Icono pequeño HUD/UI. name sin extensión. */
	public static TextureRegion smallRegion(String name) {
		if (name == null || name.isEmpty())
			return null;
		return region("icons/small/" + name + ".png");
	}

	public static Drawable smallDrawable(String name) {
		if (name == null)
			return null;
		String key = "small:" + name;
		if (DRAWABLE_CACHE.containsKey(key))
			return DRAWABLE_CACHE.get(key);

		TextureRegion r = smallRegion(name);
		Drawable d = (r != null) ? new TextureRegionDrawable(r) : null;
		DRAWABLE_CACHE.put(key, d);
		return d;
	}

	/** Precarga segura (no rompe si faltan archivos). */
	public static void preloadAll() {
		for (EmotionEffect e : EmotionEffect.values())
			effectRegion(e);
		for (EmotionType t : EmotionType.values())
			emotionRegion(t);

		smallRegion("turn_icon");
		smallRegion("duration_icon");
		smallRegion("buff_timer");
	}

	// ---------------- internals ----------------

	private static TextureRegion region(String path) {
		if (REGION_CACHE.containsKey(path))
			return REGION_CACHE.get(path);

		if (!Gdx.files.internal(path).exists()) {
			REGION_CACHE.put(path, null);
			return null;
		}

		Texture tex = new Texture(Gdx.files.internal(path));
		TextureRegion tr = new TextureRegion(tex);
		REGION_CACHE.put(path, tr);
		return tr;
	}

	/** "SPRITE_HEAL" -> "heal" */
	private static String normalizeSpriteId(String spriteId) {
		String s = spriteId.trim().toLowerCase();
		if (s.startsWith("sprite_"))
			s = s.substring("sprite_".length());
		return s;
	}
}
