package roguelike_emotions.graphics;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.visual.VisualStyle;

public class ActorView {
	public final int id;
	public String name = "";
	public float x, y, targetX, targetY, vx, vy;
	public int hp = 1, maxHp = 1, shield = 0;
	public final List<EmotionInstance> emotions = new ArrayList<>();
	private float hitFlash = 0f;

	public ActorView(int id) {
		this.id = id;
	}

	public void syncFromPlayer(Player p, float tx, float ty) {
		this.name = "Player";
		this.hp = p.getHealth();
		this.maxHp = Math.max(maxHp, hp);
		this.targetX = tx;
		this.targetY = ty;
		// TODO: emociones activas si las expones en Player
		// emotions.clear(); emotions.addAll(p.getEmocionesActivas());
	}

	public float getHitFlash() {
		return hitFlash;
	}

	public void setHitFlash(float hitFlash) {
		this.hitFlash = hitFlash;
	}

	public void syncFromEnemy(Enemy e, float tx, float ty) {
		this.name = e.getNombre() != null ? e.getNombre() : "Enemy";
		this.hp = e.getHealth();
		this.maxHp = Math.max(maxHp, hp);
		this.targetX = tx;
		this.targetY = ty;
		// TODO: emociones del enemigo si las expones
	}

	public void update(float dt) {
		float k = 10f;
		vx += (targetX - x) * k * dt;
		vy += (targetY - y) * k * dt;
		x += vx * dt;
		y += vy * dt;
		vx *= 0.7f;
		vy *= 0.7f;
		if (hitFlash > 0f)
			hitFlash = Math.max(0f, hitFlash - dt);

	}

	public void render(SpriteBatch b, BitmapFont font, Texture white, VisualStyle style) {
		// cuerpo (placeholder) — sustituye por tu sprite/atlas
		if (hitFlash > 0f)
			b.setColor(1f, 0.6f, 0.6f, 1f); // tono rojizo breve
		else
			b.setColor(com.badlogic.gdx.graphics.Color.WHITE);

		b.draw(white, x - 24, y - 24, 48, 48);

		// barra HP
		float w = 80, h = 8, hpPct = Math.max(0f, Math.min(1f, hp / (float) maxHp));
		b.setColor(0, 0, 0, 0.6f);
		b.draw(white, x - w / 2, y + 34, w, h);
		b.setColor(style.hpColor);
		b.draw(white, x - w / 2 + 1, y + 35, (w - 2) * hpPct, h - 2);

		// insignias de emociones (chips)
		float cx = x - w / 2, cy = y + 44;
		for (EmotionInstance emo : emotions) {
			b.setColor(style.colorOf(emo));
			b.draw(white, cx, cy, 10, 10);
			cx += 12;
		}

		b.setColor(Color.WHITE);
		font.draw(b, name, x - 20, y + 60);
	}

	public void triggerHitFlash(float seconds) {
		if (seconds > 0f && seconds > hitFlash)
			hitFlash = seconds;
	}
}
