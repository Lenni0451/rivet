package net.lenni0451.rivet.text.model;

import lombok.With;
import lombok.experimental.WithBy;

import java.util.List;

@With
@WithBy
public record TextBlock(List<TextLine> lines) {

    public TextBlock(final TextLine... lines) {
        this(List.of(lines));
    }

    public TextBlock {
        lines = List.copyOf(lines);
    }

}
