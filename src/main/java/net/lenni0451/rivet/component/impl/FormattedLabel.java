package net.lenni0451.rivet.component.impl;

import lombok.experimental.Accessors;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;

import javax.annotation.Nonnull;
import java.util.Objects;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class FormattedLabel extends Label {

    public FormattedLabel(@Nonnull final String text) {
        super(text);
    }

    public FormattedLabel(@Nonnull final String text, @Nonnull final TextFormat format) {
        super(text, Objects.requireNonNullElse(format, TextFormat.DEFAULT));
    }

    public FormattedLabel(@Nonnull final TextLine line) {
        super(line);
    }

    {
        this.overflowBehavior().set(OverflowBehavior.WRAP);
    }

    @Override
    protected TextLine createTextLine(final String text, final TextFormat format) {
        return TextParser.parse(text, format);
    }

}
