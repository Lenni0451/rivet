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
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayoutOptions;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class DragAndDropManager {

    private static final float STACK_OFFSET = 5;
    private static final int STACK_SIZE = 3;

    private final Rivet rivet;

    @Getter
    private boolean dragging;
    @Getter
    private List<Object> dragData = List.of();
    private GhostContainer ghostContainer;
    private float ghostOffsetX;
    private float ghostOffsetY;

    private Layer dragLayer;
    private Layer hoveredDragLayer;

    private float mouseX = -Float.MAX_VALUE;
    private float mouseY = -Float.MAX_VALUE;

    public void startDrag(@Nullable final Object dragData, @Nullable final Component ghostComponent) {
        this.startDrag(
                dragData == null ? List.of() : List.of(dragData),
                ghostComponent == null ? List.of() : List.of(ghostComponent),
                0, 0
        );
    }

    public void startDrag(@Nullable final Object dragData, @Nullable final Component ghostComponent, final float ghostOffsetX, final float ghostOffsetY) {
        this.startDrag(
                dragData == null ? List.of() : List.of(dragData),
                ghostComponent == null ? List.of() : List.of(ghostComponent),
                ghostOffsetX, ghostOffsetY
        );
    }

    public void startDrag(final List<Object> dragData, final List<Component> ghostComponents) {
        this.startDrag(dragData, ghostComponents, 0, 0);
    }

    public void startDrag(final List<Object> dragData, final Function<Object, Component> ghostFactory) {
        this.startDrag(dragData, ghostFactory, 0, 0);
    }

    public void startDrag(final List<Object> dragData, final Function<Object, Component> ghostFactory, final float ghostOffsetX, final float ghostOffsetY) {
        List<Component> ghostComponents = dragData.stream().map(ghostFactory).toList();
        this.startDrag(dragData, ghostComponents, ghostOffsetX, ghostOffsetY);
    }

    public void startDrag(final List<Object> dragData, final List<Component> ghostComponents, final float ghostOffsetX, final float ghostOffsetY) {
        this.dragging = true;
        this.dragData = List.copyOf(dragData);
        if (ghostComponents.isEmpty()) {
            this.cancelDrag();
            return;
        }

        this.ghostOffsetX = ghostOffsetX;
        this.ghostOffsetY = ghostOffsetY;
        this.ghostContainer = new GhostContainer();
        for (int i = Math.min(ghostComponents.size(), STACK_SIZE) - 1; i >= 0; i--) {
            this.ghostContainer.addChild(ghostComponents.get(i));
        }
        this.ghostContainer.layoutOptions(new AbsoluteLayoutOptions(this.mouseX + this.ghostOffsetX, this.mouseY + this.ghostOffsetY));
        Container dragContainer = new Container(AbsoluteLayout.INSTANCE) {
            @Override
            public void render(final Renderer renderer, final Size size) {
                if (DragAndDropManager.this.mouseX != -Float.MAX_VALUE) {
                    super.render(renderer, size);
                }
            }
        };
        dragContainer.addChild(this.ghostContainer);
        this.dragLayer = new Layer(dragContainer, LayerBucket.DRAG);
        this.rivet.addLayer(this.dragLayer);
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
        this.dragData = List.of();
        this.ghostContainer = null;
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
            this.ghostContainer.layoutOptions(new AbsoluteLayoutOptions(this.mouseX + this.ghostOffsetX, this.mouseY + this.ghostOffsetY));
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


    private static class GhostContainer extends Container {
        public GhostContainer() {
            super(new StackLayout());
        }

        private static class StackLayout implements Layout {
            @Override
            public Size computeIdealSize(final Size constraints, final Collection<Component> components) {
                float offset = 0;
                float width = 0;
                float height = 0;
                for (Component child : components) {
                    Size idealSize = child.computeIdealSize(constraints);
                    if (width <= 0) {
                        width = this.widthOf(child, idealSize);
                    } else {
                        width = Math.max(width, offset + this.widthOf(child, idealSize));
                    }
                    if (height <= 0) {
                        height = this.heightOf(child, idealSize);
                    } else {
                        height = Math.max(height, offset + this.heightOf(child, idealSize));
                    }
                    offset += STACK_OFFSET;
                }
                return new Size(width, height);
            }

            @Override
            public void layoutComponents(final Size containerSize, final Collection<Component> components, final BiConsumer<Component, Rectangle> setBounds) {
                float offset = STACK_OFFSET * (components.size() - 1);
                for (Component component : components) {
                    Size idealSize = component.computeIdealSize(containerSize);
                    setBounds.accept(component, new Rectangle(offset, offset, this.widthOf(component, idealSize), this.heightOf(component, idealSize)));
                    offset -= STACK_OFFSET;
                }
            }
        }
    }

}
