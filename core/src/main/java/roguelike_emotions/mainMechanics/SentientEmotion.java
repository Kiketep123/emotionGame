package roguelike_emotions.mainMechanics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import roguelike_emotions.characters.Player;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.utils.CombatLogger;

/**
 * Emoción consciente con personalidad, lealtad y sistema de negociación.
 * Patrón: Entity + Strategy (via EmotionPersonality)
 * 
 * @version 3.0 - Refactorizado y optimizado
 */
public class SentientEmotion extends EmotionInstance {

	// ==================== CONSTANTS ====================

	private static final int MAX_STAT = 100;
	private static final int MAX_MEMORIES = 20;
	private static final Random RANDOM = new Random(); // Reutilizable

	// ==================== FIELDS ====================

	private EmotionPersonality personality;
	private final Map<EmotionType, EmotionRelationship> relationshipMap;
	private final List<String> memories;

	private int loyalty;
	private int hunger;
	private int evolution;
	private int age;
	private boolean isCorrupted;
	private boolean isAscended;

	// ==================== ENUMS ====================

	public enum EmotionPersonality {
		SUMISA(0.8, 10, 5), REBELDE(1.3, -5, 10), GLOTONA(1.5, 5, 20), PARASÍTICA(1.2, 0, 0), SIMBIONTE(1.2, 15, 3),
		VOLÁTIL(1.0, -10, 8), SABIA(1.0, 20, 2);

		public final double effectMultiplier;
		public final int loyaltyGainRate;
		public final int hungerRate;

		EmotionPersonality(double mult, int loyalty, int hunger) {
			this.effectMultiplier = mult;
			this.loyaltyGainRate = loyalty;
			this.hungerRate = hunger;
		}
	}

	public enum EmotionRelationship {
		ALIADAS(1.5), NEUTRAS(1.0), RIVALES(0.7), ENEMIGAS(0.0);

		public final double fusionMultiplier;

		EmotionRelationship(double mult) {
			this.fusionMultiplier = mult;
		}
	}

	// ==================== CONSTRUCTOR ====================

	public SentientEmotion(String nombre, EmotionType tipoBase, List<EffectDetail> efectos, String color,
			String simbolo) {
		super(nombre, tipoBase, efectos, color, simbolo);

		this.personality = assignPersonalityByType(tipoBase);
		this.relationshipMap = new HashMap<>();
		this.memories = new ArrayList<>();
		this.loyalty = 50;
		this.hunger = 0;
		this.evolution = 0;
		this.age = 0;
		this.isCorrupted = false;
		this.isAscended = false;

		initializeRelationships();
	}

	// ==================== FACTORY METHOD ====================

	public static SentientEmotion fromEmotionInstance(EmotionInstance base) {
		return new SentientEmotion(base.getNombre(), base.getTipoBase(), new ArrayList<>(base.getEfectos()),
				base.getColor(), base.getSimbolo());
	}

	// ==================== INITIALIZATION ====================

	private EmotionPersonality assignPersonalityByType(EmotionType tipo) {
		double roll = RANDOM.nextDouble();

		switch (tipo) {
		case IRA:
		case RABIA:
			if (roll < 0.6)
				return EmotionPersonality.REBELDE;
			if (roll < 0.8)
				return EmotionPersonality.VOLÁTIL;
			return EmotionPersonality.GLOTONA;

		case TRISTEZA:
		case CULPA:
			if (roll < 0.5)
				return EmotionPersonality.PARASÍTICA;
			if (roll < 0.8)
				return EmotionPersonality.SUMISA;
			return EmotionPersonality.VOLÁTIL;

		case ALEGRIA:
		case ESPERANZA:
			if (roll < 0.5)
				return EmotionPersonality.SIMBIONTE;
			if (roll < 0.8)
				return EmotionPersonality.SABIA;
			return EmotionPersonality.SUMISA;

		case CALMA:
			return roll < 0.7 ? EmotionPersonality.SABIA : EmotionPersonality.SIMBIONTE;

		case MIEDO:
			return roll < 0.6 ? EmotionPersonality.SUMISA : EmotionPersonality.VOLÁTIL;

		default:
			return EmotionPersonality.values()[RANDOM.nextInt(EmotionPersonality.values().length)];
		}
	}

	private void initializeRelationships() {
		EmotionType myType = getTipoBase();
		for (EmotionType other : EmotionType.values()) {
			relationshipMap.put(other, calculateRelationship(myType, other));
		}
	}

	private EmotionRelationship calculateRelationship(EmotionType a, EmotionType b) {
		if (a == b)
			return EmotionRelationship.ALIADAS;

		try {
			double avgWeight = getAverageWeightFromMatrix(a, b);

			if (avgWeight >= 1.3)
				return EmotionRelationship.ALIADAS;
			if (avgWeight >= 1.0)
				return EmotionRelationship.NEUTRAS;
			if (avgWeight >= 0.7)
				return EmotionRelationship.RIVALES;
			return EmotionRelationship.ENEMIGAS;

		} catch (Exception e) {
			return calculateRelationshipFallback(a, b);
		}
	}

	private double getAverageWeightFromMatrix(EmotionType a, EmotionType b) {
		EmotionDominanceMatrix matrix = GameManager.getInstance().getDominanceMatrix();
		if (matrix == null)
			return 1.0;

		double w1 = matrix.getPeso(a, b);
		double w2 = matrix.getPeso(b, a);
		return (w1 + w2) / 2.0;
	}

	private EmotionRelationship calculateRelationshipFallback(EmotionType a, EmotionType b) {
		// ENEMIGAS (Opuestos)
		if ((a == EmotionType.IRA && b == EmotionType.CALMA) || (a == EmotionType.CALMA && b == EmotionType.IRA)
				|| (a == EmotionType.ALEGRIA && b == EmotionType.TRISTEZA)
				|| (a == EmotionType.TRISTEZA && b == EmotionType.ALEGRIA)) {
			return EmotionRelationship.ENEMIGAS;
		}

		// ALIADAS (Misma familia)
		if ((a == EmotionType.IRA && b == EmotionType.RABIA) || (a == EmotionType.RABIA && b == EmotionType.IRA)
				|| (a == EmotionType.ALEGRIA && b == EmotionType.ESPERANZA)
				|| (a == EmotionType.ESPERANZA && b == EmotionType.ALEGRIA)
				|| (a == EmotionType.TRISTEZA && b == EmotionType.CULPA)
				|| (a == EmotionType.CULPA && b == EmotionType.TRISTEZA)) {
			return EmotionRelationship.ALIADAS;
		}

		// RIVALES (Incompatibles)
		if ((a == EmotionType.MIEDO && b == EmotionType.IRA) || (a == EmotionType.IRA && b == EmotionType.MIEDO)) {
			return EmotionRelationship.RIVALES;
		}

		return EmotionRelationship.NEUTRAS;
	}

	// ==================== UPDATE LOGIC ====================

	public void onTurnTick(Player player) {
		// Incrementar stats usando métodos públicos
		incrementAge();
		incrementHunger(personality.hungerRate);

		// Parasítica drena HP
		if (personality == EmotionPersonality.PARASÍTICA && player.getHealth() > 30) {
			player.takeDamage(30);
			CombatLogger.get().log(getNombre() + " drena 30 HP");
		}

		// Verificar ascensión/corrupción
		if (evolution >= 100 && !isCorrupted && !isAscended) {
			if (loyalty < 30) {
				evolveToCorrupted();
			} else if (loyalty >= 80) {
				evolveToAscended();
			} else {
				evolution = 0; // Reset si no cumple requisitos
			}
		}
	}

	private void evolveToCorrupted() {
		isCorrupted = true;
		CombatLogger.get().log("💀 " + getNombre() + " se ha CORROMPIDO!");

		for (EffectDetail effect : getEfectos()) {
			effect.setIntensidad(effect.getIntensidad() * 1.5);
		}

		personality = EmotionPersonality.PARASÍTICA;
		loyalty = 0;
		addMemory("corrupted");
	}

	private void evolveToAscended() {
		isAscended = true;
		CombatLogger.get().log("✨ " + getNombre() + " ha ASCENDIDO!");

		for (EffectDetail effect : getEfectos()) {
			effect.setIntensidad(effect.getIntensidad() * 2.0);
		}

		hunger = 0;
		personality = EmotionPersonality.SABIA;
		loyalty = 100;
		addMemory("ascended");
	}

	// ==================== FUSION NEGOTIATION ====================

	/**
	 * Negocia fusión con otra emoción sentiente. Patrón: Strategy + Factory Method
	 */
	public FusionNegotiation negotiateFusionWith(SentientEmotion other) {
		// 1. Verificar incompatibilidad
		EmotionRelationship relation = (other != null) ? relationshipMap.get(other.getTipoBase())
				: EmotionRelationship.NEUTRAS;

		if (relation == EmotionRelationship.ENEMIGAS) {
			return FusionNegotiation.rejected(getNombre() + ": ¡Jamás me fusionaré con " + other.getNombre() + "!",
					FusionNegotiation.RejectionReason.INCOMPATIBLE);
		}

		// 2. Verificar hambre (debilitamiento)
		if (hunger >= 85) {
			return FusionNegotiation.weakened(getNombre() + ": Estoy demasiado débil... la fusión será imperfecta.",
					0.65);
		}

		// 3. Verificar lealtad (REBELDE)
		if (personality == EmotionPersonality.REBELDE && loyalty < 50) {
			if (RANDOM.nextDouble() < (50 - loyalty) / 100.0) {
				return FusionNegotiation.requiresHP(
						getNombre() + ": Demuestra tu determinación. Cuesta " + (50 - loyalty) + " HP", 50 - loyalty);
			}
		}

		// 4. Verificar hambre (GLOTONA)
		if (personality == EmotionPersonality.GLOTONA && hunger >= 60) {
			return FusionNegotiation.requiresSacrifice(getNombre() + ": Dame otra emoción... necesito absorberla.", 1);
		}

		// 5. Verificar inestabilidad (VOLÁTIL)
		if (personality == EmotionPersonality.VOLÁTIL) {
			int failChance = 30 - (loyalty / 3);
			if (failChance > 0 && RANDOM.nextDouble() * 100 < failChance) {
				return FusionNegotiation.unstable(
						getNombre() + ": ¡Mi esencia es caótica! Fusión arriesgada (" + failChance + "% fallo)",
						failChance, 0.5);
			}
		}

		// 6. ACEPTACIÓN
		loyalty = Math.min(MAX_STAT, loyalty + 10);
		if (other != null) {
			other.loyalty = Math.min(MAX_STAT, other.loyalty + 10);
			addMemory("fused_with:" + other.getTipoBase());
			other.addMemory("fused_with:" + getTipoBase());
		}

		double multiplier = relation.fusionMultiplier;
		if (loyalty >= 80) {
			multiplier += 0.15;
		}

		return FusionNegotiation.accepted(multiplier);
	}

	// ==================== FEEDING ====================

	public void feed(int amount) {
		hunger = Math.max(0, hunger - amount);
		loyalty = Math.min(MAX_STAT, loyalty + amount / 2);
		CombatLogger.get().log(getNombre() + " alimentada. Hambre: " + hunger);
		addMemory("fed");
	}

	public void feedWithEmotion(EmotionInstance sacrifice) {
		int nutrition = 20;
		if (sacrifice instanceof SentientEmotion) {
			nutrition += ((SentientEmotion) sacrifice).loyalty / 10;
		}
		feed(nutrition);
	}

	// ==================== MEMORIES ====================

	public void addMemory(String event) {
		memories.add(event);
		if (memories.size() > MAX_MEMORIES) {
			memories.remove(0);
		}
	}

	public boolean hasMemory(String pattern) {
		return memories.stream().anyMatch(m -> m.contains(pattern));
	}

	// ==================== DIALOGUE ====================

	public String getDialogue(boolean inCombat, boolean inHub) {
		if (hunger >= 80)
			return getNombre() + ": Tengo hambre...";
		if (loyalty < 30)
			return getNombre() + ": No confío en ti";
		if (loyalty >= 80)
			return getNombre() + ": Lucharé contigo";
		if (evolution >= 85 && !isAscended && !isCorrupted) {
			return getNombre() + ": Estoy... cambiando...";
		}
		if (personality == EmotionPersonality.SABIA && inHub) {
			return getNombre() + ": Las emociones opuestas ocultan poder...";
		}
		return null;
	}

	// ==================== EFFECT MODIFIER ====================

	public double getEffectModifier() {
		double modifier = personality.effectMultiplier;

		if (hunger >= 80)
			modifier *= 0.7;
		if (loyalty >= 90)
			modifier *= 1.1;

		if (personality == EmotionPersonality.VOLÁTIL) {
			modifier *= (0.5 + RANDOM.nextDouble());
		}

		if (personality == EmotionPersonality.GLOTONA) {
			modifier *= (hunger < 30) ? 1.5 : 0.8;
		}

		return modifier;
	}

	@Override
	public void aplicarAlJugador(Player player) {
		double modifier = getEffectModifier();

		for (EffectDetail efecto : getEfectos()) {
			EffectDetail modified = new EffectDetail(efecto.getTipo(), efecto.getIntensidad() * modifier,
					efecto.getProbabilidad(), efecto.getRemainingTurns());
			modified.aplicarA(player);
		}
	}

	// ==================== GETTERS ====================
	public void forcePersonality(EmotionPersonality newPersonality) {
		this.personality = newPersonality;
		CombatLogger.get().log(getNombre() + " cambió a personalidad " + newPersonality.name());
	}

	public EmotionPersonality getPersonality() {
		return personality;
	}

	public int getLoyalty() {
		return loyalty;
	}

	public int getHunger() {
		return hunger;
	}

	public int getEvolution() {
		return evolution;
	}

	public int getAge() {
		return age;
	}

	public boolean isCorrupted() {
		return isCorrupted;
	}

	public boolean isAscended() {
		return isAscended;
	}

	public List<String> getMemories() {
		return new ArrayList<>(memories);
	}

	public void setLoyalty(int value) {
		this.loyalty = Math.max(0, Math.min(MAX_STAT, value));
	}

	// ==================== STAT INCREMENTERS ====================

	/**
	 * Incrementa la edad de la emoción sentiente. Llamado cada turno en combate.
	 */
	public void incrementAge() {
		this.age++;

		// Evolución cada 3 turnos
		if (age % 3 == 0) {
			int evolutionGain = (personality == EmotionPersonality.VOLÁTIL) ? 4 : 2;
			evolution = Math.min(MAX_STAT, evolution + evolutionGain);
		}
	}

	/**
	 * Incrementa el hambre de la emoción sentiente.
	 * 
	 * @param amount Cantidad de hambre a añadir
	 */
	public void incrementHunger(int amount) {
		this.hunger = Math.min(MAX_STAT, hunger + amount);

		// Penalización por hambre extrema
		if (hunger >= 90) {
			loyalty = Math.max(0, loyalty - 2);

			if (hunger >= 95 && RANDOM.nextInt(100) < 15) {
				CombatLogger.get().log("⚠️ " + getNombre() + " está MURIENDO de hambre (Lealtad: -2)");
			}
		}
	}

	/**
	 * Decrementa el hambre (cuando se alimenta).
	 * 
	 * @param amount Cantidad de hambre a reducir
	 */
	public void decrementHunger(int amount) {
		this.hunger = Math.max(0, hunger - amount);
	}

	/**
	 * Establece directamente el estado de corrupción.
	 * 
	 * @param corrupted Si la emoción está corrupta
	 */
	public void setCorrupted(boolean corrupted) {
		this.isCorrupted = corrupted;
		if (corrupted) {
			addMemory("corrupted");
		}
	}

	/**
	 * Establece directamente el estado de ascensión.
	 * 
	 * @param ascended Si la emoción ha ascendido
	 */
	public void setAscended(boolean ascended) {
		this.isAscended = ascended;
		if (ascended) {
			addMemory("ascended");
		}
	}

	// ==================== MISSING METHODS (FOR COMPATIBILITY) ====================

	/**
	 * Establece experiencia inicial (usado en combate)
	 */
	public void setInitialExperience(int experience, int level) {
		// Convertir experiencia en evolución
		this.evolution = Math.min(MAX_STAT, (experience / 10));
		CombatLogger.get().log(getNombre() + " ganó " + experience + " experiencia");
	}

	/**
	 * Marca la emoción como mutada (eventos especiales)
	 */
	public void setMutated(boolean mutated) {
		if (mutated) {
			addMemory("mutated");
			CombatLogger.get().log("🧬 " + getNombre() + " ha mutado!");
		}
	}

	@Override
	public String toString() {
		return String.format("%s %s [%s] L:%d H:%d E:%d T:%d%s%s", getSimbolo(), getNombre(), personality.name(),
				loyalty, hunger, evolution, age, isCorrupted ? " 💀" : "", isAscended ? " ✨" : "");
	}
}
