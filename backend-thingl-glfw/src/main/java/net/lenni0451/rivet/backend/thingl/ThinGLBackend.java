package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedText;
import net.lenni0451.rivet.backend.thingl.text.ThinGLShapedTextBlock;
import net.lenni0451.rivet.backend.thingl.util.GLFWMapper;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.text.model.TextSection;
import net.raphimc.thingl.text.TextBlock;
import net.raphimc.thingl.text.TextLine;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.font.FontSet;
import net.raphimc.thingl.text.shaping.ShapedTextLine;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public record ThinGLBackend(long window, FontSet fontSet) implements Backend {

    @Override
    public float getTextHeight() {
        return this.fontSet.getMainFont().getHeight();
    }

    @Override
    public ShapedText shapeText(final String text, final Color color) {
        TextStyle style = new TextStyle(color, 0, Color.TRANSPARENT);
        ShapedTextLine shapedTextLine = TextLine.fromString(this.fontSet, text, style).shape();
        return new ThinGLShapedText(shapedTextLine);
    }

    @Override
    public ShapedText shapeText(final net.lenni0451.rivet.text.model.TextLine line) {
        return new ThinGLShapedText(this.toThinGL(line).shape());
    }

    @Override
    public ShapedTextBlock shapeText(final net.lenni0451.rivet.text.model.TextBlock block) {
        TextBlock thinglBlock = new TextBlock();
        for (net.lenni0451.rivet.text.model.TextLine line : block.lines()) {
            thinglBlock.add(this.toThinGL(line));
        }
        return new ThinGLShapedTextBlock(thinglBlock.shape());
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

    private TextLine toThinGL(final net.lenni0451.rivet.text.model.TextLine line) {
        TextLine textLine = new TextLine();
        for (TextSection section : line.sections()) {
            int flags = TextStyle.buildFlags(
                    section.format().shadow(),
                    section.format().bold(),
                    section.format().italic(),
                    section.format().underlined(),
                    section.format().strikethrough()
            );
            TextStyle style = new TextStyle(section.format().color(), flags, section.format().outlineColor());
            textLine.runs().addAll(TextLine.fromString(this.fontSet, section.text(), style).runs());
        }
        textLine.compact();
        return textLine;
    }

}
