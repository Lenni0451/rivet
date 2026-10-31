package net.lenni0451.rivet.component.container;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.dragdrop.DropMarkerStrategy;
import net.lenni0451.rivet.layout.Layout;
import net.lenni0451.rivet.math.Size;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DynamicListContainer<E> extends ReorderableContainer {

    private final Supplier<List<E>> listSupplier;
    private final Function<E, Component> componentFactory;
    private final IdentityHashMap<E, Component> componentCache = new IdentityHashMap<>();
    private final List<E> lastSeenList = new ArrayList<>();
    private final Runnable updateListener;

    private final IdentityHashMap<Component, Integer> orderMap = new IdentityHashMap<>();
    private final Comparator<Component> orderComparator = Comparator.comparingInt(c -> this.orderMap.getOrDefault(c, 0));

    private boolean internalChange = false;
    private boolean childrenDirty = false;

    public DynamicListContainer(final Layout layout, final List<E> list, final Function<E, Component> componentFactory) {
        this(layout, null, obj -> false, () -> list, componentFactory);
    }

    public DynamicListContainer(final Layout layout, final Supplier<List<E>> listSupplier, final Function<E, Component> componentFactory) {
        this(layout, null, obj -> false, listSupplier, componentFactory);
    }

    public DynamicListContainer(final Layout layout, final DropMarkerStrategy strategy, final Predicate<Object> dropFilter, final List<E> list, final Function<E, Component> componentFactory) {
        this(layout, strategy, dropFilter, () -> list, componentFactory);
    }

    public DynamicListContainer(final Layout layout, final DropMarkerStrategy strategy, final Predicate<Object> dropFilter, final Supplier<List<E>> listSupplier, final Function<E, Component> componentFactory) {
        super(layout, strategy, dropFilter);
        this.listSupplier = listSupplier;
        this.componentFactory = componentFactory;
        this.updateListener = () -> {
            List<E> currentList = this.listSupplier.get();
            if (this.childrenDirty || !this.isSame(currentList, this.lastSeenList)) {
                this.requestLayoutRecalculation();
            }
        };
        this.childChangedListener().add(() -> {
            if (!this.internalChange) {
                this.childrenDirty = true;
            }
        });
    }

    public final DynamicListContainer<E> recreate(final E item) {
        Component oldComponent = this.componentCache.remove(item);
        if (oldComponent != null) {
            this.internalChange = true;
            try {
                this.remove(oldComponent);
                Integer index = this.orderMap.remove(oldComponent);
                Component newComponent = this.componentFactory.apply(item);
                this.componentCache.put(item, newComponent);
                this.add(newComponent);
                if (index != null) {
                    this.orderMap.put(newComponent, index);
                }
                this.sort(this.orderComparator);
            } finally {
                this.internalChange = false;
            }
        }
        return this;
    }

    @Override
    protected void onAddedInternal() {
        super.onAddedInternal();
        this.rivet().renderListener().add(this.updateListener);
    }

    @Override
    protected void onRemovedInternal() {
        super.onRemovedInternal();
        this.rivet().renderListener().remove(this.updateListener);
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        this.syncChildrenIfNeeded();
        return super.computeIdealSize(constraints);
    }

    @Override
    public void computeLayout(final Size size) {
        this.syncChildrenIfNeeded();
        super.computeLayout(size);
    }

    private void syncChildrenIfNeeded() {
        List<E> currentList = this.listSupplier.get();
        if (this.childrenDirty || !this.isSame(currentList, this.lastSeenList)) {
            this.syncChildren(currentList);
        }
    }

    private boolean isSame(final List<E> a, final List<E> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i)) return false;
        }
        return true;
    }

    private void syncChildren(final List<E> currentList) {
        this.internalChange = true;
        try {
            Set<E> currentItems = identitySet(currentList);
            Set<Component> children = identitySet(this.children());
            this.componentCache.entrySet().removeIf(entry -> !currentItems.contains(entry.getKey()) || !children.contains(entry.getValue()));

            Set<Component> managedComponents = identitySet(this.componentCache.values());
            for (Component child : children) {
                if (!managedComponents.contains(child)) {
                    this.remove(child);
                }
            }

            this.orderMap.clear();
            for (int i = 0; i < currentList.size(); i++) {
                E item = currentList.get(i);
                Component component = this.componentCache.computeIfAbsent(item, itm -> {
                    Component c = this.componentFactory.apply(itm);
                    this.add(c);
                    return c;
                });
                this.orderMap.put(component, i);
            }
            this.sort(this.orderComparator);

            this.lastSeenList.clear();
            this.lastSeenList.addAll(currentList);
            this.childrenDirty = false;
        } finally {
            this.internalChange = false;
        }
    }

    private static <T> Set<T> identitySet(final Collection<T> elements) {
        Set<T> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.addAll(elements);
        return set;
    }

}
