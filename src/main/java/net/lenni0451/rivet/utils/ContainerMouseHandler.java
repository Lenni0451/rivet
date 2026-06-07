package net.lenni0451.rivet.utils;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.math.Rectangle;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public abstract class ContainerMouseHandler<E> {

    private E hoveredElement;
    private E clickedElement;
    private E hoveredDragElement;
    private final Set<MouseButton> componentMouseButtons = EnumSet.noneOf(MouseButton.class);

    public boolean isMouseHeld() {
        return !this.componentMouseButtons.isEmpty();
    }

    public void checkAndRemove(final E component) {
        if (component == null) return;
        if (this.hoveredElement == component) {
            this.map(component).onMouseLeave();
            this.hoveredElement = null;
        }
        if (this.clickedElement == component) {
            for (MouseButton mouseButton : this.componentMouseButtons) {
                this.map(component).onMouseUp(
                        new MouseButtonEvent(-1, -1, mouseButton, Set.of(), Set.of()),
                        this.relativeBounds(Rectangle.EMPTY, component)
                );
            }
            this.clickedElement = null;
            this.componentMouseButtons.clear();
        }
        if (this.hoveredDragElement == component) {
            this.map(component).onDragLeave();
            this.hoveredDragElement = null;
        }
    }

    public void clear() {
        this.checkAndRemove(this.hoveredElement);
        this.checkAndRemove(this.clickedElement);
        this.checkAndRemove(this.hoveredDragElement);
    }

    public void unsafeClear() {
        this.hoveredElement = null;
        this.clickedElement = null;
        this.hoveredDragElement = null;
        this.componentMouseButtons.clear();
    }


    public EventState onMouseLeave() {
        if (this.hoveredElement != null) {
            this.map(this.hoveredElement).onMouseLeave();
            this.hoveredElement = null;
            return EventState.HANDLED;
        }
        return EventState.MISS;
    }

    public EventState onMouseDown(final Rivet rivet, final MouseButtonEvent event, final Rectangle containerBounds) {
        E hoveredElement = this.elementAt(event.x(), event.y(), containerBounds);
        boolean nonComponentButtonsHeld = event.heldButtons().size() - 1 > this.componentMouseButtons.size();
        if (hoveredElement != null && (this.clickedElement == hoveredElement || (this.clickedElement == null && !nonComponentButtonsHeld))) {
            this.clickedElement = hoveredElement;
            this.componentMouseButtons.add(event.button());

            Component hoveredComponent = this.map(hoveredElement);
            Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
            rivet.focusedComponent(hoveredComponent);
            return EventState.component(hoveredComponent.onMouseDown(
                    event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                    new Rectangle(containerBounds.x() + hoveredRelativeBounds.x(), containerBounds.y() + hoveredRelativeBounds.y(), hoveredRelativeBounds.width(), hoveredRelativeBounds.height())
            ));
        }
        return EventState.MISS;
    }

    public EventState onMouseUp(final Rivet rivet, final MouseButtonEvent event, final Rectangle containerBounds) {
        if (this.componentMouseButtons.remove(event.button())) {
            try {
                Rectangle relativeBounds = this.relativeBounds(containerBounds, this.clickedElement);
                return EventState.component(this.map(this.clickedElement).onMouseUp(
                        event.withX(event.x() - relativeBounds.x()).withY(event.y() - relativeBounds.y()),
                        new Rectangle(containerBounds.x() + relativeBounds.x(), containerBounds.y() + relativeBounds.y(), relativeBounds.width(), relativeBounds.height())
                ));
            } finally {
                if (this.componentMouseButtons.isEmpty()) {
                    this.clickedElement = null;
                    rivet.updateMouseState();
                }
            }
        }
        return EventState.MISS;
    }

    public EventState onMouseMove(final MouseMoveEvent event, final Rectangle containerBounds) {
        E hoveredElement = this.elementAt(event.x(), event.y(), containerBounds);
        boolean nonComponentButtonsHeld = event.buttons().size() > this.componentMouseButtons.size();
        if (this.hoveredElement != null && (this.hoveredElement != hoveredElement || nonComponentButtonsHeld)) {
            this.map(this.hoveredElement).onMouseLeave();
            this.hoveredElement = null;
        }
        if (this.hoveredElement == null && hoveredElement != null && !nonComponentButtonsHeld && (this.clickedElement == null || this.clickedElement == hoveredElement)) {
            this.hoveredElement = hoveredElement;
            this.map(hoveredElement).onMouseEnter();
        }
        if (this.clickedElement != null) {
            Component clickedComponent = this.map(this.clickedElement);
            Rectangle clickedRelativeBounds = this.relativeBounds(containerBounds, this.clickedElement);
            return EventState.component(clickedComponent.onMouseMove(
                    event.withX(event.x() - clickedRelativeBounds.x()).withY(event.y() - clickedRelativeBounds.y()).withButtons(this.componentMouseButtons),
                    new Rectangle(containerBounds.x() + clickedRelativeBounds.x(), containerBounds.y() + clickedRelativeBounds.y(), clickedRelativeBounds.width(), clickedRelativeBounds.height())
            ));
        } else if (this.hoveredElement != null) {
            Component hoveredComponent = this.map(this.hoveredElement);
            Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, this.hoveredElement);
            return EventState.component(hoveredComponent.onMouseMove(
                    event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()).withButtons(this.componentMouseButtons),
                    new Rectangle(containerBounds.x() + hoveredRelativeBounds.x(), containerBounds.y() + hoveredRelativeBounds.y(), hoveredRelativeBounds.width(), hoveredRelativeBounds.height())
            ));
        } else {
            return EventState.MISS;
        }
    }

    public EventState onMouseScroll(final MouseScrollEvent event, final Rectangle containerBounds) {
        E hoveredElement = this.elementAt(event.x(), event.y(), containerBounds);
        if (hoveredElement != null) {
            Component hoveredComponent = this.map(hoveredElement);
            Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
            return EventState.component(hoveredComponent.onMouseScroll(
                    event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                    new Rectangle(containerBounds.x() + hoveredRelativeBounds.x(), containerBounds.y() + hoveredRelativeBounds.y(), hoveredRelativeBounds.width(), hoveredRelativeBounds.height())
            ));
        }
        return EventState.MISS;
    }

    public EventState onDrop(final DropEvent event, final Rectangle containerBounds) {
        E hoveredElement = this.elementAt(event.x(), event.y(), containerBounds);
        if (hoveredElement != null) {
            Component hoveredComponent = this.map(hoveredElement);
            Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
            return EventState.component(hoveredComponent.onDrop(
                    event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                    new Rectangle(containerBounds.x() + hoveredRelativeBounds.x(), containerBounds.y() + hoveredRelativeBounds.y(), hoveredRelativeBounds.width(), hoveredRelativeBounds.height())
            ));
        }
        return EventState.MISS;
    }

    public EventState onDragOver(final DragOverEvent event, final Rectangle containerBounds) {
        E hoveredElement = this.elementAt(event.x(), event.y(), containerBounds);
        if (this.hoveredDragElement != null && this.hoveredDragElement != hoveredElement) {
            this.map(this.hoveredDragElement).onDragLeave();
            this.hoveredDragElement = null;
        }
        if (hoveredElement != null) {
            this.hoveredDragElement = hoveredElement;

            Component hoveredComponent = this.map(hoveredElement);
            Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
            return EventState.component(hoveredComponent.onDragOver(
                    event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                    new Rectangle(containerBounds.x() + hoveredRelativeBounds.x(), containerBounds.y() + hoveredRelativeBounds.y(), hoveredRelativeBounds.width(), hoveredRelativeBounds.height())
            ));
        }
        return EventState.MISS;
    }

    public EventState onDragLeave() {
        if (this.hoveredDragElement != null) {
            this.map(this.hoveredDragElement).onDragLeave();
            this.hoveredDragElement = null;
            return EventState.HANDLED;
        }
        return EventState.MISS;
    }


    protected abstract Component map(final E element);

    protected abstract Rectangle relativeBounds(final Rectangle containerBounds, final E element);

    @Nullable
    protected abstract E elementAt(final float x, final float y, final Rectangle containerBounds);

    protected abstract List<E> allElementsAt(final float x, final float y, final Rectangle containerBounds);


    public enum EventState {
        HANDLED,
        NOT_HANDLED,
        MISS;

        public static EventState component(boolean handled) {
            return handled ? HANDLED : NOT_HANDLED;
        }

        public boolean handled() {
            return this == HANDLED;
        }

        public boolean handled(final BooleanSupplier onMiss) {
            return switch (this) {
                case HANDLED -> true;
                case NOT_HANDLED -> false;
                case MISS -> onMiss.getAsBoolean();
            };
        }
    }

}
