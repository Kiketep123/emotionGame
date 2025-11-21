// === FILE: core/src/main/java/roguelike_emotions/graphics/RenderGraph.java
package roguelike_emotions.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import roguelike_emotions.graphics.passes.RenderPass;
import java.util.*;

public class RenderGraph {
    private final List<RenderPass> passes = new ArrayList<>();
    public RenderGraph add(RenderPass p){ passes.add(p); return this; }
    public void render(RenderContext ctx){
        // mundo → FBO
        ctx.sceneFbo.begin();
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ctx.batch.setProjectionMatrix(ctx.camera.combined);
        ctx.batch.begin();
        for (RenderPass p : passes) p.executeWorld(ctx);
        ctx.batch.end();
        ctx.sceneFbo.end();

        // overlay (post-pro + HUD)
        ctx.batch.begin();
        for (RenderPass p : passes) p.executeOverlay(ctx);
        ctx.batch.end();
    }
}
