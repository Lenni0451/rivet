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
import net.lenni0451.rivet.math.Size;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
                        this.relativeBounds(Size.EMPTY, component).size() //TODO: Is this even necessary?
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

    public EventState onMouseDown(final Rivet rivet, final MouseButtonEvent event, final Size containerBounds) {
        List<E> hoveredElements = this.elementsAt(event.x(), event.y(), containerBounds);
        boolean nonComponentButtonsHeld = event.heldButtons().size() - 1 > this.componentMouseButtons.size();
        if (this.clickedElement != null) {
            if (hoveredElements.contains(this.clickedElement)) {
                this.componentMouseButtons.add(event.button());
                Component hoveredComponent = this.map(this.clickedElement);
                Rectangle clickedRelativeBounds = this.relativeBounds(containerBounds, this.clickedElement);
                rivet.focusedComponent(hoveredComponent);
                return EventState.component(hoveredComponent.onMouseDown(
                        event.withX(event.x() - clickedRelativeBounds.x()).withY(event.y() - clickedRelativeBounds.y()),
                        clickedRelativeBounds.size()
                ));
            }
            return EventState.NOT_HANDLED;
        } else if (!nonComponentButtonsHeld && !hoveredElements.isEmpty()) {
            for (E hoveredElement : hoveredElements) {
                Component hoveredComponent = this.map(hoveredElement);
                Rectangle clickedRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);

                this.clickedElement = hoveredElement;
                this.componentMouseButtons.add(event.button());

                if (hoveredComponent.onMouseDown(
                        event.withX(event.x() - clickedRelativeBounds.x()).withY(event.y() - clickedRelativeBounds.y()),
                        clickedRelativeBounds.size()
                )) {
                    if (this.clickedElement != null) {
                        // Only set focus if the component didn't remove itself during onMouseDown (like a combobox popup)
                        // checkAndRemove would have already cleared clickedElement
                        rivet.focusedComponent(hoveredComponent);
                    }
                    return EventState.HANDLED;
                }
            }
            this.clickedElement = null;
            this.componentMouseButtons.remove(event.button());
            return EventState.NOT_HANDLED;
        }
        return EventState.MISS;
    }

    public EventState onMouseUp(final Rivet rivet, final MouseButtonEvent event, final Size containerBounds) {
        if (this.componentMouseButtons.remove(event.button())) {
            try {
                Rectangle relativeBounds = this.relativeBounds(containerBounds, this.clickedElement);
                return EventState.component(this.map(this.clickedElement).onMouseUp(
                        event.withX(event.x() - relativeBounds.x()).withY(event.y() - relativeBounds.y()),
                        relativeBounds.size()
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

    public EventState onMouseMove(final MouseMoveEvent event, final Size containerBounds) {
        List<E> hoveredElements = this.elementsAt(event.x(), event.y(), containerBounds);
        boolean nonComponentButtonsHeld = event.buttons().size() > this.componentMouseButtons.size();
        if (this.clickedElement != null) {
            if (hoveredElements.contains(this.clickedElement) && this.hoveredElement != this.clickedElement) {
                if (this.hoveredElement != null) {
                    this.map(this.hoveredElement).onMouseLeave();
                }
                this.map(this.clickedElement).onMouseEnter();
                this.hoveredElement = this.clickedElement;
            } else if (!hoveredElements.contains(this.clickedElement) && this.hoveredElement == this.clickedElement) {
                this.map(this.clickedElement).onMouseLeave();
                this.hoveredElement = null;
            }
        } else {
            if (this.hoveredElement != null && (!hoveredElements.contains(this.hoveredElement) || nonComponentButtonsHeld)) {
                this.map(this.hoveredElement).onMouseLeave();
                this.hoveredElement = null;
            }
            if (!nonComponentButtonsHeld) {
                E newHoveredElement = null;
                for (E hoveredElement : hoveredElements) {
                    if (this.hoveredElement == hoveredElement || this.map(hoveredElement).onMouseEnter()) {
                        newHoveredElement = hoveredElement;
                        break;
                    }
                }
                if (newHoveredElement != this.hoveredElement) {
                    if (this.hoveredElement != null) {
                        this.map(this.hoveredElement).onMouseLeave();
                    }
                    this.hoveredElement = newHoveredElement;
                }
            }
        }

        if (this.clickedElement != null) {
            Component clickedComponent = this.map(this.clickedElement);
            Rectangle clickedRelativeBounds = this.relativeBounds(containerBounds, this.clickedElement);
            return EventState.component(clickedComponent.onMouseMove(
                    event.withX(event.x() - clickedRelativeBounds.x()).withY(event.y() - clickedRelativeBounds.y()).withButtons(this.componentMouseButtons),
                    clickedRelativeBounds.size()
            ));
        } else if (!hoveredElements.isEmpty()) {
            for (E hoveredElement : hoveredElements) {
                Component hoveredComponent = this.map(hoveredElement);
                Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
                if (hoveredComponent.onMouseMove(
                        event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()).withButtons(this.componentMouseButtons),
                        hoveredRelativeBounds.size()
                )) {
                    return EventState.HANDLED;
                }
            }
            return EventState.NOT_HANDLED;
        }
        return EventState.MISS;
    }

    public EventState onMouseScroll(final MouseScrollEvent event, final Size containerBounds) {
        List<E> hoveredElements = this.elementsAt(event.x(), event.y(), containerBounds);
        if (!hoveredElements.isEmpty()) {
            for (E hoveredElement : hoveredElements) {
                Component hoveredComponent = this.map(hoveredElement);
                Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
                if (hoveredComponent.onMouseScroll(
                        event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                        hoveredRelativeBounds.size()
                )) {
                    return EventState.HANDLED;
                }
            }
            return EventState.NOT_HANDLED;
        }
        return EventState.MISS;
    }

    public EventState onDrop(final DropEvent event, final Size containerBounds) {
        List<E> hoveredElements = this.elementsAt(event.x(), event.y(), containerBounds);
        if (!hoveredElements.isEmpty()) {
            for (E hoveredElement : hoveredElements) {
                Component hoveredComponent = this.map(hoveredElement);
                Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
                if (hoveredComponent.onDrop(
                        event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                        hoveredRelativeBounds.size()
                )) {
                    return EventState.HANDLED;
                }
            }
            return EventState.NOT_HANDLED;
        }
        return EventState.MISS;
    }

    public EventState onDragOver(final DragOverEvent event, final Size containerBounds) {
        List<E> hoveredElements = this.elementsAt(event.x(), event.y(), containerBounds);
        if (hoveredElements.isEmpty()) return EventState.MISS;

        for (E hoveredElement : hoveredElements) {
            Component hoveredComponent = this.map(hoveredElement);
            Rectangle hoveredRelativeBounds = this.relativeBounds(containerBounds, hoveredElement);
            if (hoveredComponent.onDragOver(
                    event.withX(event.x() - hoveredRelativeBounds.x()).withY(event.y() - hoveredRelativeBounds.y()),
                    hoveredRelativeBounds.size()
            )) {
                if (this.hoveredDragElement != hoveredElement) {
                    if (this.hoveredDragElement != null) {
                        this.map(this.hoveredDragElement).onDragLeave();
                    }
                    this.hoveredDragElement = hoveredElement;
                }
                return EventState.HANDLED;
            }
        }
        if (this.hoveredDragElement != null) {
            this.map(this.hoveredDragElement).onDragLeave();
            this.hoveredDragElement = null;
        }
        return EventState.NOT_HANDLED;
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

    protected abstract Rectangle relativeBounds(final Size containerBounds, final E element);

    protected abstract List<E> elementsAt(final float x, final float y, final Size containerBounds);


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
    }

}
