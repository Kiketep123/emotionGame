package roguelike_emotions.ui.components;


import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.ui.actions.ActionDescriptor;
import roguelike_emotions.ui.actions.ActionTheme;

public final class ActionStrip {
 private final GlyphLayout layout = new GlyphLayout();
 private final ActionTheme theme;
 public ActionStrip(ActionTheme theme){ this.theme = (theme!=null? theme : new ActionTheme(40,12,16,120)); }

 public void render(RenderContext ctx, List<ActionDescriptor> actions, int selectedIndex){
     float w = ctx.viewport.getWorldWidth();
     float y = 0f, pad = theme.padding(), pillH = theme.pillHeight(), gap = theme.gap();
     float x = pad;

     // banda de fondo
     ctx.batch.setColor(0,0,0,0.35f);
     ctx.batch.draw(ctx.whitePx, 0, y, w, pillH + pad*2f);
     ctx.batch.setColor(Color.WHITE);

     for (int i=0;i<actions.size();i++){
         var a = actions.get(i);
         layout.setText(ctx.font, a.label());
         float pillW = Math.max(theme.minPillWidth(), layout.width + 26f);

         // estado hover/selección: resalta el seleccionado (coincide con tu input index)
         boolean selected = (i == selectedIndex);

         ctx.batch.setColor(selected ? 1f : 0.88f, selected ? 1f : 0.88f, selected ? 1f : 0.88f, 1f);
         ctx.batch.draw(ctx.whitePx, x, y+pad, pillW, pillH);

         ctx.batch.setColor(0,0,0,1f);
         ctx.font.draw(ctx.batch, a.label(), x+12f, y+pad + pillH*0.65f);

         x += pillW + gap;
     }
     ctx.batch.setColor(Color.WHITE);
 }
}
