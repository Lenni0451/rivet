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
public class RenderList {

    @Getter
    @Nullable
    private final TransformCommand transform;
    private final List<RenderCommand> renders = new ArrayList<>();
    private final List<RenderList> subLists = new ArrayList<>();

    public RenderList() {
        this(null);
    }

    public List<RenderCommand> renders() {
        return List.copyOf(this.renders);
    }

    public void render(final RenderCommand command) {
        this.renders.add(command);
    }

    public List<RenderList> subLists() {
        return List.copyOf(this.subLists);
    }

    public void subList(final RenderList subList) {
        this.subLists.add(subList);
    }

}
