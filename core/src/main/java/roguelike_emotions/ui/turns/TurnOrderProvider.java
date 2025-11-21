package roguelike_emotions.ui.turns;

import java.util.List;

/** Proveedor del orden de turnos para pintar (no ejecuta lógica). */
public interface TurnOrderProvider {
    List<TurnToken> snapshot();
}
