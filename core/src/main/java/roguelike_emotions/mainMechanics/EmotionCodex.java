package roguelike_emotions.mainMechanics;

import java.util.ArrayList;
import java.util.List;
import roguelike_emotions.utils.CombatLogger;

/**
 * ✅ ACTUALIZADO: Ahora soporta emociones sentientes
 */
public class EmotionCodex {

	private final List<EmotionInstance> codexEntries = new ArrayList<>();
	private final EmotionDominanceMatrix dominanceMatrix;

	public EmotionCodex(EmotionDominanceMatrix dominanceMatrix) {
		this.dominanceMatrix = dominanceMatrix;
	}

	/**
	 * ✅ MEJORADO: Registra emoción y detecta si es Sentient
	 */
	public void registrar(EmotionInstance e) {
		if (codexEntries.stream().noneMatch(x -> x.getId().equals(e.getId()))) {
			codexEntries.add(e);

			// ✅ NUEVO: Log especial para emociones sentientes
			if (e instanceof SentientEmotion) {
				SentientEmotion sentient = (SentientEmotion) e;
				CombatLogger.get().log(String.format("🧠 Nueva emoción CONSCIENTE descubierta: %s [%s]", e.getNombre(),
						sentient.getPersonality().name()));
			} else {
				CombatLogger.get().log(String.format("📔 Nueva emoción registrada: %s", e.getNombre()));
			}
		}
	}

	/**
	 * Devuelve todas las emociones registradas en el códice.
	 */
	public List<EmotionInstance> getEntries() {
		return new ArrayList<>(codexEntries);
	}

	/**
	 * ✅ NUEVO: Devuelve solo emociones sentientes descubiertas
	 */
	public List<SentientEmotion> getSentientEntries() {
		List<SentientEmotion> sentients = new ArrayList<>();
		for (EmotionInstance e : codexEntries) {
			if (e instanceof SentientEmotion) {
				sentients.add((SentientEmotion) e);
			}
		}
		return sentients;
	}

	/**
	 * ✅ NUEVO: Contador de emociones conscientes
	 */
	public int getSentientCount() {
		return (int) codexEntries.stream().filter(e -> e instanceof SentientEmotion).count();
	}

	/**
	 * ✅ MEJORADO: Muestra emociones con detalles de consciencia
	 */
	public void mostrarCodex() {
		CombatLogger.get().log("📔 CÓDICE DE EMOCIONES FUSIONADAS");

		int normalCount = 0;
		int sentientCount = 0;

		for (int i = 0; i < codexEntries.size(); i++) {
			EmotionInstance e = codexEntries.get(i);

			if (e instanceof SentientEmotion) {
				SentientEmotion sentient = (SentientEmotion) e;
				CombatLogger.get()
						.log(String.format("🧠 %d. %s %s - %s [%s] L:%d H:%d E:%d", i + 1, e.getSimbolo(),
								e.getNombre(), e.getColor(), sentient.getPersonality().name(), sentient.getLoyalty(),
								sentient.getHunger(), sentient.getEvolution()));
				sentientCount++;
			} else {
				CombatLogger.get()
						.log(String.format("   %d. %s %s - %s", i + 1, e.getSimbolo(), e.getNombre(), e.getColor()));
				normalCount++;
			}
		}

		if (codexEntries.isEmpty()) {
			CombatLogger.get().log("   (aún no has fusionado ninguna emoción)");
		} else {
			CombatLogger.get().log(String.format("\n📊 Total: %d emociones (%d normales, %d conscientes)",
					codexEntries.size(), normalCount, sentientCount));
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
						CombatLogger.get().log("  " + dominante + " domina a " + dominado + " (" + peso + ")");
					} else if (peso < 0.9) {
						CombatLogger.get().log("  " + dominante + " es dominado por " + dominado + "(" + peso + ")");
					}
				}
			}
		}
	}

	public void clear() {
		codexEntries.clear();
	}
}
