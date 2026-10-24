package net.lenni0451.rivet.utils;

import lombok.experimental.Accessors;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.ModifierKey;

@Accessors(fluent = true, chain = true, makeFinal = true)
public abstract class TextInputNavigationHandler {

    public abstract StringBuffer textBuffer();

    public abstract int cursor();

    public abstract void cursor(final int cursor);

    public abstract int selection();

    public abstract void selection(final int selection);

    public abstract String wordBoundaries();

    public abstract void onTextModified(final boolean fireListeners);

    public abstract void onCursorMoved();

    public abstract String getClipboard();

    public abstract void setClipboard(final String text);

    public boolean isMultiLine() {
        return false;
    }

    public int lineCount() {
        return 1;
    }

    public int lineIndexAt(final int offset) {
        return 0;
    }

    public int lineStartOffset(final int lineIndex) {
        return 0;
    }

    public int lineEndOffset(final int lineIndex) {
        return this.textLength();
    }

    public int lineLength(final int lineIndex) {
        return this.textLength();
    }

    public int tabWidth() {
        return 4;
    }


    public final int textLength() {
        return this.textBuffer().length();
    }

    public final char charAt(final int index) {
        return this.textBuffer().charAt(index);
    }

    public final boolean isWordBoundary(final char c) {
        String boundaries = this.wordBoundaries();
        return boundaries != null && boundaries.indexOf(c) != -1;
    }

    public final int findWordStart(final int from) {
        int length = this.textLength();
        int i = Math.max(0, Math.min(length, from));
        if (i <= 0) return 0;

        if (this.charAt(i - 1) == '\n') {
            return i - 1;
        }

        while (i > 0 && Character.isWhitespace(this.charAt(i - 1)) && this.charAt(i - 1) != '\n') {
            i--;
        }
        if (i == 0 || this.charAt(i - 1) == '\n') {
            return i;
        }

        if (this.isWordBoundary(this.charAt(i - 1))) {
            while (i > 0 && this.isWordBoundary(this.charAt(i - 1)) && !Character.isWhitespace(this.charAt(i - 1))) {
                i--;
            }
        } else {
            while (i > 0 && !this.isWordBoundary(this.charAt(i - 1)) && !Character.isWhitespace(this.charAt(i - 1))) {
                i--;
            }
        }
        return i;
    }

    public final int findWordEnd(final int from) {
        int length = this.textLength();
        int i = Math.max(0, Math.min(length, from));
        if (i >= length) return length;

        if (this.charAt(i) == '\n') {
            return i + 1;
        }

        while (i < length && Character.isWhitespace(this.charAt(i)) && this.charAt(i) != '\n') {
            i++;
        }
        if (i >= length || this.charAt(i) == '\n') {
            return i;
        }

        if (this.isWordBoundary(this.charAt(i))) {
            while (i < length && this.isWordBoundary(this.charAt(i)) && !Character.isWhitespace(this.charAt(i))) {
                i++;
            }
        } else {
            while (i < length && !this.isWordBoundary(this.charAt(i)) && !Character.isWhitespace(this.charAt(i))) {
                i++;
            }
        }
        return i;
    }

    public final void selectWordAt(final int pos) {
        int length = this.textLength();
        if (length == 0) {
            this.cursor(0);
            this.selection(0);
            return;
        }
        int p = MathUtils.clamp(pos, 0, length);
        if (p >= length || this.charAt(p) == '\n') {
            if (p > 0 && this.charAt(p - 1) != '\n') {
                p--;
            } else if (p >= length) {
                this.cursor(length);
                this.selection(length);
                return;
            }
        }

        char c = this.charAt(p);
        int start = p;
        int end = p;

        if (Character.isWhitespace(c)) {
            while (start > 0 && Character.isWhitespace(this.charAt(start - 1)) && this.charAt(start - 1) != '\n') {
                start--;
            }
            while (end < length && Character.isWhitespace(this.charAt(end)) && this.charAt(end) != '\n') {
                end++;
            }
        } else if (this.isWordBoundary(c)) {
            while (start > 0 && this.isWordBoundary(this.charAt(start - 1)) && !Character.isWhitespace(this.charAt(start - 1))) {
                start--;
            }
            while (end < length && this.isWordBoundary(this.charAt(end)) && !Character.isWhitespace(this.charAt(end))) {
                end++;
            }
        } else {
            while (start > 0 && !this.isWordBoundary(this.charAt(start - 1)) && !Character.isWhitespace(this.charAt(start - 1))) {
                start--;
            }
            while (end < length && !this.isWordBoundary(this.charAt(end)) && !Character.isWhitespace(this.charAt(end))) {
                end++;
            }
        }

        this.selection(start);
        this.cursor(end);
        this.onCursorMoved();
    }

    public final int handleMouseClickSelection(final int clickCount, final int lineIndex) {
        if (clickCount == 2) {
            this.selectWordAt(this.cursor());
            return clickCount;
        } else if (clickCount == 3) {
            if (this.isMultiLine()) {
                this.selection(this.lineStartOffset(lineIndex));
                if (lineIndex + 1 < this.lineCount()) {
                    this.cursor(this.lineStartOffset(lineIndex + 1));
                } else {
                    this.cursor(this.textLength());
                }
                this.onCursorMoved();
            } else {
                this.selectAll();
            }
            return clickCount;
        } else if (clickCount == 4) {
            if (this.isMultiLine()) {
                this.selectAll();
                return clickCount;
            } else {
                return 1;
            }
        } else if (clickCount > 4) {
            return 1;
        }
        return clickCount;
    }

    public final void deleteSelection() {
        this.deleteSelection(true);
    }

    public final void deleteSelection(final boolean fireListeners) {
        int cursor = this.cursor();
        int selection = this.selection();
        if (cursor == selection) return;

        int start = Math.min(cursor, selection);
        int end = Math.max(cursor, selection);
        this.textBuffer().delete(start, end);
        this.cursor(start);
        this.selection(start);
        this.onTextModified(fireListeners);
        this.onCursorMoved();
    }

    public final void copy() {
        int cursor = this.cursor();
        int selection = this.selection();
        if (cursor == selection) return;

        int start = Math.min(cursor, selection);
        int end = Math.max(cursor, selection);
        this.setClipboard(this.textBuffer().substring(start, end));
    }

    public final void cut() {
        this.copy();
        this.deleteSelection();
    }

    public final void paste() {
        String clipboard = this.getClipboard();
        if (clipboard == null || clipboard.isEmpty()) return;

        if (!this.isMultiLine()) {
            clipboard = clipboard.replace("\r\n", "").replace("\r", "").replace("\n", "");
        } else {
            clipboard = clipboard.replace("\r\n", "\n").replace("\r", "\n");
        }
        if (clipboard.isEmpty()) return;

        this.deleteSelection(false);
        int cursor = this.cursor();
        this.textBuffer().insert(cursor, clipboard);
        int newCursor = cursor + clipboard.length();
        this.cursor(newCursor);
        this.selection(newCursor);
        this.onTextModified(true);
        this.onCursorMoved();
    }

    public final void selectAll() {
        this.selection(0);
        this.cursor(this.textLength());
        this.onCursorMoved();
    }

    public final boolean onCharTyped(final CharEvent event) {
        if (event.codePoint() < 32 || event.codePoint() == 127) return false;

        this.deleteSelection(false);
        int cursor = this.cursor();
        if (Character.isBmpCodePoint(event.codePoint())) {
            this.textBuffer().insert(cursor, (char) event.codePoint());
            cursor++;
        } else {
            char[] chars = Character.toChars(event.codePoint());
            this.textBuffer().insert(cursor, chars);
            cursor += chars.length;
        }
        this.cursor(cursor);
        this.selection(cursor);
        this.onTextModified(true);
        this.onCursorMoved();
        return true;
    }

    public final boolean onKeyDown(final KeyEvent event) {
        boolean shift = event.modifiers().contains(ModifierKey.SHIFT);
        boolean ctrl = event.modifiers().contains(ModifierKey.CONTROL);

        int cursor = this.cursor();
        int selection = this.selection();
        int length = this.textLength();

        if (event.key().isEquivalent(Key.LEFT)) {
            if (cursor != selection && !shift && !ctrl) {
                cursor = Math.min(cursor, selection);
                selection = cursor;
            } else {
                if (ctrl) {
                    cursor = this.findWordStart(cursor);
                } else {
                    cursor = Math.max(0, cursor - 1);
                }
                if (!shift) selection = cursor;
            }
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.RIGHT)) {
            if (cursor != selection && !shift && !ctrl) {
                cursor = Math.max(cursor, selection);
                selection = cursor;
            } else {
                if (ctrl) {
                    cursor = this.findWordEnd(cursor);
                } else {
                    cursor = Math.min(length, cursor + 1);
                }
                if (!shift) selection = cursor;
            }
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.UP)) {
            if (!this.isMultiLine()) return false;
            int basePos = shift ? cursor : Math.min(cursor, selection);
            int currentLine = this.lineIndexAt(basePos);
            if (currentLine > 0) {
                int column = basePos - this.lineStartOffset(currentLine);
                int targetLine = currentLine - 1;
                int targetColumn = Math.min(column, this.lineLength(targetLine));
                cursor = this.lineStartOffset(targetLine) + targetColumn;
            } else {
                cursor = 0;
            }
            if (!shift) selection = cursor;
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.DOWN)) {
            if (!this.isMultiLine()) return false;
            int basePos = shift ? cursor : Math.max(cursor, selection);
            int currentLine = this.lineIndexAt(basePos);
            if (currentLine < this.lineCount() - 1) {
                int column = basePos - this.lineStartOffset(currentLine);
                int targetLine = currentLine + 1;
                int targetColumn = Math.min(column, this.lineLength(targetLine));
                cursor = this.lineStartOffset(targetLine) + targetColumn;
            } else {
                cursor = length;
            }
            if (!shift) selection = cursor;
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.HOME)) {
            if (ctrl || !this.isMultiLine()) {
                cursor = 0;
            } else {
                cursor = this.lineStartOffset(this.lineIndexAt(cursor));
            }
            if (!shift) selection = cursor;
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.END)) {
            if (ctrl || !this.isMultiLine()) {
                cursor = length;
            } else {
                cursor = this.lineEndOffset(this.lineIndexAt(cursor));
            }
            if (!shift) selection = cursor;
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.PAGE_UP)) {
            if (!this.isMultiLine()) return false;
            int basePos = shift ? cursor : Math.min(cursor, selection);
            int currentLine = this.lineIndexAt(basePos);
            int column = basePos - this.lineStartOffset(currentLine);
            int targetLine = Math.max(0, currentLine - 10);
            cursor = this.lineStartOffset(targetLine) + Math.min(column, this.lineLength(targetLine));
            if (!shift) selection = cursor;
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.PAGE_DOWN)) {
            if (!this.isMultiLine()) return false;
            int basePos = shift ? cursor : Math.max(cursor, selection);
            int currentLine = this.lineIndexAt(basePos);
            int column = basePos - this.lineStartOffset(currentLine);
            int targetLine = Math.min(this.lineCount() - 1, currentLine + 10);
            cursor = this.lineStartOffset(targetLine) + Math.min(column, this.lineLength(targetLine));
            if (!shift) selection = cursor;
            this.cursor(cursor);
            this.selection(selection);
        } else if (event.key().isEquivalent(Key.ENTER)) {
            if (!this.isMultiLine()) return false;
            this.deleteSelection(false);
            cursor = this.cursor();
            this.textBuffer().insert(cursor, '\n');
            cursor++;
            this.cursor(cursor);
            this.selection(cursor);
            this.onTextModified(true);
            this.onCursorMoved();
            return true;
        } else if (event.key().isEquivalent(Key.TAB)) {
            if (!this.isMultiLine()) return false;
            this.deleteSelection(false);
            int tabWidth = Math.max(1, this.tabWidth());
            String spaces = " ".repeat(tabWidth);
            cursor = this.cursor();
            this.textBuffer().insert(cursor, spaces);
            cursor += spaces.length();
            this.cursor(cursor);
            this.selection(cursor);
            this.onTextModified(true);
            this.onCursorMoved();
            return true;
        } else if (event.key().isEquivalent(Key.BACKSPACE)) {
            if (cursor != selection) {
                this.deleteSelection();
            } else if (ctrl) {
                this.selection(this.findWordStart(cursor));
                this.deleteSelection();
            } else if (cursor > 0) {
                this.textBuffer().deleteCharAt(cursor - 1);
                cursor--;
                this.cursor(cursor);
                this.selection(cursor);
                this.onTextModified(true);
            }
        } else if (event.key().isEquivalent(Key.DELETE)) {
            if (cursor != selection) {
                this.deleteSelection();
            } else if (ctrl) {
                this.selection(this.findWordEnd(cursor));
                this.deleteSelection();
            } else if (cursor < length) {
                this.textBuffer().deleteCharAt(cursor);
                this.onTextModified(true);
            }
        } else if (ctrl && event.key().isEquivalent(Key.A)) {
            this.selectAll();
            return true;
        } else if (ctrl && event.key().isEquivalent(Key.C)) {
            this.copy();
            return true;
        } else if (ctrl && event.key().isEquivalent(Key.V)) {
            this.paste();
            return true;
        } else if (ctrl && event.key().isEquivalent(Key.X)) {
            this.cut();
            return true;
        } else {
            return false;
        }

        this.onCursorMoved();
        return true;
    }

}
