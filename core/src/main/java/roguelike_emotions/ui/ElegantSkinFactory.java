package roguelike_emotions.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import roguelike_emotions.ui.fonts.FontManager;

/**
 * Skin minimalista elegante — Paleta A2 Midnight Nebula
 * Autocontenida: no atlas/json. Usa fuentes de FontManager.
 */
public final class ElegantSkinFactory {

    private ElegantSkinFactory(){}

    // ===== Paleta A2 =====
    public static final Color PRIMARY_UP = Color.valueOf("4F8CF7");
    public static final Color PRIMARY_OVER = Color.valueOf("6DA8FF");
    public static final Color PRIMARY_DOWN = Color.valueOf("3A72D6");
    public static final Color PANEL_BG = Color.valueOf("0A0E14");
    public static final Color PANEL_INNER = Color.valueOf("121722");
    public static final Color BORDER_SOFT = Color.valueOf("20252E");
    public static final Color TEXT_MAIN = Color.valueOf("E6EDF7");
    public static final Color TEXT_MUTED = Color.valueOf("A9B4C0");
    public static final Color BTN_UP = Color.valueOf("191F2A");
    public static final Color BTN_OVER = Color.valueOf("252C39");
    public static final Color BTN_DOWN = Color.valueOf("141924");
    public static final Color BTN_DISABLED = Color.valueOf("0F1218");

    public static Skin create() {
        Skin s = new Skin();

        // ---------- Drawables ----------
        s.add("white", colored(Color.WHITE), Drawable.class); // NUEVO: Para Scene2D
        s.add("panel", panelDrawable(PANEL_BG, BORDER_SOFT), Drawable.class);
        s.add("panelInner", panelDrawable(PANEL_INNER, BORDER_SOFT), Drawable.class);
        s.add("btnUp", colored(BTN_UP), Drawable.class);
        s.add("btnOver", colored(BTN_OVER), Drawable.class);
        s.add("btnDown", colored(BTN_DOWN), Drawable.class);
        s.add("btnDisabled", colored(BTN_DISABLED), Drawable.class);
        s.add("primaryUp", colored(PRIMARY_UP), Drawable.class);
        s.add("primaryOver", colored(PRIMARY_OVER), Drawable.class);
        s.add("primaryDown", colored(PRIMARY_DOWN), Drawable.class);
        s.add("scrollBg", colored(Color.valueOf("0A0D12")), Drawable.class);

        // ---------- Fonts ----------
        BitmapFont titleFont = FontManager.title();
        BitmapFont h2Font = FontManager.h2();
        BitmapFont bodyFont = FontManager.body();

        // ---------- Label styles ----------
        s.add("default", new Label.LabelStyle(bodyFont, TEXT_MAIN));
        s.add("title", new Label.LabelStyle(titleFont, TEXT_MAIN));
        s.add("h2", new Label.LabelStyle(h2Font, TEXT_MAIN));
        s.add("body", new Label.LabelStyle(bodyFont, TEXT_MAIN));
        s.add("muted", new Label.LabelStyle(bodyFont, TEXT_MUTED));
        s.add("slot", new Label.LabelStyle(bodyFont, Color.valueOf("CFE0FF")));
        s.add("cardTitle", new Label.LabelStyle(h2Font, TEXT_MAIN));
        s.add("symbol", new Label.LabelStyle(h2Font, TEXT_MAIN));

        // ---------- Buttons ----------
        TextButton.TextButtonStyle secondary = new TextButton.TextButtonStyle();
        secondary.font = bodyFont;
        secondary.up = s.getDrawable("btnUp");
        secondary.over = s.getDrawable("btnOver");
        secondary.down = s.getDrawable("btnDown");
        secondary.disabled = s.getDrawable("btnDisabled");
        secondary.fontColor = TEXT_MAIN;
        secondary.disabledFontColor = TEXT_MUTED;
        s.add("secondary", secondary);

        TextButton.TextButtonStyle primary = new TextButton.TextButtonStyle();
        primary.font = bodyFont;
        primary.up = s.getDrawable("primaryUp");
        primary.over = s.getDrawable("primaryOver");
        primary.down = s.getDrawable("primaryDown");
        primary.disabled = s.getDrawable("btnDisabled");
        primary.fontColor = Color.WHITE;
        primary.disabledFontColor = TEXT_MUTED;
        s.add("primary", primary);

        // ---------- ScrollPane ----------
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = s.getDrawable("scrollBg");

        // CAMBIO: Añadir con ambos nombres para compatibilidad
        s.add("scroll", scrollStyle); // Para FusionCodex
        s.add("default", scrollStyle); // Para Scene2D genérico

        return s;
    }

    // ===== Helpers =====
    private static Drawable colored(Color c) {
        Pixmap pm = new Pixmap(2,2, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    /** Panel con borde suave (no mosaico). */
    private static Drawable panelDrawable(Color fill, Color border) {
        int w = 8, h = 8;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }
}
