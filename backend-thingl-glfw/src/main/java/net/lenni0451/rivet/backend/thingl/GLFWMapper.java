package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.util.EnumSet;
import java.util.Set;

public class GLFWMapper {

    public static KeyEvent mapKey(final int keyCode, final int modifier) {
        Key key = switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> Key.SPACE;
            case GLFW.GLFW_KEY_APOSTROPHE -> Key.APOSTROPHE;
            case GLFW.GLFW_KEY_COMMA -> Key.COMMA;
            case GLFW.GLFW_KEY_MINUS -> Key.MINUS;
            case GLFW.GLFW_KEY_PERIOD -> Key.PERIOD;
            case GLFW.GLFW_KEY_SLASH -> Key.SLASH;
            case GLFW.GLFW_KEY_0 -> Key.KB_0;
            case GLFW.GLFW_KEY_1 -> Key.KB_1;
            case GLFW.GLFW_KEY_2 -> Key.KB_2;
            case GLFW.GLFW_KEY_3 -> Key.KB_3;
            case GLFW.GLFW_KEY_4 -> Key.KB_4;
            case GLFW.GLFW_KEY_5 -> Key.KB_5;
            case GLFW.GLFW_KEY_6 -> Key.KB_6;
            case GLFW.GLFW_KEY_7 -> Key.KB_7;
            case GLFW.GLFW_KEY_8 -> Key.KB_8;
            case GLFW.GLFW_KEY_9 -> Key.KB_9;
            case GLFW.GLFW_KEY_SEMICOLON -> Key.SEMICOLON;
            case GLFW.GLFW_KEY_EQUAL -> Key.EQUAL;
            case GLFW.GLFW_KEY_A -> Key.A;
            case GLFW.GLFW_KEY_B -> Key.B;
            case GLFW.GLFW_KEY_C -> Key.C;
            case GLFW.GLFW_KEY_D -> Key.D;
            case GLFW.GLFW_KEY_E -> Key.E;
            case GLFW.GLFW_KEY_F -> Key.F;
            case GLFW.GLFW_KEY_G -> Key.G;
            case GLFW.GLFW_KEY_H -> Key.H;
            case GLFW.GLFW_KEY_I -> Key.I;
            case GLFW.GLFW_KEY_J -> Key.J;
            case GLFW.GLFW_KEY_K -> Key.K;
            case GLFW.GLFW_KEY_L -> Key.L;
            case GLFW.GLFW_KEY_M -> Key.M;
            case GLFW.GLFW_KEY_N -> Key.N;
            case GLFW.GLFW_KEY_O -> Key.O;
            case GLFW.GLFW_KEY_P -> Key.P;
            case GLFW.GLFW_KEY_Q -> Key.Q;
            case GLFW.GLFW_KEY_R -> Key.R;
            case GLFW.GLFW_KEY_S -> Key.S;
            case GLFW.GLFW_KEY_T -> Key.T;
            case GLFW.GLFW_KEY_U -> Key.U;
            case GLFW.GLFW_KEY_V -> Key.V;
            case GLFW.GLFW_KEY_W -> Key.W;
            case GLFW.GLFW_KEY_X -> Key.X;
            case GLFW.GLFW_KEY_Y -> Key.Y;
            case GLFW.GLFW_KEY_Z -> Key.Z;
            case GLFW.GLFW_KEY_LEFT_BRACKET -> Key.LEFT_BRACKET;
            case GLFW.GLFW_KEY_BACKSLASH -> Key.BACKSLASH;
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> Key.RIGHT_BRACKET;
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> Key.GRAVE_ACCENT;
            case GLFW.GLFW_KEY_ESCAPE -> Key.ESCAPE;
            case GLFW.GLFW_KEY_ENTER -> Key.ENTER;
            case GLFW.GLFW_KEY_TAB -> Key.TAB;
            case GLFW.GLFW_KEY_BACKSPACE -> Key.BACKSPACE;
            case GLFW.GLFW_KEY_INSERT -> Key.INSERT;
            case GLFW.GLFW_KEY_DELETE -> Key.DELETE;
            case GLFW.GLFW_KEY_RIGHT -> Key.RIGHT;
            case GLFW.GLFW_KEY_LEFT -> Key.LEFT;
            case GLFW.GLFW_KEY_DOWN -> Key.DOWN;
            case GLFW.GLFW_KEY_UP -> Key.UP;
            case GLFW.GLFW_KEY_PAGE_UP -> Key.PAGE_UP;
            case GLFW.GLFW_KEY_PAGE_DOWN -> Key.PAGE_DOWN;
            case GLFW.GLFW_KEY_HOME -> Key.HOME;
            case GLFW.GLFW_KEY_END -> Key.END;
            case GLFW.GLFW_KEY_CAPS_LOCK -> Key.CAPS_LOCK;
            case GLFW.GLFW_KEY_SCROLL_LOCK -> Key.SCROLL_LOCK;
            case GLFW.GLFW_KEY_NUM_LOCK -> Key.NUM_LOCK;
            case GLFW.GLFW_KEY_PRINT_SCREEN -> Key.PRINT_SCREEN;
            case GLFW.GLFW_KEY_PAUSE -> Key.PAUSE;
            case GLFW.GLFW_KEY_F1 -> Key.F1;
            case GLFW.GLFW_KEY_F2 -> Key.F2;
            case GLFW.GLFW_KEY_F3 -> Key.F3;
            case GLFW.GLFW_KEY_F4 -> Key.F4;
            case GLFW.GLFW_KEY_F5 -> Key.F5;
            case GLFW.GLFW_KEY_F6 -> Key.F6;
            case GLFW.GLFW_KEY_F7 -> Key.F7;
            case GLFW.GLFW_KEY_F8 -> Key.F8;
            case GLFW.GLFW_KEY_F9 -> Key.F9;
            case GLFW.GLFW_KEY_F10 -> Key.F10;
            case GLFW.GLFW_KEY_F11 -> Key.F11;
            case GLFW.GLFW_KEY_F12 -> Key.F12;
            case GLFW.GLFW_KEY_F13 -> Key.F13;
            case GLFW.GLFW_KEY_F14 -> Key.F14;
            case GLFW.GLFW_KEY_F15 -> Key.F15;
            case GLFW.GLFW_KEY_F16 -> Key.F16;
            case GLFW.GLFW_KEY_F17 -> Key.F17;
            case GLFW.GLFW_KEY_F18 -> Key.F18;
            case GLFW.GLFW_KEY_F19 -> Key.F19;
            case GLFW.GLFW_KEY_F20 -> Key.F20;
            case GLFW.GLFW_KEY_F21 -> Key.F21;
            case GLFW.GLFW_KEY_F22 -> Key.F22;
            case GLFW.GLFW_KEY_F23 -> Key.F23;
            case GLFW.GLFW_KEY_F24 -> Key.F24;
            case GLFW.GLFW_KEY_KP_0 -> Key.KP_0;
            case GLFW.GLFW_KEY_KP_1 -> Key.KP_1;
            case GLFW.GLFW_KEY_KP_2 -> Key.KP_2;
            case GLFW.GLFW_KEY_KP_3 -> Key.KP_3;
            case GLFW.GLFW_KEY_KP_4 -> Key.KP_4;
            case GLFW.GLFW_KEY_KP_5 -> Key.KP_5;
            case GLFW.GLFW_KEY_KP_6 -> Key.KP_6;
            case GLFW.GLFW_KEY_KP_7 -> Key.KP_7;
            case GLFW.GLFW_KEY_KP_8 -> Key.KP_8;
            case GLFW.GLFW_KEY_KP_9 -> Key.KP_9;
            case GLFW.GLFW_KEY_KP_DECIMAL -> Key.KP_DECIMAL;
            case GLFW.GLFW_KEY_KP_DIVIDE -> Key.KP_DIVIDE;
            case GLFW.GLFW_KEY_KP_MULTIPLY -> Key.KP_MULTIPLY;
            case GLFW.GLFW_KEY_KP_SUBTRACT -> Key.KP_SUBTRACT;
            case GLFW.GLFW_KEY_KP_ADD -> Key.KP_ADD;
            case GLFW.GLFW_KEY_KP_ENTER -> Key.KP_ENTER;
            case GLFW.GLFW_KEY_KP_EQUAL -> Key.KP_EQUAL;
            case GLFW.GLFW_KEY_LEFT_SHIFT -> Key.LEFT_SHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL -> Key.LEFT_CONTROL;
            case GLFW.GLFW_KEY_LEFT_ALT -> Key.LEFT_ALT;
            case GLFW.GLFW_KEY_LEFT_SUPER -> Key.LEFT_SUPER;
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> Key.RIGHT_SHIFT;
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> Key.RIGHT_CONTROL;
            case GLFW.GLFW_KEY_RIGHT_ALT -> Key.RIGHT_ALT;
            case GLFW.GLFW_KEY_RIGHT_SUPER -> Key.RIGHT_SUPER;
            case GLFW.GLFW_KEY_MENU -> Key.MENU;
            default -> null;
        };
        if (key == null) return null;
        return new KeyEvent(key, mapModifiers(modifier));
    }

    public static MouseButtonEvent mapMouseButton(final float x, final float y, final int button, final int modifier) {
        MouseButton mouseButton = switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> MouseButton.LEFT;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> MouseButton.RIGHT;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> MouseButton.MIDDLE;
            default -> null;
        };
        if (mouseButton == null) return null;
        return new MouseButtonEvent(x, y, mouseButton, mapModifiers(modifier));
    }

    private static Set<ModifierKey> mapModifiers(final int modifier) {
        Set<ModifierKey> modifiers = EnumSet.noneOf(ModifierKey.class);
        if ((modifier & GLFW.GLFW_MOD_SHIFT) != 0) modifiers.add(ModifierKey.SHIFT);
        if ((modifier & GLFW.GLFW_MOD_CONTROL) != 0) modifiers.add(ModifierKey.CONTROL);
        if ((modifier & GLFW.GLFW_MOD_ALT) != 0) modifiers.add(ModifierKey.ALT);
        if ((modifier & GLFW.GLFW_MOD_SUPER) != 0) modifiers.add(ModifierKey.SUPER);
        return modifiers;
    }

}
