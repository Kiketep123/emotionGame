package roguelike_emotions.graphics.passes;

import roguelike_emotions.graphics.RenderContext;

public class HudOverlayPass implements RenderPass {
    @Override public void executeOverlay(RenderContext ctx) {
        float w = ctx.viewport.getWorldWidth();
        ctx.batch.setColor(0,0,0,0.35f);
        ctx.batch.draw(ctx.whitePx, 0, 0, w, 64);
        ctx.batch.setColor(1,1,1,1);
        ctx.font.draw(ctx.batch, "1: Atacar   2: Defender   3: Usar emoción   ENTER: Confirmar   ←/→: Cambiar objetivo", 24, 40);

        String sel = ctx.selectedActionLabel != null ? ctx.selectedActionLabel : "-";
        int idx = (ctx.selectedEnemyViewId == null) ? -1 : (ctx.selectedEnemyViewId - 100);
        ctx.font.draw(ctx.batch, "Acción: " + sel + "   Objetivo: " + (idx < 0 ? "-" : idx), 24, 22);
    }
}
