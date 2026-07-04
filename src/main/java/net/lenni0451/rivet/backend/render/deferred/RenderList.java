package net.lenni0451.rivet.backend.render.deferred;

import java.util.List;

public record RenderList(List<ModifierCommand> modifiers, List<RenderElement> elements) implements RenderElement {

    public RenderList {
        modifiers = List.copyOf(modifiers);
        elements = List.copyOf(elements);
    }

}
