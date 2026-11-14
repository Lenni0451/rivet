package net.lenni0451.rivet.component.container;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.impl.ScrollBar.ScrollBarType;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.lenni0451.rivet.utils.MathUtils.EPSILON;
import static net.lenni0451.rivet.utils.MathUtils.isGreaterThan;
import static org.junit.jupiter.api.Assertions.*;

class ScrollContainerTest {

    private static final float BAR = 10;
    private static final Size VIEWPORT = new Size(100, 100);
    // Values around every threshold of a 100px viewport with 10px scroll bars
    private static final float[] EDGE_SIZES = {0, 50, 89.5F, 89.9995F, 90, 90.0005F, 90.5F, 95, 99.5F, 99.9995F, 100, 100.0005F, 100.5F, 105, 109.5F, 110, 110.5F, 250};
    private static final boolean[] BOOLEANS = {false, true};

    @Test
    void testFixedContent() {
        List<String> failures = new ArrayList<>();
        for (Config config : Config.all()) {
            for (float contentWidth : EDGE_SIZES) {
                for (float contentHeight : EDGE_SIZES) {
                    Probe probe = new Probe(constraints -> new Size(contentWidth, contentHeight));
                    Fixture fixture = new Fixture(config, probe);
                    fixture.layout(VIEWPORT);
                    String name = config + " content=" + contentWidth + "x" + contentHeight;
                    fixture.checkInvariants(name, probe, true, failures);

                    // The bars reserve space, so one bar can force the other one to show up but nothing else
                    boolean h = false;
                    boolean v = false;
                    for (int i = 0; i < 3; i++) {
                        float visibleWidth = VIEWPORT.width() - (v && config.vNormal ? BAR : 0);
                        float visibleHeight = VIEWPORT.height() - (h && config.hNormal ? BAR : 0);
                        h |= config.horizontal && isGreaterThan(contentWidth, visibleWidth);
                        v |= config.vertical && isGreaterThan(contentHeight, visibleHeight);
                    }
                    if (h != fixture.hVisible() || v != fixture.vVisible()) {
                        failures.add(name + ": expected h=" + h + " v=" + v + " but got h=" + fixture.hVisible() + " v=" + fixture.vVisible());
                    }
                }
            }
        }
        assertNoFailures(failures);
    }

    @Test
    void testWrappingContent() {
        // Like wrapped text: the narrower the content, the taller it gets
        List<String> failures = new ArrayList<>();
        for (Config config : Config.all()) {
            for (float area : new float[]{5_000, 8_990, 9_000, 9_010, 9_990, 10_000, 10_010, 11_000, 50_000}) {
                for (float minWidth : new float[]{0, 85, 95, 150}) {
                    Probe probe = new Probe(constraints -> {
                        float width = Math.max(minWidth, Math.min(constraints.width(), 400));
                        return new Size(width, area / width);
                    });
                    Fixture fixture = new Fixture(config, probe);
                    fixture.layout(VIEWPORT);
                    fixture.checkInvariants(config + " area=" + area + " minWidth=" + minWidth, probe, true, failures);
                }
            }
        }
        assertNoFailures(failures);
    }

    @Test
    void testShrinkingContent() {
        // Like an image keeping its aspect ratio: the narrower the content, the shorter it gets.
        // A vertical scroll bar can make the content fit which makes the scroll bar obsolete again.
        List<String> failures = new ArrayList<>();
        for (Config config : Config.all()) {
            for (float ratio : new float[]{0.5F, 0.95F, 1, 1.05F, 1.1F, 1.15F, 2}) {
                Probe probe = new Probe(constraints -> {
                    float width = Math.min(constraints.width(), 400);
                    return new Size(width, width * ratio);
                });
                Fixture fixture = new Fixture(config, probe);
                fixture.layout(VIEWPORT);
                // The bar is allowed to stay visible without anything to scroll if the content only fits because of the bar
                fixture.checkInvariants(config + " ratio=" + ratio, probe, false, failures);
            }
        }
        assertNoFailures(failures);

        Config verticalOnly = new Config(false, true, false, true, true);
        Probe fitting = new Probe(constraints -> new Size(constraints.width(), constraints.width() * 0.95F));
        Fixture fittingFixture = new Fixture(verticalOnly, fitting);
        fittingFixture.layout(VIEWPORT);
        assertFalse(fittingFixture.vVisible());
        assertEquals(VIEWPORT, fitting.arranged);

        Probe cycling = new Probe(constraints -> new Size(constraints.width(), constraints.width() * 1.05F));
        Fixture cyclingFixture = new Fixture(verticalOnly, cycling);
        cyclingFixture.layout(VIEWPORT);
        assertTrue(cyclingFixture.vVisible(), "The bar must stay visible, the content does not fit without it");
        assertEquals(VIEWPORT.width() - BAR, cycling.arranged.width(), "The content must not be arranged below the bar");
        assertEquals(0, cyclingFixture.scrollContainer.maxScrollY());
    }

    @Test
    void testMinAndMaxSize() {
        List<String> failures = new ArrayList<>();
        for (Config config : Config.all()) {
            for (float contentSize : EDGE_SIZES) {
                Probe min = new Probe(constraints -> new Size(contentSize, contentSize));
                min.minSize(105, 95);
                Fixture minFixture = new Fixture(config, min);
                minFixture.layout(VIEWPORT);
                minFixture.checkInvariants(config + " min content=" + contentSize, null, true, failures);
                if (config.horizontal && !minFixture.hVisible()) failures.add(config + " min content=" + contentSize + ": horizontal bar hidden but the content is always wider than the viewport");

                Probe max = new Probe(constraints -> new Size(contentSize, contentSize));
                max.maxSize(85, 105);
                Fixture maxFixture = new Fixture(config, max);
                maxFixture.layout(VIEWPORT);
                maxFixture.checkInvariants(config + " max content=" + contentSize, null, true, failures);
                if (maxFixture.hVisible()) failures.add(config + " max content=" + contentSize + ": horizontal bar visible but the content can't be wider than the viewport");
            }
        }
        assertNoFailures(failures);
    }

    @Test
    void testContainerContent() {
        List<String> failures = new ArrayList<>();
        for (Config config : Config.all()) {
            for (int items : new int[]{0, 1, 8, 9, 10, 11, 12, 30}) {
                for (float itemWidth : new float[]{10, 30, 45, 50, 89, 91, 99, 101, 120}) {
                    List<Supplier<Container>> layouts = List.of(
                            () -> new Container(VerticalListLayout.DEFAULT),
                            () -> new Container(new VerticalListLayout(0, true)),
                            () -> new Container(HorizontalFlowLayout.DEFAULT)
                    );
                    for (Supplier<Container> layout : layouts) {
                        Container container = layout.get();
                        for (int i = 0; i < items; i++) {
                            container.add(new Probe(constraints -> new Size(itemWidth, 10)));
                        }
                        Fixture fixture = new Fixture(config, container);
                        fixture.layout(VIEWPORT);
                        String name = config + " layout=" + container.layout().getClass().getSimpleName() + " items=" + items + " itemWidth=" + itemWidth;
                        fixture.checkInvariants(name, null, true, failures);
                    }
                }
            }
        }
        assertNoFailures(failures);
    }

    @Test
    void testResultIsIndependentOfPreviousLayout() {
        List<String> failures = new ArrayList<>();
        Size[] viewports = {new Size(100, 100), new Size(50, 50), new Size(300, 300), new Size(95, 105), new Size(105, 95), new Size(100, 100)};
        for (Config config : Config.all()) {
            for (float contentWidth : EDGE_SIZES) {
                for (float contentHeight : EDGE_SIZES) {
                    Function<Size, Size> ideal = constraints -> new Size(contentWidth, contentHeight);
                    Probe probe = new Probe(ideal);
                    Fixture fixture = new Fixture(config, probe);
                    for (Size viewport : viewports) {
                        fixture.layout(viewport);
                        Fixture fresh = new Fixture(config, new Probe(ideal));
                        fresh.layout(viewport);
                        if (fixture.hVisible() != fresh.hVisible() || fixture.vVisible() != fresh.vVisible() || !fixture.childSize().equals(fresh.childSize())) {
                            failures.add(config + " content=" + contentWidth + "x" + contentHeight + " viewport=" + viewport + ": differs from a fresh layout");
                        }
                    }
                }
            }
        }
        assertNoFailures(failures);
    }

    @Test
    void testContentChangeUpdatesVisibility() {
        for (Config config : Config.all()) {
            if (!config.vertical) continue;
            Size[] ideal = {new Size(10, 10)};
            Probe probe = new Probe(constraints -> ideal[0]);
            Fixture fixture = new Fixture(config, probe);
            fixture.layout(VIEWPORT);
            assertFalse(fixture.vVisible(), config.toString());

            ideal[0] = new Size(10, 500);
            probe.invalidateLayout();
            fixture.layout(VIEWPORT);
            assertTrue(fixture.vVisible(), config.toString());
            assertEquals(400, fixture.scrollContainer.maxScrollY(), EPSILON, config.toString());

            ideal[0] = new Size(10, 10);
            probe.invalidateLayout();
            fixture.layout(VIEWPORT);
            assertFalse(fixture.vVisible(), config.toString());
            assertEquals(0, fixture.scrollContainer.maxScrollY(), config.toString());
        }
    }

    private static void assertNoFailures(final List<String> failures) {
        assertTrue(failures.isEmpty(), failures.size() + " failures:\n" + String.join("\n", failures.subList(0, Math.min(failures.size(), 40))));
    }


    private record Config(boolean horizontal, boolean vertical, boolean hNormal, boolean vNormal, boolean presentInfiniteSize) {
        private static List<Config> all() {
            List<Config> configs = new ArrayList<>();
            for (boolean horizontal : BOOLEANS) {
                for (boolean vertical : BOOLEANS) {
                    for (boolean hNormal : BOOLEANS) {
                        for (boolean vNormal : BOOLEANS) {
                            for (boolean presentInfiniteSize : BOOLEANS) {
                                configs.add(new Config(horizontal, vertical, hNormal, vNormal, presentInfiniteSize));
                            }
                        }
                    }
                }
            }
            return configs;
        }
    }

    private static final class Fixture {
        private final Config config;
        private final Rivet rivet;
        private final ScrollContainer scrollContainer;
        private Size viewport;

        private Fixture(final Config config, final Component child) {
            this.config = config;
            this.rivet = new Rivet(new TestBackend(), FullSizeLayout.INSTANCE, VIEWPORT);
            this.scrollContainer = new ScrollContainer(child, config.horizontal, config.vertical).presentInfiniteSize(config.presentInfiniteSize);
            // Explicit values to not resolve theme defaults, they require a font
            this.scrollContainer.scrollSpeed().set(1F);
            this.scrollContainer.hScrollBar().barWidth().set(BAR);
            this.scrollContainer.vScrollBar().barWidth().set(BAR);
            this.scrollContainer.hScrollBar().barType().set(config.hNormal ? ScrollBarType.NORMAL : ScrollBarType.FLOATING);
            this.scrollContainer.vScrollBar().barType().set(config.vNormal ? ScrollBarType.NORMAL : ScrollBarType.FLOATING);
            this.rivet.root().add(this.scrollContainer);
        }

        private void layout(final Size viewport) {
            this.viewport = viewport;
            this.rivet.root().layout(viewport);
        }

        private boolean hVisible() {
            return !this.scrollContainer.childBounds(this.scrollContainer.hScrollBar()).equals(Rectangle.EMPTY);
        }

        private boolean vVisible() {
            return !this.scrollContainer.childBounds(this.scrollContainer.vScrollBar()).equals(Rectangle.EMPTY);
        }

        private Size childSize() {
            return this.scrollContainer.childBounds(this.scrollContainer.child()).size();
        }

        private void checkInvariants(final String name, final Probe probe, final boolean strict, final List<String> failures) {
            boolean h = this.hVisible();
            boolean v = this.vVisible();
            Size childSize = this.childSize();
            float visibleWidth = this.viewport.width() - (v && this.config.vNormal ? BAR : 0);
            float visibleHeight = this.viewport.height() - (h && this.config.hNormal ? BAR : 0);
            String state = name + " [h=" + h + " v=" + v + " child=" + childSize + (probe == null ? "" : " arranged=" + probe.arranged) + "]";

            if (h && !this.config.horizontal) failures.add(state + ": horizontal bar visible without horizontal scrolling");
            if (v && !this.config.vertical) failures.add(state + ": vertical bar visible without vertical scrolling");
            // Overflowing content must always be reachable, in strict mode a bar must also not be visible without anything to scroll
            boolean hOverflow = isGreaterThan(childSize.width(), visibleWidth);
            boolean vOverflow = isGreaterThan(childSize.height(), visibleHeight);
            if (this.config.horizontal && (strict ? h != hOverflow : hOverflow && !h)) {
                failures.add(state + ": horizontal bar does not match the overflow (visible width " + visibleWidth + ")");
            }
            if (this.config.vertical && (strict ? v != vOverflow : vOverflow && !v)) {
                failures.add(state + ": vertical bar does not match the overflow (visible height " + visibleHeight + ")");
            }
            if (this.config.horizontal && hOverflow != this.scrollContainer.maxScrollX() > 0) {
                failures.add(state + ": horizontal overflow does not match maxScrollX " + this.scrollContainer.maxScrollX());
            }
            if (this.config.vertical && vOverflow != this.scrollContainer.maxScrollY() > 0) {
                failures.add(state + ": vertical overflow does not match maxScrollY " + this.scrollContainer.maxScrollY());
            }
            // The child must be arranged for the space which is left next to the final bars
            if (probe != null) {
                if (this.config.horizontal ? probe.arranged.width() < visibleWidth : probe.arranged.width() != visibleWidth) {
                    failures.add(state + ": child was not arranged for the visible width " + visibleWidth);
                }
                if (this.config.vertical ? probe.arranged.height() < visibleHeight : probe.arranged.height() != visibleHeight) {
                    failures.add(state + ": child was not arranged for the visible height " + visibleHeight);
                }
            }
        }
    }

    private static final class Probe extends Component {
        private final Function<Size, Size> ideal;
        private Size arranged = Size.EMPTY;

        private Probe(final Function<Size, Size> ideal) {
            this.ideal = ideal;
        }

        @Override
        protected Size idealSizeInternal(final Size constraints) {
            return this.ideal.apply(constraints);
        }

        @Override
        protected void layoutInternal(final Size size) {
            this.arranged = size;
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
