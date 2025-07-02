package net.lenni0451.rivet.component.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.KeyboardListener;
import net.lenni0451.rivet.component.MouseListener;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.constants.KeyboardConstants;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.math.impl.FloatPadding;
import net.lenni0451.rivet.text.TextBuffer;
import org.joml.Matrix4fStack;

public class TextField extends Component implements Renderable, MouseListener, KeyboardListener {

    private final StringBuffer text = new StringBuffer();
    private final FloatPadding innerPadding = new FloatPadding(5, 5, 5, 5);
    private ShapedTextBuffer shapedText;
    private int cursor;
    private Float cursorX;
    private boolean focused = false;

    public TextField() {
    }

    public Padding getInnerPadding() {
        return this.innerPadding;
    }

    public void setInnerPadding(final int left, final int top, final int right, final int bottom) {
        this.innerPadding.set(left, top, right, bottom);
        this.triggerLayoutChange();
    }

    @Override
    public void onFocusGained() {
        this.focused = true;
        this.cursor = this.text.length();
        this.cursorX = null;
    }

    @Override
    public void onFocusLost() {
        this.focused = false;
    }

    @Override
    public void onCharTyped(final char c) {
        this.text.insert(this.cursor, c);
        this.shapedText = null;
        this.cursor++;
        this.cursorX = null;
    }

    @Override
    public void onKeyDown(final int key, final int modifiers) {
        switch (key) {
            case KeyboardConstants.KEY_BACKSPACE -> {
                if (this.cursor > 0) {
                    this.text.deleteCharAt(this.cursor - 1);
                    this.shapedText = null;
                    this.cursor--;
                    this.cursorX = null;
                }
            }
            case KeyboardConstants.KEY_DELETE -> {
                if (this.cursor < this.text.length()) {
                    this.text.deleteCharAt(this.cursor);
                    this.shapedText = null;
                }
            }
            case KeyboardConstants.KEY_LEFT -> {
                if (this.cursor > 0) {
                    this.cursor--;
                    this.cursorX = null;
                }
            }
            case KeyboardConstants.KEY_RIGHT -> {
                if (this.cursor < this.text.length()) {
                    this.cursor++;
                    this.cursorX = null;
                }
            }
            case KeyboardConstants.KEY_HOME -> {
                this.cursor = 0;
                this.cursorX = null;
            }
            case KeyboardConstants.KEY_END -> {
                this.cursor = this.text.length();
                this.cursorX = null;
            }
        }

        if (key == KeyboardConstants.KEY_V && (modifiers & KeyboardConstants.MODIFIER_CONTROL) != 0) {
            final String clipboardText = this.rivet.getBackend().getClipboardText();
            if (clipboardText != null) {
                this.text.insert(this.cursor, clipboardText);
                this.shapedText = null;
                this.cursor += clipboardText.length();
                this.cursorX = null;
            }
        }
    }

    @Override
    public void render(final Renderer renderer, final Matrix4fStack positionMatrix, final Size size) {
        renderer.filledRectangle(positionMatrix, 0, 0, size.width(), size.height(), Color.BLACK);
        renderer.outlinedRectangle(positionMatrix, 0, 0, size.width(), size.height(), this.focused ? Color.WHITE : Color.GRAY, 2);

        if (this.shapedText == null) {
            this.shapedText = this.rivet.getBackend().shapeTextBuffer(TextBuffer.fromString(this.rivet.getDefaultFonts(), this.text.toString()));
        }
        Font mainFont = this.rivet.getDefaultFonts().getMainFont();
        float textY = (size.height() - mainFont.getDescent()) / 2 + mainFont.getAscent() / 2;
        renderer.text(positionMatrix, this.shapedText, 0, textY, true);
        if (this.focused) {
            if (this.cursorX == null) {
                final ShapedTextBuffer cursorText = this.rivet.getBackend().shapeTextBuffer(TextBuffer.fromString(this.rivet.getDefaultFonts(), this.text.substring(0, this.cursor)));
                this.cursorX = cursorText.extendedWidth();
            }
            renderer.filledRectangle(positionMatrix, this.cursorX, textY - mainFont.getAscent(), 2, mainFont.getHeight(), Color.WHITE);
        }
    }

    @Override
    protected void computePreferredSize() {
        Font mainFont = this.rivet.getDefaultFonts().getMainFont();
        float fontSize = mainFont.getSize();
        float fontHeight = mainFont.getHeight();
        this.preferredSize.set(fontSize * 10, fontHeight);
    }

}
