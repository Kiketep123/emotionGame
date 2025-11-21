package roguelike_emotions.ui.turns;

/** VO de la UI: un actor en la cola de turnos. */
public record TurnToken(int viewId, String label, boolean isPlayer, boolean alive) { }
