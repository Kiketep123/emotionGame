package roguelike_emotions.graphics.passes;

import roguelike_emotions.graphics.RenderContext;

public interface RenderPass {
    default void executeWorld(RenderContext ctx) {}
    default void executeOverlay(RenderContext ctx) {}
}
