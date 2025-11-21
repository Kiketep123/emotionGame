package roguelike_emotions.vfx;

import java.util.ArrayDeque;

public final class VisBus {
    private static final ArrayDeque<VisEvent> Q = new ArrayDeque<>();
    private VisBus(){}
    public static void post(VisEvent e){ if (e!=null) Q.add(e); }
    public static VisEvent poll(){ return Q.poll(); }
    public static boolean isEmpty(){ return Q.isEmpty(); }
}
