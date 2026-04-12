package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.text.TextSection;
import net.raphimc.thingl.text.TextLine;
import net.raphimc.thingl.text.TextRun;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.font.FontSet;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record ThinGLBackend(long window, FontSet fontSet) implements Backend {

    @Override
    public float getTextHeight() {
        return this.fontSet.getMainFont().getHeight();
    }

    @Override
    public ShapedText shapeText(final String text) {
        ShapedTextLine shapedTextLine = TextLine.fromString(this.fontSet, text).shape();
        return new ThinGLShapedText(shapedTextLine);
    }

    @Override
    public ShapedText shapeText(final List<TextSection> sections) {
        List<TextRun> runs = new ArrayList<>();
        for (TextSection section : sections) {
            int flags = TextStyle.buildFlags(
                    section.format().shadow(),
                    section.format().bold(),
                    section.format().italic(),
                    section.format().underlined(),
                    section.format().strikethrough()
            );
            TextStyle style = new TextStyle(section.format().color(), flags, section.format().outlineColor());
            runs.addAll(TextLine.fromString(this.fontSet, section.text(), style).runs());
        }
        return new ThinGLShapedText(new TextLine(runs).shape());
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

}
