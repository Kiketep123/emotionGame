package roguelike_emotions.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;

public class FusionParticleSystem {

	private List<FusionParticle> particles = new ArrayList<>();
	private Texture whitePixel;
	private float viewportW;
	private float viewportH;

	public FusionParticleSystem(float viewportW, float viewportH) {
		this.viewportW = viewportW;
		this.viewportH = viewportH;
	}

	public void createFusionParticles(Color colorA, Color colorB, float slotAX, float slotAY, float slotBX,
			float slotBY) {
		for (int i = 0; i < 30; i++) {
			particles.add(new FusionParticle(slotAX, slotAY, colorA, viewportW, viewportH));
		}

		for (int i = 0; i < 30; i++) {
			particles.add(new FusionParticle(slotBX, slotBY, colorB, viewportW, viewportH));
		}
	}

	public void update(float delta) {
		for (int i = particles.size() - 1; i >= 0; i--) {
			FusionParticle p = particles.get(i);
			p.update(delta);
			if (p.isDead()) {
				particles.remove(i);
			}
		}
	}

	public void render(SpriteBatch batch) {
		batch.begin();
		for (FusionParticle p : particles) {
			p.render(batch, getWhitePixel());
		}
		batch.end();
	}

	private Texture getWhitePixel() {
		if (whitePixel == null) {
			Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
			pm.setColor(Color.WHITE);
			pm.fill();
			whitePixel = new Texture(pm);
			pm.dispose();
		}
		return whitePixel;
	}

	public void dispose() {
		if (whitePixel != null) {
			whitePixel.dispose();
		}
	}

	private static class FusionParticle {
		float x, y;
		float vx, vy;
		float life;
		Color color;
		float size;
		float centerX, centerY;

		FusionParticle(float x, float y, Color color, float viewportW, float viewportH) {
			this.x = x;
			this.y = y;
			this.color = new Color(color);
			this.life = 1f;
			this.size = 3f + (float) Math.random() * 3f;
			this.centerX = viewportW / 2;
			this.centerY = viewportH / 2;

			float angle = (float) (Math.random() * Math.PI * 2);
			float speed = 50f + (float) Math.random() * 100f;
			this.vx = (float) Math.cos(angle) * speed;
			this.vy = (float) Math.sin(angle) * speed;
		}

		void update(float delta) {
			x += vx * delta;
			y += vy * delta;

			float dx = centerX - x;
			float dy = centerY - y;
			float dist = (float) Math.sqrt(dx * dx + dy * dy);
			if (dist > 0) {
				vx += (dx / dist) * 200f * delta;
				vy += (dy / dist) * 200f * delta;
			}

			life -= delta * 0.8f;
			color.a = Math.max(0, life);
		}

		boolean isDead() {
			return life <= 0;
		}

		void render(SpriteBatch batch, Texture whitePixel) {
			batch.setColor(color);
			batch.draw(whitePixel, x - size / 2, y - size / 2, size, size);
		}
	}
}
