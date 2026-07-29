package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.component.ParentContainer;
import net.lenni0451.rivet.component.impl.Arrow;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.layout.grid.GridAnchor;
import net.lenni0451.rivet.layout.grid.GridFill;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.ComponentPopup;

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
    private final ComponentPopup popup;

    @Getter
    private final ThemeOption<Color> arrowColor = new ThemeOption<>(this, Theme.Arrow.COLOR);
    @Getter
    private final ThemeOption<Color> arrowDisabledColor = new ThemeOption<>(this, Theme.Arrow.DISABLED_COLOR);
    @Getter
    private final ThemeOption<Float> arrowLineWidth = new ThemeOption<>(this, Theme.Arrow.LINE_WIDTH);
    @Getter
    private final ThemeOption<Float> arrowSize = new ThemeOption<>(this, Theme.Arrow.SIZE);
    @Getter
    private final ThemeOption<Float> maxPopupHeight = new ThemeOption<>(this, Theme.ComboBox.MAX_POPUP_HEIGHT);
    @Getter
    private final ThemeOption<Boolean> interceptOutsideClicks = new ThemeOption<>(this, Theme.ComboBox.INTERCEPT_OUTSIDE_CLICKS);

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
        this.popup = new ComponentPopup(this, this.child, () -> new Size(Float.MAX_VALUE, this.maxPopupHeight.value()), this.interceptOutsideClicks::value);

        this.arrowColor.initListener().add(this.arrow.color()::set);
        this.arrowDisabledColor.initListener().add(this.arrow.disabledColor()::set);
        this.arrowLineWidth.initListener().add(this.arrow.lineWidth()::set);
        this.arrowSize.initListener().add(this.arrow.size()::set);

        textInitializer.accept(this, text);
        initializer.accept(this, child);
    }

    public final ComboBox open() {
        this.popup.open();
        return this;
    }

    public final ComboBox close() {
        this.popup.close();
        return this;
    }

    public final boolean isOpen() {
        return this.popup.isOpen();
    }

    public final ListenerList<Runnable> openListener() {
        return this.popup.openListener();
    }

    public final ListenerList<Runnable> closeListener() {
        return this.popup.closeListener();
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
