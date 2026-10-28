package net.lenni0451.rivet.dragdrop;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;

import java.util.List;
import java.util.Set;

@With
@WithBy
public record DragOverEvent(float x, float y, MouseMoveEvent mouseEvent, List<Object> dragData) {

    public static DragOverEvent at(final float x, final float y, final List<?> dragData) {
        MouseMoveEvent mouseEvent = new MouseMoveEvent(x, y, Set.of(MouseButton.LEFT));
        return new DragOverEvent(x, y, mouseEvent, List.copyOf(dragData));
    }


    public DragOverEvent {
        dragData = List.copyOf(dragData);
    }

}
