package roguelike_emotions.cfg;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roguelike_emotions.mainMechanics.EmotionEffect;

public class EffectVisualLoader {

    private static final Map<EmotionEffect, EffectVisualConfig> visualMap = new HashMap<>();

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = Gdx.files.internal("data/effects/visual_effects.json").read();

            if (inputStream == null) {
                throw new RuntimeException("visual_effects.json no encontrado en resources");
            }

            List<EffectVisualConfig> visualConfigs = mapper.readValue(
                    inputStream,
                    new TypeReference<List<EffectVisualConfig>>() {}
            );

            for (EffectVisualConfig config : visualConfigs) {
                visualMap.put(config.getEffect(), config);
            }

        } catch (Exception e) {
            System.err.println("❌ Error cargando effect_visuals.json:");
            e.printStackTrace();
        }
    }

    public static EffectVisualConfig getVisual(EmotionEffect effect) {
        return visualMap.get(effect);
    }

    public static Map<EmotionEffect, EffectVisualConfig> getAllVisuals() {
        return visualMap;
    }
}
