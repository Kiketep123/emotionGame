// core/src/main/java/roguelike_emotions/graphics/passes/TurnTimelinePass.java
package roguelike_emotions.graphics.passes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.ui.turns.TurnToken;

import java.util.List;

public class TurnTimelinePass implements RenderPass {
    // estilo
    private static final float BAR_H = 70f;
    private static final float PILL_H = 34f;
    private static final float PILL_PAD_X = 14f;
    private static final float GAP = 10f;
    private static final float SHADOW_A = 0.25f;

    private float scrollT = 0f; // autoscroll sutil si no cabe
    private final GlyphLayout layout = new GlyphLayout(); // reutilizable (evita allocs por frame)

    @Override public void executeOverlay(RenderContext ctx) {
        if (ctx.turnQueue == null) return;
        List<TurnToken> tokens = ctx.turnQueue.tokens();
        if (tokens.isEmpty()) return;

        final float W = ctx.viewport.getWorldWidth();
        final float H = ctx.viewport.getWorldHeight();
        final float yBar = H - BAR_H;

        // barra de fondo
        ctx.batch.setColor(0,0,0,0.38f);
        ctx.batch.draw(ctx.whitePx, 0, yBar, W, BAR_H);
        ctx.batch.setColor(1,1,1,1);

        // medir ancho requerido por cada pill mediante GlyphLayout
        float total = 0f;
        float[] widths = new float[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            String label = tokens.get(i).isPlayer() ? "Tú" : tokens.get(i).label();
            layout.setText(ctx.font, label);
            float tw = layout.width;                         // ancho real del texto
            float w  = Math.max(72f, tw + PILL_PAD_X * 2f);  // pill = texto + padding
            widths[i] = w;
            total += w + (i > 0 ? GAP : 0f);
        }

        // compactación y scroll
        float scale = total > (W - 40f) ? (W - 40f) / total : 1f;
        float usable = Math.min(total, W - 40f);
        float xStart = (W - usable * scale) * 0.5f;
        if (scale < 1f) scrollT += com.badlogic.gdx.Gdx.graphics.getDeltaTime() * 12f; else scrollT = 0f;

        float x = xStart - (scale < 1f ? (float) Math.sin(scrollT) * 12f : 0f);

        for (int i = 0; i < tokens.size(); i++) {
            TurnToken t = tokens.get(i);
            float w = widths[i] * scale;
            float cx = x + w * 0.5f;
            float cy = yBar + BAR_H * 0.5f + 4f;

            boolean isCurrent = (i == ctx.turnQueue.cursor());

            // sombra
            ctx.batch.setColor(0, 0, 0, SHADOW_A);
            ctx.batch.draw(ctx.whitePx, cx - w / 2f, cy - PILL_H / 2f - 2f, w, PILL_H);

            // pill
            if (!t.alive()) ctx.batch.setColor(0.42f, 0.42f, 0.42f, 1f);
            else if (t.isPlayer()) ctx.batch.setColor(0.20f, 0.65f, 0.95f, 1f);
            else ctx.batch.setColor(0.88f, 0.88f, 0.88f, 1f);
            ctx.batch.draw(ctx.whitePx, cx - w / 2f, cy - PILL_H / 2f, w, PILL_H);

            // borde + pointer para el actual
            if (isCurrent) {
                ctx.batch.setColor(1f, 0.95f, 0.35f, 1f);
                ctx.batch.draw(ctx.whitePx, cx - w / 2f, cy - PILL_H / 2f, w, 2f);
                ctx.batch.draw(ctx.whitePx, cx - w / 2f, cy + PILL_H / 2f - 2f, w, 2f);
                ctx.batch.draw(ctx.whitePx, cx - w / 2f, cy - PILL_H / 2f, 2f, PILL_H);
                ctx.batch.draw(ctx.whitePx, cx + w / 2f - 2f, cy - PILL_H / 2f, 2f, PILL_H);
                float py = cy + PILL_H / 2f + 6f;
                ctx.batch.draw(ctx.whitePx, cx - 2, py, 4, 10);
            }

            // etiqueta centrada con GlyphLayout
            String label = t.isPlayer() ? "Tú" : t.label();
            layout.setText(ctx.font, label);
            ctx.batch.setColor(Color.BLACK);
            ctx.font.draw(ctx.batch, layout, cx - (layout.width / 2f), cy + (layout.height / 2f) - 2f);

            x += w + GAP;
        }

        ctx.batch.setColor(Color.WHITE);
    }
}
