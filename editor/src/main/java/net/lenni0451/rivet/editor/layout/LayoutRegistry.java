package net.lenni0451.rivet.editor.layout;

import net.lenni0451.rivet.editor.properties.ImmutableProperty;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.layout.border.*;
import net.lenni0451.rivet.layout.dock.DockLayout;
import net.lenni0451.rivet.layout.flex.*;
import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.layout.flow.VerticalFlowLayout;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.list.HorizontalListLayout;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.layout.tile.TileLayout;

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
                "Anchor",
                () -> AnchorLayout.INSTANCE
        ));
        register(new RegisteredLayout(
                "Border",
                () -> BorderLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Top Left Priority", TopLeftPriority.class, BorderLayout::topLeftPriority, BorderLayout::withTopLeftPriority),
                        new ImmutableProperty<>("Bottom Left Priority", BottomLeftPriority.class, BorderLayout::bottomLeftPriority, BorderLayout::withBottomLeftPriority),
                        new ImmutableProperty<>("Bottom Right Priority", BottomRightPriority.class, BorderLayout::bottomRightPriority, BorderLayout::withBottomRightPriority),
                        new ImmutableProperty<>("Top Right Priority", TopRightPriority.class, BorderLayout::topRightPriority, BorderLayout::withTopRightPriority)
                )
        ));
        register(new RegisteredLayout(
                "Dock",
                () -> DockLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Gap", Float.class, DockLayout::gap, DockLayout::withGap)
                )
        ));
        register(new RegisteredLayout(
                "Flex",
                () -> FlexLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Direction", FlexDirection.class, FlexLayout::direction, FlexLayout::withDirection),
                        new ImmutableProperty<>("Wrap", FlexWrap.class, FlexLayout::wrap, FlexLayout::withWrap),
                        new ImmutableProperty<>("Justify Content", FlexJustify.class, FlexLayout::justifyContent, FlexLayout::withJustifyContent),
                        new ImmutableProperty<>("Align Items", FlexAlignItems.class, FlexLayout::alignItems, FlexLayout::withAlignItems),
                        new ImmutableProperty<>("Align Content", FlexAlignContent.class, FlexLayout::alignContent, FlexLayout::withAlignContent),
                        new ImmutableProperty<>("Row Gap", Integer.class, FlexLayout::rowGap, FlexLayout::withRowGap),
                        new ImmutableProperty<>("Column Gap", Integer.class, FlexLayout::columnGap, FlexLayout::withColumnGap)
                )
        ));
        register(new RegisteredLayout(
                "Horizontal Flow",
                () -> HorizontalFlowLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Horizontal Gap", Integer.class, HorizontalFlowLayout::horizontalGap, HorizontalFlowLayout::withHorizontalGap),
                        new ImmutableProperty<>("Vertical Gap", Integer.class, HorizontalFlowLayout::verticalGap, HorizontalFlowLayout::withVerticalGap)
                )
        ));
        register(new RegisteredLayout(
                "Vertical Flow",
                () -> VerticalFlowLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Horizontal Gap", Integer.class, VerticalFlowLayout::horizontalGap, VerticalFlowLayout::withHorizontalGap),
                        new ImmutableProperty<>("Vertical Gap", Integer.class, VerticalFlowLayout::verticalGap, VerticalFlowLayout::withVerticalGap)
                )
        ));
        register(new RegisteredLayout(
                "Full Size",
                () -> FullSizeLayout.INSTANCE
        ));
        register(new RegisteredLayout(
                "Grid",
                () -> GridLayout.DEFAULT,
                List.of(
                        new ImmutableProperty<>("Horizontal Gap", Integer.class, GridLayout::horizontalGap, GridLayout::withHorizontalGap),
                        new ImmutableProperty<>("Vertical Gap", Integer.class, GridLayout::verticalGap, GridLayout::withVerticalGap),
                        new ImmutableProperty<>("Homogeneous Columns", Boolean.class, GridLayout::homogeneousColumns, GridLayout::withHomogeneousColumns),
                        new ImmutableProperty<>("Homogeneous Rows", Boolean.class, GridLayout::homogeneousRows, GridLayout::withHomogeneousRows),
                        new ImmutableProperty<>("Shrink Columns", Boolean.class, GridLayout::shrinkColumns, GridLayout::withShrinkColumns),
                        new ImmutableProperty<>("Shrink Rows", Boolean.class, GridLayout::shrinkRows, GridLayout::withShrinkRows)
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
                "Tile",
                () -> new TileLayout(1, 1, 0, 0),
                List.of(
                        new ImmutableProperty<>("Columns", Integer.class, TileLayout::columns, TileLayout::withColumns),
                        new ImmutableProperty<>("Rows", Integer.class, TileLayout::rows, TileLayout::withRows),
                        new ImmutableProperty<>("Horizontal Gap", Integer.class, TileLayout::horizontalGap, TileLayout::withHorizontalGap),
                        new ImmutableProperty<>("Vertical Gap", Integer.class, TileLayout::verticalGap, TileLayout::withVerticalGap)
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
