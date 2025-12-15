package roguelike_emotions.vfx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import roguelike_emotions.mainMechanics.EmotionType;

/**
 * Sistema de partículas emocionales que responden al estado del combate.
 */
public class EmotionalParticleSystem {

	private static final int MAX_PARTICLES = 1000; // AUMENTADO de 500
	private static final float SPAWN_RATE = 0.1f; // REDUCIDO de 0.05f (menos partículas ambient)

	private final List<EmotionalParticle> particles = new ArrayList<>();
	private final Texture particleTexture;
	private float spawnTimer = 0f;

	// Estado emocional del campo de batalla
	private EmotionType currentMood = EmotionType.NEUTRO;
	private float moodIntensity = 0.5f;

	public EmotionalParticleSystem() {
		this.particleTexture = createParticleTexture();
	}

	/**
	 * Crea partículas en respuesta a eventos emocionales
	 */
	public void spawnEmotionalBurst(float x, float y, String emotionTag, int count) {
		EmotionType type = parseEmotionType(emotionTag);
		Color baseColor = getEmotionalColor(type);

		for (int i = 0; i < count; i++) {
			if (particles.size() >= MAX_PARTICLES)
				break;

			float angle = MathUtils.random(360f);
			float speed = MathUtils.random(80f, 250f); // MÁS VELOCIDAD

			EmotionalParticle p = new EmotionalParticle(x, y, MathUtils.cosDeg(angle) * speed,
					MathUtils.sinDeg(angle) * speed, baseColor, type);

			p.maxLife = 1.5f; // VIDA MÁS LARGA
			p.life = 1.5f;

			particles.add(p);
		}
	}

	/**
	 * Crea un trail de partículas desde origen a destino
	 */
	public void spawnAttackTrail(float srcX, float srcY, float dstX, float dstY, String emotionTag) {
		EmotionType type = parseEmotionType(emotionTag);
		Color color = getEmotionalColor(type);

		int trailCount = 15;
		for (int i = 0; i < trailCount; i++) {
			float t = i / (float) trailCount;
			float x = MathUtils.lerp(srcX, dstX, t);
			float y = MathUtils.lerp(srcY, dstY, t);

			float dx = dstX - srcX;
			float dy = dstY - srcY;
			float perpX = -dy * 0.3f;
			float perpY = dx * 0.3f;

			EmotionalParticle p = new EmotionalParticle(x + MathUtils.random(-10f, 10f),
					y + MathUtils.random(-10f, 10f), perpX + MathUtils.random(-30f, 30f),
					perpY + MathUtils.random(-30f, 30f), color, type);
			p.life = 0.5f + (t * 0.5f);
			particles.add(p);
		}
	}

	/**
	 * Actualiza todas las partículas y el mood del campo
	 */
	public void update(float delta, float worldWidth, float worldHeight) {
		decayMood(delta);

		spawnAmbientParticles(delta, worldWidth, worldHeight);
		updateParticles(delta, worldWidth, worldHeight);
	}

	private void spawnAmbientParticles(float delta, float worldWidth, float worldHeight) {
		spawnTimer += delta;

		// SOLO spawn si el mood NO es NEUTRO (evita partículas grises constantes)
		if (spawnTimer >= SPAWN_RATE && particles.size() < MAX_PARTICLES && currentMood != EmotionType.NEUTRO) {
			spawnTimer = 0f;

			// MUCHO MENOS partículas ambientales
			if (MathUtils.randomBoolean(0.3f)) { // Solo 30% de las veces
				float x = MathUtils.random(worldWidth);
				float y = MathUtils.random(worldHeight);

				Color color = getEmotionalColor(currentMood);
				color.a = 0.4f * moodIntensity; // MÁS OPACAS

				EmotionalParticle p = new EmotionalParticle(x, y, MathUtils.random(-15f, 15f), // Más lentas
						MathUtils.random(-15f, 15f), color, currentMood);
				p.life = 0.8f; // Vida más corta
				particles.add(p);
			}
		}
	}

	private void updateParticles(float delta, float worldWidth, float worldHeight) {
		for (int i = particles.size() - 1; i >= 0; i--) {
			EmotionalParticle p = particles.get(i);
			p.update(delta, currentMood, moodIntensity, worldWidth, worldHeight);

			if (p.isDead()) {
				particles.remove(i);
			}
		}
	}

	/**
	 * Renderiza todas las partículas
	 */
	public void render(SpriteBatch batch) {
		for (EmotionalParticle p : particles) {
			p.render(batch, particleTexture);
		}
	}

	/**
	 * Color de fondo sugerido por el mood actual
	 */
	public Color getBackgroundTint() {
		Color tint = getEmotionalColor(currentMood).cpy();
		tint.a = 0.25f * moodIntensity; // AUMENTADO de 0.15f
		return tint;
	}

	public EmotionType getCurrentMood() {
		return currentMood;
	}

	public float getMoodIntensity() {
		return moodIntensity;
	}

	// ========== HELPERS ==========

	private EmotionType parseEmotionType(String tag) {
		if (tag == null)
			return EmotionType.NEUTRO;
		String t = tag.toUpperCase();

		// Mapeo de tags a tus EmotionTypes
		if (t.contains("FIRE") || t.contains("IRA"))
			return EmotionType.IRA;
		if (t.contains("RABIA"))
			return EmotionType.RABIA;
		if (t.contains("FEAR") || t.contains("MIEDO"))
			return EmotionType.MIEDO;
		if (t.contains("JOY") || t.contains("ALEGR"))
			return EmotionType.ALEGRIA;
		if (t.contains("SAD") || t.contains("TRIST"))
			return EmotionType.TRISTEZA;
		if (t.contains("CALM") || t.contains("CALMA"))
			return EmotionType.CALMA;
		if (t.contains("GUILT") || t.contains("CULP"))
			return EmotionType.CULPA;
		if (t.contains("HOPE") || t.contains("ESPERAN"))
			return EmotionType.ESPERANZA;

		return EmotionType.NEUTRO;
	}

	Color getEmotionalColor(EmotionType type) {
		return switch (type) {
		case IRA -> new Color(1f, 0.2f, 0.1f, 1f); // Rojo intenso
		case RABIA -> new Color(0.8f, 0f, 0f, 1f); // Rojo oscuro
		case MIEDO -> new Color(0.3f, 0.1f, 0.5f, 1f); // Púrpura oscuro
		case ALEGRIA -> new Color(1f, 0.9f, 0.3f, 1f); // Dorado
		case TRISTEZA -> new Color(0.2f, 0.4f, 0.7f, 1f); // Azul profundo
		case CALMA -> new Color(0.4f, 0.8f, 0.6f, 1f); // Verde sereno
		case CULPA -> new Color(0.5f, 0.3f, 0.2f, 1f); // Marrón oscuro
		case ESPERANZA -> new Color(0.7f, 0.9f, 1f, 1f); // Azul claro
		case FUSIONADA -> new Color(0.8f, 0.5f, 0.9f, 1f); // Violeta mágico
		default -> new Color(0.7f, 0.7f, 0.8f, 1f); // Gris neutro
		};
	}

	private Texture createParticleTexture() {
		int size = 8;
		Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

		int center = size / 2;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				float dist = (float) Math.sqrt((x - center) * (x - center) + (y - center) * (y - center));

				if (dist <= center) {
					float alpha = 1f - (dist / center);
					pm.setColor(1f, 1f, 1f, alpha);
					pm.drawPixel(x, y);
				}
			}
		}

		Texture tex = new Texture(pm);
		pm.dispose();
		return tex;
	}

	public void dispose() {
		if (particleTexture != null) {
			particleTexture.dispose();
		}
	}

	// ========== INNER CLASS ==========

	private static class EmotionalParticle {
		float x, y;
		float vx, vy;
		float life;
		float maxLife;
		Color color;
		EmotionType type;
		float rotation;
		float rotationSpeed;
		float scale;

		EmotionalParticle(float x, float y, float vx, float vy, Color color, EmotionType type) {
			this.x = x;
			this.y = y;
			this.vx = vx;
			this.vy = vy;
			this.color = color.cpy();
			this.type = type;
			this.life = 1f;
			this.maxLife = 1f;
			this.rotation = MathUtils.random(360f);
			this.rotationSpeed = MathUtils.random(-180f, 180f);
			this.scale = MathUtils.random(0.5f, 2f);
		}

		void update(float delta, EmotionType mood, float moodIntensity, float worldWidth, float worldHeight) {
			x += vx * delta;
			y += vy * delta;
			rotation += rotationSpeed * delta;

			applyEmotionalBehavior(delta, mood, moodIntensity, worldWidth, worldHeight);
			applyPhysics(delta);

			life -= delta;
			color.a = Math.max(0f, life / maxLife);
		}

		private void applyEmotionalBehavior(float delta, EmotionType mood, float intensity, float w, float h) {
			switch (type) {
			case IRA, RABIA:
				// Movimiento errático y rápido
				vx += MathUtils.random(-50f, 50f) * delta * intensity;
				vy += MathUtils.random(-50f, 50f) * delta * intensity;
				rotationSpeed += MathUtils.random(-90f, 90f) * delta;
				break;

			case MIEDO:
				// Alejarse del centro
				float centerX = w / 2f;
				float centerY = h / 2f;
				float dx = x - centerX;
				float dy = y - centerY;
				float dist = (float) Math.sqrt(dx * dx + dy * dy);
				if (dist > 1f) {
					vx += (dx / dist) * 80f * delta * intensity;
					vy += (dy / dist) * 80f * delta * intensity;
				}
				break;

			case ALEGRIA:
				// Movimiento ondulante
				vy += (float) Math.sin(x * 0.05f + y * 0.05f) * 40f * delta;
				vx += (float) Math.cos(y * 0.05f) * 40f * delta;
				scale += (float) Math.sin(life * 10f) * 0.5f * delta;
				break;

			case TRISTEZA:
				// Caída lenta
				vy -= 60f * delta * intensity;
				vx *= 0.98f;
				break;

			case CALMA:
				// Movimiento circular suave
				float angle = (float) Math.atan2(vy, vx);
				angle += 1f * delta;
				float speed = (float) Math.sqrt(vx * vx + vy * vy);
				vx = (float) Math.cos(angle) * speed;
				vy = (float) Math.sin(angle) * speed;
				break;

			case CULPA:
				// Movimiento pesado hacia abajo
				vy -= 100f * delta;
				break;

			case ESPERANZA:
				// Ascenso suave
				vy += 30f * delta;
				break;
			case NEUTRO:
			default:
				// Movimiento simple y suave (drift)
				vx *= 0.95f; // Fricción mayor
				vy *= 0.95f;
				// Sin comportamiento especial, solo física básica
				break;
			}
		}

		private void applyPhysics(float delta) {
			vy -= 20f * delta; // Gravedad sutil

			// Límite de velocidad
			float speed = (float) Math.sqrt(vx * vx + vy * vy);
			if (speed > 300f) {
				vx = (vx / speed) * 300f;
				vy = (vy / speed) * 300f;
			}

			vx *= 0.99f;
			vy *= 0.99f;
		}

		boolean isDead() {
			return life <= 0f;
		}

		void render(SpriteBatch batch, Texture texture) {
			float size = 8f * scale;
			batch.setColor(color);
			batch.draw(texture, x - size / 2f, y - size / 2f, size / 2f, size / 2f, size, size, 1f, 1f, rotation, 0, 0,
					texture.getWidth(), texture.getHeight(), false, false);
			batch.setColor(Color.WHITE);
		}
	}

	/**
	 * Fuerza un cambio de mood inmediato (llamado desde VisualEvents)
	 */
	public void forceMood(EmotionType newMood, float intensity) {
		if (newMood == EmotionType.NEUTRO)
			return;

		this.currentMood = newMood;
		this.moodIntensity = Math.min(1f, intensity);

		// Debug
		System.out.println("[MOOD] Cambiado a: " + newMood + " (intensidad: " + intensity + ")");
	}

	/**
	 * Decrementa el mood gradualmente
	 */
	public void decayMood(float delta) {
		moodIntensity = Math.max(0f, moodIntensity - delta * 0.3f);
		if (moodIntensity < 0.1f) {
			currentMood = EmotionType.NEUTRO;
		}
	}

	/**
	 * API MEJORADA: Spawn con control total de velocidad
	 */
	public void spawnDirectionalBurst(float x, float y, EmotionType type, int count, float speedMin, float speedMax,
			float lifetime, float scaleMin, float scaleMax) {
		if (type == null || count <= 0)
			return;

		Color baseColor = getEmotionalColor(type);
		int actualCount = Math.min(count, MAX_PARTICLES - particles.size());

		for (int i = 0; i < actualCount; i++) {
			float angle = MathUtils.random(360f);
			float speed = MathUtils.random(speedMin, speedMax);

			spawnParticleWithVelocity(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, baseColor,
					type, lifetime, MathUtils.random(scaleMin, scaleMax));
		}
	}

	/**
	 * API MEJORADA: Spawn con velocidad explícita (NO se desperdicia)
	 */
	public void spawnParticleWithVelocity(float x, float y, float vx, float vy, Color color, EmotionType type,
			float lifetime, float scale) {
		if (particles.size() >= MAX_PARTICLES)
			return;

		EmotionalParticle p = new EmotionalParticle(x, y, vx, vy, color, type);
		p.maxLife = lifetime;
		p.life = lifetime;
		p.scale = scale;

		particles.add(p);
	}

	/**
	 * Spawn en anillo con velocidad radial controlada
	 */
	public void spawnRing(float centerX, float centerY, EmotionType type, int particleCount, float radius,
			float radialSpeed, float lifetime, float scale) {
		if (type == null || particleCount <= 0)
			return;

		Color baseColor = getEmotionalColor(type);
		float angleStep = 360f / particleCount;
		int actualCount = Math.min(particleCount, MAX_PARTICLES - particles.size());

		for (int i = 0; i < actualCount; i++) {
			float angle = i * angleStep;
			float angleRad = MathUtils.degreesToRadians * angle;

			// Posición en el anillo
			float spawnX = centerX + MathUtils.cos(angleRad) * radius;
			float spawnY = centerY + MathUtils.sin(angleRad) * radius;

			// Velocidad radial hacia afuera (AHORA SÍ SE USA)
			float vx = MathUtils.cos(angleRad) * radialSpeed;
			float vy = MathUtils.sin(angleRad) * radialSpeed;

			spawnParticleWithVelocity(spawnX, spawnY, vx, vy, baseColor, type, lifetime, scale);
		}
	}

}
