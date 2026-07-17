package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;
import net.lenni0451.rivet.backend.thingl.utils.SDLMapper;
import net.lenni0451.rivet.input.keyboard.Key;
import net.raphimc.thingl.implementation.util.sdl.SdlException;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class SDLBackend extends ThinGLBackend {

    public SDLBackend(final long window, final ThinGLFont font) {
        super(window, font);
    }

    @Override
    public @Nullable String getClipboard() {
        String clipboard = SDLClipboard.SDL_GetClipboardText();
        if (clipboard == null) return null;
        if (clipboard.isEmpty()) return null;
        return clipboard;
    }

    @Override
    public void setClipboard(final String clipboard) {
        ByteBuffer buffer = MemoryUtil.memUTF8(clipboard);
        try {
            SdlException.check(SDLClipboard.SDL_SetClipboardText(buffer), "Failed to set clipboard text");
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    @Override
    public boolean isKeyDown(final Key key) {
        ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
        int keycode = SDLMapper.mapKey(key);
        int scancode = SDLKeyboard.SDL_GetScancodeFromKey(keycode, null);
        return keyboardState.get(scancode) != 0;
    }

}
