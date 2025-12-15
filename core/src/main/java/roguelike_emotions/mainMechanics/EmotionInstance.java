package roguelike_emotions.mainMechanics;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import roguelike_emotions.characters.Attack;
import roguelike_emotions.characters.Player;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.ui.IconRegistry;

/**
 * 🧠 EmotionInstance v2.0 - CON SISTEMA DE DESPERTAR Ahora las emociones pueden
 * evolucionar a SentientEmotion mediante uso en combate
 */
public class EmotionInstance implements Emotion {

	// ==================== CAMPOS BASE ====================
	private final String id;
	private String nombre;
	private EmotionType tipoBase;
	private List<EffectDetail> efectos;
	private String color;
	private String simbolo;

	// ==================== 🆕 SISTEMA DE DESPERTAR ====================
	private int usageCount = 0;
	private int experiencePoints = 0;
	private static final int AWAKENING_THRESHOLD = 3; // Testing: 3 usos
	private static final int MIN_EXPERIENCE = 50; // Testing: 50 XP
	private static final Random rand = new Random();

	// ==================== CONSTRUCTOR ====================
	public EmotionInstance(String nombre, EmotionType tipoBase, List<EffectDetail> efectos, String color,
			String simbolo) {
		this.id = UUID.randomUUID().toString();
		this.nombre = nombre;
		this.tipoBase = tipoBase;
		this.efectos = efectos;
		this.color = color;
		this.simbolo = simbolo;
	}

	// ==================== 🆕 SISTEMA DE USO Y EXPERIENCIA ====================

	/**
	 * Incrementa contador de uso (llamado cada turno)
	 */
	public void incrementUsageCount() {
		usageCount++;
	}

	/**
	 * Gana experiencia por acciones
	 */
	public void gainExperience(int points) {
		experiencePoints += points;
	}

	/**
	 * Verifica si puede despertar
	 */
	public boolean canAwaken() {
		return usageCount >= AWAKENING_THRESHOLD && experiencePoints >= MIN_EXPERIENCE;
	}

	/**
	 * Calcula probabilidad de despertar (0-100)
	 */
	public int getAwakeningChance() {
		if (!canAwaken())
			return 0;

		int baseChance = 40;

		// +5% por cada uso extra
		int extraUses = usageCount - AWAKENING_THRESHOLD;
		baseChance += Math.min(30, extraUses * 5);

		// +10% por cada 50 XP extra
		int extraExp = experiencePoints - MIN_EXPERIENCE;
		baseChance += Math.min(20, (extraExp / 50) * 10);

		// Tipos emocionales volátiles tienen más chance
		if (tipoBase == EmotionType.IRA || tipoBase == EmotionType.MIEDO || tipoBase == EmotionType.TRISTEZA) {
			baseChance += 15;
		}

		return Math.min(95, baseChance);
	}

	/**
	 * Intenta despertar consciencia
	 */
	public boolean tryAwaken() {
		if (!canAwaken())
			return false;
		int chance = getAwakeningChance();
		return rand.nextInt(100) < chance;
	}

	/**
	 * Progreso hacia despertar (0-100%)
	 */
	public int getAwakeningProgress() {
		int usageProgress = Math.min(100, (usageCount * 100) / AWAKENING_THRESHOLD);
		int expProgress = Math.min(100, (experiencePoints * 100) / MIN_EXPERIENCE);
		return (usageProgress + expProgress) / 2;
	}

	// ==================== GETTERS SISTEMA DESPERTAR ====================
	public int getUsageCount() {
		return usageCount;
	}

	public int getExperiencePoints() {
		return experiencePoints;
	}

	// ==================== MÉTODOS ORIGINALES ====================
	public String getId() {
		return id;
	}

	@Override
	public String getNombre() {
		return nombre;
	}

	@Override
	public EmotionType getTipoBase() {
		return tipoBase;
	}

	@Override
	public EmotionEffect getEfecto() {
		return efectos.isEmpty() ? null : efectos.get(0).getTipo();
	}

	@Override
	public String getColor() {
		return color;
	}

	public String getSimbolo() {
		return simbolo;
	}

	public List<EffectDetail> getEfectos() {
		return efectos;
	}

	@Override
	public void aplicarAlJugador(Player player) {
		for (EffectDetail e : efectos) {
			e.aplicarA(player);
		}
	}

	@Override
	public void modificarAtaque(Attack attack) {
		for (EffectDetail e : efectos) {
			e.aplicarA(attack);
		}
	}

	public void setEfectos(List<EffectDetail> nuevosEfectos) {
		this.efectos = nuevosEfectos;
	}

	@Override
	public String toString() {
		return simbolo + " " + nombre + " (" + tipoBase.name().toLowerCase() + ")";
	}

	public void tickEfectos(Player jugador, Attack ataque) {
		for (EffectDetail detalle : efectos) {
			detalle.aplicarA(jugador);
			detalle.aplicarA(ataque);
		}
	}

	public void tickDuracion() {
		for (EffectDetail detalle : efectos) {
			detalle.reducirDuracion(1);
		}
		efectos.removeIf(e -> e.haExpirado());
	}

	public boolean estaExpirada() {
		return efectos.isEmpty();
	}

	public Drawable getIconDrawable() {
		return IconRegistry.emotionDrawable(getTipoBase());
	}
}
