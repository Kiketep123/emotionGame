package roguelike_emotions.mainMechanics;

import java.util.*;

public class EmotionNameGenerator {

	private static final Map<EmotionType, String[]> RAICES_TIPO = Map.of(EmotionType.IRA,
			new String[] { "Zar", "Vor", "Karn" }, EmotionType.TRISTEZA, new String[] { "Myr", "Dol", "Lug" },
			EmotionType.MIEDO, new String[] { "Sha", "Vek", "Dren" }, EmotionType.ALEGRIA,
			new String[] { "Lum", "Eli", "Sol" });

	private static final String[] SUFIJOS = { "abismal", "celeste", "eterno", "fragmentado", "profundo",
			"incandescente", "del ocaso", "interior", "resonante", "errante", "susurrante" };

	private static final String[] PREFIJOS = { "Eco de", "Sombras de", "Fulgor de", "Susurro de", "Rastro de",
			"Visión de" };
	private static final String[] VARIANTES = { "fragmento", "resonancia", "sombra", "eco", "vestigio", "rastro",
			"miraje", "bruma", "estigma" };
	private static final Map<DominantEmotionType, String> SIMBOLOS = Map.of(DominantEmotionType.CURA, "💚",
			DominantEmotionType.DEFENSA, "🛡️", DominantEmotionType.ATAQUE, "🔥", DominantEmotionType.DEBILIDAD, "🕸️");

	private static final Random RNG = new Random();
	/** Lleva el registro de todos los nombres ya generados */
	private static final Set<String> NOMBRES_USADOS = new HashSet<>();

	/** Resetea el registro de nombres (p.ej. al empezar partida) */
	public static void resetTracking() {
		NOMBRES_USADOS.clear();
	}

	// Para emociones base no fusionadas
	public static String generarNombrePorTipo(EmotionType tipo) {
		String[] raices = RAICES_TIPO.getOrDefault(tipo, new String[] { "Xen" });
		String raiz = raices[RNG.nextInt(raices.length)];
		String sufijo = SUFIJOS[RNG.nextInt(SUFIJOS.length)];
		String baseName = raiz + " " + sufijo;
		return buildUniqueName(baseName);
	}

	// Para emociones fusionadas, guiado por tipo dominante
	public static String generarNombreGuiado(DominantEmotionType tipo) {
		String prefijo = PREFIJOS[RNG.nextInt(PREFIJOS.length)];
		String sufijo = SUFIJOS[RNG.nextInt(SUFIJOS.length)];
		String baseName = prefijo + " " + sufijo;
		return buildUniqueName(baseName);
	}

	public static String generarSimbolo(DominantEmotionType tipo) {
		return SIMBOLOS.getOrDefault(tipo, "✨");
	}

	private static String buildUniqueName(String base) {
		if (!NOMBRES_USADOS.contains(base)) {
			NOMBRES_USADOS.add(base);
			return base;
		}
		// Si ya existe, añadir un sufijo de VARIANTES
		String nombre;
		int intentos = 0;
		do {
			String variante = VARIANTES[RNG.nextInt(VARIANTES.length)];
			nombre = base + " (" + variante + ")";
			intentos++;
		} while (NOMBRES_USADOS.contains(nombre) && intentos < 10);

		// Finalmente lo registramos
		NOMBRES_USADOS.add(nombre);
		return nombre;
	}
}
