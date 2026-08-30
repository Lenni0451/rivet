package net.lenni0451.rivet.backend.awt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.input.keyboard.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.EnumSet;
import java.util.Set;

@Getter
@ApiStatus.Experimental
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class AWTBackend implements Backend {

    private final Font font;
    private final AssetLoader assetLoader;
    private final Set<Key> heldKeys = EnumSet.noneOf(Key.class);

    @Nullable
    @Override
    public String getClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                return String.valueOf(clipboard.getData(DataFlavor.stringFlavor));
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    @Override
    public void setClipboard(final String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, selection);
    }

    @Override
    public boolean isKeyDown(final Key key) {
        return this.heldKeys.contains(key);
    }

}
