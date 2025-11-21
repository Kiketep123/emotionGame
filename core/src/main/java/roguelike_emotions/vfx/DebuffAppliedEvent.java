package roguelike_emotions.vfx;

public record DebuffAppliedEvent(int dstId, String tag, int stacks, int turns) implements VisEvent {}
