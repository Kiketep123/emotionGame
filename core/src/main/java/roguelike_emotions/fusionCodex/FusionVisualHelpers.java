package roguelike_emotions.fusionCodex;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class FusionVisualHelpers {
    
    public static Drawable makeGradientPanel() {
        int w = 100, h = 100;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        for (int y = 0; y < h; y++) {
            float t = y / (float) h;
            float r = 0.08f + (0.02f * t);
            float g = 0.10f + (0.02f * t);
            float b = 0.14f + (0.03f * t);
            pm.setColor(r, g, b, 0.98f);
            pm.drawLine(0, y, w - 1, y);
        }
        
        Color borderColor = new Color(0.25f, 0.35f, 0.50f, 0.6f);
        pm.setColor(borderColor);
        pm.drawRectangle(0, 0, w, h);
        
        pm.setColor(0.3f, 0.45f, 0.65f, 0.15f);
        pm.drawLine(1, 1, w - 2, 1);
        pm.drawLine(1, 2, w - 2, 2);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Drawable makeInnerPanelGradient() {
        int w = 100, h = 100;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        for (int y = 0; y < h; y++) {
            float t = y / (float) h;
            float r = 0.04f + (0.01f * t);
            float g = 0.05f + (0.01f * t);
            float b = 0.09f + (0.02f * t);
            pm.setColor(r, g, b, 0.99f);
            pm.drawLine(0, y, w - 1, y);
        }
        
        pm.setColor(0.18f, 0.22f, 0.32f, 0.5f);
        pm.drawRectangle(0, 0, w, h);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Drawable makeSlotGradientBg() {
        int w = 80, h = 80;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        int centerX = w / 2;
        int centerY = h / 2;
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float dx = (x - centerX) / (float) w;
                float dy = (y - centerY) / (float) h;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                
                float r = 0.05f + (dist * 0.03f);
                float g = 0.06f + (dist * 0.03f);
                float b = 0.10f + (dist * 0.04f);
                
                pm.setColor(r, g, b, 0.98f);
                pm.drawPixel(x, y);
            }
        }
        
        Color borderGlow = new Color(0.35f, 0.50f, 0.75f, 0.4f);
        pm.setColor(borderGlow);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Drawable makeIconCircleBg() {
        int size = 70;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        int center = size / 2;
        int radius = 30;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int dx = x - center;
                int dy = y - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (dist <= radius) {
                    float t = dist / radius;
                    float r = 0.08f + (t * 0.04f);
                    float g = 0.10f + (t * 0.05f);
                    float b = 0.16f + (t * 0.06f);
                    pm.setColor(r, g, b, 1f);
                    pm.drawPixel(x, y);
                }
            }
        }
        
        pm.setColor(0.45f, 0.60f, 0.85f, 0.7f);
        pm.drawCircle(center, center, radius);
        pm.drawCircle(center, center, radius - 1);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Drawable makePreviewIconBg() {
        int size = 110;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        int center = size / 2;
        int radius = 50;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int dx = x - center;
                int dy = y - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (dist <= radius) {
                    float t = dist / radius;
                    float r = 0.06f + (t * 0.03f);
                    float g = 0.08f + (t * 0.04f);
                    float b = 0.14f + (t * 0.06f);
                    pm.setColor(r, g, b, 1f);
                    pm.drawPixel(x, y);
                }
            }
        }
        
        pm.setColor(0.50f, 0.70f, 0.95f, 0.5f);
        pm.drawCircle(center, center, radius);
        pm.setColor(0.40f, 0.60f, 0.85f, 0.8f);
        pm.drawCircle(center, center, radius - 1);
        pm.drawCircle(center, center, radius - 2);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Table makeGradientDivider() {
        int w = 200, h = 2;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        for (int x = 0; x < w; x++) {
            float t = x / (float) w;
            float intensity = (float) Math.sin(t * Math.PI);
            float alpha = intensity * 0.6f;
            pm.setColor(0.45f, 0.65f, 0.95f, alpha);
            pm.drawLine(x, 0, x, h - 1);
        }
        
        Texture t = new Texture(pm);
        pm.dispose();
        
        Image line = new Image(t);
        Table divider = new Table();
        divider.add(line).height(2f).expandX().fillX();
        return divider;
    }

    public static Drawable makeSlotWithEmotionGlow(Color emotionColor) {
        int w = 80, h = 80;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        int centerX = w / 2;
        int centerY = h / 2;
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float dx = (x - centerX) / (float) w;
                float dy = (y - centerY) / (float) h;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                
                float r = 0.05f + (dist * 0.03f) + (emotionColor.r * 0.10f);
                float g = 0.06f + (dist * 0.03f) + (emotionColor.g * 0.10f);
                float b = 0.10f + (dist * 0.04f) + (emotionColor.b * 0.15f);
                
                pm.setColor(r, g, b, 0.98f);
                pm.drawPixel(x, y);
            }
        }
        
        pm.setColor(emotionColor.r, emotionColor.g, emotionColor.b, 0.7f);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Drawable makeEffectRowBg() {
        int w = 100, h = 30;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        for (int y = 0; y < h; y++) {
            float t = y / (float) h;
            float val = 0.06f + (t * 0.02f);
            pm.setColor(val, val + 0.01f, val + 0.03f, 0.7f);
            pm.drawLine(0, y, w - 1, y);
        }
        
        pm.setColor(0.35f, 0.55f, 0.85f, 0.6f);
        pm.fillRectangle(0, 0, 2, h);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }

    public static Drawable makeDialogBg() {
        int w = 50, h = 50;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        
        for (int y = 0; y < h; y++) {
            float t = y / (float) h;
            float val = 0.08f + (t * 0.04f);
            pm.setColor(val, val + 0.01f, val + 0.03f, 0.98f);
            pm.drawLine(0, y, w - 1, y);
        }
        
        pm.setColor(0.4f, 0.5f, 0.7f, 0.8f);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }
    
    public static Drawable makeTooltipBg() {
        int w = 10, h = 10;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(0.05f, 0.06f, 0.08f, 0.95f);
        pm.fill();
        pm.setColor(0.4f, 0.55f, 0.75f, 0.9f);
        pm.drawRectangle(0, 0, w, h);
        pm.setColor(0.5f, 0.65f, 0.85f, 0.6f);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(t);
    }
}
