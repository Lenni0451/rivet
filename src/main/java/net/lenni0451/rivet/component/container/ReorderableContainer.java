package net.lenni0451.rivet.component.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class ReorderableContainer extends Container {

    @Getter
    private final DropMarkerStrategy strategy;
    @Getter
    private final Predicate<Object> dropFilter;
    @Getter
    private final ListenerList<ReorderListener> reorderListener = new ListenerList<>();
    @Getter
    @Setter
    private Color markerColor = Color.GREEN;

    private DropMarkerStrategy.DropTarget currentTarget = null;

    public ReorderableContainer(final Layout layout, final DropMarkerStrategy strategy, final Predicate<Object> dropFilter) {
        super(layout);
        this.strategy = strategy;
        this.dropFilter = dropFilter;
    }

    private boolean isAccepted(final List<Object> dragData) {
        if (dragData.isEmpty()) return false;
        for (Object data : dragData) {
            if (!this.dropFilter.test(data)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onComponentDisabled() {
        this.currentTarget = null;
        super.onComponentDisabled();
    }

    @Override
    protected boolean onComponentDragOver(final DragOverEvent event, final Size size) {
        if (this.isAccepted(event.dragData())) {
            this.currentTarget = this.strategy.resolve(this, event.x(), event.y());
            return true;
        }
        return super.onComponentDragOver(event, size);
    }

    @Override
    protected void onComponentDragLeave() {
        this.currentTarget = null;
        super.onComponentDragLeave();
    }

    @Override
    protected boolean onComponentDrop(final DropEvent event, final Size size) {
        if (this.currentTarget != null && this.isAccepted(event.dragData())) {
            this.reorderListener.call(listener -> listener.onReorder(event.dragData(), this.currentTarget.insertIndex()));
            this.currentTarget = null;
            return true;
        }
        this.currentTarget = null;
        return super.onComponentDrop(event, size);
    }

    @Override
    protected void renderComponent(final Renderer renderer, final Size size) {
        super.renderComponent(renderer, size);
        if (this.currentTarget != null && this.currentTarget.markerBounds() != null) {
            Rectangle markerBounds = this.currentTarget.markerBounds();
            renderer.fillRect(markerBounds.x(), markerBounds.y(), markerBounds.width(), markerBounds.height(), this.markerColor);
        }
    }


    @FunctionalInterface
    public interface ReorderListener {
        void onReorder(final List<Object> dragData, final int insertIndex);
    }

    @FunctionalInterface
    public interface SingleReorderListener extends ReorderListener {
        void onReorder(final Object dragData, final int insertIndex);

        @Override
        default void onReorder(final List<Object> dragData, final int insertIndex) {
            if (dragData.size() == 1) {
                this.onReorder(dragData.get(0), insertIndex);
            }
        }
    }

    @RequiredArgsConstructor
    public static class ListReorderListener<E> implements ReorderListener {
        private final Supplier<List<E>> listSupplier;
        @Nullable
        private final Runnable onReorder;

        public ListReorderListener(final Supplier<List<E>> listSupplier) {
            this(listSupplier, null);
        }

        @Override
        public void onReorder(final List<Object> dragData, final int insertIndex) {
            List<E> list = this.listSupplier.get();
            List<E> toMove = new ArrayList<>(dragData.size());
            int index = insertIndex;
            for (Object o : dragData) {
                if (!list.contains(o)) continue;

                E element = (E) o;
                toMove.add(element);
                if (list.indexOf(element) < index) {
                    index--;
                }
                list.remove(element);
            }
            list.addAll(index, toMove);

            if (this.onReorder != null) {
                this.onReorder.run();
            }
        }
    }

}
