package roguelike_emotions.utils;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.mainMechanics.*;

/**
 * Combina emociones aplicando sinergias y tensiones según la
 * EmotionDominanceMatrix. Soporta fusión de 2 o más emociones simultáneamente.
 */
public class EmotionCombiner {

	// ==================== CONSTANTES ====================
	private static final double FUSION_THRESHOLD = 1.1;
	private static final double MIN_COMPATIBILITY_FACTOR = 0.75;
	private static final double MAX_COMPATIBILITY_FACTOR = 1.25;
	private static final double COMPATIBILITY_WEIGHT = 0.5;
	private static final double ANTAGONISM_PENALTY = 0.9;
	private static final String MULTI_FUSION_SYMBOL = "🔀";
	private static final String FUSION_KEY_SEPARATOR = "|";

	// ==================== DEPENDENCIAS ====================
	private static EmotionDominanceMatrix matrix;

	/**
	 * Inyecta la matriz de dominancia (debe llamarse al inicializar GameManager)
	 */
	public static void setDominanceMatrix(EmotionDominanceMatrix m) {
		if (m == null) {
			throw new IllegalArgumentException("La matriz de dominancia no puede ser null");
		}
		matrix = m;
	}

	// ==================== FUSIÓN DE DOS EMOCIONES ====================

	/**
	 * Combina dos emociones en una nueva emoción fusionada.
	 *
	 * @throws IllegalArgumentException si las emociones no son compatibles o son
	 *                                  null
	 */
	public static EmotionInstance combinar(EmotionInstance e1, EmotionInstance e2) {
		validateEmotions(e1, e2);

		if (!canFuse(e1, e2)) {
			throw new IllegalArgumentException(String.format("Las emociones %s y %s no son compatibles para fusionar.",
					e1.getNombre(), e2.getNombre()));
		}

		// Buscar fusión existente en el registro
		EmotionInstance cached = FusionRegistry.obtenerFusion(e1.getId(), e2.getId());
		if (cached != null) {
			return cached;
		}

		// Crear nueva fusión
		List<EffectDetail> fusionEffects = mergeEffects(Arrays.asList(e1, e2));
		double compatibilityFactor = calcularFactorCompatibilidad(e1.getTipoBase(), e2.getTipoBase());
		List<EffectDetail> adjustedEffects = applyCompatibilityFactor(fusionEffects, compatibilityFactor);

		DominantEmotionType dominantType = EmotionUtils.detectarTipoDominante(e1, e2);
		EmotionType baseType = EmotionUtils.convertirDominantToEmotionType(dominantType);

		String name = EmotionNameGenerator.generarNombreGuiado(dominantType);
		String symbol = EmotionNameGenerator.generarSimbolo(dominantType);
		String color = mixColors(Arrays.asList(e1.getColor(), e2.getColor()));

		EmotionInstance fusion = new EmotionInstance(name, baseType, adjustedEffects, color, symbol);
		FusionRegistry.registrarFusion(e1.getId(), e2.getId(), fusion);

		return fusion;
	}

	/**
	 * Verifica si dos emociones pueden fusionarse según su compatibilidad.
	 */
	public static boolean canFuse(EmotionInstance e1, EmotionInstance e2) {
		validateMatrix();
		validateEmotions(e1, e2);

		double avgWeight = calculateAverageWeight(e1.getTipoBase(), e2.getTipoBase());
		return avgWeight >= FUSION_THRESHOLD;
	}

	// ==================== FUSIÓN MÚLTIPLE ====================

	/**
	 * Combina simultáneamente n emociones (n ≥ 2).
	 *
	 * @throws IllegalArgumentException si hay menos de 2 emociones o son null
	 */
	public static EmotionInstance combinarMultiples(List<EmotionInstance> emotions) {
		validateMultipleEmotions(emotions);

		// Verificar caché con clave concatenada
		String cacheKey = buildMultiFusionKey(emotions);
		EmotionInstance cached = FusionRegistry.obtenerFusionPorClave(cacheKey);
		if (cached != null) {
			return cached;
		}

		// Consolidar efectos
		List<EffectDetail> mergedEffects = mergeEffects(emotions);

		// Calcular tipo base dominante
		EmotionType baseType = detectarTipoBaseMultiple(emotions);

		// Generar atributos visuales
		String name = EmotionNameGenerator.generarNombrePorTipo(baseType);
		String symbol = MULTI_FUSION_SYMBOL;
		String color = mixColors(emotions.stream().map(EmotionInstance::getColor).collect(Collectors.toList()));

		// Aplicar factores de compatibilidad y penalizaciones
		double globalFactor = calcularFactorCompatibilidadMultiple(emotions);
		double penaltyFactor = aplicarPenalizacionesAntagonistas(emotions);
		double finalFactor = globalFactor * penaltyFactor;

		List<EffectDetail> adjustedEffects = applyCompatibilityFactor(mergedEffects, finalFactor);

		// Crear y registrar
		EmotionInstance fusion = new EmotionInstance(name, baseType, adjustedEffects, color, symbol);
		FusionRegistry.registrarFusionMultiple(cacheKey, emotions, fusion);

		return fusion;
	}

	// ==================== MÉTODOS DE COMPATIBILIDAD ====================

	/**
	 * Calcula el factor de compatibilidad entre dos tipos base. Rango normalizado:
	 * [0.75, 1.25]
	 */
	private static double calcularFactorCompatibilidad(EmotionType t1, EmotionType t2) {
		if (matrix == null) {
			return 1.0;
		}

		double avgWeight = calculateAverageWeight(t1, t2);
		double factor = 1.0 + (avgWeight - 1.0) * COMPATIBILITY_WEIGHT;

		return clampFactor(factor);
	}

	/**
	 * Calcula el factor de compatibilidad global para múltiples emociones.
	 * Considera todas las interacciones por pares.
	 */
	private static double calcularFactorCompatibilidadMultiple(List<EmotionInstance> emotions) {
		if (matrix == null || emotions.size() < 2) {
			return 1.0;
		}

		List<Double> pairWeights = new ArrayList<>();

		for (int i = 0; i < emotions.size(); i++) {
			for (int j = i + 1; j < emotions.size(); j++) {
				EmotionType t1 = emotions.get(i).getTipoBase();
				EmotionType t2 = emotions.get(j).getTipoBase();
				pairWeights.add(calculateAverageWeight(t1, t2));
			}
		}

		double avgInternal = pairWeights.stream().mapToDouble(Double::doubleValue).average().orElse(1.0);

		double factor = 1.0 + (avgInternal - 1.0) * COMPATIBILITY_WEIGHT;

		return clampFactor(factor);
	}

	/**
	 * Aplica penalizaciones por combinaciones antagónicas.
	 */
	private static double aplicarPenalizacionesAntagonistas(List<EmotionInstance> emotions) {
		Map<EmotionType, List<EmotionType>> antagonisms = buildAntagonismMap();

		double penalty = 1.0;

		for (int i = 0; i < emotions.size(); i++) {
			EmotionType t1 = emotions.get(i).getTipoBase();

			for (int j = i + 1; j < emotions.size(); j++) {
				EmotionType t2 = emotions.get(j).getTipoBase();

				if (areAntagonistic(t1, t2, antagonisms)) {
					penalty *= ANTAGONISM_PENALTY;
				}
			}
		}

		return Math.max(MIN_COMPATIBILITY_FACTOR, penalty);
	}

	// ==================== PROCESAMIENTO DE EFECTOS ====================

	/**
	 * Fusiona efectos de múltiples emociones, sumando intensidades y tomando
	 * máximos.
	 */
	private static List<EffectDetail> mergeEffects(List<EmotionInstance> emotions) {
		Map<EmotionEffect, EffectDetail> effectMap = new LinkedHashMap<>();

		emotions.stream().flatMap(e -> e.getEfectos().stream()).forEach(detail -> {
			EmotionEffect type = detail.getTipo();

			effectMap.merge(type, detail,
					(existing, incoming) -> new EffectDetail(type, existing.getIntensidad() + incoming.getIntensidad(),
							Math.max(existing.getProbabilidad(), incoming.getProbabilidad()),
							Math.max(existing.getRemainingTurns(), incoming.getRemainingTurns())));
		});

		return new ArrayList<>(effectMap.values());
	}

	/**
	 * Aplica un factor de compatibilidad a las intensidades de todos los efectos.
	 */
	private static List<EffectDetail> applyCompatibilityFactor(List<EffectDetail> effects, double factor) {

		return effects.stream().map(ed -> new EffectDetail(ed.getTipo(), ed.getIntensidad() * factor,
				ed.getProbabilidad(), ed.getRemainingTurns())).collect(Collectors.toList());
	}

	// ==================== DETECCIÓN DE TIPO DOMINANTE ====================

	/**
	 * Detecta el tipo base dominante entre múltiples emociones. Usa scoring basado
	 * en pesos de la matriz de dominancia.
	 */
	private static EmotionType detectarTipoBaseMultiple(List<EmotionInstance> emotions) {
		Map<EmotionType, Double> scores = initializeScoreMap();

		// Acumular pesos para cada candidato
		for (EmotionInstance emotion : emotions) {
			EmotionType sourceType = emotion.getTipoBase();

			for (EmotionType candidate : EmotionType.values()) {
				double weight = matrix != null ? matrix.getPeso(sourceType, candidate) : 1.0;

				scores.merge(candidate, weight, Double::sum);
			}
		}

		// Normalizar por cantidad de emociones
		int n = emotions.size();
		scores.replaceAll((type, score) -> score / n);

		// Retornar el tipo con mayor puntaje
		return scores.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
				.orElse(EmotionType.NEUTRO);
	}

	// ==================== UTILIDADES DE COLOR ====================

	/**
	 * Mezcla múltiples colores promediando sus componentes RGB.
	 */
	private static String mixColors(List<String> hexColors) {
		if (hexColors.isEmpty()) {
			return "#808080"; // Gris por defecto
		}

		float rSum = 0, gSum = 0, bSum = 0;

		for (String hex : hexColors) {
			Color color = Color.decode(hex);
			rSum += color.getRed();
			gSum += color.getGreen();
			bSum += color.getBlue();
		}

		int n = hexColors.size();

		return String.format("#%02X%02X%02X", Math.min(255, Math.round(rSum / n)), Math.min(255, Math.round(gSum / n)),
				Math.min(255, Math.round(bSum / n)));
	}

	// ==================== HELPERS Y VALIDACIONES ====================

	private static void validateMatrix() {
		if (matrix == null) {
			throw new IllegalStateException(
					"La matriz de dominancia no ha sido inicializada. " + "Llama a setDominanceMatrix() primero.");
		}
	}

	private static void validateEmotions(EmotionInstance e1, EmotionInstance e2) {
		if (e1 == null || e2 == null) {
			throw new IllegalArgumentException("Las emociones no pueden ser null");
		}
	}

	private static void validateMultipleEmotions(List<EmotionInstance> emotions) {
		if (emotions == null || emotions.size() < 2) {
			throw new IllegalArgumentException("Se requieren al menos dos emociones para fusionar");
		}

		if (emotions.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("La lista contiene emociones null");
		}
	}

	private static double calculateAverageWeight(EmotionType t1, EmotionType t2) {
		double w12 = matrix.getPeso(t1, t2);
		double w21 = matrix.getPeso(t2, t1);
		return (w12 + w21) / 2.0;
	}

	private static double clampFactor(double factor) {
		return Math.max(MIN_COMPATIBILITY_FACTOR, Math.min(MAX_COMPATIBILITY_FACTOR, factor));
	}

	private static String buildMultiFusionKey(List<EmotionInstance> emotions) {
		return emotions.stream().map(EmotionInstance::getId).sorted().collect(Collectors.joining(FUSION_KEY_SEPARATOR));
	}

	private static Map<EmotionType, Double> initializeScoreMap() {
		Map<EmotionType, Double> scores = new LinkedHashMap<>();
		for (EmotionType type : EmotionType.values()) {
			scores.put(type, 0.0);
		}
		return scores;
	}

	private static Map<EmotionType, List<EmotionType>> buildAntagonismMap() {
		Map<EmotionType, List<EmotionType>> antagonisms = new HashMap<>();
		antagonisms.put(EmotionType.IRA, Arrays.asList(EmotionType.TRISTEZA, EmotionType.MIEDO));
		antagonisms.put(EmotionType.TRISTEZA, Collections.singletonList(EmotionType.ALEGRIA));
		antagonisms.put(EmotionType.MIEDO, Collections.singletonList(EmotionType.RABIA));
		return antagonisms;
	}

	private static boolean areAntagonistic(EmotionType t1, EmotionType t2,
			Map<EmotionType, List<EmotionType>> antagonisms) {

		return antagonisms.getOrDefault(t1, Collections.emptyList()).contains(t2)
				|| antagonisms.getOrDefault(t2, Collections.emptyList()).contains(t1);
	}
}