package net.lenni0451.rivet.event;

import net.lenni0451.rivet.component.Component;

public abstract class ComponentEvent {

    private final Component owner;

    public ComponentEvent(final Component owner) {
        this.owner = owner;
    }

    public Component owner() {
        return this.owner;
    }

}
