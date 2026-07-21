package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.component.impl.Arrow;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerBucket;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.List;
import java.util.function.BiConsumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ComboBox extends ParentContainer {

    @Getter
    private final Arrow arrow;
    @Getter
    private final Button button;
    @Getter
    private final Component child;
    @Getter
    private final ListenerList<Runnable> openListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> closeListener = new ListenerList<>();
    private Layer layer;

    @Getter
    private final ThemeOption<Color> arrowColor = new ThemeOption<>(this, Theme.ARROW_COLOR);
    @Getter
    private final ThemeOption<Color> arrowDisabledColor = new ThemeOption<>(this, Theme.ARROW_DISABLED_COLOR);
    @Getter
    private final ThemeOption<Float> arrowLineWidth = new ThemeOption<>(this, Theme.ARROW_LINE_WIDTH);
    @Getter
    private final ThemeOption<Float> arrowSize = new ThemeOption<>(this, Theme.ARROW_SIZE);
    @Getter
    private final ThemeOption<Float> maxPopupHeight = new ThemeOption<>(this, Theme.COMBOBOX_MAX_POPUP_HEIGHT);
    @Getter
    private final ThemeOption<Boolean> interceptOutsideClicks = new ThemeOption<>(this, Theme.COMBOBOX_INTERCEPT_OUTSIDE_CLICKS);

    public ComboBox(final String text, final Component child) {
        this(text, child, (b, c) -> {});
    }

    public <C extends Component> ComboBox(final String text, final C child, final BiConsumer<ComboBox, C> initializer) {
        this(new Label(text).horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT), (b, t) -> {}, child, initializer);
    }

    public <T extends Component, C extends Component> ComboBox(final T text, final C child) {
        this(text, (b, t) -> {}, child, (b, c) -> {});
    }

    public <T extends Component, C extends Component> ComboBox(final T text, final BiConsumer<ComboBox, T> textInitializer, final C child, final BiConsumer<ComboBox, C> initializer) {
        this.arrow = new Arrow(() -> this.isOpen() ? 1F : 0F);
        this.button = new Button(new Container(GridLayout.DEFAULT), buttonContent -> {
            buttonContent.addChild(text.layoutOptions(GridOptions.EMPTY.at(0, 0).withWeightX(1).withFill(GridFill.HORIZONTAL)));
            buttonContent.addChild(this.arrow.layoutOptions(GridOptions.EMPTY.at(1, 0).withAnchor(GridAnchor.RIGHT)));
        }, () -> {
            if (this.isOpen()) {
                this.close();
            } else {
                this.open();
            }
        });
        this.child = child;

        this.arrowColor.initListener().add(this.arrow.color()::set);
        this.arrowDisabledColor.initListener().add(this.arrow.disabledColor()::set);
        this.arrowLineWidth.initListener().add(this.arrow.lineWidth()::set);
        this.arrowSize.initListener().add(this.arrow.size()::set);

        textInitializer.accept(this, text);
        initializer.accept(this, child);
    }

    public final ComboBox open() {
        if (this.isOpen()) return this;
        Container container = new Container(AbsoluteLayout.INSTANCE);
        if (this.interceptOutsideClicks.value()) {
            SolidColor clickInterceptor = new SolidColor();
            clickInterceptor.mouseDownListener().add((event, bounds) -> {
                this.close();
                return true;
            });
            clickInterceptor.mouseMoveListener().add((event, bounds) -> true);
            container.addChild(clickInterceptor.layoutOptions(new AbsoluteOptions(0, 0, -1F, -1F)));
        }
        container.addChild(this.child);
        this.layer = new Layer(container, LayerBucket.OVERLAY);
        this.rivet().addLayer(this.layer);
        this.updatePopupPosition(this.absoluteBounds());
        this.openListener.callVoid(Runnable::run);
        return this;
    }

    private void updatePopupPosition(final Rectangle absoluteBounds) {
        Size screenSize = this.rivet().scaledSize();
        float availableWidth = screenSize.width() - absoluteBounds.x();
        float availableHeight = screenSize.height() - absoluteBounds.y() - absoluteBounds.height();
        float width = Math.min(availableWidth, absoluteBounds.width());
        width = MathUtils.clamp(width, this.child.minSize().width(), this.child.maxSize().width());
        Size idealSize = this.child.computeIdealSize(new Size(width, availableHeight));
        float maxHeight = Math.min(availableHeight, this.maxPopupHeight.value());
        float height = Math.min(idealSize.height(), maxHeight);
        height = MathUtils.clamp(height, this.child.minSize().height(), this.child.maxSize().height());
        Rectangle region = new Rectangle(
                absoluteBounds.x(),
                absoluteBounds.y() + absoluteBounds.height(),
                width,
                height
        );
        if (!(this.child.layoutOptions() instanceof AbsoluteOptions options)
                || options.x() != region.x() || options.y() != region.y()
                || options.width() == null || options.width() != region.width()
                || options.height() == null || options.height() != region.height()) {
            this.child.layoutOptions(new AbsoluteOptions(region));
        }
    }

    public final ComboBox close() {
        if (!this.isOpen()) return this;
        this.rivet().removeLayer(this.layer);
        this.layer = null;
        this.closeListener.callVoid(Runnable::run);
        return this;
    }

    public final boolean isOpen() {
        return this.layer != null;
    }

    @Override
    protected void onComponentRemoved() {
        super.onComponentRemoved();
        this.close();
    }

    @Override
    protected void onComponentDisabled() {
        super.onComponentDisabled();
        this.close();
    }

    @Override
    protected void updateComponentPosition(final Rectangle absoluteBounds) {
        if (this.isOpen()) {
            this.updatePopupPosition(absoluteBounds);
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        this.button.render(renderer, size);
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.button.computeIdealSize(constraints);
    }

    @Override
    public void computeLayout(final Size size) {
        this.button.computeLayout(size);
        this.updateChildPositions();
    }

    @Override
    public Size contentSize() {
        return this.button.contentSize();
    }

    @Override
    public List<Component> children() {
        return List.of(this.button);
    }

    @Override
    public Rectangle childBounds(final Component component) {
        if (component == this.button) {
            return new Rectangle(this.relativeBounds().size());
        }
        return Rectangle.EMPTY;
    }

}
