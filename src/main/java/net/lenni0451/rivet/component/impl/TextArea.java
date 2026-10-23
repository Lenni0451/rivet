package net.lenni0451.rivet.component.impl;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.animation.Animation;
import net.lenni0451.commons.color.Color;
import net.lenni0451.commons.math.MathUtils;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.Interpolator;
import net.lenni0451.rivet.animation.Transition;
import net.lenni0451.rivet.backend.render.Renderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.highlight.SyntaxHighlighter;
import net.lenni0451.rivet.text.model.TextBlock;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class TextArea extends Component {

    @Getter
    private Font font;
    private final StringBuffer text = new StringBuffer();
    @Getter
    private String hint;
    @Getter
    private final ListenerList<Consumer<String>> valueChangeListener = new ListenerList<>();
    @Getter
    private Predicate<String> validator;
    @Getter
    private SyntaxHighlighter highlighter = SyntaxHighlighter.NONE;

    private ShapedTextBlock shapedTextBlock;
    private ShapedText shapedHintText;
    @Getter
    private boolean valid = true;
    @Getter
    private int cursor = 0;
    @Getter
    private int selection = 0;
    private boolean focused;
    private boolean selecting = false;
    private int clickCount;
    private long lastClick;
    private Animation cursorAnimation;
    private int[] lineStarts = new int[]{0};

    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.TextArea.TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> invalidTextColor = new ThemeOption<>(this, Theme.TextArea.INVALID_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> hintColor = new ThemeOption<>(this, Theme.TextArea.HINT_COLOR);
    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.TextArea.BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.TextArea.OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> focusedOutlineColor = new ThemeOption<>(this, Theme.TextArea.FOCUSED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> invalidOutlineColor = new ThemeOption<>(this, Theme.TextArea.INVALID_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> selectionColor = new ThemeOption<>(this, Theme.TextArea.SELECTION_COLOR);
    @Getter
    private final ThemeOption<Color> cursorColor = new ThemeOption<>(this, Theme.TextArea.CURSOR_COLOR);
    @Getter
    private final ThemeOption<Float> cursorWidth = new ThemeOption<>(this, Theme.TextArea.CURSOR_WIDTH);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.TextArea.OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Corners> cornerRadius = new ThemeOption<>(this, Theme.TextArea.CORNER_RADIUS);
    @Getter
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.TextArea.INNER_PADDING);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.TextArea.DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.TextArea.DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.TextArea.DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> cursorAnimationConfig = new ThemeOption<>(this, Theme.TextArea.CURSOR_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> focusAnimationConfig = new ThemeOption<>(this, Theme.TextArea.FOCUS_ANIMATION);
    @Getter
    private final ThemeOption<Integer> tabWidth = new ThemeOption<>(this, Theme.TextArea.TAB_WIDTH);

    private Transition<Color> outlineColorTransition;

    public TextArea() {
        this("");
    }

    public TextArea(final String text) {
        this.text(text);

        this.positionUpdateListener().add(new PositionUpdateListener(() -> this.focused, this::updateInputPosition));
    }

    public final TextArea font(final Font font) {
        if (this.font != font) {
            this.font = font;
            if (this.rivet() != null) {
                this.updateShapedText();
                if (this.parent() != null) {
                    this.parent().requestLayoutRecalculation();
                }
            }
        }
        return this;
    }

    public final String text() {
        return this.text.toString();
    }

    public final TextArea text(final String text) {
        return this.text(text, true);
    }

    public final TextArea text(final String text, final boolean fireListeners) {
        this.text.setLength(0);
        if (text != null) {
            this.text.append(text.replace("\r\n", "\n").replace('\r', '\n'));
        }
        this.cursor = Math.min(this.cursor, this.text.length());
        this.selection = Math.min(this.selection, this.text.length());
        this.updateLineStarts();
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        this.onTextChange(fireListeners);
        return this;
    }

    public final TextArea hint(final String hint) {
        this.hint = hint;
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextArea validator(final Predicate<String> validator) {
        this.validator = validator;
        this.validate();
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextArea highlighter(@Nullable final SyntaxHighlighter highlighter) {
        this.highlighter = highlighter != null ? highlighter : SyntaxHighlighter.NONE;
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextArea cursor(final int cursor) {
        this.cursor = MathUtils.clamp(cursor, 0, this.text.length());
        this.selection = this.cursor;
        if (this.cursorAnimation != null) {
            this.cursorAnimation.reset().start();
        }
        return this;
    }

    public final TextArea selection(final int selection) {
        this.selection = MathUtils.clamp(selection, 0, this.text.length());
        return this;
    }

    private void updateLineStarts() {
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        for (int i = 0; i < this.text.length(); i++) {
            if (this.text.charAt(i) == '\n') {
                starts.add(i + 1);
            }
        }
        this.lineStarts = new int[starts.size()];
        for (int i = 0; i < starts.size(); i++) {
            this.lineStarts[i] = starts.get(i);
        }
    }

    public final int lineCount() {
        return this.lineStarts.length;
    }

    public final int lineIndexAt(final int charOffset) {
        int low = 0;
        int high = this.lineStarts.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int start = this.lineStarts[mid];
            int nextStart = (mid + 1 < this.lineStarts.length) ? this.lineStarts[mid + 1] : Integer.MAX_VALUE;
            if (charOffset < start) {
                high = mid - 1;
            } else if (charOffset >= nextStart) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return Math.max(0, Math.min(this.lineStarts.length - 1, low));
    }

    public final int lineStartOffset(final int lineIndex) {
        if (lineIndex < 0 || lineIndex >= this.lineStarts.length) return 0;
        return this.lineStarts[lineIndex];
    }

    public final int lineLength(final int lineIndex) {
        if (lineIndex < 0 || lineIndex >= this.lineStarts.length) return 0;
        int start = this.lineStarts[lineIndex];
        int end = (lineIndex + 1 < this.lineStarts.length) ? this.lineStarts[lineIndex + 1] - 1 : this.text.length();
        return Math.max(0, end - start);
    }

    public final int lineEndOffset(final int lineIndex) {
        return this.lineStartOffset(lineIndex) + this.lineLength(lineIndex);
    }

    private void updateShapedText() {
        this.validate();
        String fullText = this.text.toString();
        boolean disabled = this.disabled().get();
        Color activeTextColor;
        if (disabled) activeTextColor = this.disabledTextColor.value();
        else if (!this.valid) activeTextColor = this.invalidTextColor.value();
        else activeTextColor = this.textColor.value();

        TextFormat defaultFormat = TextFormat.DEFAULT.withColor(activeTextColor);
        SyntaxHighlighter activeHighlighter = this.highlighter != null ? this.highlighter : SyntaxHighlighter.NONE;
        TextBlock textBlock = activeHighlighter.highlight(fullText, defaultFormat);
        this.shapedTextBlock = this.usedFont().shapeText(textBlock);

        if (this.hint != null && !this.hint.isEmpty()) {
            Color hintColor = disabled ? this.disabledTextColor.value() : this.hintColor.value();
            this.shapedHintText = this.usedFont().shapeText(this.hint, hintColor);
        } else {
            this.shapedHintText = null;
        }
    }

    protected final Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    private ShapedText getShapedLine(final int lineIndex) {
        return this.shapedTextBlock.lines().get(lineIndex);
    }

    private float lineHeight() {
        return this.usedFont().height();
    }

    private float totalTextHeight() {
        return this.lineCount() * this.lineHeight();
    }

    private State state() {
        if (this.disabled().get()) {
            return State.DISABLED;
        } else if (!this.valid) {
            return State.INVALID;
        } else if (this.focused) {
            return State.FOCUSED;
        } else {
            return State.IDLE;
        }
    }

    private void updateInputPosition(final Rectangle absoluteBounds) {
        if (this.focused) {
            this.rivet().backend().textInput().area(absoluteBounds);
        }
    }

    @Override
    protected void onAddedInternal() {
        this.updateLineStarts();
        this.updateShapedText();
        this.cursorAnimation = this.cursorAnimationConfig.value().create().start();
        this.outlineColorTransition = new Transition<>(
                this,
                () -> switch (this.state()) {
                    case IDLE -> this.outlineColor.value();
                    case FOCUSED -> this.focusedOutlineColor.value();
                    case INVALID -> this.invalidOutlineColor.value();
                    case DISABLED -> this.disabledOutlineColor.value();
                },
                this.focusAnimationConfig::value,
                Interpolator.COLOR
        );
    }

    @Override
    protected void onRemovedInternal() {
        this.selection = this.cursor;
        this.focused = false;
        this.selecting = false;
        this.clickCount = 0;
    }

    @Override
    protected void onDisabledInternal() {
        this.onRemovedInternal();
        if (this.rivet() != null) {
            this.updateShapedText();
        }
    }

    @Override
    protected void onEnabledInternal() {
        this.updateShapedText();
    }

    @Override
    protected void onFocusGainedInternal() {
        this.focused = true;
        this.rivet().backend().textInput().start();
        this.updateInputPosition(this.absoluteBounds());
    }

    @Override
    protected void onFocusLostInternal() {
        this.selection = this.cursor;
        this.focused = false;
        this.rivet().backend().textInput().stop();
    }

    @Override
    protected void onThemeChangedInternal() {
        this.cursorAnimation = this.cursorAnimationConfig.value().create().start();
        this.updateShapedText();
    }

    @Override
    protected boolean onKeyDownInternal(final KeyEvent event) {
        boolean shift = event.modifiers().contains(ModifierKey.SHIFT);
        boolean ctrl = event.modifiers().contains(ModifierKey.CONTROL);

        if (event.key().isEquivalent(Key.LEFT)) {
            if (ctrl) {
                this.cursor = this.findWordStart(this.cursor);
            } else {
                this.cursor = Math.max(0, this.cursor - 1);
            }
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.RIGHT)) {
            if (ctrl) {
                this.cursor = this.findWordEnd(this.cursor);
            } else {
                this.cursor = Math.min(this.text.length(), this.cursor + 1);
            }
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.UP)) {
            int currentLine = this.lineIndexAt(this.cursor);
            if (currentLine > 0) {
                int column = this.cursor - this.lineStarts[currentLine];
                int targetLine = currentLine - 1;
                int targetColumn = Math.min(column, this.lineLength(targetLine));
                this.cursor = this.lineStarts[targetLine] + targetColumn;
            } else {
                this.cursor = 0;
            }
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.DOWN)) {
            int currentLine = this.lineIndexAt(this.cursor);
            if (currentLine < this.lineCount() - 1) {
                int column = this.cursor - this.lineStarts[currentLine];
                int targetLine = currentLine + 1;
                int targetColumn = Math.min(column, this.lineLength(targetLine));
                this.cursor = this.lineStarts[targetLine] + targetColumn;
            } else {
                this.cursor = this.text.length();
            }
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.HOME)) {
            if (ctrl) {
                this.cursor = 0;
            } else {
                this.cursor = this.lineStarts[this.lineIndexAt(this.cursor)];
            }
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.END)) {
            if (ctrl) {
                this.cursor = this.text.length();
            } else {
                this.cursor = this.lineEndOffset(this.lineIndexAt(this.cursor));
            }
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.PAGE_UP)) {
            int currentLine = this.lineIndexAt(this.cursor);
            int column = this.cursor - this.lineStarts[currentLine];
            int targetLine = Math.max(0, currentLine - 10);
            this.cursor = this.lineStarts[targetLine] + Math.min(column, this.lineLength(targetLine));
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.PAGE_DOWN)) {
            int currentLine = this.lineIndexAt(this.cursor);
            int column = this.cursor - this.lineStarts[currentLine];
            int targetLine = Math.min(this.lineCount() - 1, currentLine + 10);
            this.cursor = this.lineStarts[targetLine] + Math.min(column, this.lineLength(targetLine));
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.ENTER)) {
            this.deleteSelection();
            this.text.insert(this.cursor, '\n');
            this.cursor++;
            this.selection = this.cursor;
            this.updateLineStarts();
            this.updateShapedText();
            this.onTextChange(true);
        } else if (event.key().isEquivalent(Key.TAB)) {
            this.deleteSelection();
            int tabWidth = Math.max(1, this.tabWidth.value());
            String spaces = " ".repeat(tabWidth);
            this.text.insert(this.cursor, spaces);
            this.cursor += spaces.length();
            this.selection = this.cursor;
            this.updateLineStarts();
            this.updateShapedText();
            this.onTextChange(true);
        } else if (event.key().isEquivalent(Key.BACKSPACE)) {
            if (this.cursor != this.selection) {
                this.deleteSelection();
            } else if (ctrl) {
                this.selection = this.findWordStart(this.cursor);
                this.deleteSelection();
            } else if (this.cursor > 0) {
                this.text.deleteCharAt(this.cursor - 1);
                this.cursor--;
                this.selection = this.cursor;
                this.updateLineStarts();
                this.updateShapedText();
                this.onTextChange(true);
            }
        } else if (event.key().isEquivalent(Key.DELETE)) {
            if (this.cursor != this.selection) {
                this.deleteSelection();
            } else if (ctrl) {
                this.selection = this.findWordEnd(this.cursor);
                this.deleteSelection();
            } else if (this.cursor < this.text.length()) {
                this.text.deleteCharAt(this.cursor);
                this.updateLineStarts();
                this.updateShapedText();
                this.onTextChange(true);
            }
        } else if (ctrl && event.key().isEquivalent(Key.A)) {
            this.selection = 0;
            this.cursor = this.text.length();
        } else if (ctrl && event.key().isEquivalent(Key.C)) {
            this.copy();
        } else if (ctrl && event.key().isEquivalent(Key.V)) {
            this.paste();
        } else if (ctrl && event.key().isEquivalent(Key.X)) {
            this.copy();
            this.deleteSelection();
        } else {
            return false;
        }
        this.cursorAnimation.reset().start();
        return true;
    }

    @Override
    protected boolean onCharTypedInternal(final CharEvent event) {
        if (event.codePoint() < 32 || event.codePoint() == 127) return false;
        this.deleteSelection();
        if (Character.isBmpCodePoint(event.codePoint())) {
            this.text.insert(this.cursor, (char) event.codePoint());
        } else {
            this.text.insert(this.cursor, Character.toChars(event.codePoint()));
        }
        this.cursor++;
        this.selection = this.cursor;
        this.cursorAnimation.reset().start();
        this.updateLineStarts();
        this.updateShapedText();
        this.onTextChange(true);
        return true;
    }

    @Override
    protected boolean onMouseDownInternal(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            long now = System.currentTimeMillis();
            if (now - this.lastClick < 250) {
                this.clickCount++;
            } else {
                this.clickCount = 1;
            }
            this.lastClick = now;

            float localX = event.x() - this.innerPadding.value().left();
            float localY = event.y() - this.innerPadding.value().top();
            int lineId = MathUtils.clamp((int) (localY / this.lineHeight()), 0, this.lineCount() - 1);
            ShapedText lineShaped = this.getShapedLine(lineId);
            int column = lineShaped.index(localX, 0);
            this.cursor = this.lineStarts[lineId] + column;

            if (!event.modifiers().contains(ModifierKey.SHIFT)) {
                this.selection = this.cursor;
            }
            this.selecting = true;

            if (this.clickCount == 2) {
                this.selection = this.findWordStart(this.cursor);
                this.cursor = this.findWordEnd(this.cursor);
            } else if (this.clickCount == 3) {
                this.selection = this.lineStarts[lineId];
                this.cursor = this.lineEndOffset(lineId);
            } else if (this.clickCount > 3) {
                this.selection = 0;
                this.cursor = this.text.length();
            }
        }
        return true;
    }

    @Override
    protected boolean onMouseUpInternal(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.selecting = false;
        }
        return true;
    }

    @Override
    protected boolean onMouseMoveInternal(final MouseMoveEvent event, final Size size) {
        if (this.selecting) {
            float localX = event.x() - this.innerPadding.value().left();
            float localY = event.y() - this.innerPadding.value().top();
            int lineId = MathUtils.clamp((int) (localY / this.lineHeight()), 0, this.lineCount() - 1);
            ShapedText lineShaped = this.getShapedLine(lineId);
            int column = lineShaped.index(localX, 0);
            this.cursor = this.lineStarts[lineId] + column;
        }
        return true;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        Rectangle effectiveVisibleArea = visibleArea.intersection(new Rectangle(0, 0, size.width(), size.height()));
        if (effectiveVisibleArea.width() <= 0 || effectiveVisibleArea.height() <= 0) return;

        Color backgroundColor = this.disabled().get() ? this.disabledBackgroundColor.value() : this.backgroundColor.value();
        Corners cornerRadius = this.cornerRadius.value();
        float outlineWidth = this.outlineWidth.value();
        Color outlineColor = this.outlineColorTransition.value();
        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), cornerRadius, backgroundColor);
        renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), cornerRadius, outlineWidth, outlineColor);

        Padding padding = this.innerPadding.value();
        float contentVisibleY1 = effectiveVisibleArea.y() - padding.top();
        float contentVisibleY2 = effectiveVisibleArea.maxY() - padding.top();
        float contentVisibleX1 = effectiveVisibleArea.x() - padding.left();
        float contentVisibleX2 = effectiveVisibleArea.maxX() - padding.left();

        float lineHeight = this.lineHeight();
        int firstVisibleLine = MathUtils.clamp((int) Math.floor(contentVisibleY1 / lineHeight), 0, this.lineCount() - 1);
        int lastVisibleLine = MathUtils.clamp((int) Math.floor(contentVisibleY2 / lineHeight), 0, this.lineCount() - 1);

        boolean hasVisibleLines = firstVisibleLine <= lastVisibleLine && contentVisibleY2 > 0 && contentVisibleY1 < this.totalTextHeight() && contentVisibleX2 > 0;

        renderer.translate(padding.left(), padding.top(), () -> {
            // Render multi-line selection
            if (this.cursor != this.selection && hasVisibleLines) {
                int selectionStart = Math.min(this.cursor, this.selection);
                int selectionEnd = Math.max(this.cursor, this.selection);
                int selectionStartLine = this.lineIndexAt(selectionStart);
                int selectionEndLine = this.lineIndexAt(selectionEnd);

                int startLine = Math.max(firstVisibleLine, selectionStartLine);
                int endLine = Math.min(lastVisibleLine, selectionEndLine);

                for (int i = startLine; i <= endLine; i++) {
                    int lineStart = this.lineStarts[i];
                    int lineEnd = this.lineEndOffset(i);
                    int lineEndWithNewline = (i + 1 < this.lineStarts.length) ? this.lineStarts[i + 1] : this.text.length();

                    if (selectionEnd >= lineStart && selectionStart <= lineEndWithNewline) {
                        int lineSelectionStart = Math.max(selectionStart, lineStart) - lineStart;
                        int lineSelectionEnd = Math.min(selectionEnd, lineEnd) - lineStart;

                        ShapedText lineShaped = this.getShapedLine(i);
                        float x1 = lineShaped.cursorPosition(lineSelectionStart).x();
                        float x2 = lineShaped.cursorPosition(lineSelectionEnd).x();
                        if (selectionEnd > lineEnd && i + 1 < this.lineStarts.length) {
                            x2 += lineHeight * 0.3F;
                        }
                        float y = i * lineHeight;
                        Color selectionColor = this.selectionColor.value();
                        renderer.fillRect(Math.min(x1, x2), y, Math.abs(x2 - x1), lineHeight, selectionColor);
                    }
                }
            }

            // Render text / hint
            if (this.text.isEmpty() && this.shapedHintText != null) {
                if (firstVisibleLine == 0 && contentVisibleY1 < lineHeight) {
                    renderer.text(this.shapedHintText, 0, 0, TextOrigin.Horizontal.LOGICAL_LEFT, TextOrigin.Vertical.LOGICAL_TOP);
                }
            } else if (hasVisibleLines) {
                int maxIndex = Math.min(lastVisibleLine, this.shapedTextBlock.lines().size() - 1);
                for (int i = firstVisibleLine; i <= maxIndex; i++) {
                    ShapedText line = this.shapedTextBlock.lines().get(i);
                    if (line.logicalBounds().maxX() >= contentVisibleX1) {
                        float y = i * lineHeight;
                        renderer.text(line, 0, y, TextOrigin.Horizontal.LOGICAL_LEFT, TextOrigin.Vertical.LOGICAL_TOP);
                    }
                }
            }

            // Render cursor
            if (this.focused && hasVisibleLines) {
                int currentLine = this.lineIndexAt(this.cursor);
                if (currentLine >= firstVisibleLine && currentLine <= lastVisibleLine) {
                    int currentColumn = this.cursor - this.lineStarts[currentLine];
                    ShapedText lineShaped = this.getShapedLine(currentLine);
                    float cursorX = lineShaped.cursorPosition(currentColumn).x();
                    float cursorY = currentLine * lineHeight;
                    float cursorWidth = this.cursorWidth.value();
                    Color cursorColor = this.cursorColor.value();
                    float cursorAlpha = this.cursorAnimation != null ? this.cursorAnimation.getValue() : 1F;
                    renderer.fillRect(cursorX - cursorWidth / 2F, cursorY, cursorWidth, lineHeight, cursorColor.withAlphaF(cursorAlpha));
                }
            }
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float lineHeight = this.lineHeight();
        float contentWidth = (this.text.isEmpty() && this.shapedHintText != null)
                ? this.shapedHintText.logicalBounds().width()
                : this.shapedTextBlock.logicalBounds().width();
        float contentHeight = Math.max(1, this.lineCount()) * lineHeight;
        Padding padding = this.innerPadding.value();
        return new Size(
                contentWidth + padding.horizontal(),
                contentHeight + padding.vertical()
        );
    }

    private void deleteSelection() {
        if (this.cursor == this.selection) return;
        int start = Math.min(this.cursor, this.selection);
        int end = Math.max(this.cursor, this.selection);
        this.text.delete(start, end);
        this.cursor = start;
        this.selection = start;
        this.updateLineStarts();
        this.updateShapedText();
        this.onTextChange(true);
    }

    private void copy() {
        if (this.cursor == this.selection) return;
        int start = Math.min(this.cursor, this.selection);
        int end = Math.max(this.cursor, this.selection);
        this.rivet().backend().setClipboard(this.text.substring(start, end));
    }

    private void paste() {
        String clipboard = this.rivet().backend().getClipboard();
        if (clipboard == null || clipboard.isEmpty()) return;
        clipboard = clipboard.replace("\r\n", "\n").replace('\r', '\n');
        this.deleteSelection();
        this.text.insert(this.cursor, clipboard);
        this.cursor += clipboard.length();
        this.selection = this.cursor;
        this.updateLineStarts();
        this.updateShapedText();
        this.onTextChange(true);
    }

    private int findWordStart(int from) {
        int i = Math.max(0, Math.min(this.text.length(), from));
        while (i > 0 && Character.isWhitespace(this.text.charAt(i - 1)) && this.text.charAt(i - 1) != '\n') i--;
        while (i > 0 && !Character.isWhitespace(this.text.charAt(i - 1))) i--;
        return i;
    }

    private int findWordEnd(int from) {
        int i = Math.max(0, Math.min(this.text.length(), from));
        while (i < this.text.length() && Character.isWhitespace(this.text.charAt(i)) && this.text.charAt(i) != '\n') i++;
        while (i < this.text.length() && !Character.isWhitespace(this.text.charAt(i))) i++;
        return i;
    }

    private void onTextChange(final boolean fireListeners) {
        this.validate();
        if (this.parent() != null) {
            this.parent().requestLayoutRecalculation();
        }
        if (fireListeners) {
            this.valueChangeListener.call(c -> c.accept(this.text()));
        }
    }

    private void validate() {
        if (this.validator == null) {
            this.valid = true;
        } else {
            this.valid = this.validator.test(this.text.toString());
        }
    }


    private enum State {
        IDLE, FOCUSED, INVALID, DISABLED
    }

}
