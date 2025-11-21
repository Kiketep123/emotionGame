package roguelike_emotions.vfx;
public record HealEvent(int srcId, int dstId, int amount, String tag) implements VisEvent {}