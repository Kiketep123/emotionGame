package roguelike_emotions.vfx;

import java.util.List;

import com.badlogic.gdx.graphics.Color;

import roguelike_emotions.characters.Player;
import roguelike_emotions.graphics.ActorView;
import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.graphics.passes.VfxPass;
import roguelike_emotions.mainMechanics.DominantEmotionType;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.utils.EmotionUtils;

public final class VisualEvents {

	private VisualEvents() {
	}

	public static void apply(RenderContext ctx, VisEvent ev) {
		if (ev instanceof DamageEvent d)
			onDamage(ctx, d);
		else if (ev instanceof HealEvent h)
			onHeal(ctx, h);
		else if (ev instanceof BuffAppliedEvent b)
			onBuff(ctx, b);
		else if (ev instanceof DebuffAppliedEvent db)
			onDebuff(ctx, db);
		else if (ev instanceof ComboMaxEvent cm)
			onComboMax(ctx, cm);
	}

	private static ActorView viewOf(RenderContext ctx, int id) {
		return ctx.views.get(id);
	}

	private static void onDamage(RenderContext ctx, DamageEvent ev) {
		ActorView src = viewOf(ctx, ev.srcId());
		ActorView dst = viewOf(ctx, ev.dstId());

		if (dst == null)
			return;

		// Determinar si es crítico ANTES
		boolean isCritical = ev.amount() > 50;

		// 1. Knockback MÁS FUERTE
		if (src != null) {
			float dx = dst.x - src.x;
			float dy = dst.y - src.y;
			float len = (float) Math.max(1e-3, Math.hypot(dx, dy));

			float knockbackPower = isCritical ? 350f : 220f;
			dst.vx += (dx / len) * knockbackPower;
			dst.vy += (dy / len) * (knockbackPower * 0.5f);
		}

		// 2. Flash MÁS VISIBLE
		dst.setHitFlash(isCritical ? 0.3f : 0.18f);

		VfxPass vfx = getVfxPass(ctx);
		if (vfx != null) {
			// 3. Calcular mood emocional
			EmotionType attackMood = calculateDominantMood(ev.srcId());
			float moodIntensity = Math.min(1f, ev.amount() / 60f);

			vfx.forceMood(attackMood, moodIntensity);

			// 4. Attack trail (SOLO si viene de src válido)
			if (src != null) {
				vfx.spawnAttackTrail(src.x, src.y, dst.x, dst.y, attackMood.name());
			}

			// 5. Partículas ESCALADAS por daño
			int particleCount = Math.min(80, Math.max(15, ev.amount()));
			vfx.spawnParticleBurst(dst.x, dst.y, attackMood.name(), particleCount);

			// 6. Screen shake PROPORCIONAL
			float trauma = isCritical ? 0.8f : Math.min(0.6f, ev.amount() / 100f);
			vfx.addScreenShake(trauma);

			// 7. Damage number con MEJOR VISIBILIDAD
			Color numColor = getEmotionalColor(attackMood);
			vfx.spawnDamageNumber(dst.x, dst.y + 60, ev.amount(), numColor, isCritical);

			// 8. NUEVO: Texto "CRÍTICO" si aplica
			if (isCritical) {
				ctx.addText(dst.x, dst.y + 90, "¡CRÍTICO!", Color.valueOf("FFD700"));
			}
		}

		// 9. Screen tint más visible
		Color tintColor = colorTag(ev.tag(), new Color(0.2f, 0f, 0f, 0.4f));
		ctx.style.screenTint.set(tintColor);
	}

	/**
	 * Calcula el mood dominante del atacante usando EmotionUtils
	 */
	private static EmotionType calculateDominantMood(int srcId) {
		try {
			List<EmotionInstance> emotions = null;

			// Jugador (id = 1)
			if (srcId == 1) {
				Player player = GameManager.getInstance().getPlayer();
				if (player != null) {
					emotions = player.getEmocionesActivas();
				}
			}
			// Enemigo (id >= 100)
			else if (srcId >= 100) {
				int enemyIndex = srcId - 100;
				var enemies = GameManager.getInstance().getEnemies();
				if (enemies != null && enemyIndex < enemies.size()) {
					var enemy = enemies.get(enemyIndex);
					if (enemy != null) {
						emotions = enemy.getEmocionesActivas();
					}
				}
			}

			if (emotions == null || emotions.isEmpty()) {
				return EmotionType.IRA; // Fallback agresivo
			}

			// Si tiene 1 sola emoción
			if (emotions.size() == 1) {
				EmotionType type = emotions.get(0).getTipoBase();
				return type != null ? type : EmotionType.IRA;
			}

			// Si tiene 2+ emociones, calcular dominante
			EmotionInstance first = emotions.get(0);
			EmotionInstance second = emotions.get(1);

			DominantEmotionType dominant = EmotionUtils.detectarTipoDominante(first, second);
			EmotionType converted = EmotionUtils.convertirDominantToEmotionType(dominant);

			return converted;

		} catch (Exception e) {
			return EmotionType.IRA;
		}
	}

	private static void onHeal(RenderContext ctx, HealEvent ev) {
		ActorView dst = viewOf(ctx, ev.dstId());
		if (dst == null)
			return;

		VfxPass vfx = getVfxPass(ctx);
		if (vfx != null) {
			// Partículas verdes curativas
			vfx.spawnParticleBurst(dst.x, dst.y, "ALEGRIA", 30);

			// Número verde brillante
			vfx.spawnDamageNumber(dst.x, dst.y + 60, ev.amount(), Color.valueOf("7FFFB0"), false);

			vfx.forceMood(EmotionType.ALEGRIA, 0.6f);
		}

		// Texto de curación
		ctx.addText(dst.x, dst.y + 90, "+HP", Color.valueOf("7FFFB0"));
		ctx.style.screenTint.set(0f, 0.12f, 0.05f, 0.3f);
	}

	private static void onBuff(RenderContext ctx, BuffAppliedEvent ev) {
		ActorView dst = viewOf(ctx, ev.dstId());
		if (dst == null)
			return;

		VfxPass vfx = getVfxPass(ctx);
		if (vfx != null) {
			vfx.spawnParticleBurst(dst.x, dst.y, "ESPERANZA", 25);
			vfx.forceMood(EmotionType.ESPERANZA, 0.5f);
		}

		ctx.addText(dst.x, dst.y + 70, ev.tag() + " ↑", Color.valueOf("B0E0FF"));
	}

	private static void onDebuff(RenderContext ctx, DebuffAppliedEvent ev) {
		ActorView dst = viewOf(ctx, ev.dstId());
		if (dst == null)
			return;

		VfxPass vfx = getVfxPass(ctx);
		if (vfx != null) {
			vfx.spawnParticleBurst(dst.x, dst.y, "CULPA", 25);
			vfx.addScreenShake(0.25f);
			vfx.forceMood(EmotionType.CULPA, 0.5f);
		}

		ctx.addText(dst.x, dst.y + 70, ev.tag() + " ↓", Color.valueOf("FFB0B0"));
	}

	private static VfxPass getVfxPass(RenderContext ctx) {
		return ctx.vfxPass;
	}

	private static Color getEmotionalColor(EmotionType type) {
		return switch (type) {
		case IRA -> new Color(1f, 0.3f, 0.2f, 1f);
		case RABIA -> new Color(0.9f, 0.1f, 0.1f, 1f);
		case MIEDO -> new Color(0.6f, 0.3f, 0.9f, 1f);
		case ALEGRIA -> new Color(1f, 0.95f, 0.4f, 1f);
		case TRISTEZA -> new Color(0.4f, 0.6f, 0.95f, 1f);
		case CALMA -> new Color(0.5f, 0.95f, 0.8f, 1f);
		case CULPA -> new Color(0.7f, 0.4f, 0.3f, 1f);
		case ESPERANZA -> new Color(0.8f, 0.95f, 1f, 1f);
		case FUSIONADA -> new Color(0.95f, 0.6f, 1f, 1f);
		default -> new Color(0.9f, 0.9f, 0.95f, 1f);
		};
	}

	private static Color colorTag(String tag, Color fallback) {
		if (tag == null)
			return fallback;
		String t = tag.toUpperCase();

		if (t.contains("FIRE") || t.contains("IRA"))
			return new Color(0.4f, 0.1f, 0f, 0.45f);
		if (t.contains("POISON"))
			return new Color(0f, 0.3f, 0.1f, 0.4f);
		if (t.contains("HEAL"))
			return new Color(0f, 0.25f, 0.05f, 0.35f);
		if (t.contains("ICE"))
			return new Color(0f, 0.2f, 0.4f, 0.4f);

		return fallback;
	}

	private static void onComboMax(RenderContext ctx, ComboMaxEvent ev) {
		ActorView player = viewOf(ctx, ev.srcId());
		if (player == null)
			return;

		VfxPass vfx = getVfxPass(ctx);
		if (vfx != null) {
			// 1. Triple screen shake progresivo
			vfx.addScreenShake(0.3f);

			// 2. Anillo explosivo de partículas doradas (efecto 360°)
			for (int ring = 0; ring < 3; ring++) {
				float radius = 60f + (ring * 30f);
				int particlesPerRing = 20 + (ring * 10);

				for (int i = 0; i < particlesPerRing; i++) {
					float angle = (i / (float) particlesPerRing) * 360f;
					float delay = ring * 0.05f; // Anillos escalonados

					// Spawn con delay simulado usando vida inicial
					spawnDelayedParticle(vfx, player.x, player.y, angle, radius, "ESPERANZA", 2.0f - delay);
				}
			}

			// 3. Explosión central de partículas brillantes
			vfx.spawnParticleBurst(player.x, player.y, "ALEGRIA", 60);

			// 4. Segundo burst retrasado (efecto doble explosión)
			vfx.spawnParticleBurst(player.x, player.y + 50, "ESPERANZA", 40);

			// 5. Partículas ascendentes tipo "fuegos artificiales"
			for (int i = 0; i < 8; i++) {
				float angle = i * 45f;
				spawnFireworkParticle(vfx, player.x, player.y, angle, "ESPERANZA");
			}

			// 6. Mood épico MÁXIMO
			vfx.forceMood(EmotionType.ESPERANZA, 1.0f);

			// 7. Damage number GIGANTE con el texto "x3 COMBO!"
			Color goldColor = new Color(1f, 0.84f, 0f, 1f); // Oro puro
			vfx.spawnDamageNumber(player.x, player.y + 120, 0, goldColor, true);
		}

		// 8. Múltiples textos flotantes con escala
		ctx.addText(player.x, player.y + 140, "✨ COMBO x3 ✨", Color.valueOf("FFD700")); // Dorado
		ctx.addText(player.x, player.y + 110, "¡MÁXIMO!", Color.valueOf("FFA500")); // Naranja dorado

		// 9. Flash de pantalla dorado intenso (más visible que tinte)
		ctx.style.screenTint.set(0.25f, 0.20f, 0f, 0.6f); // Más intenso

		// 10. Knockback del player hacia atrás (efecto de poder)
		if (player != null) {
			player.vx -= 80f; // Retroceso visual épico
			player.vy += 150f; // Salto ligero
			player.setHitFlash(0.4f); // Flash dorado
		}
	}

	// ✅ HELPER: Spawn de partículas en anillo
	private static void spawnDelayedParticle(VfxPass vfx, float centerX, float centerY, float angle, float radius,
			String emotion, float life) {
		if (vfx == null || vfx.getParticleSystem() == null)
			return;

		float angleRad = (float) Math.toRadians(angle);
		float x = centerX + (float) Math.cos(angleRad) * radius;
		float y = centerY + (float) Math.sin(angleRad) * radius;

		// Velocidad hacia afuera desde el centro
		float speed = 200f;
		float vx = (float) Math.cos(angleRad) * speed;
		float vy = (float) Math.sin(angleRad) * speed;

		vfx.getParticleSystem().spawnEmotionalBurst(x, y, emotion, 1);
	}

	// HELPER: Partículas tipo fuegos artificiales
	private static void spawnFireworkParticle(VfxPass vfx, float x, float y, float angle, String emotion) {
		if (vfx == null)
			return;

		float angleRad = (float) Math.toRadians(angle);
		float distance = 100f;

		// Trail ascendente
		for (int i = 0; i < 5; i++) {
			float t = i / 5f;
			float px = x + (float) Math.cos(angleRad) * distance * t;
			float py = y + 80f * t; // Ascenso

			vfx.spawnParticleBurst(px, py, emotion, 3);
		}
	}

}
