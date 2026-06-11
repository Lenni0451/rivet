package net.lenni0451.rivet.component.container.tabcontainer;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.ScrollContainer;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
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
public class TabContainer extends Component implements Parent {

    private final List<Tab> tabs = new ArrayList<>();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final Container tabContainer = new Container(BorderLayout.INSTANCE);
    private final Container leftTabContainer = new Container(new HorizontalListLayout(0, true));
    private final Container centerTabContainer = new Container(new TabLayout());
    private final Container rightTabContainer = new Container(new HorizontalListLayout(0, true));
    private final Container contentContainer = new Container(FullSizeLayout.INSTANCE);
    private Size tabSize;
    private Size contentSize;
    @Getter
    private final ThemeOption<Color> headerBackgroundColor;
    @Getter
    private final ThemeOption<Color> separatorColor;
    @Getter
    private final ThemeOption<Float> separatorThickness;
    @Getter
    private final ThemeOption<TabAlignment> tabAlignment;
    @Getter
    private final ThemeOption<Boolean> tabSameSize;
    @Getter
    private final ThemeOption<Float> tabVerticalGap;
    @Getter
    private final ThemeOption<Float> tabGap;

    public TabContainer() {
        this.tabContainer.addChild(this.leftTabContainer.layoutOptions(BorderPosition.LEFT));
        this.tabContainer.addChild(new ScrollContainer(this.centerTabContainer.layoutOptions(BorderPosition.CENTER), true, false));
        this.tabContainer.addChild(this.rightTabContainer.layoutOptions(BorderPosition.RIGHT));

        this.headerBackgroundColor = new ThemeOption<>(this, Theme.TAB_HEADER_BACKGROUND_COLOR);
        this.separatorColor = new ThemeOption<>(this, Theme.TAB_SEPARATOR_COLOR);
        this.separatorThickness = new ThemeOption<>(this, Theme.TAB_SEPARATOR_THICKNESS);
        this.tabAlignment = new ThemeOption<>(this, Theme.TAB_ALIGNMENT);
        this.tabSameSize = new ThemeOption<>(this, Theme.TAB_SAME_SIZE);
        this.tabVerticalGap = new ThemeOption<>(this, Theme.TAB_VERTICAL_GAP);
        this.tabGap = new ThemeOption<>(this, Theme.TAB_TAB_GAP);

        this.tabAlignment.changeListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).alignment = val;
            this.requestLayoutRecalculation();
        });
        this.tabSameSize.changeListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).sameSize = val;
            this.requestLayoutRecalculation();
        });
        this.tabVerticalGap.changeListener().add(val -> {
            ((TabLayout) this.centerTabContainer.layout()).verticalGap = val;
            this.requestLayoutRecalculation();
        });
        this.tabGap.changeListener().add(val -> {
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

    private void updateLayout() {
        TabLayout layout = (TabLayout) this.centerTabContainer.layout();
        layout.alignment = this.tabAlignment.value();
        layout.sameSize = this.tabSameSize.value();
        layout.verticalGap = this.tabVerticalGap.value();
        layout.tabGap = this.tabGap.value();
    }

    @Override
    protected void onComponentAdded() {
        this.tabContainer.setRivet(this.rivet(), this);
        this.contentContainer.setRivet(this.rivet(), this);
        this.updateLayout();
    }

    @Override
    protected void onComponentRemoved() {
        this.tabContainer.setRivet(null, null);
        this.contentContainer.setRivet(null, null);
    }

    @Override
    protected void onComponentMouseLeave() {
        this.mouseHandler.onMouseLeave();
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        return this.mouseHandler.onMouseDown(this.rivet(), event, size).handled();
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        return this.mouseHandler.onMouseUp(this.rivet(), event, size).handled();
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        return this.mouseHandler.onMouseMove(event, size).handled();
    }

    @Override
    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Size size) {
        return this.mouseHandler.onMouseScroll(event, size).handled();
    }

    @Override
    protected boolean onComponentDrop(final DropEvent event, final Size size) {
        return this.mouseHandler.onDrop(event, size).handled();
    }

    @Override
    protected boolean onComponentDragOver(final DragOverEvent event, final Size size) {
        return this.mouseHandler.onDragOver(event, size).handled();
    }

    @Override
    protected void onComponentDragLeave() {
        this.mouseHandler.onDragLeave();
    }

    @Override
    public void onThemeChanged() {
        this.tabContainer.onThemeChanged();
        this.contentContainer.onThemeChanged();
        this.updateLayout();
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
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
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
            if (y < TabContainer.this.tabSize.height()) {
                return List.of(TabContainer.this.tabContainer);
            } else {
                return List.of(TabContainer.this.contentContainer);
            }
        }
    }

}
