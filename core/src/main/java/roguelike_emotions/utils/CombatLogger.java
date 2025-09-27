package roguelike_emotions.utils;

import java.util.LinkedList;
import java.util.List;

public class CombatLogger {

    private static final int MAX_LOGS = 50;
    private static CombatLogger instance;

    private final LinkedList<String> logs = new LinkedList<>();

    private CombatLogger() {}

    public static CombatLogger get() {
        if (instance == null) {
            instance = new CombatLogger();
        }
        return instance;
    }

    public void log(String texto) {
        if (logs.size() >= MAX_LOGS) {
            logs.removeFirst();
        }
        logs.add(texto);
        System.out.println("[LOG] " + texto);
    }

    public List<String> getLogs() {
        return new LinkedList<>(logs); // copia segura para evitar modificaciones externas
    }

    public List<String> extractAndClear() {
        List<String> copia = new LinkedList<>(logs);
        logs.clear();
        return copia;
    }

    public void clear() {
        logs.clear();
    }
}
