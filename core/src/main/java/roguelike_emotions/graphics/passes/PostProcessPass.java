package roguelike_emotions.graphics.passes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import roguelike_emotions.graphics.RenderContext;

public class PostProcessPass implements RenderPass {
    private ShaderProgram shader; private Texture sceneTex;

    private void ensure(RenderContext ctx){
        if (shader==null){
            ShaderProgram.pedantic=false;
            String vs = Gdx.files.internal("shaders/post.vert").readString();
            String fs = Gdx.files.internal("shaders/post.frag").readString();
            shader = new ShaderProgram(vs, fs);
            if(!shader.isCompiled()) { Gdx.app.log("PostProcess","Shader error: "+shader.getLog()); shader=null; }
        }
        sceneTex = ctx.sceneFbo.getColorBufferTexture();
        sceneTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override public void executeOverlay(RenderContext ctx) {
        ensure(ctx);
        ctx.batch.setShader(shader);
        if (shader!=null) shader.setUniformf("u_tint", ctx.style.screenTint);
        float w=ctx.viewport.getWorldWidth(), h=ctx.viewport.getWorldHeight();
        ctx.batch.setColor(Color.WHITE);
        // ojo: el FBO está invertido en Y
        ctx.batch.draw(sceneTex, 0,0, w,h, 0,0, sceneTex.getWidth(), sceneTex.getHeight(), false, true);
        ctx.batch.setShader(null);
    }
}
