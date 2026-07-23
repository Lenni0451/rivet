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
import net.lenni0451.rivet.component.ListenerList;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.text.model.TextOrigin;
import net.lenni0451.rivet.theme.Theme;
import net.lenni0451.rivet.theme.ThemeOption;

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
    private final ThemeOption<Color> textColor = new ThemeOption<>(this, Theme.TEXT_FIELD_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> invalidTextColor = new ThemeOption<>(this, Theme.TEXT_FIELD_INVALID_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> hintColor = new ThemeOption<>(this, Theme.TEXT_FIELD_HINT_COLOR);
    @Getter
    private final ThemeOption<Color> backgroundColor = new ThemeOption<>(this, Theme.TEXT_FIELD_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> outlineColor = new ThemeOption<>(this, Theme.TEXT_FIELD_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> focusedOutlineColor = new ThemeOption<>(this, Theme.TEXT_FIELD_FOCUSED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> invalidOutlineColor = new ThemeOption<>(this, Theme.TEXT_FIELD_INVALID_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<Color> selectionColor = new ThemeOption<>(this, Theme.TEXT_FIELD_SELECTION_COLOR);
    @Getter
    private final ThemeOption<Color> cursorColor = new ThemeOption<>(this, Theme.TEXT_FIELD_CURSOR_COLOR);
    @Getter
    private final ThemeOption<Float> cursorWidth = new ThemeOption<>(this, Theme.TEXT_FIELD_CURSOR_WIDTH);
    @Getter
    private final ThemeOption<Float> outlineWidth = new ThemeOption<>(this, Theme.TEXT_FIELD_OUTLINE_WIDTH);
    @Getter
    private final ThemeOption<Float> cornerRadius = new ThemeOption<>(this, Theme.TEXT_FIELD_CORNER_RADIUS);
    @Getter
    private final ThemeOption<Padding> innerPadding = new ThemeOption<>(this, Theme.TEXT_FIELD_INNER_PADDING);
    @Getter
    private final ThemeOption<Character> passwordChar = new ThemeOption<>(this, Theme.TEXT_FIELD_PASSWORD_CHAR);
    @Getter
    private final ThemeOption<Color> disabledTextColor = new ThemeOption<>(this, Theme.TEXT_FIELD_DISABLED_TEXT_COLOR);
    @Getter
    private final ThemeOption<Color> disabledBackgroundColor = new ThemeOption<>(this, Theme.TEXT_FIELD_DISABLED_BACKGROUND_COLOR);
    @Getter
    private final ThemeOption<Color> disabledOutlineColor = new ThemeOption<>(this, Theme.TEXT_FIELD_DISABLED_OUTLINE_COLOR);
    @Getter
    private final ThemeOption<AnimationConfig> cursorAnimationConfig = new ThemeOption<>(this, Theme.TEXT_FIELD_CURSOR_ANIMATION);
    @Getter
    private final ThemeOption<AnimationConfig> focusAnimationConfig = new ThemeOption<>(this, Theme.TEXT_FIELD_FOCUS_ANIMATION);

    private Transition<Color> outlineColorTransition;

    public TextField() {
        this("");
    }

    public TextField(final String text) {
        this.text(text);
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
        this.text.setLength(0);
        this.text.append(text);
        this.cursor = Math.min(this.cursor, this.text.length());
        this.selection = Math.min(this.selection, this.text.length());
        if (this.rivet() != null) {
            this.updateShapedText();
        }
        this.onTextChange();
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
        Color textColor;
        if (this.disabled()) textColor = this.disabledTextColor.value();
        else if (!this.valid) textColor = this.invalidTextColor.value();
        else textColor = this.textColor.value();
        this.shapedText = this.usedFont().shapeText(text, textColor);
        if (this.hint != null && !this.hint.isEmpty()) {
            Color hintColor = this.disabled() ? this.disabledTextColor.value() : this.hintColor.value();
            this.shapedHintText = this.usedFont().shapeText(this.hint, hintColor);
        } else {
            this.shapedHintText = null;
        }
    }

    protected final Font usedFont() {
        return this.font != null ? this.font : this.rivet().backend().font();
    }

    private State state() {
        if (this.disabled()) {
            return State.DISABLED;
        } else if (!this.valid) {
            return State.INVALID;
        } else if (this.focused) {
            return State.FOCUSED;
        } else {
            return State.IDLE;
        }
    }

    @Override
    protected void onComponentAdded() {
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
    protected void onComponentRemoved() {
        this.selection = this.cursor;
        this.focused = false;
        this.selecting = false;
        this.clickCount = 0;
    }

    @Override
    protected void onComponentDisabled() {
        this.onComponentRemoved();
        if (this.rivet() != null) {
            this.updateShapedText();
        }
    }

    @Override
    protected void onComponentEnabled() {
        this.updateShapedText();
    }

    @Override
    protected void onComponentFocusGained() {
        this.focused = true;
        this.rivet().backend().textInput().start();
        this.updateComponentPosition(this.absoluteBounds());
    }

    @Override
    protected void onComponentFocusLost() {
        this.selection = this.cursor;
        this.focused = false;
        this.rivet().backend().textInput().stop();
    }

    @Override
    protected void onComponentThemeChanged() {
        this.cursorAnimation = this.cursorAnimationConfig.value().create().start();
        this.updateShapedText();
    }

    @Override
    protected boolean onComponentKeyDown(final KeyEvent event) {
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
        } else if (event.key().isEquivalent(Key.HOME)) {
            this.cursor = 0;
            if (!shift) this.selection = this.cursor;
        } else if (event.key().isEquivalent(Key.END)) {
            this.cursor = this.text.length();
            if (!shift) this.selection = this.cursor;
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
                this.updateShapedText();
                this.onTextChange();
            }
        } else if (event.key().isEquivalent(Key.DELETE)) {
            if (this.cursor != this.selection) {
                this.deleteSelection();
            } else if (ctrl) {
                this.selection = this.findWordEnd(this.cursor);
                this.deleteSelection();
            } else if (this.cursor < this.text.length()) {
                this.text.deleteCharAt(this.cursor);
                this.updateShapedText();
                this.onTextChange();
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
    protected boolean onComponentCharTyped(final CharEvent event) {
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
        this.updateShapedText();
        this.onTextChange();
        return true;
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
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
            }
            this.selecting = true;

            if (this.clickCount == 2) {
                this.selection = this.findWordStart(this.cursor);
                this.cursor = this.findWordEnd(this.cursor);
            } else if (this.clickCount == 3) {
                this.selection = 0;
                this.cursor = this.text.length();
            } else {
                this.clickCount = 1;
            }
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        if (event.button().equals(MouseButton.LEFT)) {
            this.selecting = false;
        }
        return true;
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        if (this.selecting) {
            this.cursor = this.shapedText.index(event.x() - this.innerPadding.value().left() + this.scrollX, 0);
        }
        return true;
    }

    @Override
    protected void updateComponentPosition(final Rectangle absoluteBounds) {
        if (this.focused) {
            this.rivet().backend().textInput().area(absoluteBounds);
        }
    }

    @Override
    public void render(final Renderer renderer, final Size size) {
        float visibleWidth = size.width() - this.innerPadding.value().horizontal();
        float textHeight = this.shapedText.logicalBounds().height();
        float cursorHeight = textHeight == 0 ? this.usedFont().height() : textHeight;
        this.ensureCursorVisible(visibleWidth);

        Color backgroundColor = this.disabled() ? this.disabledBackgroundColor.value() : this.backgroundColor.value();
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

    private void deleteSelection() {
        if (this.cursor == this.selection) return;
        int start = Math.min(this.cursor, this.selection);
        int end = Math.max(this.cursor, this.selection);
        this.text.delete(start, end);
        this.cursor = start;
        this.selection = start;
        this.updateShapedText();
        this.onTextChange();
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
        this.deleteSelection();
        this.text.insert(this.cursor, clipboard);
        this.cursor += clipboard.length();
        this.selection = this.cursor;
        this.updateShapedText();
        this.onTextChange();
    }

    private int findWordStart(int from) {
        int i = Math.max(0, Math.min(this.text.length(), from));
        while (i > 0 && Character.isWhitespace(this.text.charAt(i - 1))) i--;
        while (i > 0 && !Character.isWhitespace(this.text.charAt(i - 1))) i--;
        return i;
    }

    private int findWordEnd(int from) {
        int i = Math.max(0, Math.min(this.text.length(), from));
        while (i < this.text.length() && Character.isWhitespace(this.text.charAt(i))) i++;
        while (i < this.text.length() && !Character.isWhitespace(this.text.charAt(i))) i++;
        return i;
    }

    private void onTextChange() {
        this.validate();
        this.valueChangeListener.callVoid(c -> c.accept(this.text()));
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
