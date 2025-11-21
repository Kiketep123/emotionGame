package roguelike_emotions.effects;

import roguelike_emotions.characters.Player;

public abstract class AbstractTimedEffect implements TimedEffect {
    protected int remainingTurns;

    public AbstractTimedEffect(int duration) {
        this.remainingTurns = duration;
    }

    @Override
    public boolean reducirDuracion() {
        remainingTurns--;
        return remainingTurns <= 0;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    @Override
    public void aplicar(Player player) {
        // Por defecto no hace nada cada turno; las subclases pueden sobrescribir esto si lo necesitan
    }

}