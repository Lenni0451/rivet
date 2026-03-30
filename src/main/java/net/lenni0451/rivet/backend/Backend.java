package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.text.TextSection;

import java.util.List;

public interface Backend {

    ShapedText shapeText(final String text);

    ShapedText shapeText(final List<TextSection> sections);

}
