package roguelike_emotions.graphics.passes;

import roguelike_emotions.graphics.ActorView;
import roguelike_emotions.graphics.RenderContext;

public class EntityPass implements RenderPass {
    @Override public void executeWorld(RenderContext ctx) {
        for (ActorView v : ctx.views.values()) {
            v.render(ctx.batch, ctx.font, ctx.whitePx, ctx.style);
            if (ctx.selectedEnemyViewId != null && v.id == ctx.selectedEnemyViewId) {
                ctx.batch.setColor(1,1,0,1);
                ctx.batch.draw(ctx.whitePx, v.x-30, v.y-30, 60, 2);
                ctx.batch.draw(ctx.whitePx, v.x-30, v.y+28, 60, 2);
                ctx.batch.draw(ctx.whitePx, v.x-30, v.y-30, 2, 60);
                ctx.batch.draw(ctx.whitePx, v.x+28, v.y-30, 2, 60);
                ctx.batch.setColor(1,1,1,1);
            }
        }
    }
}
