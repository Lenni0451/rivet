package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.constants.KeyboardConstants;

import java.awt.event.KeyEvent;

public class AWTKeymap {

    public static int mapKeyCode(final int awtKeyCode, final int awtKeyLocation) {
        return switch (awtKeyCode) {
            case KeyEvent.VK_SPACE -> KeyboardConstants.KEY_SPACE;
            case KeyEvent.VK_QUOTE -> KeyboardConstants.KEY_APOSTROPHE;
            case KeyEvent.VK_COMMA -> KeyboardConstants.KEY_COMMA;
            case KeyEvent.VK_MINUS -> KeyboardConstants.KEY_MINUS;
            case KeyEvent.VK_PERIOD -> KeyboardConstants.KEY_PERIOD;
            case KeyEvent.VK_SLASH -> KeyboardConstants.KEY_SLASH;
            case KeyEvent.VK_0 -> KeyboardConstants.KEY_0;
            case KeyEvent.VK_1 -> KeyboardConstants.KEY_1;
            case KeyEvent.VK_2 -> KeyboardConstants.KEY_2;
            case KeyEvent.VK_3 -> KeyboardConstants.KEY_3;
            case KeyEvent.VK_4 -> KeyboardConstants.KEY_4;
            case KeyEvent.VK_5 -> KeyboardConstants.KEY_5;
            case KeyEvent.VK_6 -> KeyboardConstants.KEY_6;
            case KeyEvent.VK_7 -> KeyboardConstants.KEY_7;
            case KeyEvent.VK_8 -> KeyboardConstants.KEY_8;
            case KeyEvent.VK_9 -> KeyboardConstants.KEY_9;
            case KeyEvent.VK_SEMICOLON -> KeyboardConstants.KEY_SEMICOLON;
            case KeyEvent.VK_EQUALS -> {
                if (awtKeyLocation == KeyEvent.KEY_LOCATION_NUMPAD) {
                    yield KeyboardConstants.KEY_KP_EQUAL;
                } else {
                    yield KeyboardConstants.KEY_EQUAL;
                }
            }
            case KeyEvent.VK_A -> KeyboardConstants.KEY_A;
            case KeyEvent.VK_B -> KeyboardConstants.KEY_B;
            case KeyEvent.VK_C -> KeyboardConstants.KEY_C;
            case KeyEvent.VK_D -> KeyboardConstants.KEY_D;
            case KeyEvent.VK_E -> KeyboardConstants.KEY_E;
            case KeyEvent.VK_F -> KeyboardConstants.KEY_F;
            case KeyEvent.VK_G -> KeyboardConstants.KEY_G;
            case KeyEvent.VK_H -> KeyboardConstants.KEY_H;
            case KeyEvent.VK_I -> KeyboardConstants.KEY_I;
            case KeyEvent.VK_J -> KeyboardConstants.KEY_J;
            case KeyEvent.VK_K -> KeyboardConstants.KEY_K;
            case KeyEvent.VK_L -> KeyboardConstants.KEY_L;
            case KeyEvent.VK_M -> KeyboardConstants.KEY_M;
            case KeyEvent.VK_N -> KeyboardConstants.KEY_N;
            case KeyEvent.VK_O -> KeyboardConstants.KEY_O;
            case KeyEvent.VK_P -> KeyboardConstants.KEY_P;
            case KeyEvent.VK_Q -> KeyboardConstants.KEY_Q;
            case KeyEvent.VK_R -> KeyboardConstants.KEY_R;
            case KeyEvent.VK_S -> KeyboardConstants.KEY_S;
            case KeyEvent.VK_T -> KeyboardConstants.KEY_T;
            case KeyEvent.VK_U -> KeyboardConstants.KEY_U;
            case KeyEvent.VK_V -> KeyboardConstants.KEY_V;
            case KeyEvent.VK_W -> KeyboardConstants.KEY_W;
            case KeyEvent.VK_X -> KeyboardConstants.KEY_X;
            case KeyEvent.VK_Y -> KeyboardConstants.KEY_Y;
            case KeyEvent.VK_Z -> KeyboardConstants.KEY_Z;
            case KeyEvent.VK_OPEN_BRACKET -> KeyboardConstants.KEY_LEFT_BRACKET;
            case KeyEvent.VK_BACK_SLASH -> KeyboardConstants.KEY_BACKSLASH;
            case KeyEvent.VK_CLOSE_BRACKET -> KeyboardConstants.KEY_RIGHT_BRACKET;
            case KeyEvent.VK_BACK_QUOTE -> KeyboardConstants.KEY_GRAVE_ACCENT;
            case KeyEvent.VK_ESCAPE -> KeyboardConstants.KEY_ESCAPE;
            case KeyEvent.VK_ENTER -> {
                if (awtKeyLocation == KeyEvent.KEY_LOCATION_NUMPAD) {
                    yield KeyboardConstants.KEY_KP_ENTER;
                } else {
                    yield KeyboardConstants.KEY_ENTER;
                }
            }
            case KeyEvent.VK_TAB -> KeyboardConstants.KEY_TAB;
            case KeyEvent.VK_BACK_SPACE -> KeyboardConstants.KEY_BACKSPACE;
            case KeyEvent.VK_INSERT -> KeyboardConstants.KEY_INSERT;
            case KeyEvent.VK_DELETE -> KeyboardConstants.KEY_DELETE;
            case KeyEvent.VK_RIGHT -> KeyboardConstants.KEY_RIGHT;
            case KeyEvent.VK_LEFT -> KeyboardConstants.KEY_LEFT;
            case KeyEvent.VK_DOWN -> KeyboardConstants.KEY_DOWN;
            case KeyEvent.VK_UP -> KeyboardConstants.KEY_UP;
            case KeyEvent.VK_PAGE_UP -> KeyboardConstants.KEY_PAGE_UP;
            case KeyEvent.VK_PAGE_DOWN -> KeyboardConstants.KEY_PAGE_DOWN;
            case KeyEvent.VK_HOME -> KeyboardConstants.KEY_HOME;
            case KeyEvent.VK_END -> KeyboardConstants.KEY_END;
            case KeyEvent.VK_CAPS_LOCK -> KeyboardConstants.KEY_CAPS_LOCK;
            case KeyEvent.VK_SCROLL_LOCK -> KeyboardConstants.KEY_SCROLL_LOCK;
            case KeyEvent.VK_NUM_LOCK -> KeyboardConstants.KEY_NUM_LOCK;
            case KeyEvent.VK_PRINTSCREEN -> KeyboardConstants.KEY_PRINT_SCREEN;
            case KeyEvent.VK_PAUSE -> KeyboardConstants.KEY_PAUSE;
            case KeyEvent.VK_F1 -> KeyboardConstants.KEY_F1;
            case KeyEvent.VK_F2 -> KeyboardConstants.KEY_F2;
            case KeyEvent.VK_F3 -> KeyboardConstants.KEY_F3;
            case KeyEvent.VK_F4 -> KeyboardConstants.KEY_F4;
            case KeyEvent.VK_F5 -> KeyboardConstants.KEY_F5;
            case KeyEvent.VK_F6 -> KeyboardConstants.KEY_F6;
            case KeyEvent.VK_F7 -> KeyboardConstants.KEY_F7;
            case KeyEvent.VK_F8 -> KeyboardConstants.KEY_F8;
            case KeyEvent.VK_F9 -> KeyboardConstants.KEY_F9;
            case KeyEvent.VK_F10 -> KeyboardConstants.KEY_F10;
            case KeyEvent.VK_F11 -> KeyboardConstants.KEY_F11;
            case KeyEvent.VK_F12 -> KeyboardConstants.KEY_F12;
            case KeyEvent.VK_F13 -> KeyboardConstants.KEY_F13;
            case KeyEvent.VK_F14 -> KeyboardConstants.KEY_F14;
            case KeyEvent.VK_F15 -> KeyboardConstants.KEY_F15;
            case KeyEvent.VK_F16 -> KeyboardConstants.KEY_F16;
            case KeyEvent.VK_F17 -> KeyboardConstants.KEY_F17;
            case KeyEvent.VK_F18 -> KeyboardConstants.KEY_F18;
            case KeyEvent.VK_F19 -> KeyboardConstants.KEY_F19;
            case KeyEvent.VK_F20 -> KeyboardConstants.KEY_F20;
            case KeyEvent.VK_F21 -> KeyboardConstants.KEY_F21;
            case KeyEvent.VK_F22 -> KeyboardConstants.KEY_F22;
            case KeyEvent.VK_F23 -> KeyboardConstants.KEY_F23;
            case KeyEvent.VK_F24 -> KeyboardConstants.KEY_F24;
            case KeyEvent.VK_NUMPAD0 -> KeyboardConstants.KEY_KP_0;
            case KeyEvent.VK_NUMPAD1 -> KeyboardConstants.KEY_KP_1;
            case KeyEvent.VK_NUMPAD2 -> KeyboardConstants.KEY_KP_2;
            case KeyEvent.VK_NUMPAD3 -> KeyboardConstants.KEY_KP_3;
            case KeyEvent.VK_NUMPAD4 -> KeyboardConstants.KEY_KP_4;
            case KeyEvent.VK_NUMPAD5 -> KeyboardConstants.KEY_KP_5;
            case KeyEvent.VK_NUMPAD6 -> KeyboardConstants.KEY_KP_6;
            case KeyEvent.VK_NUMPAD7 -> KeyboardConstants.KEY_KP_7;
            case KeyEvent.VK_NUMPAD8 -> KeyboardConstants.KEY_KP_8;
            case KeyEvent.VK_NUMPAD9 -> KeyboardConstants.KEY_KP_9;
            case KeyEvent.VK_DECIMAL -> KeyboardConstants.KEY_KP_DECIMAL;
            case KeyEvent.VK_DIVIDE -> KeyboardConstants.KEY_KP_DIVIDE;
            case KeyEvent.VK_MULTIPLY -> KeyboardConstants.KEY_KP_MULTIPLY;
            case KeyEvent.VK_SUBTRACT -> KeyboardConstants.KEY_KP_SUBTRACT;
            case KeyEvent.VK_ADD -> KeyboardConstants.KEY_KP_ADD;
            case KeyEvent.VK_SHIFT -> {
                if (awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) {
                    yield KeyboardConstants.KEY_RIGHT_SHIFT;
                } else {
                    yield KeyboardConstants.KEY_LEFT_SHIFT;
                }
            }
            case KeyEvent.VK_CONTROL -> {
                if (awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) {
                    yield KeyboardConstants.KEY_RIGHT_CONTROL;
                } else {
                    yield KeyboardConstants.KEY_LEFT_CONTROL;
                }
            }
            case KeyEvent.VK_ALT -> {
                if (awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) {
                    yield KeyboardConstants.KEY_RIGHT_ALT;
                } else {
                    yield KeyboardConstants.KEY_LEFT_ALT;
                }
            }
            case KeyEvent.VK_META -> {
                if (awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT) {
                    yield KeyboardConstants.KEY_RIGHT_SUPER;
                } else {
                    yield KeyboardConstants.KEY_LEFT_SUPER;
                }
            }
            case KeyEvent.VK_CONTEXT_MENU -> KeyboardConstants.KEY_MENU;
            default -> -1;
        };
    }

}
