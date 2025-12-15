package roguelike_emotions.graphics;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.ui.IconRegistry;
import roguelike_emotions.visual.VisualStyle;

public class ActorView {

    // ============================
    // CAMPOS ORIGINALES (NO TOCAR)
    // ============================
    public final int id;
    public String name = "";
    public float x, y, targetX, targetY, vx, vy;
    public int hp = 1, maxHp = 1, shield = 0;
    public final List<EmotionInstance> emotions = new ArrayList<>();
    private float hitFlash = 0f;

    // HP suavizado (añadido sin romper API)
    private float hpSmooth = 1f;

    public ActorView(int id) {
        this.id = id;
    }

    public float getHitFlash() {
        return hitFlash;
    }

    public void setHitFlash(float hitFlash) {
        this.hitFlash = hitFlash;
    }

    public void syncFromPlayer(Player p, float tx, float ty) {
        this.name = "Player";
        this.hp = p.getHealth();
        this.maxHp = Math.max(maxHp, hp);

        this.targetX = tx;
        this.targetY = ty;

        emotions.clear();
        if (p.getEmocionesActivas() != null)
            emotions.addAll(p.getEmocionesActivas());
    }

    public void syncFromEnemy(Enemy e, float tx, float ty) {
        this.name = e.getNombre() != null ? e.getNombre() : "Enemy";

        this.hp = e.getHealth();
        this.maxHp = Math.max(maxHp, hp);

        this.targetX = tx;
        this.targetY = ty;

        emotions.clear();
        if (e.getEmocionesActivas() != null)
            emotions.addAll(e.getEmocionesActivas());
    }

    // ==================================
    // UPDATE PRO (mantiene tu arquitectura)
    // ==================================
    public void update(float dt) {

        // Movimiento hacia target — NO SE CAMBIAN TUS VARIABLES
        float spring = 10f;
        vx += (targetX - x) * spring * dt;
        vy += (targetY - y) * spring * dt;

        x += vx * dt;
        y += vy * dt;

        vx *= 0.70f;
        vy *= 0.70f;

        // HP suavizado
        float pct = (maxHp <= 0) ? 0f : (float) hp / maxHp;
        hpSmooth += (pct - hpSmooth) * Math.min(1f, 10f * dt);

        // Daño flash
        if (hitFlash > 0f)
            hitFlash = Math.max(0f, hitFlash - dt);
    }

    // ===============================
    // RENDER PRO (icons + glow + hp)
    // ===============================
    public void render(SpriteBatch b, BitmapFont font, Texture white, VisualStyle style) {

        // ==== 1. COLOR DEL CUERPO ====
        if (hitFlash > 0f)
            b.setColor(1f, 0.55f, 0.55f, 1f); // golpeado
        else
            b.setColor(Color.GOLD);

        float bodySize = 54f;
        b.draw(white, x - bodySize / 2f, y - bodySize / 2f, bodySize, bodySize);

        // ==== 2. BARRA DE VIDA PRO ====
        float barW = 90f;
        float barH = 10f;
        float hpW = barW * hpSmooth;

        // fondo
        b.setColor(0f, 0f, 0f, 0.55f);
        b.draw(white, x - barW / 2f, y + 38, barW, barH);

        // vida
        b.setColor(style.hpColor);
        b.draw(white, x - barW / 2f, y + 38, hpW, barH);

        // texto hp
        b.setColor(Color.WHITE);
        String t = hp + "/" + maxHp;
        GlyphLayout gl = new GlyphLayout(font, t);
        font.draw(b, t, x - gl.width / 2f, y + 38 + barH + 12f);

        // ==== 3. Nombre ====
        GlyphLayout gln = new GlyphLayout(font, name);
        font.draw(b, name, x - gln.width / 2f, y + 70);

        // ==== 4. EMOCIONES (icons) ====
        float cx = x - 36;
        float cy = y + 80;

        for (EmotionInstance emo : emotions) {

            TextureRegion icon = IconRegistry.emotionRegion(emo.getTipoBase());
            if (icon != null) {
                b.setColor(Color.WHITE);
                b.draw(icon, cx, cy, 24, 24);
            } else {
                // fallback color block
                b.setColor(Color.valueOf(emo.getColor()));
                b.draw(white, cx, cy, 20, 20);
            }

            cx += 28;
        }

        b.setColor(Color.WHITE); // reset color global
    }
}
