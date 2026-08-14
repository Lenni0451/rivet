package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.TextInput;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.thingl.utils.SDLMapper;
import net.lenni0451.rivet.input.keyboard.Key;
import net.raphimc.thingl.implementation.util.sdl.SdlException;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class SDLBackend extends ThinGLBackend {

    private final SDLTextInput textInput;

    public SDLBackend(final long window, final Font font, final AssetLoader assetLoader) {
        super(window, font, assetLoader);
        this.textInput = new SDLTextInput(window);
    }

    @Override
    @Nullable
    public String getClipboard() {
        if (!SDLClipboard.SDL_HasClipboardText()) return null;
        String clipboard = SDLClipboard.SDL_GetClipboardText();
        if (clipboard == null || clipboard.isEmpty()) return null;
        return clipboard;
    }

    @Override
    public void setClipboard(final String clipboard) {
        if (clipboard != null) {
            ByteBuffer buffer = MemoryUtil.memUTF8(clipboard);
            try {
                SdlException.check(SDLClipboard.SDL_SetClipboardText(buffer), "Failed to set clipboard text");
            } finally {
                MemoryUtil.memFree(buffer);
            }
        }
    }

    @Override
    public boolean isKeyDown(final Key key) {
        if (key == null) return false;
        ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
        if (keyboardState == null) return false;
        int keycode = SDLMapper.mapKey(key);
        int scancode = SDLKeyboard.SDL_GetScancodeFromKey(keycode, null);
        if (scancode <= 0 || scancode >= keyboardState.capacity()) return false;
        return keyboardState.get(scancode) != 0;
    }

    @Override
    public TextInput textInput() {
        return this.textInput;
    }

}
