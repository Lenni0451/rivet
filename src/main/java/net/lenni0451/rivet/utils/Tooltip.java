package net.lenni0451.rivet.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerBucket;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class Tooltip {

    private final Component component;
    private final Supplier<Component> tooltip;
    private final BooleanSupplier mouseEnterListener = () -> {
        this.onMouseEnter();
        return false;
    };
    private final BooleanSupplier mouseLeaveListener = () -> {
        this.onMouseLeave();
        return false;
    };
    private final BiPredicate<MouseMoveEvent, Size> mouseMoveListener = (event, size) -> {
        this.onMouseMove(event);
        return false;
    };
    private final Runnable removedListener = this::onRemoved;
    private final Runnable renderListener = this::onRenderTick;

    @Getter
    private final ThemeOption<Long> delay;
    @Getter
    private final ThemeOption<Boolean> removeOnMouseMove;
    @Getter
    private final ThemeOption<Integer> mouseOffset;
    @Getter
    @Setter
    private boolean hideOnDrag;

    private boolean mouseOver;
    private long lastMoveTime;
    private float mouseOffsetX;
    private float mouseOffsetY;
    private Layer layer;
    private Component currentTooltip;

    public Tooltip(final Component component, final Supplier<Component> tooltip) {
        this.component = component;
        this.tooltip = tooltip;

        this.delay = new ThemeOption<>(component, Theme.TOOLTIP_DELAY);
        this.removeOnMouseMove = new ThemeOption<>(component, Theme.TOOLTIP_REMOVE_ON_MOUSE_MOVE);
        this.mouseOffset = new ThemeOption<>(component, Theme.TOOLTIP_MOUSE_OFFSET);

        this.register();
    }

    public Tooltip register() {
        this.component.mouseEnterListener().add(this.mouseEnterListener);
        this.component.mouseLeaveListener().add(this.mouseLeaveListener);
        this.component.mouseMoveListener().add(this.mouseMoveListener);
        this.component.removedListener().add(this.removedListener);
        return this;
    }

    public Tooltip unregister() {
        this.component.mouseEnterListener().remove(this.mouseEnterListener);
        this.component.mouseLeaveListener().remove(this.mouseLeaveListener);
        this.component.mouseMoveListener().remove(this.mouseMoveListener);
        this.component.removedListener().remove(this.removedListener);
        this.stopRenderTick();
        this.remove();
        return this;
    }

    private void add() {
        if (this.layer == null) {
            if (this.hideOnDrag && this.component.rivet().dragAndDropManager().isDragging()) {
                return;
            }

            this.currentTooltip = this.tooltip.get();
            this.layer = new Layer(new Container(AbsoluteLayout.INSTANCE).addChild(this.currentTooltip), LayerBucket.TOOLTIP);
            this.component.rivet().addLayer(this.layer);
        }
    }

    private void remove() {
        if (this.layer != null) {
            this.component.rivet().removeLayer(this.layer);
            this.layer = null;
            this.currentTooltip = null;
        }
    }

    private void startRenderTick() {
        this.component.rivet().renderListener().add(this.renderListener);
    }

    private void stopRenderTick() {
        this.component.rivet().renderListener().remove(this.renderListener);
    }


    private void onMouseEnter() {
        this.mouseOver = true;
        this.lastMoveTime = System.currentTimeMillis();
        this.startRenderTick();
    }

    private void onMouseLeave() {
        this.mouseOver = false;
        this.stopRenderTick();
        this.remove();
    }

    private void onMouseMove(final MouseMoveEvent event) {
        if (Math.abs(this.mouseOffsetX - event.x()) > 1 || Math.abs(this.mouseOffsetY - event.y()) > 1) {
            this.lastMoveTime = System.currentTimeMillis();
            if (this.removeOnMouseMove.value()) {
                this.remove();
            }
        }
        this.mouseOffsetX = event.x();
        this.mouseOffsetY = event.y();
    }

    private void onRemoved() {
        this.stopRenderTick();
        this.remove();
    }

    private void onRenderTick() {
        if (!this.mouseOver) {
            this.stopRenderTick();
            this.remove();
            return;
        } else if (this.hideOnDrag && this.component.rivet().dragAndDropManager().isDragging()) {
            this.remove();
            return;
        } else if (this.layer == null) {
            if (System.currentTimeMillis() - this.lastMoveTime >= this.delay.value()) {
                this.add();
            }
        }
        if (this.currentTooltip != null) {
            Rectangle absoluteBounds = this.component.absoluteBounds();
            this.currentTooltip.layoutOptions(new AbsoluteOptions(
                    absoluteBounds.x() + this.mouseOffsetX + this.mouseOffset.value(),
                    absoluteBounds.y() + this.mouseOffsetY + this.mouseOffset.value()
            ));
        }
    }

}
