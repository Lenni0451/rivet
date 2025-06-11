package net.lenni0451.rivet;

import net.lenni0451.rivet.component.Component;

import javax.annotation.Nullable;

public class RootContainer {

    private Component focusedComponent;

    public Component getFocusedComponent() {
        return this.focusedComponent;
    }

    public void setFocusedComponent(@Nullable final Component component) {
        if (this.focusedComponent == component) return;
        if (this.focusedComponent != null) {
            this.focusedComponent.onFocusLost();
        }
        this.focusedComponent = component;
        if (component != null) {
            component.onFocusGained();
        }
    }

}
