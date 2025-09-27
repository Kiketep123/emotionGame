package roguelike_emotions.characters;

import java.util.List;
import java.util.Scanner;

import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.EmotionCombiner;

public class EmotionFusionMenu {

	public static void mostrarMenu(Player player) {
		List<EmotionInstance> emociones = player.getEmocionesActivas();

		if (emociones.size() < 2) {
			CombatLogger.get().log("No hay suficientes emociones para fusionar.");
			return;
		}

		try (Scanner sc = new Scanner(System.in)) {
			CombatLogger.get().log(" Emociones disponibles:");
			for (int i = 0; i < emociones.size(); i++) {
				EmotionInstance e = emociones.get(i);
				CombatLogger.get().log(i + ": " + e.getNombre() + " (" + e.getColor() + ")");
			}

			System.out.print("Selecciona el índice de la primera emoción: ");
			int idx1 = sc.nextInt();

			System.out.print("Selecciona el índice de la segunda emoción: ");
			int idx2 = sc.nextInt();

			if (idx1 == idx2 || idx1 < 0 || idx2 < 0 || idx1 >= emociones.size() || idx2 >= emociones.size()) {
				CombatLogger.get().log(" Selección inválida.");
				return;
			}

			EmotionInstance combinada = EmotionCombiner.combinar(emociones.get(idx1), emociones.get(idx2));
			CombatLogger.get().log("✨ Nueva emoción fusionada: " + combinada.getNombre() + " " + combinada.getColor());

			// Eliminar originales y añadir la nueva
			player.eliminarEmocion(emociones.get(idx1));
			// Ajustamos por si idx1 < idx2
			if (idx2 > idx1)
				idx2--;
			player.eliminarEmocion(emociones.get(idx2));
			player.añadirEmocion(combinada);
		}
	}
}
