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
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.input.keyboard.CharEvent;
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
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;
import net.lenni0451.rivet.utils.TextInputNavigationHandler;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class TextField extends Component {

    @Getter
    private Font font;
    private final StringBuffer text = new StringBuffer();
    @Getter
    private String hint;
    @Getter
    private final ListenerList<Consumer<String>> valueChangeListener = new ListenerList<>();
    @Getter
    @Nullable
    private Function<Character, Character> charReplacer;
    @Getter
    private Predicate<String> validator;
    @Getter
    @Nullable
    private SyntaxHighlighter highlighter;

    private ShapedText shapedText;
    private ShapedText shapedHintText;
    @Getter
    private boolean valid = true;
    @Getter
    private int cursor = 0;
    @Getter
    private int selection = 0;
    private boolean focused;
    private boolean selecting = false;
    private float scrollX = 0;
    private int clickCount;
    private long lastClick;
    private Animation cursorAnimation;
    @Getter
    private final NavigationHandler navigationHandler = new NavigationHandler();

    @Getter
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.TextField.TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> invalidTextColor = new ThemeOption<>(this, Theme.TextField.INVALID_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> hintColor = new ThemeOption<>(this, Theme.TextField.HINT_COLOR);
    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.TextField.BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.TextField.OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> focusedOutlineColor = new ThemeOption<>(this, Theme.TextField.FOCUSED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> invalidOutlineColor = new ThemeOption<>(this, Theme.TextField.INVALID_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> selectionColor = new ThemeOption<>(this, Theme.TextField.SELECTION_COLOR);
    @Getter
    private final ThemeOption<Color> cursorColor = new ThemeOption<>(this, Theme.TextField.CURSOR_COLOR);
    @Getter
    private final ThemeOption<Float> cursorWidth = new ThemeOption<>(this, Theme.TextField.CURSOR_WIDTH);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.TextField.OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Corners> cornerRadius = new ThemeOption<>(this, Theme.TextField.CORNER_RADIUS);
    @Getter
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.TextField.INNER_PADDING);
    @Getter
    private final ThemeOption<Character> passwordChar = new ThemeOption<>(this, Theme.TextField.PASSWORD_CHAR);
    @Getter
    private final ThemeOption<String> wordBoundaries = new ThemeOption<>(this, Theme.TextField.WORD_BOUNDARIES);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.TextField.DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.TextField.DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.TextField.DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> cursorAnimationConfig = new ThemeOption<>(this, Theme.TextField.CURSOR_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> focusAnimationConfig = new ThemeOption<>(this, Theme.TextField.FOCUS_ANIMATION);

    private Transition<Color> outlineColorTransition;

    public TextField() {
        this("");
    }

    public TextField(final String text) {
        this.text(text);

        this.positionUpdateListener().add(new PositionUpdateListener(() -> this.focused, this::updateInputPosition));
    }

    public final TextField font(final Font font) {
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

    public final TextField text(final String text) {
        return this.text(text, true);
    }

    public final TextField text(final String text, final boolean fireListeners) {
        this.text.setLength(0);
        this.text.append(text);
        this.cursor = Math.min(this.cursor, this.text.length());
        this.selection = Math.min(this.selection, this.text.length());
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        this.onTextChange(fireListeners);
        return this;
    }

    public final TextField hint(final String hint) {
        this.hint = hint;
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextField passwordField(final boolean enabled) {
        if (enabled) {
            this.charReplacer = c -> this.passwordChar.value();
        } else {
            this.charReplacer = null;
        }
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextField charReplacer(final Function<Character, Character> charReplacer) {
        this.charReplacer = charReplacer;
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextField validator(final Predicate<String> validator) {
        this.validator = validator;
        this.validate();
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextField highlighter(@Nullable final SyntaxHighlighter highlighter) {
        this.highlighter = highlighter;
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        return this;
    }

    public final TextField cursor(final int cursor) {
        this.cursor = MathUtils.clamp(cursor, 0, this.text.length());
        this.selection = MathUtils.clamp(cursor, 0, this.text.length());
        if (this.cursorAnimation != null) {
            this.cursorAnimation.reset().start();
        }
        return this;
    }

    public final TextField selection(final int selection) {
        this.selection = MathUtils.clamp(selection, 0, this.text.length());
        return this;
    }

    private void updateShapedText() {
        this.validate();
        String text = this.text.toString();
        if (this.charReplacer != null) {
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                chars[i] = this.charReplacer.apply(chars[i]);
            }
            text = new String(chars);
        }
        boolean disabled = this.disabled().get();
        Color textColor;
        if (disabled) textColor = this.disabledTextColor.value();
        else if (!this.valid) textColor = this.invalidTextColor.value();
        else textColor = this.textColor.value();
        TextFormat defaultFormat = TextFormat.DEFAULT.withColor(textColor);
        if (this.highlighter != null) {
            TextBlock block = this.highlighter.highlight(text, defaultFormat);
            TextLine line = block.lines().isEmpty() ? new TextLine() : block.lines().get(0);
            this.shapedText = this.usedFont().shapeText(line);
        } else {
            this.shapedText = this.usedFont().shapeText(text, textColor);
        }
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

    private void onTextChange(final boolean fireListeners) {
        this.validate();
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

    @Override
    protected void onAddedInternal() {
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
        return this.navigationHandler.onKeyDown(event);
    }

    @Override
    protected boolean onCharTypedInternal(final CharEvent event) {
        return this.navigationHandler.onCharTyped(event);
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

            this.cursor = this.shapedText.index(event.x() - this.innerPadding.value().left() + this.scrollX, 0);
            if (!event.modifiers().contains(ModifierKey.SHIFT)) {
                this.selection = this.cursor;
                this.clickCount = this.navigationHandler.handleMouseClickSelection(this.clickCount, 0);
            }
            this.selecting = true;
            if (this.cursorAnimation != null) {
                this.cursorAnimation.reset().start();
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
            this.cursor = this.shapedText.index(event.x() - this.innerPadding.value().left() + this.scrollX, 0);
            this.cursorAnimation.reset().start();
        }
        return true;
    }

    @Override
    protected void renderInternal(final Renderer renderer, final Size size, final Rectangle visibleArea) {
        float visibleWidth = size.width() - this.innerPadding.value().horizontal();
        float textHeight = this.shapedText.logicalBounds().height();
        float cursorHeight = textHeight == 0 ? this.usedFont().height() : textHeight;
        this.ensureCursorVisible(visibleWidth);

        Color backgroundColor = this.disabled().get() ? this.disabledBackgroundColor.value() : this.backgroundColor.value();
        renderer.optimizedFillRoundedRect(0, 0, size.width(), size.height(), this.cornerRadius.value(), backgroundColor);
        renderer.optimizedOutlineRoundedRect(0, 0, size.width(), size.height(), this.cornerRadius.value(), this.outlineWidth.value(), this.outlineColorTransition.value());

        renderer.scissor(this.innerPadding.value().left(), this.innerPadding.value().top(), visibleWidth, size.height() - this.innerPadding.value().top() - this.innerPadding.value().bottom(), () -> {
            renderer.translate(this.innerPadding.value().left(), this.innerPadding.value().top() + (size.height() - this.innerPadding.value().top() - this.innerPadding.value().bottom()) / 2F, () -> {
                renderer.translate(-this.scrollX, 0, () -> {
                    if (this.cursor != this.selection) {
                        float x1 = this.shapedText.cursorPosition(this.cursor).x();
                        float x2 = this.shapedText.cursorPosition(this.selection).x();
                        renderer.fillRect(Math.min(x1, x2), -cursorHeight / 2F, Math.abs(x1 - x2), cursorHeight, this.selectionColor.value());
                    }

                    if (this.text.isEmpty() && this.shapedHintText != null) {
                        renderer.text(this.shapedHintText, 0, 0, TextOrigin.Horizontal.VISUAL_LEFT, TextOrigin.Vertical.LOGICAL_CENTER);
                    } else {
                        renderer.text(this.shapedText, 0, 0, TextOrigin.Horizontal.VISUAL_LEFT, TextOrigin.Vertical.LOGICAL_CENTER);
                    }

                    if (this.focused) {
                        float cursorWidth = this.cursorWidth.value();
                        float cursorX = this.shapedText.cursorPosition(this.cursor).x();
                        float cursorAlpha = this.cursorAnimation != null ? this.cursorAnimation.getValue() : 1;
                        renderer.fillRect(cursorX - cursorWidth / 2F, -cursorHeight / 2F, cursorWidth, cursorHeight, this.cursorColor.value().withAlphaF(cursorAlpha));
                    }
                });
            });
        });
    }

    @Override
    public Size computeIdealSize(final Size constraints) {
        float textHeight = this.usedFont().height();
        return new Size(
                textHeight * 10 + this.innerPadding.value().horizontal(),
                textHeight + this.innerPadding.value().vertical()
        );
    }

    private void ensureCursorVisible(final float visibleWidth) {
        float cursorX = this.shapedText.cursorPosition(this.cursor).x();
        if (cursorX < this.scrollX) {
            this.scrollX = cursorX;
        } else if (cursorX > this.scrollX + visibleWidth) {
            this.scrollX = cursorX - visibleWidth;
        }
        this.scrollX = Math.max(0, Math.min(this.scrollX, Math.max(0, this.shapedText.logicalBounds().width() - visibleWidth)));
    }


    private enum State {
        IDLE, FOCUSED, INVALID, DISABLED
    }

    private class NavigationHandler extends TextInputNavigationHandler {
        @Override
        public StringBuffer textBuffer() {
            return TextField.this.text;
        }

        @Override
        public int cursor() {
            return TextField.this.cursor;
        }

        @Override
        public void cursor(final int cursor) {
            TextField.this.cursor = MathUtils.clamp(cursor, 0, TextField.this.text.length());
        }

        @Override
        public int selection() {
            return TextField.this.selection;
        }

        @Override
        public void selection(final int selection) {
            TextField.this.selection = MathUtils.clamp(selection, 0, TextField.this.text.length());
        }

        @Override
        public String wordBoundaries() {
            return TextField.this.wordBoundaries.value();
        }

        @Override
        public void onTextModified(final boolean fireListeners) {
            if (fireListeners) {
                TextField.this.updateShapedText();
                TextField.this.onTextChange(true);
            }
        }

        @Override
        public void onCursorMoved() {
            if (TextField.this.cursorAnimation != null) {
                TextField.this.cursorAnimation.reset().start();
            }
        }

        @Override
        public String getClipboard() {
            return TextField.this.rivet().backend().getClipboard();
        }

        @Override
        public void setClipboard(final String text) {
            TextField.this.rivet().backend().setClipboard(text);
        }
    }

}
