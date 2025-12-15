package roguelike_emotions.graphics.passes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import roguelike_emotions.graphics.ActorView;
import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.vfx.EmotionalParticleSystem;
import roguelike_emotions.vfx.FloatingDamageNumber;
import roguelike_emotions.vfx.ScreenShakeManager;

import java.util.ArrayList;
import java.util.List;

public class VfxPass implements RenderPass {

	private EmotionalParticleSystem particleSystem;
	private ScreenShakeManager shakeManager;
	private List<FloatingDamageNumber> damageNumbers = new ArrayList<>();

	// NUEVO: Sistema de transición suave de colores
	private Color currentTint = new Color(0, 0, 0, 0);
	private Color targetTint = new Color(0, 0, 0, 0);
	private float tintTransitionSpeed = 2.5f;

	public VfxPass() {
		this.setParticleSystem(new EmotionalParticleSystem());
		this.shakeManager = new ScreenShakeManager();
	}

	@Override
	public void executeWorld(RenderContext ctx) {
		float w = ctx.viewport.getWorldWidth();
		float h = ctx.viewport.getWorldHeight();
		float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();

		// 1. Screen shake
		shakeManager.update(ctx.camera, delta);

		// 2. Actualizar partículas
		getParticleSystem().update(delta, w, h);

		// 3. NUEVO: Transición suave de color de fondo
		updateBackgroundTint(delta);

		// 4. Aplicar tint con degradado desde arriba
		if (currentTint.a > 0.01f) {
			drawAtmosphericGradient(ctx, w, h);
		}

		// 5. Renderizar partículas
		getParticleSystem().render(ctx.batch);

		// 6. Low health overlay
		renderLowHealthOverlay(ctx);

		// 7. Actualizar damage numbers
		updateDamageNumbers(delta);
	}

	/**
	 * Transición suave entre colores de fondo (NO flashazos)
	 */
	private void updateBackgroundTint(float delta) {
		Color bgTint = getParticleSystem().getBackgroundTint();
		targetTint.set(bgTint);
		targetTint.a = Math.min(0.3f, bgTint.a * 2f); // Intensidad moderada

		// Lerp suave hacia el color objetivo
		currentTint.r = MathUtils.lerp(currentTint.r, targetTint.r, delta * tintTransitionSpeed);
		currentTint.g = MathUtils.lerp(currentTint.g, targetTint.g, delta * tintTransitionSpeed);
		currentTint.b = MathUtils.lerp(currentTint.b, targetTint.b, delta * tintTransitionSpeed);
		currentTint.a = MathUtils.lerp(currentTint.a, targetTint.a, delta * tintTransitionSpeed);
	}

	/**
	 * Dibuja un degradado atmosférico sutil (arriba más intenso, abajo se
	 * desvanece)
	 */
	private void drawAtmosphericGradient(RenderContext ctx, float w, float h) {
		int steps = 8; // Más steps = transición más suave
		float stepHeight = h / steps;

		for (int i = 0; i < steps; i++) {
			float t = i / (float) steps;
			float alpha = currentTint.a * (1f - t * 0.7f); // Más intenso arriba, se desvanece abajo

			Color stepColor = new Color(currentTint.r, currentTint.g, currentTint.b, alpha);
			ctx.batch.setColor(stepColor);
			ctx.batch.draw(ctx.whitePx, 0, h - (i + 1) * stepHeight, w, stepHeight);
		}

		ctx.batch.setColor(Color.WHITE);
	}

	@Override
	public void executeOverlay(RenderContext ctx) {
		renderDamageNumbers(ctx);
	}

	private void renderLowHealthOverlay(RenderContext ctx) {
		for (ActorView v : ctx.views.values()) {
			if (v.maxHp <= 0)
				continue;

			float pct = 1f - (v.hp / (float) v.maxHp);
			if (pct > 0.6f) {
				// Pulso suave en lugar de overlay constante
				float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.003) * 0.5 + 0.5);
				float a = (pct - 0.6f) * 0.4f * pulse;
				ctx.batch.setColor(1, 0, 0, a);
				ctx.batch.draw(ctx.whitePx, v.x - 40, v.y - 40, 80, 80);
			}
		}
		ctx.batch.setColor(Color.WHITE);
	}

	private void updateDamageNumbers(float delta) {
		for (int i = damageNumbers.size() - 1; i >= 0; i--) {
			FloatingDamageNumber num = damageNumbers.get(i);
			num.update(delta);
			if (num.isDead()) {
				damageNumbers.remove(i);
			}
		}
	}

	private void renderDamageNumbers(RenderContext ctx) {
		for (FloatingDamageNumber num : damageNumbers) {
			num.render(ctx.batch, ctx.font);
		}
	}

	// ========== API PÚBLICA ==========

	public void spawnParticleBurst(float x, float y, String emotionTag, int count) {
		getParticleSystem().spawnEmotionalBurst(x, y, emotionTag, count);
	}

	public void spawnAttackTrail(float srcX, float srcY, float dstX, float dstY, String emotionTag) {
		getParticleSystem().spawnAttackTrail(srcX, srcY, dstX, dstY, emotionTag);
	}

	public void addScreenShake(float trauma) {
		shakeManager.addTrauma(trauma);
	}

	public void spawnDamageNumber(float x, float y, int amount, Color color, boolean isCritical) {
		damageNumbers.add(new FloatingDamageNumber(x, y, amount, color, isCritical));
	}

	public void forceMood(EmotionType mood, float intensity) {
		getParticleSystem().forceMood(mood, intensity);
	}

	public void dispose() {
		getParticleSystem().dispose();
	}

	public EmotionalParticleSystem getParticleSystem() {
		return particleSystem;
	}

	public void setParticleSystem(EmotionalParticleSystem particleSystem) {
		this.particleSystem = particleSystem;
	}
}
