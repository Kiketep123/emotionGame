package roguelike_emotions.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class FloatingDamageNumber {

	private float x, y;
	private float vx, vy;
	private float life, maxLife;
	private String text;
	private Color color;
	private float scale;
	private float rotation;
	private boolean isCritical;

	public FloatingDamageNumber(float x, float y, int amount, Color color, boolean isCritical) {
		this.x = x;
		this.y = y;
		this.text = String.valueOf(Math.abs(amount));
		this.color = color.cpy();
		this.isCritical = isCritical;
		this.life = 1.5f; // MÁS TIEMPO VISIBLE
		this.maxLife = 1.5f;

		// Velocidad inicial más lenta para que sea más legible
		this.vx = MathUtils.random(-20f, 20f);
		this.vy = isCritical ? 180f : 120f;

		// Escala inicial más grande
		this.scale = isCritical ? 0.1f : 0.5f;
		this.rotation = MathUtils.random(-10f, 10f);
	}

	public void update(float delta) {
		// Física
		x += vx * delta;
		y += vy * delta;
		vy -= 150f * delta; // Menos gravedad
		vx *= 0.97f; // Más fricción para que no se mueva tanto

		// Animación de escala (pop inicial)
		if (scale < 1f) {
			scale += delta * 10f;
			if (scale > 1f)
				scale = 1f;
		}

		// Rotación decreciente
		rotation *= 0.97f;

		// Fade out más suave
		life -= delta;
		float t = life / maxLife;

		// Fade solo al final
		if (t > 0.3f) {
			color.a = 1f;
		} else {
			color.a = t / 0.3f;
		}

		// Critical hace "pulse"
		if (isCritical && life > 1.0f) {
			scale = 1f + (float) Math.sin((maxLife - life) * 25f) * 0.3f;
		}
	}

	public void render(SpriteBatch batch, BitmapFont font) {
		float finalScale = scale * (isCritical ? 2.5f : 1.8f); // MUCHO MÁS GRANDE
		font.getData().setScale(finalScale);

		// Sombra más gruesa y visible
		font.setColor(0f, 0f, 0f, color.a * 0.8f);
		for (int ox = -2; ox <= 2; ox++) {
			for (int oy = -2; oy <= 2; oy++) {
				if (ox != 0 || oy != 0) {
					font.draw(batch, text, x + ox, y + oy);
				}
			}
		}

		// Texto principal BRILLANTE
		Color brightColor = color.cpy();
		brightColor.r = Math.min(1f, brightColor.r * 1.3f);
		brightColor.g = Math.min(1f, brightColor.g * 1.3f);
		brightColor.b = Math.min(1f, brightColor.b * 1.3f);
		font.setColor(brightColor);
		font.draw(batch, text, x, y);

		font.getData().setScale(1f);
		font.setColor(Color.WHITE);
	}

	public boolean isDead() {
		return life <= 0f;
	}
}
