package net.lenni0451.rivet.dragdrop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layer.Layer;
import net.lenni0451.rivet.layer.LayerBucket;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayoutOptions;
import net.lenni0451.rivet.math.Size;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class DragAndDropManager {

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

    private Layer dragLayer;
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
            this.ghostComponent.layoutOptions(new AbsoluteLayoutOptions(
                    this.mouseX + this.ghostOffsetX, this.mouseY + this.ghostOffsetY,
                    this.ghostSize.width(), this.ghostSize.height()
            ));

            Container dragContainer = new Container(AbsoluteLayout.INSTANCE) {
                @Override
                public void render(final Renderer renderer, final Size size) {
                    if (DragAndDropManager.this.mouseX != -Float.MAX_VALUE) {
                        super.render(renderer, size);
                    }
                }
            };
            dragContainer.addChild(this.ghostComponent);
            this.dragLayer = new Layer(dragContainer, LayerBucket.DRAG);
            this.rivet.addLayer(this.dragLayer);
        }
    }

    public void cancelDrag() {
        if (this.dragLayer != null) {
            this.rivet.removeLayer(this.dragLayer);
            this.dragLayer = null;
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

    public boolean onMouseUp(final MouseButtonEvent event, final Supplier<List<Layer>> hoveredLayerSupplier) {
        if (!this.dragging) return false;

        List<Layer> hoveredLayers = hoveredLayerSupplier.get();
        if (!hoveredLayers.isEmpty()) {
            DropEvent dropEvent = new DropEvent(event.x(), event.y(), event, this.dragData);
            for (Layer hoveredLayer : hoveredLayers) {
                boolean handled = hoveredLayer.container().onDrop(dropEvent, this.rivet.scaledSize());
                if (handled) break;
            }
        }
        this.cancelDrag();
        return true;
    }

    public boolean onMouseMove(final MouseMoveEvent event, final Supplier<List<Layer>> hoveredLayerSupplier) {
        if (!this.dragging) return false;
        this.mouseX = event.x();
        this.mouseY = event.y();

        if (this.dragLayer != null) {
            this.ghostComponent.layoutOptions(new AbsoluteLayoutOptions(
                    this.mouseX + this.ghostOffsetX, this.mouseY + this.ghostOffsetY,
                    this.ghostSize.width(), this.ghostSize.height()
            ));
            this.dragLayer.requestLayoutRecalculation();
        }

        List<Layer> hoveredLayers = hoveredLayerSupplier.get();
        Layer handledLayer = null;
        if (!hoveredLayers.isEmpty()) {
            for (Layer hoveredLayer : hoveredLayers) {
                DragOverEvent dragOverEvent = new DragOverEvent(event.x(), event.y(), event, this.dragData);
                boolean handled = hoveredLayer.container().onDragOver(dragOverEvent, this.rivet.scaledSize());
                if (handled) {
                    handledLayer = hoveredLayer;
                    break;
                }
            }
        }
        if (this.hoveredDragLayer != null && this.hoveredDragLayer != handledLayer) {
            this.hoveredDragLayer.container().onDragLeave();
        }
        this.hoveredDragLayer = handledLayer;
        return handledLayer != null;
    }

}
