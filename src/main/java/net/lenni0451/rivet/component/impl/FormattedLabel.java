package net.lenni0451.rivet.component.impl;

import lombok.experimental.Accessors;
import net.lenni0451.rivet.text.TextParser;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;

import javax.annotation.Nullable;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class FormattedLabel extends Label {

    public FormattedLabel(final String text) {
        super(text);
    }

    public FormattedLabel(final String text, @Nullable final TextFormat format) {
        super(text, format);
    }

    public FormattedLabel(final TextLine line) {
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
