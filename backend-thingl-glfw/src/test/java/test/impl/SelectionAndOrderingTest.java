package test.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.container.ReorderableContainer;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.utils.SelectionModel;
import test.TestBase;

import java.util.ArrayList;
import java.util.List;

public class SelectionAndOrderingTest extends TestBase {

    void main() {
        this.run();
    }

    @Override
    protected void init(final Rivet rivet) {
        List<SelectableLabel> labels = new ArrayList<>();
        SelectionModel<SelectableLabel> selectionModel = new SelectionModel<>(labels);

        ReorderableContainer container = new ReorderableContainer(
                new VerticalListLayout(5, true),
                DropMarkerStrategy.vertical(5, 3),
                dragData -> dragData instanceof SelectableLabel
        );
        container.reorderListener().add((dragData, insertIndex) -> {
            SelectableLabel draggedLabel = (SelectableLabel) dragData;
            int sourceIndex = labels.indexOf(draggedLabel);
            if (sourceIndex != -1) {
                labels.remove(sourceIndex);
                if (sourceIndex < insertIndex) insertIndex--;
                labels.add(insertIndex, draggedLabel);

                container.sortChildren(java.util.Comparator.comparingInt(labels::indexOf));
            }
        });
        for (int i = 0; i < 10; i++) {
            SelectableLabel label = new SelectableLabel("Test " + i, selectionModel);
            label.mouseDownListener().add((event, bounds) -> {
                if (event.button().equals(MouseButton.LEFT)) {
                    selectionModel.select(label, event.modifiers());
                }
                return false;
            });
            container.addChild(label);
            labels.add(label);
        }
        rivet.root().addChild(container);
    }


    private static class SelectableLabel extends Label {
        private static final Color SELECTED_COLOR = Color.BLUE;
        private static final Color HOVERED_COLOR = Color.GRAY.withAlpha(100);

        private final SelectionModel<SelectableLabel> selectionModel;
        private boolean hovered;
        private boolean mouseDown;

        public SelectableLabel(final String text, final SelectionModel<SelectableLabel> selectionModel) {
            super(text);
            this.selectionModel = selectionModel;
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
            this.mouseDown = true;
            return true;
        }

        @Override
        protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
            this.mouseDown = false;
            return true;
        }

        @Override
        protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
            if (this.mouseDown && !this.rivet().dragAndDropManager().isDragging()) {
                this.rivet().dragAndDropManager().startDrag(this, new Label(this.text()), this.computeIdealSize(Size.EMPTY));
            }
            return true;
        }

        @Override
        public void render(final Renderer renderer, final Size size) {
            if (this.selectionModel.isSelected(this)) {
                renderer.fillRect(0, 0, size.width(), size.height(), SELECTED_COLOR);
            }
            if (this.hovered) {
                renderer.fillRect(0, 0, size.width(), size.height(), HOVERED_COLOR);
            }
            super.render(renderer, size);
        }
    }

}
