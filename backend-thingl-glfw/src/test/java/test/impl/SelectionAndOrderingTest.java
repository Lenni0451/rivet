package test.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.component.container.DecoratedContainer;
import net.lenni0451.rivet.component.container.ReorderableContainer;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.component.impl.SolidColor;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.utils.SelectionModel;
import test.TestBase;

import java.util.ArrayList;
import java.util.Comparator;
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
            List<SelectableLabel> toMove = new ArrayList<>();
            for (Object data : dragData) {
                if (data instanceof SelectableLabel label) {
                    toMove.add(label);
                }
            }
            toMove.sort(Comparator.comparingInt(labels::indexOf));

            SelectableLabel targetItem = null;
            for (int i = insertIndex; i < labels.size(); i++) {
                SelectableLabel item = labels.get(i);
                if (!toMove.contains(item)) {
                    targetItem = item;
                    break;
                }
            }

            labels.removeAll(toMove);

            int newInsertIndex = targetItem == null ? labels.size() : labels.indexOf(targetItem);
            labels.addAll(newInsertIndex, toMove);

            container.sortChildren(Comparator.comparingInt(labels::indexOf));
        });
        for (int i = 0; i < 10; i++) {
            SelectableLabel label = new SelectableLabel("Test " + i, selectionModel);
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
        private boolean selectionDeferred;

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
            this.mouseDown = false;
            this.selectionDeferred = false;
        }

        @Override
        protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
            if (event.button().equals(MouseButton.LEFT)) {
                if (this.selectionModel.isSelected(this) && !event.modifiers().contains(ModifierKey.CONTROL) && !event.modifiers().contains(ModifierKey.SHIFT)) {
                    this.selectionDeferred = true;
                } else {
                    this.selectionModel.select(this, event.modifiers());
                    this.selectionDeferred = false;
                }
                this.mouseDown = true;
                return true;
            }
            return super.onComponentMouseDown(event, size);
        }

        @Override
        protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
            if (event.button().equals(MouseButton.LEFT)) {
                if (this.selectionDeferred) {
                    this.selectionModel.select(this, event.modifiers());
                }
                this.selectionDeferred = false;
                this.mouseDown = false;
                return true;
            }
            return super.onComponentMouseUp(event, size);
        }

        @Override
        protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
            if (this.mouseDown && !this.rivet().dragAndDropManager().isDragging()) {
                this.selectionDeferred = false;
                List<SelectableLabel> dragged;
                if (this.selectionModel.isSelected(this)) {
                    dragged = this.selectionModel.orderedSelected();
                } else {
                    dragged = List.of(this);
                }
                this.rivet().dragAndDropManager().startDrag(
                        List.copyOf(dragged),
                        data -> new DecoratedContainer(new SolidColor(Color.BLACK.withAlpha(170)), new Label(((SelectableLabel) data).text()))
                );
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
