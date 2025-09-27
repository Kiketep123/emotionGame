package roguelike_emotions.mainMechanics;

import roguelike_emotions.characters.Attack;
import roguelike_emotions.characters.Player;

public interface Emotion {
	String getNombre(); // "Ira ardiente"

	EmotionType getTipoBase(); // IRA, MIEDO, etc.

	EmotionEffect getEfecto(); // FUEGO, VENENO, etc.

	String getColor(); // Visualización

	void aplicarAlJugador(Player jugador);

	void modificarAtaque(Attack ataque);
}