package net.lenni0451.rivet.dragdrop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DragAndDropManager {

    private final Rivet rivet;

    @Getter
    private boolean dragging;
    @Getter
    private Object dragData;
    @Getter
    private Component ghostComponent;
    @Getter
    private Size ghostSize = Size.EMPTY;
    @Getter
    private float ghostOffsetX;
    @Getter
    private float ghostOffsetY;

    private Layer hoveredDragLayer;

    private float mouseX = -Float.MAX_VALUE;
    private float mouseY = -Float.MAX_VALUE;

    public void startDrag(final Object dragData, final Component ghostComponent, final Size ghostSize) {
        this.startDrag(dragData, ghostComponent, ghostSize, 0, 0);
    }

    public void startDrag(final Object dragData, final Component ghostComponent, final Size ghostSize, final float ghostOffsetX, final float ghostOffsetY) {
        this.dragging = true;
        this.dragData = dragData;
        this.ghostComponent = ghostComponent;
        this.ghostSize = ghostSize;
        this.ghostOffsetX = ghostOffsetX;
        this.ghostOffsetY = ghostOffsetY;

        if (this.ghostComponent != null) {
            this.ghostComponent.setRivet(this.rivet, this.rivet.root());
            this.ghostComponent.computeLayout(this.ghostSize);
        }
    }

    public void cancelDrag() {
        if (this.ghostComponent != null) {
            this.ghostComponent.setRivet(null, null);
        }
        if (this.hoveredDragLayer != null) {
            this.hoveredDragLayer.container().onDragLeave();
        }
        this.dragging = false;
        this.dragData = null;
        this.ghostComponent = null;
        this.ghostSize = Size.EMPTY;
        this.ghostOffsetX = 0;
        this.ghostOffsetY = 0;
        this.hoveredDragLayer = null;
        this.mouseX = -Float.MAX_VALUE;
        this.mouseY = -Float.MAX_VALUE;
    }

    public boolean onMouseUp(final MouseButtonEvent event, final Supplier<Layer> hoveredLayerSupplier) {
        if (!this.dragging) return false;

        Layer hoveredLayer = hoveredLayerSupplier.get();
        if (hoveredLayer != null) {
            DropEvent dropEvent = new DropEvent(event.x(), event.y(), event, this.dragData);
            hoveredLayer.container().onDrop(dropEvent, new Rectangle(this.rivet.scaledSize()));
        }
        this.cancelDrag();
        return true;
    }

    public boolean onMouseMove(final MouseMoveEvent event, final Supplier<Layer> hoveredLayerSupplier) {
        if (!this.dragging) return false;
        this.mouseX = event.x();
        this.mouseY = event.y();

        Layer hoveredLayer = hoveredLayerSupplier.get();
        if (this.hoveredDragLayer != null && this.hoveredDragLayer != hoveredLayer) {
            this.hoveredDragLayer.container().onDragLeave();
        }
        this.hoveredDragLayer = hoveredLayer;
        if (hoveredLayer != null) {
            DragOverEvent dragOverEvent = new DragOverEvent(event.x(), event.y(), event, this.dragData);
            hoveredLayer.container().onDragOver(dragOverEvent, new Rectangle(this.rivet.scaledSize()));
        }
        return false;
    }

    public void render(final Renderer renderer) {
        if (this.dragging && this.ghostComponent != null && this.mouseX != -Float.MAX_VALUE) {
            renderer.translate(this.mouseX + this.ghostOffsetX, this.mouseY + this.ghostOffsetY, () -> {
                renderer.componentBounds(0, 0, this.ghostSize.width(), this.ghostSize.height(), () -> {
                    Rectangle renderBounds = new Rectangle(0, 0, this.ghostSize.width(), this.ghostSize.height());
                    this.ghostComponent.render(renderer, renderBounds);
                });
            });
        }
    }

}
