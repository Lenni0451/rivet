package net.lenni0451.rivet.backend.text;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.model.TextFormat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextDecorationsTest {

    @Test
    void renderOrder() {
        TextFormat format = TextFormat.DEFAULT.withShadow(true).withUnderlined(true).withOutlineColor(Color.RED);
        List<String> calls = new ArrayList<>();
        TextDecorations.render(format, 20, 0, 0, new TextDecorations.Painter() {
            @Override
            public void outline(final float x, final float y, final Color color) {
                calls.add("outline@" + x);
            }

            @Override
            public void glyphs(final float x, final float y, final Color color) {
                calls.add("glyphs@" + x);
            }

            @Override
            public void lines(final float x, final float y, final Color color) {
                calls.add("lines@" + x);
            }
        });
        assertEquals(List.of("outline@1.5", "glyphs@1.5", "lines@1.5", "outline@0.0", "glyphs@0.0", "lines@0.0"), calls);
    }

    @Test
    void plainTextOnlyDrawsGlyphs() {
        List<String> calls = new ArrayList<>();
        TextDecorations.render(TextFormat.DEFAULT, 20, 0, 0, new TextDecorations.Painter() {
            @Override
            public void outline(final float x, final float y, final Color color) {
                calls.add("outline");
            }

            @Override
            public void glyphs(final float x, final float y, final Color color) {
                calls.add("glyphs");
            }

            @Override
            public void lines(final float x, final float y, final Color color) {
                calls.add("lines");
            }
        });
        assertEquals(List.of("glyphs"), calls);
    }

    @Test
    void visualBoundsIncludeOutlineAndShadow() {
        TextFormat format = TextFormat.DEFAULT.withShadow(true).withOutlineColor(Color.RED);
        Rectangle bounds = new TextDecorations.VisualBounds().addGlyphs(0, -10, 50, 2, format, 20).build();
        // Outline: 20 * 0.125 / 2 = 1.25 on every side, shadow: 20 * 0.075 = 1.5 on the max sides
        assertEquals(new Rectangle(-1.25F, -11.25F, 50 + 2.5F + 1.5F, 12 + 2.5F + 1.5F), bounds);
    }

    @Test
    void emptyVisualBounds() {
        assertEquals(new Rectangle(0, 0, 0, 0), new TextDecorations.VisualBounds().build());
    }

}
