package net.lenni0451.rivet.component.impl;

import net.lenni0451.rivet.component.base.Button;
import net.lenni0451.rivet.text.TextBuffer;

import java.util.function.IntConsumer;

public class TextButton extends Button {

    public TextButton(final String text, final IntConsumer onClick) {
        super(new Label(text), onClick);
    }

    public TextButton(final TextBuffer text, final IntConsumer onClick) {
        super(new Label(text), onClick);
    }

}
