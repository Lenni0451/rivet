package net.lenni0451.rivet.component.impl;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.Renderer;
import net.lenni0451.rivet.backend.text.ShapedTextBuffer;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.KeyboardListener;
import net.lenni0451.rivet.component.MouseListener;
import net.lenni0451.rivet.component.Renderable;
import net.lenni0451.rivet.constants.KeyboardConstants;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.TextBuffer;
import org.joml.Matrix4fStack;

public class TextField extends Component implements Renderable, MouseListener, KeyboardListener {

    private final StringBuffer text = new StringBuffer();
    private ShapedTextBuffer shapedText;
    private int cursor;
    private Float cursorX;
    private boolean focused = false;

    public TextField() {
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
        renderer.text(positionMatrix, this.shapedText, 0, 0);
        if (this.focused) {
            if (this.cursorX == null) {
                final ShapedTextBuffer cursorText = this.rivet.getBackend().shapeTextBuffer(TextBuffer.fromString(this.rivet.getDefaultFonts(), this.text.substring(0, this.cursor)));
                this.cursorX = cursorText.extendedWidth();
            }
            renderer.filledRectangle(positionMatrix, this.cursorX, 2, 2, this.shapedText.bounds().lengthY() - 2, Color.WHITE);
        }
    }

    @Override
    protected void computePreferredSize() {
        //TODO: Measure the text size and set the preferred size accordingly
        this.preferredSize.set(300, 30);
    }

}
