package roguelike_emotions.cfg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roguelike_emotions.mainMechanics.EmotionEffect;

public class EffectConfigLoader {
    private static final Map<EmotionEffect, List<EffectConfig>> CONFIGS = new EnumMap<>(EmotionEffect.class);

    static {
        try (InputStream in = Gdx.files.internal("data/effects/effects.json").read()) {
            List<EffectConfig> list = new ObjectMapper().readValue(in, new TypeReference<List<EffectConfig>>() {});
            for (EffectConfig cfg : list) {
                EmotionEffect efecto = EmotionEffect.valueOf(cfg.effect);
                CONFIGS.computeIfAbsent(efecto, k -> new ArrayList<>()).add(cfg);
            }
        } catch (Exception e) {
            throw new RuntimeException("No pude cargar effects.json", e);
        }
    }

    public static List<EffectConfig> getConfigs(EmotionEffect e) {
        return CONFIGS.getOrDefault(e, List.of());
    }
}
