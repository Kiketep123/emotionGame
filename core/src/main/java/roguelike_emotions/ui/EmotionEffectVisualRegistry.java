package roguelike_emotions.ui;

import java.util.HashMap;
import java.util.Map;

import roguelike_emotions.mainMechanics.EmotionEffect;

public class EmotionEffectVisualRegistry {

    private static final Map<EmotionEffect, EffectVisualData> visualDataMap = new HashMap<>();

    static {
        visualDataMap.put(EmotionEffect.FUEGO, new EffectVisualData(
                "Quemadura", "SPRITE_BURN", "#FF4500", "burn-effect"));
        visualDataMap.put(EmotionEffect.CURACION, new EffectVisualData(
                "Curación", "SPRITE_HEAL", "#00FF7F", "heal-effect"));
        visualDataMap.put(EmotionEffect.VENENO, new EffectVisualData(
                "Veneno", "SPRITE_POISON", "#8A2BE2", "poison-effect"));
        visualDataMap.put(EmotionEffect.REBOTE, new EffectVisualData(
                "Rebote", "SPRITE_REFLECT", "#FFD700", "reflect-effect"));
        visualDataMap.put(EmotionEffect.RALENTIZAR, new EffectVisualData(
                "Ralentización", "SPRITE_SLOW", "#1E90FF", "slow-effect"));
        visualDataMap.put(EmotionEffect.DEFENDER, new EffectVisualData(
                "Defensa", "SPRITE_DEFENSE", "#4682B4", "defense-effect"));
        visualDataMap.put(EmotionEffect.STUN, new EffectVisualData(
                "Aturdimiento", "SPRITE_STUN", "#FF1493", "stun-effect"));
        visualDataMap.put(EmotionEffect.REGENERACION, new EffectVisualData(
                "Regeneración", "SPRITE_REGEN", "#00CED1", "regen-effect"));
        visualDataMap.put(EmotionEffect.ENERGIZAR, new EffectVisualData(
                "Energía", "SPRITE_ENERGIZE", "#FFA500", "energize-effect"));
    }


    public static EffectVisualData getVisualData(EmotionEffect effect) {
        return visualDataMap.get(effect);
    }

}
