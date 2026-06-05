package net.lenni0451.rivet.backend.thingl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;
import net.lenni0451.rivet.backend.thingl.util.GLFWMapper;
import net.lenni0451.rivet.input.keyboard.Key;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class ThinGLBackend implements Backend {

    private final long window;
    private final ThinGLFont font;

    @Override
    public Font defaultFont() {
        return this.font;
    }

    @Override
    @Nullable
    public String getClipboard() {
        String clipboard = GLFW.glfwGetClipboardString(this.window);
        if (clipboard == null) return null;
        if (clipboard.isEmpty()) return null;
        return clipboard;
    }

    @Override
    public void setClipboard(final String clipboard) {
        ByteBuffer buffer = MemoryUtil.memUTF8(clipboard);
        try {
            GLFW.glfwSetClipboardString(this.window, buffer);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    @Override
    public boolean isKeyDown(final Key key) {
        return GLFW.glfwGetKey(this.window, GLFWMapper.mapKey(key)) == GLFW.GLFW_PRESS;
    }

}
