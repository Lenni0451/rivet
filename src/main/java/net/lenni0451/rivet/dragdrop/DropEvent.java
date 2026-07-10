package net.lenni0451.rivet.dragdrop;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;

import java.util.List;

@With
@WithBy
public record DropEvent(float x, float y, MouseButtonEvent mouseEvent, List<Object> dragData) {

    public DropEvent {
        dragData = List.copyOf(dragData);
    }

}
