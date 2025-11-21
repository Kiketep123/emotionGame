package roguelike_emotions.vfx;

public record DamageEvent(int srcId, int dstId, int amount, String tag) implements VisEvent {}