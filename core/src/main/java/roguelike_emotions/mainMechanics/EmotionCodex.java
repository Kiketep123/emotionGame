package roguelike_emotions.mainMechanics;

import java.util.ArrayList;
import java.util.List;

import roguelike_emotions.utils.CombatLogger;

public class EmotionCodex {
	private final List<EmotionInstance> codexEntries = new ArrayList<>();
	private final EmotionDominanceMatrix dominanceMatrix;

	public EmotionCodex(EmotionDominanceMatrix dominanceMatrix) {
		this.dominanceMatrix = dominanceMatrix;
	}

	/**
	 * Registra una emoción fusionada si no existía ya.
	 */
	public void registrar(EmotionInstance e) {
		if (codexEntries.stream().noneMatch(x -> x.getId().equals(e.getId()))) {
			codexEntries.add(e);
		}
	}

	/**
	 * Devuelve todas las emociones registradas en el códice.
	 */
	public List<EmotionInstance> getEntries() {
		return new ArrayList<>(codexEntries);
	}

	/**
	 * Muestra todas las emociones fusionadas registradas.
	 */
	public void mostrarCodex() {
		CombatLogger.get().log("📔 CÓDICE DE EMOCIONES FUSIONADAS");
		for (int i = 0; i < codexEntries.size(); i++) {
			EmotionInstance e = codexEntries.get(i);
			CombatLogger.get().log(e.getSimbolo()+"."+e.getNombre()+" - " + e.getColor());
		}
		if (codexEntries.isEmpty()) {
			CombatLogger.get().log("   (aún no has fusionado ninguna emoción)");
		}

		CombatLogger.get().log("\n📊 MATRIZ DE DOMINANCIA EMOCIONAL:");
		mostrarDominancia();
	}

	/**
	 * Imprime la tabla de dominancia emocional.
	 */
	private void mostrarDominancia() {
		for (EmotionType dominante : EmotionType.values()) {
			for (EmotionType dominado : EmotionType.values()) {
				if (dominante != dominado) {
					double peso = dominanceMatrix.getPeso(dominante, dominado);
					if (peso > 1.1) {
						CombatLogger.get().log("   "+dominante+" domina a "+dominado+" ("+peso+")");
					} else if (peso < 0.9) {
						CombatLogger.get().log("   "+dominante+" es dominado por "+dominado+"("+peso+")");
					}
				}
			}
		}
	}
	public void clear() {
		codexEntries.clear();
	}
}
