package net.lenni0451.rivet.backend.thingl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.backend.TextInput;
import net.lenni0451.rivet.math.Rectangle;
import net.raphimc.thingl.implementation.util.sdl.SdlException;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

import java.util.function.Function;

@RequiredArgsConstructor
public class SDLTextInput implements TextInput {

    private final long window;
    private final Function<Rectangle, Rectangle> toWindowRect;

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
            Rectangle windowRect = this.toWindowRect.apply(rectangle);
            SDL_Rect.Buffer rect = SDL_Rect.malloc(1, stack);
            rect.x((int) windowRect.x()).y((int) windowRect.y());
            rect.w((int) windowRect.width()).h((int) windowRect.height());
            SdlException.check(SDLKeyboard.SDL_SetTextInputArea(this.window, rect, 0), "Failed to set text input area");
        }
    }

}
