package roguelike_emotions.mainMechanics;

import java.util.*;

/**
 * Generador avanzado de nombres procedurales para emociones. Utiliza múltiples
 * estrategias de combinación para crear nombres únicos y evocadores.
 */
public class EmotionNameGenerator {

	// ==================== COMPONENTES LINGÜÍSTICOS ====================

	private static final Map<EmotionType, NameComponents> TIPO_COMPONENTS = createComponentsMap();

	private static Map<EmotionType, NameComponents> createComponentsMap() {
		Map<EmotionType, NameComponents> map = new EnumMap<>(EmotionType.class);

		map.put(EmotionType.IRA, new NameComponents(
				new String[] { "Zar", "Vor", "Karn", "Braz", "Chor", "Drax", "Grak", "Kroth", "Rax", "Vorn" },
				new String[] { "ius", "ax", "oth", "ak", "us", "ix", "or", "un" }, new String[] { "ardiente", "furioso",
						"colérico", "incandescente", "violento", "salvaje", "rugiente", "devastador" }));

		map.put(EmotionType.RABIA,
				new NameComponents(
						new String[] { "Furi", "Rag", "Ber", "Feroc", "Sav", "Brutal", "Wroth", "Veng", "Fren", "Rav" },
						new String[] { "os", "ax", "or", "us", "em", "ik", "ar", "un" }, new String[] { "desatado",
								"frenético", "brutal", "implacable", "salvaje", "feroz", "indómito", "primordial" }));

		map.put(EmotionType.TRISTEZA,
				new NameComponents(
						new String[] { "Myr", "Dol", "Lug", "Nox", "Umbr", "Veil", "Ciner", "Gris", "Pall", "Seren" },
						new String[] { "ia", "or", "en", "im", "is", "um", "an", "os" }, new String[] { "sombrío",
								"melancólico", "gris", "desolado", "lúgubre", "mortecino", "apagado", "lacrimoso" }));

		map.put(EmotionType.MIEDO, new NameComponents(
				new String[] { "Sha", "Vek", "Dren", "Phob", "Terr", "Pav", "Trem", "Umbr", "Nyx", "Crypt" },
				new String[] { "ia", "or", "ix", "us", "al", "um", "ok", "eth" }, new String[] { "tenebroso",
						"acechante", "susurrante", "espectral", "helado", "ominoso", "profundo", "inquietante" }));

		map.put(EmotionType.ALEGRIA, new NameComponents(
				new String[] { "Lum", "Eli", "Sol", "Aur", "Rad", "Clar", "Viv", "Glor", "Laet", "Felic" },
				new String[] { "ia", "en", "is", "or", "us", "ix", "an", "os" }, new String[] { "radiante", "luminoso",
						"brillante", "celeste", "dichoso", "exultante", "resplandeciente", "jubiloso" }));

		map.put(EmotionType.CULPA, new NameComponents(
				new String[] { "Remorx", "Culp", "Penit", "Contr", "Cens", "Damn", "Expi", "Tort", "Afflic", "Rueg" },
				new String[] { "ia", "us", "or", "is", "um", "en", "ium", "al" }, new String[] { "atormentado",
						"penitente", "contrito", "remordiente", "lacerado", "culposo", "arrepentido", "angustiado" }));

		map.put(EmotionType.ESPERANZA, new NameComponents(
				new String[] { "Sper", "Anim", "Opt", "Fid", "Aspir", "Desir", "Anh", "Soñ", "Crey", "Confi" },
				new String[] { "a", "o", "um", "is", "or", "ia", "en", "os" }, new String[] { "esperanzado",
						"anhelante", "optimista", "confiado", "aspirante", "soñador", "prometedor", "alentador" }));

		map.put(EmotionType.CALMA, new NameComponents(
				new String[] { "Pac", "Trang", "Quie", "Seren", "Plac", "Repos", "Silenx", "Sopor", "Lene", "Mans" },
				new String[] { "ia", "o", "us", "um", "is", "or", "en", "al" }, new String[] { "tranquilo", "apacible",
						"sereno", "plácido", "sosegado", "manso", "quieto", "silencioso" }));

		map.put(EmotionType.NEUTRO,
				new NameComponents(
						new String[] { "Zen", "Aet", "Eq", "Har", "Bal", "Neut", "Pax", "Medix", "Stab", "Imparc" },
						new String[] { "ia", "um", "is", "or", "ix", "al", "en", "us" }, new String[] { "equilibrado",
								"sereno", "neutral", "balanceado", "armónico", "templado", "estable", "contenido" }));

		map.put(EmotionType.FUSIONADA, new NameComponents(
				new String[] { "Syn", "Fus", "Merg", "Amal", "Conjun", "Hybrid", "Mixt", "Combin", "Entrel", "Simbi" },
				new String[] { "ia", "o", "um", "is", "ix", "or", "en", "os" }, new String[] { "fusionado",
						"entrelazado", "amalgamado", "sinérgico", "híbrido", "compuesto", "mezclado", "coalescente" }));

		return map;
	}

	// Prefijos místicos y evocadores
	private static final String[] PREFIJOS_MISTICOS = { "Eco de", "Sombras de", "Fulgor de", "Susurro de", "Rastro de",
			"Visión de", "Esencia de", "Fragmento de", "Vestigio de", "Latido de", "Aliento de", "Presagio de",
			"Memoria de", "Cenizas de", "Destello de", "Velo de", "Espectro de", "Hálito de", "Grito de", "Murmullo de",
			"Lamento de" };

	// Sufijos poéticos
	private static final String[] SUFIJOS_POETICOS = { "abismal", "celeste", "eterno", "fragmentado", "profundo",
			"incandescente", "del ocaso", "interior", "resonante", "errante", "susurrante", "ancestral", "perdido",
			"olvidado", "silente", "carmesí", "nebuloso", "cristalino", "sepulcral", "prístino", "marchito",
			"renaciente", "efímero", "inmutable" };

	// Variantes para colisiones
	private static final String[] VARIANTES = { "fragmento", "resonancia", "sombra", "eco", "vestigio", "rastro",
			"miraje", "bruma", "estigma", "reflejo", "réplica", "destello", "susurro", "latido", "ceniza", "chispa",
			"huella", "marca" };

	// Conectores para nombres complejos
	private static final String[] CONECTORES = { "del", "de la", "de los", "en el", "bajo el", "sobre el", "entre" };

	// Sustantivos evocadores para nombres compuestos
	private static final String[] SUSTANTIVOS_EPICOS = { "Vacío", "Abismo", "Crepúsculo", "Amanecer", "Tormenta",
			"Silencio", "Olvido", "Infinito", "Destino", "Caos", "Orden", "Eclipse", "Horizonte", "Umbral", "Éter",
			"Vórtice", "Nexo", "Prisma" };

	// Símbolos por tipo dominante
	private static final Map<DominantEmotionType, String[]> SIMBOLOS_VARIADOS = Map.of(DominantEmotionType.CURA,
			new String[] { "💚", "💖", "✨", "🌿", "🕊️" }, DominantEmotionType.DEFENSA,
			new String[] { "🛡️", "🔰", "⚔️", "🏰", "🗡️" }, DominantEmotionType.ATAQUE,
			new String[] { "🔥", "⚡", "💥", "🌪️", "💢" }, DominantEmotionType.DEBILIDAD,
			new String[] { "🕸️", "💀", "🦠", "⚠️", "🌑" });

	private static final Random RNG = new Random();
	private static final Set<String> NOMBRES_USADOS = new HashSet<>();

	// ==================== CLASE AUXILIAR ====================

	private static class NameComponents {
		final String[] raices;
		final String[] terminaciones;
		final String[] adjetivos;

		NameComponents(String[] raices, String[] terminaciones, String[] adjetivos) {
			this.raices = raices;
			this.terminaciones = terminaciones;
			this.adjetivos = adjetivos;
		}
	}

	// ==================== API PÚBLICA ====================

	/**
	 * Resetea el registro de nombres (útil al empezar nueva partida)
	 */
	public static void resetTracking() {
		NOMBRES_USADOS.clear();
	}

	/**
	 * Genera un nombre para una emoción base usando múltiples estrategias.
	 */
	public static String generarNombrePorTipo(EmotionType tipo) {
		NameStrategy strategy = selectStrategy();
		String baseName = strategy.generate(tipo);
		return buildUniqueName(baseName);
	}

	/**
	 * Genera un nombre para una emoción fusionada, considerando su tipo dominante.
	 */
	public static String generarNombreGuiado(DominantEmotionType tipoDominante) {
		NameStrategy strategy = selectFusionStrategy();
		String baseName = strategy.generateFusion(tipoDominante);
		return buildUniqueName(baseName);
	}

	/**
	 * Genera un símbolo apropiado para el tipo dominante.
	 */
	public static String generarSimbolo(DominantEmotionType tipo) {
		String[] opciones = SIMBOLOS_VARIADOS.getOrDefault(tipo, new String[] { "✨" });
		return opciones[RNG.nextInt(opciones.length)];
	}

	// ==================== ESTRATEGIAS DE GENERACIÓN ====================

	private interface NameStrategy {
		String generate(EmotionType tipo);

		String generateFusion(DominantEmotionType tipoDominante);
	}

	/**
	 * Estrategia 1: Raíz + Terminación + Adjetivo Ejemplo: "Zarax Furioso"
	 */
	private static class CompoundStrategy implements NameStrategy {
		@Override
		public String generate(EmotionType tipo) {
			NameComponents comp = TIPO_COMPONENTS.getOrDefault(tipo, TIPO_COMPONENTS.get(EmotionType.NEUTRO));

			String raiz = pickRandom(comp.raices);
			String term = pickRandom(comp.terminaciones);
			String adj = pickRandom(comp.adjetivos);

			return raiz + term + " " + adj;
		}

		@Override
		public String generateFusion(DominantEmotionType tipoDominante) {
			EmotionType tipo = mapDominantToBase(tipoDominante);
			return generate(tipo);
		}
	}

	/**
	 * Estrategia 2: Prefijo + Sustantivo Épico Ejemplo: "Eco del Vacío"
	 */
	private static class MysticStrategy implements NameStrategy {
		@Override
		public String generate(EmotionType tipo) {
			String prefijo = pickRandom(PREFIJOS_MISTICOS);
			String sustantivo = pickRandom(SUSTANTIVOS_EPICOS);
			return prefijo + " " + sustantivo;
		}

		@Override
		public String generateFusion(DominantEmotionType tipoDominante) {
			String prefijo = pickRandom(PREFIJOS_MISTICOS);
			String sufijo = pickRandom(SUFIJOS_POETICOS);
			return prefijo + " " + sufijo;
		}
	}

	/**
	 * Estrategia 3: Raíz + Conector + Sustantivo Ejemplo: "Vorix del Abismo"
	 */
	private static class EpicStrategy implements NameStrategy {
		@Override
		public String generate(EmotionType tipo) {
			NameComponents comp = TIPO_COMPONENTS.getOrDefault(tipo, TIPO_COMPONENTS.get(EmotionType.NEUTRO));

			String raiz = pickRandom(comp.raices);
			String term = pickRandom(comp.terminaciones);
			String conector = pickRandom(CONECTORES);
			String sustantivo = pickRandom(SUSTANTIVOS_EPICOS);

			return raiz + term + " " + conector + " " + sustantivo;
		}

		@Override
		public String generateFusion(DominantEmotionType tipoDominante) {
			EmotionType tipo = mapDominantToBase(tipoDominante);
			return generate(tipo);
		}
	}

	/**
	 * Estrategia 4: Adjetivo + Sustantivo Ejemplo: "Susurrante Crepúsculo"
	 */
	private static class PoeticStrategy implements NameStrategy {
		@Override
		public String generate(EmotionType tipo) {
			NameComponents comp = TIPO_COMPONENTS.getOrDefault(tipo, TIPO_COMPONENTS.get(EmotionType.NEUTRO));

			String adj = pickRandom(comp.adjetivos);
			String sustantivo = pickRandom(SUSTANTIVOS_EPICOS);

			// Capitalizar adjetivo
			String adjCap = adj.substring(0, 1).toUpperCase() + adj.substring(1);

			return adjCap + " " + sustantivo;
		}

		@Override
		public String generateFusion(DominantEmotionType tipoDominante) {
			String sufijo = pickRandom(SUFIJOS_POETICOS);
			String sustantivo = pickRandom(SUSTANTIVOS_EPICOS);

			String sufijoCapital = sufijo.substring(0, 1).toUpperCase() + sufijo.substring(1);

			return sufijoCapital + " " + sustantivo;
		}
	}

	/**
	 * Estrategia 5: Raíz Compuesta (fusión de dos raíces) Ejemplo: "Zarumbra" (Zar
	 * + Umbra)
	 */
	private static class FusedRootStrategy implements NameStrategy {
		@Override
		public String generate(EmotionType tipo) {
			NameComponents comp = TIPO_COMPONENTS.getOrDefault(tipo, TIPO_COMPONENTS.get(EmotionType.NEUTRO));

			// Tomar dos raíces aleatorias y fusionarlas
			String raiz1 = pickRandom(comp.raices);

			// Seleccionar otro tipo aleatorio para mezclar
			EmotionType tipo2 = pickRandomEnum(EmotionType.class);
			NameComponents comp2 = TIPO_COMPONENTS.get(tipo2);
			String raiz2 = pickRandom(comp2.raices);

			// Tomar inicio de la primera y final de la segunda
			int corte1 = raiz1.length() / 2;
			int corte2 = raiz2.length() / 2;

			String nombreFusionado = raiz1.substring(0, corte1) + raiz2.substring(corte2).toLowerCase();
			String adj = pickRandom(comp.adjetivos);

			return nombreFusionado + " " + adj;
		}

		@Override
		public String generateFusion(DominantEmotionType tipoDominante) {
			EmotionType tipo = mapDominantToBase(tipoDominante);
			return generate(tipo);
		}
	}

	// ==================== SELECCIÓN DE ESTRATEGIAS ====================

	private static final List<NameStrategy> STRATEGIES = Arrays.asList(new CompoundStrategy(), new MysticStrategy(),
			new EpicStrategy(), new PoeticStrategy(), new FusedRootStrategy());

	private static NameStrategy selectStrategy() {
		return STRATEGIES.get(RNG.nextInt(STRATEGIES.size()));
	}

	private static NameStrategy selectFusionStrategy() {
		// Para fusiones, favorecer estrategias más místicas
		int choice = RNG.nextInt(100);
		if (choice < 30)
			return new MysticStrategy();
		if (choice < 60)
			return new EpicStrategy();
		if (choice < 80)
			return new PoeticStrategy();
		if (choice < 90)
			return new FusedRootStrategy();
		return new CompoundStrategy();
	}

	// ==================== MANEJO DE UNICIDAD ====================

	private static String buildUniqueName(String base) {
		if (!NOMBRES_USADOS.contains(base)) {
			NOMBRES_USADOS.add(base);
			return base;
		}

		// Intentar con variantes
		for (int i = 0; i < 20; i++) {
			String variante = pickRandom(VARIANTES);
			String nombre = base + " (" + variante + ")";

			if (!NOMBRES_USADOS.contains(nombre)) {
				NOMBRES_USADOS.add(nombre);
				return nombre;
			}
		}

		// Último recurso: sufijo numérico
		int counter = 2;
		String nombre;
		do {
			nombre = base + " " + romanNumeral(counter);
			counter++;
		} while (NOMBRES_USADOS.contains(nombre) && counter < 20);

		NOMBRES_USADOS.add(nombre);
		return nombre;
	}

	// ==================== UTILIDADES ====================

	private static <T> T pickRandom(T[] array) {
		return array[RNG.nextInt(array.length)];
	}

	private static <T extends Enum<T>> T pickRandomEnum(Class<T> enumClass) {
		T[] values = enumClass.getEnumConstants();
		return values[RNG.nextInt(values.length)];
	}

	private static EmotionType mapDominantToBase(DominantEmotionType dominant) {
		// Mapeo aproximado para reutilizar componentes
		switch (dominant) {
		case ATAQUE:
			return EmotionType.RABIA;
		case DEFENSA:
			return EmotionType.MIEDO;
		case CURA:
			return EmotionType.ESPERANZA;
		case DEBILIDAD:
			return EmotionType.CULPA;
		default:
			return EmotionType.NEUTRO;
		}
	}

	private static String romanNumeral(int number) {
		if (number >= 10)
			return String.valueOf(number);

		String[] numerals = { "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX" };
		return numerals[number];
	}

	// ==================== MÉTODO DE DEPURACIÓN ====================

	/**
	 * Genera múltiples nombres para testing y visualización. Útil para debugging y
	 * ajustar el generador.
	 */
	public static List<String> generateSamples(EmotionType tipo, int count) {
		List<String> samples = new ArrayList<>();
		Set<String> tempUsed = new HashSet<>(NOMBRES_USADOS);

		for (int i = 0; i < count; i++) {
			String name = generarNombrePorTipo(tipo);
			samples.add(name);
		}

		// Restaurar el estado original
		NOMBRES_USADOS.clear();
		NOMBRES_USADOS.addAll(tempUsed);

		return samples;
	}
}