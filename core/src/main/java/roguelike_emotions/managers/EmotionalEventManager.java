package roguelike_emotions.managers;

import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.SentientEmotion;
import roguelike_emotions.utils.CombatLogger;

/**
 * 🎭 Sistema de eventos emocionales narrativos
 */
public class EmotionalEventManager {

	/**
	 * 🆕 Fuerza el despertar de una emoción por evento narrativo
	 */
	public static void forceAwakening(Player player, String emotionName, String reason) {
		EmotionInstance emotion = player.getEmotionByName(emotionName);

		if (emotion != null && !(emotion instanceof SentientEmotion)) {
			SentientEmotion sentient = SentientEmotion.fromEmotionInstance(emotion);
			sentient.setLoyalty(80); // Alta lealtad por evento especial

			player.replaceEmotion(emotion, sentient);

			CombatLogger.get().log("");
			CombatLogger.get().log("═══════════════════════════════════════");
			CombatLogger.get().log("⚡ DESPERTAR FORZADO ⚡");
			CombatLogger.get().log("");
			CombatLogger.get().log(reason);
			CombatLogger.get().log("");
			CombatLogger.get().log(sentient.getNombre() + " ha sido FORZADA a despertar!");
			CombatLogger.get().log("Personalidad: " + sentient.getPersonality().name());
			CombatLogger.get().log("═══════════════════════════════════════");
			CombatLogger.get().log("");
		}
	}

	/**
	 * 🆕 Evento: Traición
	 */
	public static void triggerBetrayalEvent(Player player) {
		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			if (emotion.getTipoBase().name().contains("IRA")) {
				forceAwakening(player, emotion.getNombre(), "\"La traición que sufriste fue demasiado...\"");
				break;
			}
		}
	}

	/**
	 * 🆕 Evento: Pérdida
	 */
	public static void triggerLossEvent(Player player) {
		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			if (emotion.getTipoBase().name().contains("TRISTEZA")) {
				forceAwakening(player, emotion.getNombre(), "\"El dolor de la pérdida es demasiado profundo...\"");
				break;
			}
		}
	}

	/**
	 * 🆕 Evento: Victoria épica
	 */
	public static void triggerEpicVictory(Player player) {
		// XP masiva para todas las emociones
		CombatLogger.get().log("🏆 ¡VICTORIA ÉPICA! +200 XP para todas las emociones");

		for (EmotionInstance emotion : player.getEmocionesActivas()) {
			emotion.gainExperience(200);
		}
	}
}
