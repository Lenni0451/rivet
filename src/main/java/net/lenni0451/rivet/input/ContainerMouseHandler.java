package net.lenni0451.rivet.input;

import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ContainerMouseHandler<C> {

    private C hoveredComponent;
    private C clickedComponent;
    private final Set<MouseButton> componentMouseButtons = EnumSet.noneOf(MouseButton.class);
    private final Set<MouseButton> nonComponentMouseButtons = EnumSet.noneOf(MouseButton.class);

    public boolean isMouseHeld() {
        return !this.componentMouseButtons.isEmpty() || !this.nonComponentMouseButtons.isEmpty();
    }

    public void checkAndRemove(final C component) {
        if (this.hoveredComponent == component) {
            this.hoveredComponent = null;
        }
        if (this.clickedComponent == component) {
            this.clickedComponent = null;
            this.componentMouseButtons.clear();
        }
    }

    public void clear() {
        this.hoveredComponent = null;
        this.clickedComponent = null;
        this.componentMouseButtons.clear();
    }


    public void onMouseLeave(final Consumer<C> mouseLeaveInvoker) {
        if (this.hoveredComponent != null) {
            mouseLeaveInvoker.accept(this.hoveredComponent);
            this.hoveredComponent = null;
        }
    }

    public boolean onMouseDown(final MouseButtonEvent event, @Nullable final C hoveredComponent, final Predicate<C> componentMouseDown, final BooleanSupplier containerMouseDown) {
        if (this.nonComponentMouseButtons.isEmpty() && hoveredComponent != null) {
            if (this.clickedComponent == null || this.clickedComponent == hoveredComponent) {
                this.clickedComponent = hoveredComponent;
                this.componentMouseButtons.add(event.button());
                return componentMouseDown.test(hoveredComponent);
            }
        } else {
            this.nonComponentMouseButtons.add(event.button());
        }
        return containerMouseDown.getAsBoolean();
    }

    public boolean onMouseUp(final MouseButtonEvent event, final Predicate<C> componentMouseUp, final BooleanSupplier containerMouseUp) {
        this.nonComponentMouseButtons.remove(event.button());
        if (this.componentMouseButtons.remove(event.button())) {
            try {
                return componentMouseUp.test(this.clickedComponent);
            } finally {
                if (this.componentMouseButtons.isEmpty()) {
                    this.clickedComponent = null;
                }
            }
        }
        return containerMouseUp.getAsBoolean();
    }

    public boolean onMouseMove(@Nullable final C hoveredComponent, final Consumer<C> componentMouseEnter, final Consumer<C> componentMouseLeave, final Predicate<C> componentMouseMove, final BooleanSupplier containerMouseMove) {
        if (this.hoveredComponent != null && (this.hoveredComponent != hoveredComponent || !this.nonComponentMouseButtons.isEmpty())) {
            componentMouseLeave.accept(this.hoveredComponent);
            this.hoveredComponent = null;
        }
        if (this.hoveredComponent == null && hoveredComponent != null && this.nonComponentMouseButtons.isEmpty() && (this.clickedComponent == null || this.clickedComponent == hoveredComponent)) {
            this.hoveredComponent = hoveredComponent;
            componentMouseEnter.accept(this.hoveredComponent);
        }
        if (this.clickedComponent != null) {
            return componentMouseMove.test(this.clickedComponent);
        } else if (this.hoveredComponent != null) {
            return componentMouseMove.test(this.hoveredComponent);
        } else {
            return containerMouseMove.getAsBoolean();
        }
    }

    public boolean onMouseScroll(@Nullable final C hoveredComponent, final Predicate<C> componentMouseScroll, final BooleanSupplier containerMouseScroll) {
        if (hoveredComponent != null) {
            return componentMouseScroll.test(hoveredComponent);
        } else {
            return containerMouseScroll.getAsBoolean();
        }
    }

}
