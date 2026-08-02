package net.lenni0451.rivet.editor.layout;

import net.lenni0451.rivet.editor.properties.ImmutableProperty;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.list.HorizontalListLayout;
import net.lenni0451.rivet.layout.list.VerticalListLayout;

import java.util.ArrayList;
import java.util.List;

public class LayoutRegistry {

    private static final List<RegisteredLayout> components = new ArrayList<>();

    static {
        register(new RegisteredLayout(
                "Absolute",
                () -> AbsoluteLayout.INSTANCE
        ));
        register(new RegisteredLayout(
                "Vertical List",
                () -> VerticalListLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Gap", Integer.class, VerticalListLayout::gap, VerticalListLayout::withGap),
                        new ImmutableProperty<>("Full Width", Boolean.class, VerticalListLayout::fullWidth, VerticalListLayout::withFullWidth),
                        new ImmutableProperty<>("Constrained", Boolean.class, VerticalListLayout::constrained, VerticalListLayout::withConstrained)
                )
        ));
        register(new RegisteredLayout(
                "Horizontal List",
                () -> HorizontalListLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Gap", Integer.class, HorizontalListLayout::gap, HorizontalListLayout::withGap),
                        new ImmutableProperty<>("Full Height", Boolean.class, HorizontalListLayout::fullHeight, HorizontalListLayout::withFullHeight),
                        new ImmutableProperty<>("Constrained", Boolean.class, HorizontalListLayout::constrained, HorizontalListLayout::withConstrained)
                )
        ));
    }

    public static void register(final RegisteredLayout layout) {
        components.add(layout);
    }

    public static List<RegisteredLayout> layouts() {
        return List.copyOf(components);
    }

}
