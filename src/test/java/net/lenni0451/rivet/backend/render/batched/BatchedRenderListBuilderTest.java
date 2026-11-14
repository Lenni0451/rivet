package net.lenni0451.rivet.backend.render.batched;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.deferred.DeferredRenderer;
import net.lenni0451.rivet.backend.render.deferred.ModifierCommand;
import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.math.Aabb;
import net.lenni0451.rivet.math.Transform2D;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class BatchedRenderListBuilderTest {

    private static BatchedRenderListBuilder.Layers build(final Consumer<DeferredRenderer> renderer) {
        return build(renderer, BatchedRenderListBuilder.CustomModifierHandler.UNSUPPORTED);
    }

    private static BatchedRenderListBuilder.Layers build(final Consumer<DeferredRenderer> renderer, final BatchedRenderListBuilder.CustomModifierHandler handler) {
        DeferredRenderer deferredRenderer = new DeferredRenderer();
        renderer.accept(deferredRenderer);
        return BatchedRenderListBuilder.build(deferredRenderer.complete(), handler);
    }

    @Test
    void nonOverlappingCommandsShareALayer() {
        BatchedRenderListBuilder.Layers layers = build(r -> {
            r.fillRect(0, 0, 10, 10, Color.RED);
            r.fillRect(20, 0, 10, 10, Color.GREEN);
            r.fillRect(40, 0, 10, 10, Color.BLUE);
        });
        assertEquals(1, layers.size());
        assertEquals(3, layers.get(0).commands().size());
    }

    @Test
    void overlappingCommandsKeepTheirOrder() {
        BatchedRenderListBuilder.Layers layers = build(r -> {
            r.fillRect(0, 0, 10, 10, Color.RED);
            r.fillRect(5, 5, 10, 10, Color.GREEN);
        });
        assertEquals(2, layers.size());
        assertEquals(Color.RED, ((RenderCommand.FillRect) layers.get(0).commands().get(0).command()).color());
        assertEquals(Color.GREEN, ((RenderCommand.FillRect) layers.get(1).commands().get(0).command()).color());
    }

    @Test
    void commandsAreBatchedIntoTheEarliestPossibleLayer() {
        BatchedRenderListBuilder.Layers layers = build(r -> {
            r.fillRect(0, 0, 10, 10, Color.RED); // Layer 0
            r.fillRect(5, 5, 10, 10, Color.GREEN); // Layer 1 (overlaps red)
            r.fillRect(100, 100, 10, 10, Color.BLUE); // Layer 0 (overlaps nothing)
            r.fillRect(102, 102, 10, 10, Color.WHITE); // Layer 1 (overlaps blue)
        });
        assertEquals(2, layers.size());
        assertEquals(2, layers.get(0).commands().size());
        assertEquals(2, layers.get(1).commands().size());
    }

    @Test
    void transformationsAreApplied() {
        BatchedRenderListBuilder.Layers layers = build(r -> r.translate(10, 20, () -> r.scale(2, () -> r.fillRect(1, 1, 5, 5, Color.RED))));
        Aabb bounds = layers.get(0).commands().get(0).bounds();
        assertEquals(new Aabb(12, 22, 22, 32), bounds);
        Transform2D matrix = layers.get(0).commands().get(0).matrix();
        assertEquals(12, matrix.transformX(1, 1));
        assertEquals(22, matrix.transformY(1, 1));
    }

    @Test
    void differentScissorsAreSeparated() {
        BatchedRenderListBuilder.Layers layers = build(r -> {
            r.fillRect(0, 0, 10, 10, Color.RED);
            r.scissor(20, 0, 10, 10, () -> r.fillRect(20, 0, 10, 10, Color.GREEN));
        });
        assertEquals(2, layers.size());
        assertTrue(layers.get(0).scissor().isFull());
        assertEquals(new Aabb(20, 0, 30, 10), layers.get(1).scissor());
    }

    @Test
    void nestedScissorsAreIntersected() {
        BatchedRenderListBuilder.Layers layers = build(r -> r.scissor(0, 0, 100, 100, () -> r.scissor(50, 50, 100, 100, () -> r.fillRect(0, 0, 200, 200, Color.RED))));
        assertEquals(1, layers.size());
        assertEquals(new Aabb(50, 50, 100, 100), layers.get(0).scissor());
    }

    @Test
    void disjointScissorsClipEverything() {
        BatchedRenderListBuilder.Layers layers = build(r -> {
            r.fillRect(0, 0, 10, 10, Color.RED);
            // The gap between both scissors (y = 100 to 150) must not become the scissor area
            r.scissor(0, 0, 100, 100, () -> r.scissor(0, 150, 100, 50, () -> r.fillRect(0, 0, 100, 200, Color.GREEN)));
        });
        assertEquals(1, layers.size());
        assertEquals(1, layers.get(0).commands().size());
        assertEquals(Color.RED, ((RenderCommand.FillRect) layers.get(0).commands().get(0).command()).color());
    }

    @Test
    void stencilMasksAreAttached() {
        BatchedRenderListBuilder.Layers layers = build(r -> r.stencil(
                mask -> mask.fillRect(0, 0, 10, 10, Color.RED),
                () -> {
                    r.fillRect(0, 0, 10, 10, Color.GREEN);
                    r.translate(20, 0, () -> r.fillRect(0, 0, 10, 10, Color.BLUE));
                }
        ));
        assertEquals(1, layers.size());
        assertEquals(2, layers.get(0).commands().size());
        assertEquals(StencilMode.EQUAL_INTERSECTION, layers.get(0).stencilMode());
        assertEquals(1, layers.get(0).stencilMaskLayers().size());
    }

    @Test
    void customModifiersAreUnsupportedByDefault() {
        assertThrows(UnsupportedOperationException.class, () -> build(r -> r.custom(new TestModifier(1), () -> r.fillRect(0, 0, 10, 10, Color.RED))));
    }

    @Test
    void customStateSeparatesLayersAndIsInherited() {
        BatchedRenderListBuilder.CustomModifierHandler handler = (command, state) -> ((TestModifier) command).value() + (state == null ? 0 : (Integer) state);
        BatchedRenderListBuilder.Layers layers = build(r -> {
            r.fillRect(0, 0, 10, 10, Color.RED);
            r.custom(new TestModifier(2), () -> r.translate(20, 0, () -> r.fillRect(0, 0, 10, 10, Color.GREEN)));
        }, handler);
        assertEquals(2, layers.size());
        assertNull(layers.get(0).customState());
        assertSame(2, layers.get(1).customState());
    }

    private record TestModifier(int value) implements ModifierCommand.Custom {
    }

}
