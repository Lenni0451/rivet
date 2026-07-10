package test.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.Container;
import net.lenni0451.rivet.component.container.DecoratedContainer;
import net.lenni0451.rivet.component.container.DynamicListView;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.layout.tile.TileLayout;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import test.TestBase;

import java.util.ArrayList;
import java.util.List;

public class DragBetweenTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        List<String> leftLabels = new ArrayList<>();
        leftLabels.add("A");
        leftLabels.add("B");
        leftLabels.add("C");
        List<String> rightLabels = new ArrayList<>();
        rightLabels.add("D");
        rightLabels.add("E");

        Container container = new Container(new TileLayout(2, 1));
        container.addChild(this.newListView(leftLabels, rightLabels));
        container.addChild(this.newListView(rightLabels, leftLabels));
        rivet.root().addChild(container);
    }

    private Component newListView(final List<String> list, final List<String> otherList) {
        DynamicListView<String> listView = new DynamicListView<>(new VerticalListLayout(0, true), DropMarkerStrategy.vertical(0, 2), DraggableLabel.class::isInstance, () -> list, DraggableLabel::new);
        listView.reorderListener().add((dragData, insertIndex) -> {
            String text = ((DraggableLabel) dragData.getFirst()).text();
            if (list.contains(text)) {
                int sourceIndex = list.indexOf(text);
                list.remove(sourceIndex);
                if (sourceIndex < insertIndex) insertIndex--;
                list.add(insertIndex, text);
            }
            if (otherList.contains(text)) {
                otherList.remove(text);
                list.add(insertIndex, text);
            }
        });

        SolidColor fallbackDragHandler = new SolidColor();
        fallbackDragHandler.dropListener().add((event, size) -> {
            if (event.dragData().getFirst() instanceof DraggableLabel) {
                String text = ((DraggableLabel) event.dragData().getFirst()).text();
                if (otherList.contains(text)) {
                    otherList.remove(text);
                    list.add(text);
                    return true;
                }
            }
            return false;
        });
        return new DecoratedContainer(fallbackDragHandler, listView);
    }


    private static class DraggableLabel extends Label {
        private static final Color HOVERED_COLOR = Color.GRAY.withAlpha(100);

        private boolean hovered;

        public DraggableLabel(final String text) {
            super(text);
            this.horizontalOrigin(TextOrigin.Horizontal.VISUAL_LEFT);
        }

        @Override
        protected void onComponentMouseEnter() {
            this.hovered = true;
        }

        @Override
        protected void onComponentMouseLeave() {
            this.hovered = false;
        }

        @Override
        protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
            return true;
        }

        @Override
        protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
            if (event.buttons().contains(MouseButton.LEFT) && !this.rivet().dragAndDropManager().isDragging()) {
                this.rivet().dragAndDropManager().startDrag(this, new Label(this.text()));
            }
            return true;
        }

        @Override
        public void render(final Renderer renderer, final Size size) {
            if (this.hovered) {
                renderer.fillRect(0, 0, size.width(), size.height(), HOVERED_COLOR);
            }
            super.render(renderer, size);
        }
    }

}
