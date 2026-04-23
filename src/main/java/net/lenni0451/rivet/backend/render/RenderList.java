package net.lenni0451.rivet.backend.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ToString
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public final class RenderList implements RenderElement {

    @Getter
    @Nullable
    private final TransformCommand transform;
    private final List<RenderElement> elements = new ArrayList<>();

    public RenderList() {
        this(null);
    }

    public List<RenderElement> elements() {
        return List.copyOf(this.elements);
    }

    public void render(final RenderElement element) {
        this.elements.add(element);
    }

}
