package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;
import net.lenni0451.rivet.backend.thingl.util.GLFWMapper;
import net.lenni0451.rivet.input.keyboard.Key;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class GLFWBackend extends ThinGLBackend {

    public GLFWBackend(final long window, final ThinGLFont font) {
        super(window, font);
    }

    @Override
    @Nullable
    public String getClipboard() {
        String clipboard = GLFW.glfwGetClipboardString(this.window());
        if (clipboard == null) return null;
        if (clipboard.isEmpty()) return null;
        return clipboard;
    }

    @Override
    public void setClipboard(final String clipboard) {
        ByteBuffer buffer = MemoryUtil.memUTF8(clipboard);
        try {
            GLFW.glfwSetClipboardString(this.window(), buffer);
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    @Override
    public boolean isKeyDown(final Key key) {
        return GLFW.glfwGetKey(this.window(), GLFWMapper.mapKey(key)) == GLFW.GLFW_PRESS;
    }

}
