package roguelike_emotions.graphics.passes;

import com.badlogic.gdx.graphics.Color;
import roguelike_emotions.graphics.ActorView;
import roguelike_emotions.graphics.RenderContext;

public class VfxPass implements RenderPass {
    @Override public void executeWorld(RenderContext ctx) {
        // ejemplo: resaltar en rojo si el actor está bajo de vida
        for (ActorView v : ctx.views.values()){
            if (v.maxHp<=0) continue;
            float pct = 1f - (v.hp/(float)v.maxHp);
            if (pct>0.6f){
                float a=(pct-0.6f)*0.6f;
                ctx.batch.setColor(1,0,0,a);
                ctx.batch.draw(ctx.whitePx, v.x-40, v.y-40, 80, 80);
            }
        }
        ctx.batch.setColor(Color.WHITE);
    }
}
