package roguelike_emotions.vfx;

final class DamageClip extends Clip { final DamageEvent ev; DamageClip(DamageEvent ev){ super(0.45f); this.ev=ev; } }
final class HealClip   extends Clip { final HealEvent ev;   HealClip(HealEvent ev){   super(0.35f); this.ev=ev; } }
final class BuffClip   extends Clip { final BuffAppliedEvent ev; BuffClip(BuffAppliedEvent ev){ super(0.30f); this.ev=ev; } }
final class DebuffClip extends Clip { final DebuffAppliedEvent ev; DebuffClip(DebuffAppliedEvent ev){ super(0.30f); this.ev=ev; } }
final class ComboMaxClip extends Clip { final ComboMaxEvent ev; ComboMaxClip(ComboMaxEvent ev){ super(0.6f); this.ev=ev; } }
public final class ClipFactory {
    private ClipFactory(){}
    public static Clip from(VisEvent e){
        if (e instanceof DamageEvent d) return new DamageClip(d);
        if (e instanceof HealEvent h)   return new HealClip(h);
        if (e instanceof BuffAppliedEvent b) return new BuffClip(b);
        if (e instanceof DebuffAppliedEvent db) return new DebuffClip(db);
        if (e instanceof ComboMaxEvent cm) return new ComboMaxClip(cm);

        return null;
    }
}
