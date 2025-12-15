package roguelike_emotions.ui.fonts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * FontManager centralizado con FreeType (TTF) para UI nítida.
 * IMPORTANTE: coloca tu TTF en assets/fonts/Inter-Regular.ttf
 * (o cambia la ruta aquí).
 */
public final class FontManager {

    private static BitmapFont title;
    private static BitmapFont h2;
    private static BitmapFont body;
    private static BitmapFont small;

    private FontManager() {}

    public static void init() {
        if (title != null) return; // idempotente

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"
        		+ ""));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // Sombra/borde sutil para legibilidad en fondo oscuro
        p.borderWidth = 1.2f;
        p.borderColor = new com.badlogic.gdx.graphics.Color(0,0,0,0.65f);
        p.shadowOffsetX = 1;
        p.shadowOffsetY = 1;
        p.shadowColor = new com.badlogic.gdx.graphics.Color(0,0,0,0.35f);
        p.color = new com.badlogic.gdx.graphics.Color(0.95f,0.97f,1f,1f);

        p.size = 46;
        title = gen.generateFont(p);

        p.size = 30;
        h2 = gen.generateFont(p);

        p.size = 22;
        body = gen.generateFont(p);

        p.size = 18;
        small = gen.generateFont(p);

        gen.dispose();
    }

    public static BitmapFont title() { return title; }
    public static BitmapFont h2() { return h2; }
    public static BitmapFont body() { return body; }
    public static BitmapFont small() { return small; }
}