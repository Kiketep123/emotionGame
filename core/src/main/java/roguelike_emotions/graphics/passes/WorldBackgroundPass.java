package roguelike_emotions.graphics.passes;

import com.badlogic.gdx.graphics.Color;
import roguelike_emotions.graphics.RenderContext;

public class WorldBackgroundPass implements RenderPass {
    @Override public void executeWorld(RenderContext ctx) {
        ctx.batch.setColor(new Color(0.08f,0.09f,0.10f,1f));
        ctx.batch.draw(ctx.whitePx, 0, 0, ctx.viewport.getWorldWidth(), ctx.viewport.getWorldHeight());
        ctx.batch.setColor(Color.WHITE);
    }
}
