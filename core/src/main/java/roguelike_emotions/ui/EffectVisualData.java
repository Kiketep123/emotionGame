package roguelike_emotions.ui;

public class EffectVisualData {
    private final String displayName;
    private final String spriteId;
    private final String uiColorHex;
    private final String cssClass;

    public EffectVisualData(String displayName, String spriteId, String uiColorHex, String cssClass) {
        this.displayName = displayName;
        this.spriteId = spriteId;
        this.uiColorHex = uiColorHex;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSpriteId() {
        return spriteId;
    }

    public String getUiColorHex() {
        return uiColorHex;
    }

    public String getCssClass() {
        return cssClass;
    }
}
