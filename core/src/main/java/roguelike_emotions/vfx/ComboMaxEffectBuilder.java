package roguelike_emotions.vfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import roguelike_emotions.cfg.ComboMaxConfig;
import roguelike_emotions.graphics.ActorView;
import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.graphics.passes.VfxPass;
import roguelike_emotions.mainMechanics.EmotionType;

/**
 * Builder profesional OPTIMIZADO - Sin código muerto ni variables sin usar.
 */
public final class ComboMaxEffectBuilder {

	private final RenderContext ctx;
	private final ActorView player;
	private final VfxPass vfx;
	private final EmotionalParticleSystem particleSystem;

	private ComboMaxEffectBuilder(RenderContext ctx, ActorView player, VfxPass vfx) {
		this.ctx = ctx;
		this.player = player;
		this.vfx = vfx;
		this.particleSystem = vfx.getParticleSystem();

		if (particleSystem == null) {
			throw new IllegalStateException("ParticleSystem no disponible en VfxPass");
		}
	}

	public static ComboMaxEffectBuilder create(RenderContext ctx, ActorView player, VfxPass vfx) {
		if (ctx == null || player == null || vfx == null) {
			throw new IllegalArgumentException("Context, player y vfx no pueden ser null");
		}
		return new ComboMaxEffectBuilder(ctx, player, vfx);
	}

	// ==================== EFFECTS - OPTIMIZADOS ====================

	public ComboMaxEffectBuilder applyScreenShake() {
		// Triple shake progresivo
		for (int pulse = 0; pulse < ComboMaxConfig.SHAKE_PULSE_COUNT; pulse++) {
			float traumaDecay = 1f - (pulse * 0.2f);
			float trauma = ComboMaxConfig.SHAKE_BASE_TRAUMA * traumaDecay;
			vfx.addScreenShake(trauma);
		}
		return this;
	}

	public ComboMaxEffectBuilder spawnParticleRings() {
		EmotionType emotion = parseEmotion(ComboMaxConfig.MOOD_PRIMARY_EMOTION);

		for (int ringIndex = 0; ringIndex < ComboMaxConfig.RING_COUNT; ringIndex++) {
			float radius = ComboMaxConfig.RING_BASE_RADIUS + (ringIndex * ComboMaxConfig.RING_RADIUS_INCREMENT);

			int particleCount = Math.round(ComboMaxConfig.RING_BASE_PARTICLE_COUNT
					* (float) Math.pow(ComboMaxConfig.RING_PARTICLE_COUNT_MULTIPLIER, ringIndex));

			float radialSpeed = MathUtils.random(ComboMaxConfig.RING_PARTICLE_SPEED_MIN,
					ComboMaxConfig.RING_PARTICLE_SPEED_MAX);

			float scale = MathUtils.random(ComboMaxConfig.RING_PARTICLE_SCALE_MIN,
					ComboMaxConfig.RING_PARTICLE_SCALE_MAX);

			// ✅ AHORA USA EL API CORRECTO CON VELOCIDAD RADIAL
			particleSystem.spawnRing(player.x, player.y, emotion, particleCount, radius, radialSpeed,
					ComboMaxConfig.RING_PARTICLE_LIFETIME, scale);
		}
		return this;
	}

	public ComboMaxEffectBuilder spawnCentralBurst() {
		EmotionType primaryEmotion = EmotionType.ALEGRIA;
		EmotionType secondaryEmotion = parseEmotion(ComboMaxConfig.MOOD_PRIMARY_EMOTION);

		// Explosión primaria optimizada
		particleSystem.spawnDirectionalBurst(player.x, player.y, primaryEmotion, ComboMaxConfig.BURST_PRIMARY_COUNT,
				120f, 300f, // Velocidad base
				2.0f, // Lifetime
				1.0f, 2.5f // Escala
		);

		// Explosión secundaria con offset
		particleSystem.spawnDirectionalBurst(player.x, player.y + ComboMaxConfig.BURST_SECONDARY_OFFSET_Y,
				secondaryEmotion, ComboMaxConfig.BURST_SECONDARY_COUNT, 100f, 250f, 1.8f, 0.8f, 2.0f);

		return this;
	}

	public ComboMaxEffectBuilder spawnFireworks() {
		EmotionType emotion = parseEmotion(ComboMaxConfig.MOOD_PRIMARY_EMOTION);
		Color baseColor = particleSystem.getEmotionalColor(emotion);

		float angleStep = ComboMaxConfig.FIREWORK_ARC_DEGREES / ComboMaxConfig.FIREWORK_COUNT;

		for (int i = 0; i < ComboMaxConfig.FIREWORK_COUNT; i++) {
			float angle = i * angleStep;
			spawnFireworkTrail(angle, baseColor, emotion);
		}

		return this;
	}

	private void spawnFireworkTrail(float angleDegrees, Color color, EmotionType emotion) {
		float angleRad = MathUtils.degreesToRadians * angleDegrees;

		for (int segment = 0; segment < ComboMaxConfig.FIREWORK_TRAIL_SEGMENTS; segment++) {
			float t = segment / (float) ComboMaxConfig.FIREWORK_TRAIL_SEGMENTS;

			// Posición a lo largo del trail
			float distance = ComboMaxConfig.FIREWORK_TRAIL_SPACING * segment;
			float height = ComboMaxConfig.FIREWORK_ASCENT_HEIGHT * t;

			float spawnX = player.x + MathUtils.cos(angleRad) * distance;
			float spawnY = player.y + height;

			// Velocidad del trail (hacia arriba con componente radial)
			float vx = MathUtils.cos(angleRad) * 50f;
			float vy = 80f; // Ascenso

			// ✅ USA VELOCIDAD EXPLÍCITA
			for (int p = 0; p < ComboMaxConfig.FIREWORK_PARTICLES_PER_SEGMENT; p++) {
				float randomVx = vx + MathUtils.random(-20f, 20f);
				float randomVy = vy + MathUtils.random(-15f, 15f);

				particleSystem.spawnParticleWithVelocity(spawnX, spawnY, randomVx, randomVy, color, emotion, 1.5f,
						1.2f);
			}
		}
	}

	public ComboMaxEffectBuilder applyVisualFeedback() {
		// Screen tint dorado
		ctx.style.screenTint.set(ComboMaxConfig.SCREEN_TINT);

		// Knockback épico del player
		player.vx += ComboMaxConfig.PLAYER_KNOCKBACK_X;
		player.vy += ComboMaxConfig.PLAYER_KNOCKBACK_Y;
		player.setHitFlash(ComboMaxConfig.PLAYER_FLASH_DURATION);

		return this;
	}

	public ComboMaxEffectBuilder displayText() {
		ctx.addText(player.x, player.y + ComboMaxConfig.TEXT_PRIMARY_OFFSET_Y, ComboMaxConfig.TEXT_PRIMARY,
				Color.valueOf(ComboMaxConfig.TEXT_PRIMARY_COLOR_HEX));

		ctx.addText(player.x, player.y + ComboMaxConfig.TEXT_SECONDARY_OFFSET_Y, ComboMaxConfig.TEXT_SECONDARY,
				Color.valueOf(ComboMaxConfig.TEXT_SECONDARY_COLOR_HEX));

		return this;
	}

	public ComboMaxEffectBuilder applyMood() {
		EmotionType emotion = parseEmotion(ComboMaxConfig.MOOD_PRIMARY_EMOTION);
		vfx.forceMood(emotion, ComboMaxConfig.MOOD_INTENSITY);
		return this;
	}

	private EmotionType parseEmotion(String emotionName) {
		try {
			return EmotionType.valueOf(emotionName.toUpperCase());
		} catch (IllegalArgumentException e) {
			System.err.println("[ComboMax] Emoción inválida: " + emotionName + ", usando ESPERANZA");
			return EmotionType.ESPERANZA;
		}
	}

	// ==================== BUILD FINAL ====================

	public void build() {
		try {
			this.applyScreenShake().spawnCentralBurst().spawnParticleRings().spawnFireworks().applyVisualFeedback()
					.displayText().applyMood();

			System.out.println(
					"[ComboMax] ✅ Efecto completado - " + "~" + estimateParticleCount() + " partículas spawneadas");
		} catch (Exception e) {
			System.err.println("[ComboMax] ❌ Error durante build: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private int estimateParticleCount() {
		int total = ComboMaxConfig.BURST_PRIMARY_COUNT + ComboMaxConfig.BURST_SECONDARY_COUNT;

		// Anillos
		for (int i = 0; i < ComboMaxConfig.RING_COUNT; i++) {
			total += Math.round(ComboMaxConfig.RING_BASE_PARTICLE_COUNT
					* (float) Math.pow(ComboMaxConfig.RING_PARTICLE_COUNT_MULTIPLIER, i));
		}

		// Fuegos artificiales
		total += ComboMaxConfig.FIREWORK_COUNT * ComboMaxConfig.FIREWORK_TRAIL_SEGMENTS
				* ComboMaxConfig.FIREWORK_PARTICLES_PER_SEGMENT;

		return total;
	}
}
