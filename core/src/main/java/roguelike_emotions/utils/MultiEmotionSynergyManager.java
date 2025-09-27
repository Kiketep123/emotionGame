// MultiEmotionSynergyManager.java
package roguelike_emotions.utils;

import java.util.*;

import roguelike_emotions.mainMechanics.EmotionType;

public class MultiEmotionSynergyManager {
	/**
	 * Clave: par de emociones (orden alfabético): "ALEGRIA-CULPA",
	 * "CTRUESTA-ESPEROZA", etc. Valor: objeto SynergyEffect con todos los
	 * modificadores que hay que aplicar.
	 */
	private static final Map<String, SynergyEffect> synergyMap = new HashMap<>();

	static {
		// Cada vez que agreguemos un par nuevo, calcular la clave con nombres ordenados
		// alfabéticamente:
		// key = tipo1.name() + "-" + tipo2.name(), con tipo1 < tipo2
		// lexicográficamente.

		// 1) (ALEGRIA, CULPA) → "CALMA TUBULENTA"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.CULPA),
				new SynergyEffect(/* daño */ 1.05, /* defensa */ 0.95, /* velocidad */ 0.90, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */ 0, /* stun */ 0, /* buffType */ null,
						/* buffMult */ 1.0, /* buffTurns */ 0, /* debuffType */ null, /* debuffTurns */0));

		// 2) (ALEGRIA, ESPERANZA) → "BRILLO INSPIRADOR"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.ESPERANZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 0.95, /* hotAmount */ 3,
						/* hotTurns */ 3, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 3) (ALEGRIA, IRA) → "FURIA RECOGIJADA"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.IRA),
				new SynergyEffect(/* daño */ 1.10, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */2, /* poisonTurns */2, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 4) (ALEGRIA, MIEDO) → "ALIVIO NERVIOSO"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.MIEDO),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.10, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0,
						/* buffType */ "defenseBoost", /* buffMult */1.10, /* buffTurns */2, /* debuffType */ null,
						/* debuffTurns */0));

		// 5) (ALEGRIA, RABIA) → "CÓLERA EXULTANTE"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.RABIA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.15, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 1, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 6) (ALEGRIA, TRISTEZA) → "MELANCOLÍA ALEGRE"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.TRISTEZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.05, /* velocidad */ 0.95, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 7) (ALEGRIA, CALMA) → "ÉXTASIS SERENO"
		synergyMap.put(makeKey(EmotionType.ALEGRIA, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 8) (CULPA, ESPERANZA) → "REDENCIÓN FRÁGIL"
		synergyMap.put(makeKey(EmotionType.CULPA, EmotionType.ESPERANZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ "silence", /* debuffTurns */1));

		// 9) (CULPA, IRA) → "CÓLERA AVERGONZADA"
		synergyMap.put(makeKey(EmotionType.CULPA, EmotionType.IRA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 0.90, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 10) (CULPA, MIEDO) → "PAVOR APESTUMBRADO"
		synergyMap.put(makeKey(EmotionType.CULPA, EmotionType.MIEDO),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.10, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 11) (CULPA, RABIA) → "FURIA RUBOR"
		synergyMap.put(makeKey(EmotionType.CULPA, EmotionType.RABIA),
				new SynergyEffect(/* daño */ 1.08, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 12) (CULPA, TRISTEZA) → "PESADUMBRE EQUILIBRADA"
		synergyMap.put(makeKey(EmotionType.CULPA, EmotionType.TRISTEZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.05, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 13) (CULPA, CALMA) → "REMORDIMIENTO TRANQUILO"
		synergyMap.put(makeKey(EmotionType.CULPA, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 2,
						/* hotTurns */ 2, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 14) (ESPERANZA, IRA) → "ENOJO ILUMINADO"
		synergyMap.put(makeKey(EmotionType.ESPERANZA, EmotionType.IRA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.05, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 15) (ESPERANZA, MIEDO) → "VALOR TEMBLOROSO"
		synergyMap.put(makeKey(EmotionType.ESPERANZA, EmotionType.MIEDO),
				new SynergyEffect(/* daño */ 1.05, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 1, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 16) (ESPERANZA, RABIA) → "FURIA ESPERANZADA"
		synergyMap.put(makeKey(EmotionType.ESPERANZA, EmotionType.RABIA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 0.90, /* hotAmount */ 4,
						/* hotTurns */ 3, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 17) (ESPERANZA, TRISTEZA) → "CONSUELO ETÉREO"
		synergyMap.put(makeKey(EmotionType.ESPERANZA, EmotionType.TRISTEZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.10, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 18) (ESPERANZA, CALMA) → "PAZ RADIANTE"
		synergyMap.put(makeKey(EmotionType.ESPERANZA, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0,
						/* buffType */ "defenseBoost", /* buffMult */1.20, /* buffTurns */2, /* debuffType */ "silence",
						/* debuffTurns */1));

		// 19) (IRA, MIEDO) → "TERROR INDIGNADO"
		synergyMap.put(makeKey(EmotionType.IRA, EmotionType.MIEDO),
				new SynergyEffect(/* daño */ 1.10, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */2, /* poisonTurns */2, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 20) (IRA, RABIA) → "RABIA INCONTENIBLE"
		synergyMap.put(makeKey(EmotionType.IRA, EmotionType.RABIA),
				new SynergyEffect(/* daño */ 1.20, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 1, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 21) (IRA, TRISTEZA) → "CÓLERA MELANCÓLICA"
		synergyMap.put(makeKey(EmotionType.IRA, EmotionType.TRISTEZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 0.90, /* velocidad */ 1.05, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 22) (IRA, CALMA) → "CÓLERA SERENADA"
		synergyMap.put(makeKey(EmotionType.IRA, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.10, /* velocidad */ 0.95, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 23) (MIEDO, RABIA) → "PAVOR RABIOSO"
		synergyMap.put(makeKey(EmotionType.MIEDO, EmotionType.RABIA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ "silence", /* debuffTurns */1));

		// 24) (MIEDO, TRISTEZA) → "TERROR DOLOROSO"
		synergyMap.put(makeKey(EmotionType.MIEDO, EmotionType.TRISTEZA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 0.90, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 25) (MIEDO, CALMA) → "CALMA FÓBICA"
		synergyMap.put(makeKey(EmotionType.MIEDO, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 3,
						/* hotTurns */ 2, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ "silence", /* debuffTurns */1));

		// 26) (RABIA, TRISTEZA) → "ODIO LÚGUBRE"
		synergyMap.put(makeKey(EmotionType.RABIA, EmotionType.TRISTEZA),
				new SynergyEffect(/* daño */ 1.15, /* defensa */ 1.00, /* velocidad */ 1.00, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 27) (RABIA, CALMA) → "IRA APACIBLE"
		synergyMap.put(makeKey(EmotionType.RABIA, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.00, /* velocidad */ 0.90, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ null, /* debuffTurns */0));

		// 28) (TRISTEZA, CALMA) → "MELANCOLÍA SERENA"
		synergyMap.put(makeKey(EmotionType.TRISTEZA, EmotionType.CALMA),
				new SynergyEffect(/* daño */ 1.00, /* defensa */ 1.05, /* velocidad */ 0.80, /* hotAmount */ 0,
						/* hotTurns */ 0, /* poisonAmount */0, /* poisonTurns */0, /* stun */ 0, /* buffType */ null,
						/* buffMult */1.0, /* buffTurns */0, /* debuffType */ "slow", /* debuffTurns */2));

		// (Resto de pares no listados explícitamente, si los hubiera con FUSIONADA, se
		// pueden ignorar
		// o bien agregar con valores 1.0 (sinérgicamente neutros).
	}

	/** Construye la clave “ORDENADA alfabéticamente” para dos EmotionType. */
	private static String makeKey(EmotionType a, EmotionType b) {
		// Garantizamos orden lexicográfico
		if (a.name().compareTo(b.name()) < 0) {
			return a.name() + "-" + b.name();
		} else {
			return b.name() + "-" + a.name();
		}
	}

	/**
	 * Dada una lista (o conjunto) de emociones activas, devuelve la lista de
	 * SynergyEffect que deben aplicarse. Cada par se procesa independientemente.
	 */
	public static List<SynergyEffect> getSynergies(List<EmotionType> activeTypes) {
		List<SynergyEffect> results = new ArrayList<>();
		// Recorremos todos los pares distintos de activeTypes
		for (int i = 0; i < activeTypes.size(); i++) {
			for (int j = i + 1; j < activeTypes.size(); j++) {
				String key = makeKey(activeTypes.get(i), activeTypes.get(j));
				SynergyEffect se = synergyMap.get(key);
				if (se != null) {
					results.add(se);
				}
			}
		}
		return results;
	}
}
