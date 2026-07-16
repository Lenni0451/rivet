package net.lenni0451.rivet.component.container.tabcontainer;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.layout.border.BorderLayout;
import net.lenni0451.rivet.layout.border.BorderPosition;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.layout.list.HorizontalListLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class TabContainer extends ParentContainer {

    private final List<Tab> tabs = new ArrayList<>();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final Container tabContainer = new Container(BorderLayout.DEFAULT);
    private final Container leftTabContainer = new Container(new HorizontalListLayout(0, true));
    private final Container centerTabContainer = new Container(new TabLayout());
    private final Container rightTabContainer = new Container(new HorizontalListLayout(0, true));
    private final Container contentContainer = new Container(FullSizeLayout.INSTANCE);
    private Size tabSize;
    private Size contentSize;
    @Getter
    private final ThemeOption<Color> headerBackgroundColor = new ThemeOption<>(this, Theme.TAB_HEADER_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> separatorColor = new ThemeOption<>(this, Theme.TAB_SEPARATOR_COLOR);
    @Getter
    private final ThemeOption<Float> separatorThickness = new ThemeOption<>(this, Theme.TAB_SEPARATOR_THICKNESS);
    @Getter
    private final ThemeOption<TabAlignment> tabAlignment = new ThemeOption<>(this, Theme.TAB_ALIGNMENT);
    @Getter
    private final ThemeOption<Boolean> tabSameSize = new ThemeOption<>(this, Theme.TAB_SAME_SIZE);
    @Getter
    private final ThemeOption<Float> tabVerticalGap = new ThemeOption<>(this, Theme.TAB_VERTICAL_GAP);
    @Getter
    private final ThemeOption<Float> tabGap = new ThemeOption<>(this, Theme.TAB_TAB_GAP);

    public TabContainer() {
        this.tabContainer.addChild(this.leftTabContainer.layoutOptions(BorderPosition.LEFT));
        this.tabContainer.addChild(new ScrollContainer(this.centerTabContainer.layoutOptions(BorderPosition.CENTER), true, false));
        this.tabContainer.addChild(this.rightTabContainer.layoutOptions(BorderPosition.RIGHT));

        this.tabAlignment.initListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).alignment = val;
            this.requestLayoutRecalculation();
        });
        this.tabSameSize.initListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).sameSize = val;
            this.requestLayoutRecalculation();
        });
        this.tabVerticalGap.initListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).verticalGap = val;
            this.requestLayoutRecalculation();
        });
        this.tabGap.initListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).tabGap = val;
            this.requestLayoutRecalculation();
        });
    }

    public List<Tab> tabs() {
        return Collections.unmodifiableList(this.tabs);
    }

    public TabContainer addLeftComponent(final Component component) {
        this.leftTabContainer.addChild(component);
        return this;
    }

    public TabContainer removeLeftComponent(final Component component) {
        this.leftTabContainer.removeChild(component);
        return this;
    }

    public TabContainer addRightComponent(final Component component) {
        this.rightTabContainer.addChild(component);
        return this;
    }

    public TabContainer removeRightComponent(final Component component) {
        this.rightTabContainer.removeChild(component);
        return this;
    }

    public Tab addTab(final Component header, final Component content) {
        Tab tab = new Tab(header, this::selectTab, content);
        this.tabs.add(tab);
        this.centerTabContainer.addChild(tab.button());
        if (this.centerTabContainer.children().size() == 1) {
            this.selectTab(tab);
        }
        return tab;
    }

    public TabContainer removeTab(final Tab tab) {
        if (!this.tabs.contains(tab)) throw new IllegalArgumentException("Tab is not part of this TabContainer");
        int tabIndex = this.tabs.indexOf(tab);
        if (tab.headerBackground().active()) {
            if (this.tabs.size() > 1) {
                this.selectTab(this.tabs.get(tabIndex >= this.tabs.size() - 1 ? tabIndex - 1 : tabIndex + 1));
            } else {
                this.contentContainer.clearChildren();
            }
        }
        this.tabs.remove(tab);
        this.centerTabContainer.removeChild(tab.button());
        return this;
    }

    public TabContainer selectTab(final Tab tab) {
        if (!this.tabs.contains(tab)) throw new IllegalArgumentException("Tab is not part of this TabContainer");
        this.tabs.forEach(t -> t.headerBackground().deactivate());
        tab.headerBackground().activate();
        this.contentContainer.clearChildren();
        this.contentContainer.addChild(tab.content());
        return this;
    }

    @Override
    protected ContainerMouseHandler<?> mouseHandler() {
        return this.mouseHandler;
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        Color headerColor = this.headerBackgroundColor.value();
        if (!headerColor.equals(Color.TRANSPARENT)) {
            renderer.fillRect(0, 0, size.width(), this.tabSize.height(), headerColor);
        }

        renderer.componentBounds(0, 0, size.width(), this.tabSize.height(), () -> {
            this.tabContainer.render(renderer, new Size(size.width(), this.tabSize.height()));
        });

        float separatorThickness = this.separatorThickness.value();
        if (separatorThickness > 0) {
            Color separatorColor = this.separatorColor.value();
            renderer.fillRect(0, this.tabSize.height() - separatorThickness, size.width(), separatorThickness, separatorColor);
        }

        renderer.translate(0, this.tabSize.height(), () -> {
            renderer.componentBounds(0, 0, size.width(), this.contentSize.height(), () -> {
                this.contentContainer.render(renderer, new Size(size.width(), this.contentSize.height()));
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        Size tabSize = this.tabContainer.computeIdealSize(constraints);
        Size contentSize = this.contentContainer.computeIdealSize(constraints.withHeight(constraints.height() - tabSize.height()));
        return new Size(
                Math.max(tabSize.width(), contentSize.width()),
                tabSize.height() + contentSize.height()
        );
    }

    @Override
    public void computeLayout(final Size size) {
        this.tabSize = this.tabContainer.computeIdealSize(size).withWidth(size.width());
        this.contentSize = new Size(size.width(), size.height() - this.tabSize.height());
        this.tabContainer.computeLayout(this.tabSize);
        this.contentContainer.computeLayout(this.contentSize);
    }

    @Override
    public Size contentSize() {
        return new Size(
                this.tabSize.width(),
                this.tabSize.height() + this.contentSize.height()
        );
    }

    @Override
    public List<Component> children() {
        return List.of(this.tabContainer, this.contentContainer);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.tabContainer) {
            return new Rectangle(0, 0, this.tabSize);
        } else if (component == this.contentContainer) {
            return new Rectangle(0, this.tabSize.height(), this.contentSize);
        }
        return Rectangle.EMPTY;
    }


    private class MouseHandler extends ContainerMouseHandler<Component> {
        @Override
        protected Component map(final Component element) {
            return element;
        }

        @Override
        protected Rectangle relativeBounds(final Size containerBounds, final Component element) {
            if (element == TabContainer.this.tabContainer) {
                return new Rectangle(0, 0, TabContainer.this.tabSize);
            } else if (element == TabContainer.this.contentContainer) {
                return new Rectangle(0, TabContainer.this.tabSize.height(), TabContainer.this.contentSize);
            }
            return Rectangle.EMPTY;
        }

        @Override
        protected List<Component> elementsAt(final float x, final float y, final Size containerBounds) {
            if (x < 0 || x >= containerBounds.width() || y < 0 || y >= containerBounds.height()) return List.of();
            if (y < TabContainer.this.tabSize.height()) {
                return List.of(TabContainer.this.tabContainer);
            } else {
                return List.of(TabContainer.this.contentContainer);
            }
        }
    }

}
