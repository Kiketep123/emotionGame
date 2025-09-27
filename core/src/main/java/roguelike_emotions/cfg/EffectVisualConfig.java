package roguelike_emotions.cfg;

import roguelike_emotions.mainMechanics.EmotionEffect;

public class EffectVisualConfig {
    private EmotionEffect effect;
    private String displayName;
    private String spriteId;
    private String uiColorHex;
    private String cssClass;

    // Getters y setters
    public EmotionEffect getEffect() { return effect; }
    public void setEffect(EmotionEffect effect) { this.effect = effect; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getSpriteId() { return spriteId; }
    public void setSpriteId(String spriteId) { this.spriteId = spriteId; }

    public String getUiColorHex() { return uiColorHex; }
    public void setUiColorHex(String uiColorHex) { this.uiColorHex = uiColorHex; }

    public String getCssClass() { return cssClass; }
    public void setCssClass(String cssClass) { this.cssClass = cssClass; }
}
