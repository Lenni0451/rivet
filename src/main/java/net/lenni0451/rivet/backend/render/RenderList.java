package net.lenni0451.rivet.backend.render;

import java.util.List;

public record RenderList(List<TransformCommand> transforms, List<RenderElement> elements) implements RenderElement {

    public RenderList {
        transforms = List.copyOf(transforms);
        elements = List.copyOf(elements);
    }

}
