package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.text.TextSection;

import javax.annotation.Nullable;
import java.util.List;

public interface Backend {

    float getTextHeight();

    ShapedText shapeText(final String text);

    ShapedText shapeText(final List<TextSection> sections);

    @Nullable
    String getClipboard();

    void setClipboard(final String clipboard);

}
