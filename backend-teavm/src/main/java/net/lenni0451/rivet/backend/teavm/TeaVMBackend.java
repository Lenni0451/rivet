package net.lenni0451.rivet.backend.teavm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.TextInput;
import net.lenni0451.rivet.backend.teavm.utils.JsUtils;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.input.keyboard.Key;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TeaVMBackend implements Backend {

    private final Font font;
    private final AssetLoader assetLoader;
    private final TextInput textInput;
    private final Set<Key> heldKeys = EnumSet.noneOf(Key.class);
    @Setter
    @Nullable
    private String clipboard;

    @Nullable
    @Override
    public String getClipboard() {
        return this.clipboard == null || this.clipboard.isEmpty() ? null : this.clipboard;
    }

    @Override
    public void setClipboard(final String clipboard) {
        this.clipboard = clipboard;
        JsUtils.writeClipboard(clipboard);
    }

    @Override
    public boolean isKeyDown(final Key key) {
        return this.heldKeys.contains(key);
    }

}
