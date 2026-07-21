package net.lenni0451.rivet.backend.thingl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.backend.TextInput;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.implementation.util.sdl.SdlException;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

@RequiredArgsConstructor
public class SDLTextInput implements TextInput {

    private final long window;

    @Override
    public void start() {
        if (!SDLKeyboard.SDL_TextInputActive(this.window)) {
            SdlException.check(SDLKeyboard.SDL_StartTextInput(this.window), "Failed to start text input");
        }
    }

    @Override
    public void stop() {
        if (SDLKeyboard.SDL_TextInputActive(this.window)) {
            SdlException.check(SDLKeyboard.SDL_StopTextInput(this.window), "Failed to stop text input");
        }
    }

    @Override
    public void area(final Rectangle rectangle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Rect.Buffer rect = SDL_Rect.malloc(1, stack);
            rect.x((int) rectangle.x()).y((int) rectangle.y());
            rect.w((int) rectangle.width()).h((int) rectangle.height());
            SdlException.check(SDLKeyboard.SDL_SetTextInputArea(this.window, rect, 0), "Failed to set text input area");
        }
    }

}
