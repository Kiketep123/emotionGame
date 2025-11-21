package roguelike_emotions.vfx;

import com.badlogic.gdx.graphics.Color;

import roguelike_emotions.graphics.ActorView;
import roguelike_emotions.graphics.RenderContext;

/** Traductor de VisEvent -> cambios en vistas/overlay (UI-Observer). */
public final class VisualEvents {
    private VisualEvents(){}

    public static void apply(RenderContext ctx, VisEvent ev){
        if (ev instanceof DamageEvent d) onDamage(ctx, d);
        else if (ev instanceof HealEvent h) onHeal(ctx, h);
        else if (ev instanceof BuffAppliedEvent b) onBuff(ctx, b);
        else if (ev instanceof DebuffAppliedEvent db) onDebuff(ctx, db);
    }

    private static ActorView viewOf(RenderContext ctx, int id){ return ctx.views.get(id); }

    private static void onDamage(RenderContext ctx, DamageEvent ev){
        ActorView src = viewOf(ctx, ev.srcId());
        ActorView dst = viewOf(ctx, ev.dstId());
        if (dst != null){
            if (src != null){
                float dx = dst.x - src.x, dy = dst.y - src.y;
                float len = (float)Math.max(1e-3, Math.hypot(dx, dy));
                dst.vx += (dx/len) * 220f; dst.vy += (dy/len) * 80f;
            }
            dst.setHitFlash(0.18f) ;
            // Demo visual: el modelo real ya actualiza HP; si quieres reflejo inmediato:
            // dst.hp = Math.max(0, dst.hp - ev.amount());
            ctx.addText(dst.x, dst.y + 56, "-" + ev.amount(), colorTag(ev.tag(), Color.RED));
        }
        ctx.style.screenTint.set(colorTag(ev.tag(), new Color(0.15f,0f,0f,0.35f)));
    }

    private static void onHeal(RenderContext ctx, HealEvent ev){
        ActorView dst = viewOf(ctx, ev.dstId());
        if (dst != null){
            // dst.hp = Math.min(dst.maxHp, dst.hp + ev.amount());
            ctx.addText(dst.x, dst.y + 56, "+" + ev.amount(), Color.valueOf("7FFFB0"));
        }
        ctx.style.screenTint.set(0f,0.08f,0f,0.25f);
    }

    private static void onBuff(RenderContext ctx, BuffAppliedEvent ev){
        ActorView dst = viewOf(ctx, ev.dstId());
        if (dst != null) ctx.addText(dst.x, dst.y + 70, ev.tag()+" ↑", Color.valueOf("B0E0FF"));
    }

    private static void onDebuff(RenderContext ctx, DebuffAppliedEvent ev){
        ActorView dst = viewOf(ctx, ev.dstId());
        if (dst != null) ctx.addText(dst.x, dst.y + 70, ev.tag()+" ↓", Color.valueOf("FFB0B0"));

    }

    private static Color colorTag(String tag, Color fallback){
        if (tag == null) return fallback;
        String t = tag.toUpperCase();
        if (t.contains("FIRE")) return new Color(0.4f,0.1f,0f,0.35f);
        if (t.contains("POISON")) return new Color(0f,0.3f,0f,0.35f);
        if (t.contains("HEAL")) return new Color(0f,0.25f,0.05f,0.30f);
        if (t.contains("ICE"))  return new Color(0f,0.15f,0.3f,0.35f);
        return fallback;
    }
}
