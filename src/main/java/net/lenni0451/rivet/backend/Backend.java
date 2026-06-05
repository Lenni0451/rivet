package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.ModifierKey;

import javax.annotation.Nullable;

public interface Backend {

    Font defaultFont();

    @Nullable
    String getClipboard();

    void setClipboard(final String clipboard);

    default boolean isKeyDown(final ModifierKey key) {
        return switch (key) {
            case SHIFT -> this.isKeyDown(Key.LEFT_SHIFT) || this.isKeyDown(Key.RIGHT_SHIFT);
            case CONTROL -> this.isKeyDown(Key.LEFT_CONTROL) || this.isKeyDown(Key.RIGHT_CONTROL);
            case ALT -> this.isKeyDown(Key.LEFT_ALT) || this.isKeyDown(Key.RIGHT_ALT);
            case SUPER -> this.isKeyDown(Key.LEFT_SUPER) || this.isKeyDown(Key.RIGHT_SUPER);
        };
    }

    boolean isKeyDown(final Key key);

}
