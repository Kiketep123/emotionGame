package roguelike_emotions.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roguelike_emotions.mainMechanics.EmotionInstance;

public class FusionRegistry {
    private static final Map<String, EmotionInstance> combinaciones = new HashMap<>();
    private static Map<String, EmotionInstance> multiFusions = new HashMap<>();

    public static EmotionInstance obtenerFusion(String id1, String id2) {
        String clave = generarClave(id1, id2);
        return combinaciones.get(clave);
    }

    public static void registrarFusion(String id1, String id2, EmotionInstance resultado) {
        combinaciones.put(generarClave(id1, id2), resultado);
    }

    private static String generarClave(String a, String b) {
        return a.compareTo(b) < 0 ? a + "+" + b : b + "+" + a;
    }
    public static int totalFusionesRegistradas() {
        return combinaciones.size();
    }
    
    public static void registrarFusionMultiple(String clave, List<EmotionInstance> inputs, EmotionInstance result) {
        multiFusions.put(clave, result);
    }
    public static EmotionInstance obtenerFusionPorClave(String clave) {
        return multiFusions.get(clave);
    }
}