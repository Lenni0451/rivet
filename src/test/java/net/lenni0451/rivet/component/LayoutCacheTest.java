package net.lenni0451.rivet.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutCacheTest {

    private static final Size SIZE = new Size(300, 100);

    private Rivet rivet;

    @BeforeEach
    void setUp() {
        this.rivet = new Rivet(new TestBackend(), FullSizeLayout.INSTANCE, SIZE);
    }

    @Test
    void testIdealSizeIsCached() {
        CountingComponent component = new CountingComponent(10, 10);
        this.rivet.root().add(component);

        component.idealSize(SIZE);
        component.idealSize(SIZE);
        assertEquals(1, component.measureCount);

        component.idealSize(new Size(50, 50));
        component.idealSize(SIZE);
        assertEquals(2, component.measureCount, "The two most recent constraints should be cached");
    }

    @Test
    void testInvalidationClearsIdealSizeCache() {
        CountingComponent component = new CountingComponent(10, 10);
        this.rivet.root().add(component);

        component.idealSize(SIZE);
        component.ideal(20, 20);
        assertEquals(new Size(20, 20), component.idealSize(SIZE));
        assertEquals(2, component.measureCount);
    }

    @Test
    void testUnchangedLayoutIsSkipped() {
        CountingComponent component = new CountingComponent(10, 10);
        this.rivet.root().add(component);

        this.rivet.root().layout(SIZE);
        this.rivet.root().layout(SIZE);
        assertEquals(1, component.arrangeCount);

        this.rivet.root().layout(new Size(200, 100));
        assertEquals(2, component.arrangeCount, "A size change must re-arrange the component");
    }

    @Test
    void testOnlyInvalidatedSubtreeIsArranged() {
        Container list = new Container(VerticalListLayout.DEFAULT);
        List<CountingComponent> children = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            CountingComponent child = new CountingComponent(10, 10);
            children.add(child);
            list.add(child);
        }
        this.rivet.root().add(list);
        this.rivet.root().layout(SIZE);

        children.get(50).invalidateLayout();
        this.rivet.root().layout(SIZE);

        for (int i = 0; i < children.size(); i++) {
            CountingComponent child = children.get(i);
            assertEquals(i == 50 ? 2 : 1, child.arrangeCount, "Child " + i);
            assertEquals(i == 50 ? 2 : 1, child.measureCount, "Only the invalidated child must be measured again");
        }
    }

    @Test
    void testSiblingIsResizedWhenRatioChanges() {
        CountingComponent left = new CountingComponent(100, 10);
        CountingComponent right = new CountingComponent(100, 10);
        Container ratio = new Container(new RatioLayout());
        ratio.add(left).add(right);
        this.rivet.root().add(ratio);
        this.rivet.root().layout(SIZE);
        assertEquals(new Rectangle(0, 0, 150, 100), ratio.childBounds(left));
        assertEquals(new Rectangle(150, 0, 150, 100), ratio.childBounds(right));

        // Only the left side changes, the right side has to shrink to keep the ratio
        left.ideal(200, 10);
        this.rivet.root().layout(SIZE);
        assertEquals(new Rectangle(0, 0, 200, 100), ratio.childBounds(left));
        assertEquals(new Rectangle(200, 0, 100, 100), ratio.childBounds(right));
        assertEquals(2, right.arrangeCount, "The untouched sibling must be re-arranged because its size changed");
        assertEquals(1, right.measureCount, "The untouched sibling must not be measured again");
    }

    @Test
    void testRivetRecalculateNextFrameInvalidatesEverything() {
        CountingComponent component = new CountingComponent(10, 10);
        this.rivet.root().add(component);
        this.rivet.root().layout(SIZE);

        this.rivet.recalculateNextFrame();
        this.rivet.root().layout(SIZE);
        assertEquals(2, component.arrangeCount);
    }

    @Test
    void testLayoutThemeOptionInvalidatesLayout() {
        CountingComponent component = new CountingComponent(10, 10);
        this.rivet.root().add(component);
        this.rivet.root().layout(SIZE);

        component.thickness.set(5F);
        this.rivet.root().layout(SIZE);
        assertEquals(2, component.arrangeCount);
    }

    @Test
    void testVisualThemeOptionDoesNotInvalidateLayout() {
        CountingComponent component = new CountingComponent(10, 10);
        this.rivet.root().add(component);
        this.rivet.root().layout(SIZE);

        component.color.set(Color.RED);
        this.rivet.root().layout(SIZE);
        assertEquals(1, component.arrangeCount);
    }


    private static final class CountingComponent extends Component {
        private final ThemeOption<Float> thickness = new ThemeOption<>(this, Theme.Separator.THICKNESS);
        private final ThemeOption<Color> color = new ThemeOption<>(this, Theme.Separator.COLOR);
        private Size ideal;
        private int measureCount;
        private int arrangeCount;

        private CountingComponent(final float width, final float height) {
            this.ideal = new Size(width, height);
            // Explicit values to not resolve theme defaults, they require a font
            this.thickness.set(1F);
            this.color.set(Color.WHITE);
        }

        private void ideal(final float width, final float height) {
            this.ideal = new Size(width, height);
            this.invalidateLayout();
        }

        @Override
        protected Size idealSizeInternal(final Size constraints) {
            this.measureCount++;
            return this.ideal;
        }

        @Override
        protected void layoutInternal(final Size size) {
            this.arrangeCount++;
        }
    }

    /**
     * Splits the available width between all components proportional to their ideal width.
     */
    private static final class RatioLayout implements Layout {
        @Override
        public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
            float width = 0;
            float height = 0;
            for (Component component : components) {
                Size idealSize = component.idealSize(constraints);
                width += idealSize.width();
                height = Math.max(height, idealSize.height());
            }
            return new Size(width, height);
        }

        @Override
        public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
            float totalWidth = this.computeIdealSize(containerSize, components).width();
            float x = 0;
            for (Component component : components) {
                float width = containerSize.width() * component.idealSize(containerSize).width() / totalWidth;
                setBounds.accept(component, new Rectangle(x, 0, width, containerSize.height()));
                x += width;
            }
        }
    }

    private static final class TestBackend implements Backend {
        @Override
        public Font font() {
            return null;
        }

        @Override
        public String getClipboard() {
            return null;
        }

        @Override
        public void setClipboard(final String clipboard) {
        }

        @Override
        public boolean isKeyDown(final Key key) {
            return false;
        }

        @Override
        public AssetLoader assetLoader() {
            return null;
        }
    }

}
