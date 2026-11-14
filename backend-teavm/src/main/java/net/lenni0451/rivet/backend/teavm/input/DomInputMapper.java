package net.lenni0451.rivet.backend.teavm.input;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.MouseEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

@UtilityClass
public class DomInputMapper {

    private static final Key[] KEYS = Key.values();
    private static final int IME_KEY_CODE = 229; // Used by all browsers for keys that are processed by an IME

    @Nullable
    public static Key mapKey(final KeyboardEvent event) {
        // Letters use the layout dependent key so shortcuts (e.g. Ctrl+Z) match the printed key
        String key = event.getKey();
        if (key != null && key.length() == 1) {
            char c = Character.toUpperCase(key.charAt(0));
            if (c >= 'A' && c <= 'Z') {
                return KEYS[Key.A.ordinal() + (c - 'A')];
            }
        }
        return mapCode(event.getCode());
    }

    @Nullable
    public static Key mapCode(@Nullable final String code) {
        if (code == null) return null;
        if (code.length() == 4 && code.startsWith("Key")) {
            char c = code.charAt(3);
            if (c >= 'A' && c <= 'Z') return KEYS[Key.A.ordinal() + (c - 'A')];
        }
        if (code.length() == 6 && code.startsWith("Digit")) {
            return digit(code.charAt(5), false);
        }
        if (code.length() == 7 && code.startsWith("Numpad") && Character.isDigit(code.charAt(6))) {
            return digit(code.charAt(6), true);
        }
        if (code.length() >= 2 && code.charAt(0) == 'F' && isNumber(code.substring(1))) {
            int number = Integer.parseInt(code.substring(1));
            if (number >= 1 && number <= 24) return KEYS[Key.F1.ordinal() + number - 1];
        }
        return switch (code) {
            case "Space" -> Key.SPACE;
            case "Quote" -> Key.APOSTROPHE;
            case "Comma" -> Key.COMMA;
            case "Minus" -> Key.MINUS;
            case "Period" -> Key.PERIOD;
            case "Slash" -> Key.SLASH;
            case "Semicolon" -> Key.SEMICOLON;
            case "Equal" -> Key.EQUAL;
            case "BracketLeft" -> Key.LEFT_BRACKET;
            case "Backslash", "IntlBackslash" -> Key.BACKSLASH;
            case "BracketRight" -> Key.RIGHT_BRACKET;
            case "Backquote" -> Key.GRAVE_ACCENT;
            case "Escape" -> Key.ESCAPE;
            case "Enter" -> Key.ENTER;
            case "Tab" -> Key.TAB;
            case "Backspace" -> Key.BACKSPACE;
            case "Insert" -> Key.INSERT;
            case "Delete" -> Key.DELETE;
            case "ArrowRight" -> Key.RIGHT;
            case "ArrowLeft" -> Key.LEFT;
            case "ArrowDown" -> Key.DOWN;
            case "ArrowUp" -> Key.UP;
            case "PageUp" -> Key.PAGE_UP;
            case "PageDown" -> Key.PAGE_DOWN;
            case "Home" -> Key.HOME;
            case "End" -> Key.END;
            case "CapsLock" -> Key.CAPS_LOCK;
            case "ScrollLock" -> Key.SCROLL_LOCK;
            case "NumLock" -> Key.NUM_LOCK;
            case "PrintScreen" -> Key.PRINT_SCREEN;
            case "Pause" -> Key.PAUSE;
            case "NumpadDecimal" -> Key.KP_DECIMAL;
            case "NumpadDivide" -> Key.KP_DIVIDE;
            case "NumpadMultiply" -> Key.KP_MULTIPLY;
            case "NumpadSubtract" -> Key.KP_SUBTRACT;
            case "NumpadAdd" -> Key.KP_ADD;
            case "NumpadEnter" -> Key.KP_ENTER;
            case "NumpadEqual" -> Key.KP_EQUAL;
            case "ShiftLeft" -> Key.LEFT_SHIFT;
            case "ControlLeft" -> Key.LEFT_CONTROL;
            case "AltLeft" -> Key.LEFT_ALT;
            case "MetaLeft", "OSLeft" -> Key.LEFT_SUPER;
            case "ShiftRight" -> Key.RIGHT_SHIFT;
            case "ControlRight" -> Key.RIGHT_CONTROL;
            case "AltRight" -> Key.RIGHT_ALT;
            case "MetaRight", "OSRight" -> Key.RIGHT_SUPER;
            case "ContextMenu" -> Key.MENU;
            default -> null;
        };
    }

    public static Set<ModifierKey> mapModifiers(final KeyboardEvent event) {
        return modifiers(event.isShiftKey(), event.isCtrlKey(), event.isAltKey(), event.isMetaKey());
    }

    public static Set<ModifierKey> mapModifiers(final MouseEvent event) {
        return modifiers(event.getShiftKey(), event.getCtrlKey(), event.getAltKey(), event.getMetaKey());
    }

    @Nullable
    public static MouseButton mapMouseButton(final int button) {
        return switch (button) {
            case 0 -> MouseButton.LEFT;
            case 1 -> MouseButton.MIDDLE;
            case 2 -> MouseButton.RIGHT;
            default -> null;
        };
    }

    /**
     * @return The bit of the button in {@link MouseEvent#getButtons()}
     */
    public static int buttonMask(final MouseButton button) {
        return switch (button) {
            case LEFT -> 1;
            case RIGHT -> 2;
            case MIDDLE -> 4;
        };
    }

    /**
     * Check if a key event is part of an IME composition.<br>
     * Those events must be left to the browser, otherwise keys like backspace or enter would edit the text field instead of the composition.
     */
    public static boolean isComposing(final KeyboardEvent event) {
        return event.isComposing() || event.getKeyCode() == IME_KEY_CODE;
    }

    private static Set<ModifierKey> modifiers(final boolean shift, final boolean control, final boolean alt, final boolean meta) {
        Set<ModifierKey> modifiers = EnumSet.noneOf(ModifierKey.class);
        if (shift) modifiers.add(ModifierKey.SHIFT);
        if (control) modifiers.add(ModifierKey.CONTROL);
        if (alt) modifiers.add(ModifierKey.ALT);
        if (meta) modifiers.add(ModifierKey.SUPER);
        return modifiers;
    }

    private static Key digit(final char c, final boolean keypad) {
        return KEYS[(keypad ? Key.KP_0 : Key.KB_0).ordinal() + (c - '0')];
    }

    private static boolean isNumber(final String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public static boolean isNavigationKey(final Key key) {
        return switch (key) {
            case TAB, BACKSPACE, SPACE, UP, DOWN, LEFT, RIGHT, PAGE_UP, PAGE_DOWN, HOME, END, ENTER, KP_ENTER -> true;
            default -> false;
        };
    }

}
