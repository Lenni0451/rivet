package net.lenni0451.rivet.dragdrop;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;

import java.util.List;
import java.util.Set;

@With
@WithBy
public record DropEvent(float x, float y, MouseButtonEvent mouseEvent, List<Object> dragData) {

    public static DropEvent at(final float x, final float y, final List<?> dragData) {
        MouseButtonEvent mouseEvent = new MouseButtonEvent(x, y, MouseButton.LEFT, Set.of(), Set.of());
        return new DropEvent(x, y, mouseEvent, List.copyOf(dragData));
    }


    public DropEvent {
        dragData = List.copyOf(dragData);
    }

}
