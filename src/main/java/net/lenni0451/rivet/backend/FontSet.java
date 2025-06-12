package net.lenni0451.rivet.backend;

import org.jetbrains.annotations.Nullable;

public interface FontSet {

    Font getMainFont();

    @Nullable
    Font getFont(final int codePoint);

}
