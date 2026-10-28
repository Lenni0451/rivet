package net.lenni0451.rivet.dragdrop;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;

import java.util.List;
import java.util.Set;

@With
@WithBy
public record DropEvent(float x, float y, MouseButton button, Set<ModifierKey> modifiers, Set<MouseButton> heldButtons, List<Object> dragData) {

    public static DropEvent at(final float x, final float y, final List<?> dragData) {
        return new DropEvent(x, y, MouseButton.LEFT, Set.of(), Set.of(), List.copyOf(dragData));
    }


    public DropEvent(final MouseButtonEvent event, List<Object> dragData) {
        this(event.x(), event.y(), event.button(), event.modifiers(), event.heldButtons(), dragData);
    }

    public DropEvent {
        modifiers = Set.copyOf(modifiers);
        heldButtons = Set.copyOf(heldButtons);
        dragData = List.copyOf(dragData);
    }

}
