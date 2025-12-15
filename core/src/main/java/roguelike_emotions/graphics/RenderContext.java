package roguelike_emotions.graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.Viewport;

import roguelike_emotions.characters.Player;
import roguelike_emotions.graphics.passes.VfxPass;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.ui.turns.TurnQueue;
import roguelike_emotions.utils.LayoutUtil;
import roguelike_emotions.vfx.Director;
import roguelike_emotions.visual.VisualStyle;

public class RenderContext {
	public final SpriteBatch batch;
	public final BitmapFont font;
	public final OrthographicCamera camera;
	public final Viewport viewport;
	public final FrameBuffer sceneFbo;
	public final Texture whitePx;
	public final Director director;
	public final VisualStyle style;
	public Integer selectedEnemyViewId = null; // ej. 100 + índice
	public String selectedActionLabel = "atacar"; // "atacar", "defender", "usar emoción"
	public final Map<Integer, ActorView> views = new HashMap<>();
	public VfxPass vfxPass = null;
	// Contenedor de textos activos
	private final List<FloatingText> texts = new ArrayList<>();
	// Cola de turnos (UI)
	public final TurnQueue turnQueue = new TurnQueue();

	public RenderContext(SpriteBatch batch, BitmapFont font, OrthographicCamera camera, Viewport viewport,
			FrameBuffer sceneFbo, Texture whitePx, Director director, VisualStyle style) {
		this.batch = batch;
		this.font = font;
		this.camera = camera;
		this.viewport = viewport;
		this.sceneFbo = sceneFbo;
		this.whitePx = whitePx;
		this.director = director;
		this.style = style;
	}

	public void updateViews(float dt) {
		// Player
		Player p = GameManager.getInstance().getPlayer();
		if (p != null) {
			int id = 1;
			ActorView vp = views.get(id);
			if (vp == null) {
				vp = new ActorView(id);
				views.put(id, vp);
			}
			vp.syncFromPlayer(p, 360, 360);
			vp.update(dt);
		}

		// Enemigos: layout robusto (no se salen)
		var es = roguelike_emotions.managers.GameManager.getInstance().getEnemies();
		if (es != null) {
			int n = es.size();
			int base = 100;
			float w = viewport.getWorldWidth();
			float h = viewport.getWorldHeight();

			for (int i = 0; i < n; i++) {
				var e = es.get(i);
				int id = base + i;
				var v = views.get(id);
				if (v == null) {
					v = new ActorView(id);
					views.put(id, v);
				}

				var slot = LayoutUtil.enemySlot(i, n, w, h);
				v.syncFromEnemy(e, slot.x(), slot.y());
				v.update(dt);
			}
		}

	}

	// --- VO de texto flotante (solo para UI/VFX) ---
	public static final class FloatingText {
		public float x, y, vx, vy, t, dur;
		public String text;
		public final Color color = new Color(Color.WHITE);
	}

	public void addText(float x, float y, String text, Color color) {
		FloatingText ft = new FloatingText();
		ft.x = x;
		ft.y = y;
		ft.vx = 0f;
		ft.vy = 32f; // sube
		ft.t = 0f;
		ft.dur = 0.9f;
		ft.text = text;
		if (color != null)
			ft.color.set(color);
		texts.add(ft);
	}

	public List<FloatingText> getFloatingTexts() {
		return texts;
	}

}
