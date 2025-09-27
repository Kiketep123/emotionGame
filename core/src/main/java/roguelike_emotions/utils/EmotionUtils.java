package roguelike_emotions.utils;

import java.awt.Color;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roguelike_emotions.cfg.EffectVisualConfig;
import roguelike_emotions.cfg.EffectVisualLoader;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.mainMechanics.DominantEmotionType;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;

/**
 * Detecta qué categoría (DominantEmotionType) predomina en la mezcla de dos
 * emociones.
 */
public class EmotionUtils {

	/**
	 * Suma los “pesos” de cada efecto (usamos la intensidad como peso) y devuelve
	 * la categoría dominante.
	 */
	public static DominantEmotionType detectarTipoDominante(EmotionInstance e1, EmotionInstance e2) {
		Map<DominantEmotionType, Double> pesos = new HashMap<>();
		// Acumular de la primera emoción
		for (EffectDetail detalle : e1.getEfectos()) {
			sumarPeso(pesos, detalle);
		}
		// Acumular de la segunda
		for (EffectDetail detalle : e2.getEfectos()) {
			sumarPeso(pesos, detalle);
		}

		// Elegir entry con mayor valor
		return pesos.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue))
				.orElse(new AbstractMap.SimpleEntry<>(DominantEmotionType.NEUTRA, 0.0)).getKey();
	}

	private static void sumarPeso(Map<DominantEmotionType, Double> mapa, EffectDetail detalle) {
	    EmotionEffect efecto = detalle.getTipo(); // ahora es un enum
	    DominantEmotionType categoria = convertirTipo(efecto); // <-- cambiar firma si hace falta
	    double pesoActual = mapa.getOrDefault(categoria, 0.0);
	    mapa.put(categoria, pesoActual + detalle.getIntensidad());
	}
	/**
	 * Mapea cada EmotionEffect a una categoría de DominantEmotionType. Debe
	 * reflejar tu propia intención de diseño: - CURACION y DEFENDER son
	 * salud/armadura - FUEGO y REBOTE son ofensivos (daño activo) - VENENO y
	 * RALENTIZAR son debuffs/dano prolongado - Otros quedan en NEUTRA
	 */
	private static DominantEmotionType convertirTipo(EmotionEffect efecto) {
		switch (efecto) {
		case CURACION:
			return DominantEmotionType.CURA;
		case DEFENDER:
			return DominantEmotionType.DEFENSA;
		case FUEGO:
		case REBOTE:
			return DominantEmotionType.ATAQUE;
		case VENENO:
		case RALENTIZAR:
			return DominantEmotionType.DEBILIDAD;
		default:
			return DominantEmotionType.NEUTRA;
		}
	}

	public static EmotionType convertirDominantToEmotionType(DominantEmotionType dom) {
		switch (dom) {
		case CURA:
			return EmotionType.ALEGRIA;
		case DEFENSA:
			return EmotionType.MIEDO;
		case ATAQUE:
		case DEBILIDAD:
			return EmotionType.TRISTEZA;
		case NEUTRA:
		default:
			return EmotionType.ESPERANZA; // o FUSIONADA si lo defines
		}
	}

	public static DominantEmotionType detectarTipoDominanteSimple(List<EffectDetail> efectos) {
		Map<DominantEmotionType, Double> pesos = new HashMap<>();
		for (EffectDetail e : efectos) {
			DominantEmotionType tipo = convertirTipo(e.getTipo());
			pesos.put(tipo, pesos.getOrDefault(tipo, 0.0) + e.getIntensidad());
		}
		return pesos.entrySet().stream().max(Map.Entry.comparingByValue())
				.orElse(Map.entry(DominantEmotionType.NEUTRA, 0.0)).getKey();
	}

	/**
	 * Calcula un factor de compatibilidad entre dos emociones usando la matriz de
	 * dominancia. Si el promedio de peso(t1→t2) y peso(t2→t1) es >1 → sinergia; <1
	 * → tensión. El resultado se normaliza ligeramente para mantener el balance.
	 */
	public static double calcularFactorCompatibilidad(EmotionInstance e1, EmotionInstance e2,
			EmotionDominanceMatrix matrix) {
		EmotionType t1 = e1.getTipoBase();
		EmotionType t2 = e2.getTipoBase();

		double p12 = matrix.getPeso(t1, t2);
		double p21 = matrix.getPeso(t2, t1);
		double media = (p12 + p21) / 2.0;

		// Normalizar alrededor de 1 con un rango reducido [0.75, 1.25]
		// Esto evita boosts demasiado grandes o penalizaciones excesivas
		double factor = 1.0 + (media - 1.0) * 0.5;
		return Math.max(0.75, Math.min(1.25, factor));
	}

	public static String mixColorrs(Color c1, Color c2) {
		// TODO Auto-generated method stub
		return String.format("#%02X%02X%02X", (c1.getRed() + c2.getRed()) / 2, (c1.getGreen() + c2.getGreen()) / 2,
				(c1.getBlue() + c2.getBlue()) / 2);
	}
	public static EffectVisualConfig getVisualConfig(EmotionEffect tipo) {
	    return EffectVisualLoader.getVisual(tipo);
	}
	   /**
     * Dado un mapa de pesos por DominantEmotionType, devuelve el que más suma
     * si supera el umbral; o null si ninguno.
     */
    public static DominantEmotionType detectarUmbralEspecial(Map<DominantEmotionType, Double> pesos, double umbral) {
        return pesos.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .filter(e -> e.getValue() >= umbral)
            .map(Map.Entry::getKey)
            .orElse(null);
    }
	
}
