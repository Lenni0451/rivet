package net.lenni0451.rivet.layout.flex;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.rivet.layout.LayoutOptions;

@With
@WithBy
public record FlexOptions(int order, int grow, int shrink, int basis, FlexAlignSelf align) implements LayoutOptions {

    public static final FlexOptions DEFAULT = new FlexOptions(0, 0, 1, 0, FlexAlignSelf.AUTO);

}
