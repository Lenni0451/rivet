package net.lenni0451.rivet.dragdrop;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;

import java.util.List;
import java.util.Set;

@With
@WithBy
public record DragOverEvent(float x, float y, Set<MouseButton> buttons, List<Object> dragData) {

    public static DragOverEvent at(final float x, final float y, final List<?> dragData) {
        return new DragOverEvent(x, y, Set.of(MouseButton.LEFT), List.copyOf(dragData));
    }


    public DragOverEvent(final MouseMoveEvent event, List<Object> dragData) {
        this(event.x(), event.y(), event.buttons(), dragData);
    }

    public DragOverEvent {
        buttons = Set.copyOf(buttons);
        dragData = List.copyOf(dragData);
    }

    public boolean isHeld(final MouseButton button) {
        return this.buttons.contains(button);
    }

}
