package net.lenni0451.rivet.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerBucket;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.function.Supplier;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ComponentPopup {

    @Getter
    private final Component owner;
    @Getter
    private final Component child;
    @Getter
    private final Supplier<Size> maxSize;
    @Getter
    private final Supplier<Boolean> interceptOutsideClicks;

    @Getter
    @Setter
    private Position position = Position.DOWN;
    @Getter
    @Setter
    private SizeBehavior sizeBehavior = SizeBehavior.CLAMP;
    @Getter
    @Setter
    private boolean matchOwnerWidth = true;

    @Getter
    private final ListenerList<Runnable> openedListener = new ListenerList<>();
    @Getter
    private final ListenerList<Runnable> closedListener = new ListenerList<>();

    private Layer layer;

    public ComponentPopup(final Component owner, final Component child, final Size maxSize, final boolean interceptOutsideClicks) {
        this(owner, child, () -> maxSize, () -> interceptOutsideClicks);
    }

    public ComponentPopup(final Component owner, final Component child, final Supplier<Size> maxSize, final Supplier<Boolean> interceptOutsideClicks) {
        this.owner = owner;
        this.child = child;
        this.maxSize = maxSize;
        this.interceptOutsideClicks = interceptOutsideClicks;

        this.owner.removedListener().add(this::close);
        this.owner.disabledListener().add(this::close);
        this.owner.positionUpdateListener().add(new Component.PositionUpdateListener(this::isOpen, this::updatePopupPosition));
    }

    public final boolean isOpen() {
        return this.layer != null;
    }

    public final ComponentPopup open() {
        if (this.isOpen()) return this;
        Rivet rivet = this.owner.rivet();
        if (rivet == null) {
            throw new IllegalStateException("Owner component must be attached to a Rivet instance to open a popup");
        }

        Container container = new Container(AbsoluteLayout.INSTANCE);
        if (this.interceptOutsideClicks.get()) {
            SolidColor clickInterceptor = new SolidColor();
            clickInterceptor.mouseDownListener().add((ctx, event, size) -> {
                ctx.cancel(true);
                this.close();
            });
            clickInterceptor.mouseMoveListener().add((ctx, event, size) -> ctx.cancel(true));
            container.add(clickInterceptor.layoutOptions(new AbsoluteOptions(0, 0, -1F, -1F)));
        }
        container.add(this.child);
        this.layer = new Layer(container, LayerBucket.OVERLAY);
        rivet.addLayer(this.layer);
        this.updatePopupPosition(this.owner.absoluteBounds());
        this.openedListener.call(Runnable::run);
        return this;
    }

    public final ComponentPopup close() {
        if (!this.isOpen()) return this;
        Rivet rivet = this.owner.rivet();
        if (rivet != null) {
            rivet.removeLayer(this.layer);
        }
        this.layer = null;
        this.closedListener.call(Runnable::run);
        return this;
    }

    public final void updatePopupPosition(final Rectangle absoluteBounds) {
        Rivet rivet = this.owner.rivet();
        if (rivet == null) return;

        Size screenSize = rivet.scaledSize();
        Size availableSize = switch (this.position) {
            case UP -> new Size(
                    this.matchOwnerWidth ? absoluteBounds.width() : (screenSize.width() - absoluteBounds.x()),
                    absoluteBounds.y()
            );
            case DOWN -> new Size(
                    this.matchOwnerWidth ? absoluteBounds.width() : (screenSize.width() - absoluteBounds.x()),
                    screenSize.height() - (absoluteBounds.y() + absoluteBounds.height())
            );
            case LEFT_UP -> new Size(
                    absoluteBounds.x(),
                    absoluteBounds.y() + absoluteBounds.height()
            );
            case LEFT_CENTER -> new Size(
                    absoluteBounds.x(),
                    screenSize.height()
            );
            case LEFT_DOWN -> new Size(
                    absoluteBounds.x(),
                    screenSize.height() - absoluteBounds.y()
            );
            case RIGHT_UP -> new Size(
                    screenSize.width() - (absoluteBounds.x() + absoluteBounds.width()),
                    absoluteBounds.y() + absoluteBounds.height()
            );
            case RIGHT_CENTER -> new Size(
                    screenSize.width() - (absoluteBounds.x() + absoluteBounds.width()),
                    screenSize.height()
            );
            case RIGHT_DOWN -> new Size(
                    screenSize.width() - (absoluteBounds.x() + absoluteBounds.width()),
                    screenSize.height() - absoluteBounds.y()
            );
        };
        Size maxSize = this.maxSize.get();
        Size idealSize = switch (this.sizeBehavior) {
            case CLAMP -> availableSize;
            case MOVE -> screenSize;
        };
        idealSize = this.child.computeIdealSize(idealSize.min(maxSize));

        float width = idealSize.width();
        if (this.matchOwnerWidth && (this.position.equals(Position.UP) || this.position.equals(Position.DOWN))) {
            width = absoluteBounds.width();
        }
        float height = idealSize.height();
        if (this.sizeBehavior.equals(SizeBehavior.CLAMP)) {
            width = Math.min(width, availableSize.width());
            height = Math.min(height, availableSize.height());
        }
        width = MathUtils.clamp(Math.min(width, maxSize.width()), this.child.minSize().width(), this.child.maxSize().width());
        height = MathUtils.clamp(Math.min(height, maxSize.height()), this.child.minSize().height(), this.child.maxSize().height());

        float x = 0;
        float y = 0;
        switch (this.position) {
            case UP -> {
                x = absoluteBounds.x();
                y = absoluteBounds.y() - height;
            }
            case DOWN -> {
                x = absoluteBounds.x();
                y = absoluteBounds.y() + absoluteBounds.height();
            }
            case LEFT_UP -> {
                x = absoluteBounds.x() - width;
                y = absoluteBounds.y() + absoluteBounds.height() - height;
            }
            case LEFT_CENTER -> {
                x = absoluteBounds.x() - width;
                y = absoluteBounds.y() + (absoluteBounds.height() - height) / 2F;
            }
            case LEFT_DOWN -> {
                x = absoluteBounds.x() - width;
                y = absoluteBounds.y();
            }
            case RIGHT_UP -> {
                x = absoluteBounds.x() + absoluteBounds.width();
                y = absoluteBounds.y() + absoluteBounds.height() - height;
            }
            case RIGHT_CENTER -> {
                x = absoluteBounds.x() + absoluteBounds.width();
                y = absoluteBounds.y() + (absoluteBounds.height() - height) / 2F;
            }
            case RIGHT_DOWN -> {
                x = absoluteBounds.x() + absoluteBounds.width();
                y = absoluteBounds.y();
            }
        }
        if (this.sizeBehavior.equals(SizeBehavior.MOVE)) {
            x = MathUtils.clamp(x, 0, Math.max(0, screenSize.width() - width));
            y = MathUtils.clamp(y, 0, Math.max(0, screenSize.height() - height));
        }

        if (!(this.child.layoutOptions() instanceof AbsoluteOptions options)
                || options.x() != x || options.y() != y
                || options.width() == null || options.width() != width
                || options.height() == null || options.height() != height) {
            this.child.layoutOptions(new AbsoluteOptions(new Rectangle(x, y, width, height)));
        }
    }


    public enum Position {
        UP, DOWN,
        LEFT_UP, LEFT_CENTER, LEFT_DOWN,
        RIGHT_UP, RIGHT_CENTER, RIGHT_DOWN
    }

    public enum SizeBehavior {
        CLAMP, MOVE
    }

}
