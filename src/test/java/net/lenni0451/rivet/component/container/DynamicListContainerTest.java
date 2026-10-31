package net.lenni0451.rivet.component.container;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DynamicListContainerTest {

    private static final Size SIZE = new Size(100, 100);

    private List<String> items;
    private Map<Component, String> itemsByComponent;
    private int createdComponents;
    private DynamicListContainer<String> container;

    @BeforeEach
    void setUp() {
        this.items = new ArrayList<>(List.of("a", "b", "c"));
        this.itemsByComponent = new IdentityHashMap<>();
        this.createdComponents = 0;
        this.container = new DynamicListContainer<>(FullSizeLayout.INSTANCE, this.items, item -> {
            Component component = new Container(FullSizeLayout.INSTANCE);
            this.itemsByComponent.put(component, item);
            this.createdComponents++;
            return component;
        });
        this.container.computeLayout(SIZE);
    }

    private List<String> childItems() {
        return this.container.children().stream().map(this.itemsByComponent::get).toList();
    }

    @Test
    void testChildrenFollowList() {
        assertEquals(List.of("a", "b", "c"), this.childItems());

        this.items.remove("b");
        this.items.add(0, "d");
        this.container.computeLayout(SIZE);

        assertEquals(List.of("d", "a", "c"), this.childItems());
        assertEquals(4, this.createdComponents);
    }

    @Test
    void testUnchangedListKeepsComponents() {
        List<Component> children = this.container.children();

        this.container.computeLayout(SIZE);

        assertEquals(children, this.container.children());
        assertEquals(3, this.createdComponents);
    }

    @Test
    void testManuallyRemovedComponentIsRecreatedInPlace() {
        Component removed = this.container.children().get(1);
        Component kept = this.container.children().get(0);

        this.container.remove(removed);
        this.container.computeLayout(SIZE);

        assertEquals(List.of("a", "b", "c"), this.childItems());
        assertFalse(this.container.children().contains(removed));
        assertSame(kept, this.container.children().get(0));
        assertEquals(4, this.createdComponents);
    }

    @Test
    void testManualClearRecreatesAllComponents() {
        this.container.clear();
        assertTrue(this.container.children().isEmpty());

        this.container.computeLayout(SIZE);

        assertEquals(List.of("a", "b", "c"), this.childItems());
        assertEquals(6, this.createdComponents);
    }

    @Test
    void testManuallyAddedComponentIsRemoved() {
        Component foreign = new Container(FullSizeLayout.INSTANCE);

        this.container.add(foreign);
        this.container.computeLayout(SIZE);

        assertFalse(this.container.children().contains(foreign));
        assertEquals(List.of("a", "b", "c"), this.childItems());
        assertEquals(3, this.createdComponents);
    }

    @Test
    void testReaddingManagedComponentKeepsSingleComponentPerItem() {
        List<Component> children = this.container.children();

        this.container.add(children.get(0));
        this.container.computeLayout(SIZE);

        assertEquals(children, this.container.children());
        assertEquals(3, this.createdComponents);
    }

    @Test
    void testReplacingItemWithEqualInstanceRemovesOldComponent() {
        Component oldComponent = this.container.children().get(0);

        this.items.set(0, new String("a"));
        this.container.computeLayout(SIZE);

        assertEquals(List.of("a", "b", "c"), this.childItems());
        assertFalse(this.container.children().contains(oldComponent));
        assertEquals(4, this.createdComponents);
    }

    @Test
    void testRecreateDoesNotTriggerResync() {
        Component first = this.container.children().get(0);
        Component second = this.container.children().get(1);

        this.container.recreate("b");
        assertNotSame(second, this.container.children().get(1));
        Component recreated = this.container.children().get(1);
        this.container.computeLayout(SIZE);

        assertEquals(List.of("a", "b", "c"), this.childItems());
        assertSame(first, this.container.children().get(0));
        assertSame(recreated, this.container.children().get(1));
        assertEquals(4, this.createdComponents);
    }

    @Test
    void testDuplicateItemsShareOneComponent() {
        this.items.add(this.items.get(0));
        this.container.computeLayout(SIZE);

        assertEquals(3, this.container.children().size());
        assertEquals(3, this.createdComponents);
    }

}
