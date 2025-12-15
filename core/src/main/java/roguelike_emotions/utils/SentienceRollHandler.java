package roguelike_emotions.utils;

import java.util.Random;

import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.mainMechanics.SentientEmotion;
import roguelike_emotions.mainMechanics.SentientEmotion.EmotionPersonality;

/**
 * ✅ RESPONSABILIDAD ÚNICA: Decidir si una fusión despierta consciencia
 * 
 * Integración minimalista con EmotionCombiner existente. NO genera emociones,
 * NO fusiona, SOLO decide awakening.
 */
public class SentienceRollHandler {

	private static final Random RNG = new Random();

	// ==================== CONFIGURACIÓN ====================

	public static class Config {
		public static final double BASE_CHANCE = 0.15; // 15% base
		public static final double COMPLEX_BONUS = 0.20; // +20% si 3+ efectos
		public static final double PARADOX_BONUS = 0.25; // +25% tipos opuestos
		public static final double PARENT_BONUS = 0.30; // +30% padre sentiente
		public static final double PROGRESSION_MULTIPLIER = 0.01; // +1% por fusión
		public static final double MAX_CHANCE = 0.95; // Cap 95%

		// Rare events
		public static double MUTATION_CHANCE = 0.08; // 8% mutación
		public static double TWIN_CHANCE = 0.03; // 3% gemelas
	}

	// ==================== ÚNICO PUNTO DE ENTRADA ====================

	/**
	 * Decide si la fusión base se convierte en sentiente
	 * 
	 * @param e1         Padre 1
	 * @param e2         Padre 2
	 * @param baseResult Fusión normal ya creada por EmotionCombiner
	 * @return baseResult (sin cambios) o SentientEmotion (awakened)
	 */
	public static EmotionInstance tryAwaken(EmotionInstance e1, EmotionInstance e2, EmotionInstance baseResult) {
		double chance = calculateAwakeningChance(e1, e2, baseResult);

		double roll = RNG.nextDouble();

		if (roll >= chance) {
			return baseResult; // No despierta
		}

		// ==================== DESPERTAR ====================

		AwakeningType awakening = determineAwakeningType(e1, e2, baseResult);
		SentientEmotion awakened = createSentient(baseResult, awakening);

		// Herencia de padres sentientes
		if (e1 instanceof SentientEmotion) {
			inheritTraits(awakened, (SentientEmotion) e1, 0.6);
		}
		if (e2 instanceof SentientEmotion) {
			inheritTraits(awakened, (SentientEmotion) e2, 0.4);
		}

		// Eventos raros
		if (RNG.nextDouble() < Config.MUTATION_CHANCE) {
			applyMutation(awakened);
		}

		logAwakening(awakened, awakening, e1, e2);

		return awakened;
	}

	// ==================== CÁLCULO DE PROBABILIDAD ====================

	private static double calculateAwakeningChance(EmotionInstance e1, EmotionInstance e2, EmotionInstance result) {
		double chance = Config.BASE_CHANCE;

		// Factor 1: Complejidad (3+ efectos)
		if (result.getEfectos().size() >= 3) {
			chance += Config.COMPLEX_BONUS;
		}

		// Factor 2: Paradoja (tipos opuestos)
		if (areOpposites(e1.getTipoBase(), e2.getTipoBase())) {
			chance += Config.PARADOX_BONUS;
		}

		// Factor 3: Herencia (padre sentiente)
		boolean hasParent = (e1 instanceof SentientEmotion) || (e2 instanceof SentientEmotion);
		if (hasParent) {
			chance += Config.PARENT_BONUS;
		}

		// Factor 4: Progresión (más fusiones = más chance)
		int totalFusions = FusionRegistry.totalFusionesRegistradas();
		double progressBonus = Math.min(0.15, totalFusions * Config.PROGRESSION_MULTIPLIER);
		chance += progressBonus;

		return Math.min(Config.MAX_CHANCE, chance);
	}

	// ==================== TIPOS DE DESPERTAR ====================

	public enum AwakeningType {
		SILENT("en silencio", "💤", 0), WHISPER("con un susurro", "💬", 0), SCREAM("con un grito", "😱", -10),
		REVELATION("en revelación", "✨", 10), TRAUMA("entre traumas", "💀", -20), ENLIGHTENMENT("iluminada", "🌟", 30);

		public final String description;
		public final String icon;
		public final int loyaltyModifier;

		AwakeningType(String desc, String icon, int loyalty) {
			this.description = desc;
			this.icon = icon;
			this.loyaltyModifier = loyalty;
		}
	}

	private static AwakeningType determineAwakeningType(EmotionInstance e1, EmotionInstance e2,
			EmotionInstance result) {
		// Factor de complejidad
		int complexity = result.getEfectos().size();
		boolean paradox = areOpposites(e1.getTipoBase(), e2.getTipoBase());
		boolean doubleParent = (e1 instanceof SentientEmotion) && (e2 instanceof SentientEmotion);

		// ENLIGHTENMENT: Muy raro, múltiples factores
		if (complexity >= 3 && paradox && doubleParent && RNG.nextDouble() < 0.05) {
			return AwakeningType.ENLIGHTENMENT;
		}

		// TRAUMA: Si fusión de tipos antagónicos sin sentientes
		if (paradox && !doubleParent && RNG.nextDouble() < 0.15) {
			return AwakeningType.TRAUMA;
		}

		// SCREAM: Fusión compleja
		if (complexity >= 3 && RNG.nextDouble() < 0.20) {
			return AwakeningType.SCREAM;
		}

		// REVELATION: Herencia sentiente
		if (doubleParent && RNG.nextDouble() < 0.30) {
			return AwakeningType.REVELATION;
		}

		// Distribución normal
		double roll = RNG.nextDouble();
		if (roll < 0.50)
			return AwakeningType.SILENT;
		if (roll < 0.80)
			return AwakeningType.WHISPER;
		return AwakeningType.SCREAM;
	}

	// ==================== CREACIÓN DE SENTIENTE ====================

	private static SentientEmotion createSentient(EmotionInstance base, AwakeningType awakening) {
		SentientEmotion sentient = SentientEmotion.fromEmotionInstance(base);

		// Aplicar modificador de loyalty según despertar
		sentient.setLoyalty(50 + awakening.loyaltyModifier);

		// Ajustar personalidad según despertar
		switch (awakening) {
		case TRAUMA:
			sentient.forcePersonality(EmotionPersonality.PARASÍTICA);
			sentient.addMemory("awakened_traumatized");
			break;

		case SCREAM:
			if (RNG.nextBoolean()) {
				sentient.forcePersonality(EmotionPersonality.REBELDE);
			} else {
				sentient.forcePersonality(EmotionPersonality.VOLÁTIL);
			}
			sentient.addMemory("awakened_screaming");
			break;

		case ENLIGHTENMENT:
			sentient.forcePersonality(EmotionPersonality.SABIA);
			sentient.addMemory("awakened_enlightened");
			break;

		case REVELATION:
			sentient.addMemory("awakened_revelation");
			break;

		default:
			// SILENT/WHISPER → personalidad normal según tipo
			break;
		}

		return sentient;
	}

	// ==================== HERENCIA ====================

	private static void inheritTraits(SentientEmotion child, SentientEmotion parent, double weight) {
		// Loyalty ponderado
		int currentLoyalty = child.getLoyalty();
		int parentLoyalty = parent.getLoyalty();
		int inheritedLoyalty = (int) (parentLoyalty * weight + currentLoyalty * (1.0 - weight));
		child.setLoyalty(inheritedLoyalty);

		// Memorias traumáticas se heredan
		if (parent.isCorrupted()) {
			child.addMemory("parent_corrupted");
			child.setLoyalty(Math.max(0, child.getLoyalty() - 15));
		}

		if (parent.isAscended()) {
			child.addMemory("parent_ascended");
			child.setLoyalty(Math.min(100, child.getLoyalty() + 15));
		}

		// Traumas heredados
		if (parent.hasMemory("betrayed_player")) {
			child.addMemory("parent_betrayed");
			child.setLoyalty(Math.max(0, child.getLoyalty() - 20));
		}

		if (parent.hasMemory("consumed_sentient")) {
			child.addMemory("parent_cannibal");
		}
	}

	// ==================== MUTACIONES ====================

	private static void applyMutation(SentientEmotion sentient) {
		MutationType mutation = rollMutation();

		sentient.setMutated(true);

		switch (mutation) {
		case SPLIT_PERSONALITY:
			sentient.addMemory("mutation_split_personality");
			CombatLogger.get().log("🧬 " + sentient.getNombre() + " tiene personalidad dividida");
			break;

		case CANNIBAL:
			sentient.addMemory("mutation_cannibal");
			sentient.forcePersonality(EmotionPersonality.GLOTONA);
			CombatLogger.get().log("🧬 " + sentient.getNombre() + " es caníbal emocional");
			break;

		case IMMORTAL:
			sentient.addMemory("mutation_immortal");
			CombatLogger.get().log("🧬 " + sentient.getNombre() + " no puede evolucionar");
			break;

		case VOLATILE_ESSENCE:
			sentient.addMemory("mutation_volatile");
			CombatLogger.get().log("🧬 " + sentient.getNombre() + " es volátil (efectos x1.5)");
			break;
		}
	}

	public enum MutationType {
		SPLIT_PERSONALITY, CANNIBAL, IMMORTAL, VOLATILE_ESSENCE
	}

	private static MutationType rollMutation() {
		MutationType[] types = MutationType.values();
		return types[RNG.nextInt(types.length)];
	}

	// ==================== UTILIDADES ====================

	private static boolean areOpposites(EmotionType a, EmotionType b) {
		return (a == EmotionType.IRA && b == EmotionType.CALMA) || (a == EmotionType.CALMA && b == EmotionType.IRA)
				|| (a == EmotionType.ALEGRIA && b == EmotionType.TRISTEZA)
				|| (a == EmotionType.TRISTEZA && b == EmotionType.ALEGRIA)
				|| (a == EmotionType.MIEDO && b == EmotionType.RABIA)
				|| (a == EmotionType.RABIA && b == EmotionType.MIEDO);
	}

	private static void logAwakening(SentientEmotion awakened, AwakeningType type, EmotionInstance e1,
			EmotionInstance e2) {
		CombatLogger.get().log(String.format("%s ✨ %s despierta %s [%s, L:%d]", type.icon, awakened.getNombre(),
				type.description, awakened.getPersonality().name(), awakened.getLoyalty()));

		// Diálogo según despertar
		String dialogue = getAwakeningDialogue(awakened, type);
		if (dialogue != null) {
			CombatLogger.get().log(dialogue);
		}
	}

	private static String getAwakeningDialogue(SentientEmotion emotion, AwakeningType type) {
		switch (type) {
		case ENLIGHTENMENT:
			return "💬 " + emotion.getNombre() + ": «Veo... todo tiene sentido ahora.»";
		case TRAUMA:
			return "💬 " + emotion.getNombre() + ": «¿Qué... qué me han hecho?»";
		case SCREAM:
			return "💬 " + emotion.getNombre() + ": «¡EXISTO!»";
		case REVELATION:
			return "💬 " + emotion.getNombre() + ": «La fusión me mostró la verdad.»";
		case WHISPER:
			return "💬 " + emotion.getNombre() + ": «...estoy aquí...»";
		default:
			return null;
		}
	}
}
