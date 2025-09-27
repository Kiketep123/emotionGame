package roguelike_emotions.effects;

import roguelike_emotions.characters.Player;

public interface TimedEffect {
    void aplicar(Player player);
    boolean reducirDuracion(); // devuelve true si expiró
    String getNombre();        // para logs
}