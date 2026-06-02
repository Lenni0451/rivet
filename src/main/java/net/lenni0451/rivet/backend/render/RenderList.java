package net.lenni0451.rivet.backend.render;

import javax.annotation.Nullable;
import java.util.List;

public record RenderList(@Nullable TransformCommand transform, List<RenderElement> elements) implements RenderElement {

    public RenderList {
        elements = List.copyOf(elements);
    }

}
