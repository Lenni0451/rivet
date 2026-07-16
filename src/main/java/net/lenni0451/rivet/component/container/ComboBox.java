package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.Parent;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerBucket;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.List;
import java.util.function.BiConsumer;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ComboBox extends Component implements Parent {

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
    private final ThemeOption<Color> arrowColor = new ThemeOption<>(this, Theme.COMBOBOX_ARROW_COLOR);
    @Getter
    private final ThemeOption<Color> disabledArrowColor = new ThemeOption<>(this, Theme.COMBOBOX_DISABLED_ARROW_COLOR);
    @Getter
    private final ThemeOption<Float> arrowSize = new ThemeOption<>(this, Theme.COMBOBOX_ARROW_SIZE);
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
        this.button = new Button(text, event -> {
            if (this.isOpen()) {
                this.close();
            } else {
                this.open();
            }
        });
        this.child = child;
        textInitializer.accept(this, text);
        initializer.accept(this, child);
    }

    public ComboBox open() {
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
        this.openListener.callVoid(Runnable::run);
        return this;
    }

    public ComboBox close() {
        if (!this.isOpen()) return this;
        this.rivet().removeLayer(this.layer);
        this.layer = null;
        this.closeListener.callVoid(Runnable::run);
        return this;
    }

    public boolean isOpen() {
        return this.layer != null;
    }

    @Override
    protected void onComponentAdded() {
        this.button.setRivet(this.rivet(), this);
    }

    @Override
    protected void onComponentRemoved() {
        this.close();
        this.button.setRivet(null, null);
    }

    @Override
    protected void onComponentDisabled() {
        this.close();
        this.button.disabled(true);
    }

    @Override
    protected void onComponentEnabled() {
        this.button.disabled(false);
    }

    @Override
    protected void onComponentThemeChanged() {
        this.button.onThemeChanged();
    }

    @Override
    protected void onComponentMouseEnter() {
        this.button.onMouseEnter();
    }

    @Override
    protected void onComponentMouseLeave() {
        this.button.onMouseLeave();
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        return this.button.onMouseDown(event, size);
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        return this.button.onMouseUp(event, size);
    }

    @Override
    protected void updateComponentPosition(final Rectangle absoluteBounds) {
        this.button.updatePosition(absoluteBounds);
        if (this.isOpen()) {
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
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        this.button.render(renderer, size);
        float triangleSize = this.arrowSize.value();
        Color arrowColor = this.disabled() ? this.disabledArrowColor.value() : this.arrowColor.value();
        if (this.isOpen()) {
            renderer.fillTriangle(
                    size.width() - this.button.innerPadding().value().right() - triangleSize,
                    size.height() / 2F + triangleSize / 2F,
                    size.width() - this.button.innerPadding().value().right(),
                    size.height() / 2F + triangleSize / 2F,
                    size.width() - this.button.innerPadding().value().right() - triangleSize / 2F,
                    size.height() / 2F - triangleSize / 2F,
                    arrowColor
            );
        } else {
            renderer.fillTriangle(
                    size.width() - this.button.innerPadding().value().right() - triangleSize,
                    size.height() / 2F - triangleSize / 2F,
                    size.width() - this.button.innerPadding().value().right() - triangleSize / 2F,
                    size.height() / 2F + triangleSize / 2F,
                    size.width() - this.button.innerPadding().value().right(),
                    size.height() / 2F - triangleSize / 2F,
                    arrowColor
            );
        }
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        return this.button.computeIdealSize(constraints);
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
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
