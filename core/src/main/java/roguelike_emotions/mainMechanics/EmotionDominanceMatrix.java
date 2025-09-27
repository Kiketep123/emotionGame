package roguelike_emotions.mainMechanics;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EmotionDominanceMatrix {
	private final Map<EmotionType, Map<EmotionType, Double>> dominancia = new HashMap<>();
	private final Random rng = new Random();

	public EmotionDominanceMatrix() {
		for (EmotionType t1 : EmotionType.values()) {
			Map<EmotionType, Double> mapaInterno = new HashMap<>();
			for (EmotionType t2 : EmotionType.values()) {
				if (t1 == t2) {
					mapaInterno.put(t2, 1.0); // Equilibrio
				} else {
					mapaInterno.put(t2, 0.5 + rng.nextDouble()); // 0.5 a 1.5
				}
			}
			dominancia.put(t1, mapaInterno);
		}
	}

	public double getPeso(EmotionType dominante, EmotionType dominado) {
		return dominancia.getOrDefault(dominante, new HashMap<>()).getOrDefault(dominado, 1.0);
	}

	public Map<EmotionType, Map<EmotionType, Double>> getTablaCompleta() {
		return dominancia;
	}

	public void reset() {
		dominancia.clear();
		for (EmotionType t1 : EmotionType.values()) {
			Map<EmotionType, Double> mapaInterno = new HashMap<>();
			for (EmotionType t2 : EmotionType.values()) {
				if (t1 == t2) {
					mapaInterno.put(t2, 1.0);
				} else {
					mapaInterno.put(t2, 0.5 + rng.nextDouble());
				}
			}
			dominancia.put(t1, mapaInterno);
		}
	}

}
