package roguelike_emotions.vfx;

public record BuffAppliedEvent(int dstId, String tag, int stacks, int turns) implements VisEvent {}