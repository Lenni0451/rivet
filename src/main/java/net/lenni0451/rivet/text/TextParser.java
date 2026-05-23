package net.lenni0451.rivet.text;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.text.FormatParser.HandlerException;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <h2>Text Format Documentation</h2>
 * <p>
 * This format allows styling text using tags enclosed in angle brackets.
 * Tags do <b>not</b> need to be closed; a style persists until it is explicitly changed or reset.
 * </p>
 *
 * <h3>Basic Syntax</h3>
 * <ul>
 *     <li>
 *         <b>Enable a style:</b> Use the tag name.
 *         <br>Example: <code>&lt;bold&gt;</code>
 *     </li>
 *     <li>
 *         <b>Set a value:</b> Use an equals sign.
 *         <br>Example: <code>&lt;color=red&gt;</code>
 *     </li>
 *     <li>
 *         <b>Reset a style:</b> Use a forward slash prefix to reset a style to its default value.
 *         <br>Example: <code>&lt;/bold&gt;</code>
 *     </li>
 * </ul>
 *
 * <h3>Formatting Options</h3>
 * <p>
 *     The following boolean options are available:
 *     <code>bold</code>, <code>italic</code>, <code>underlined</code>, <code>strikethrough</code>, <code>shadow</code>.
 * </p>
 * <p>
 *     <b>Manual Boolean Values:</b><br>
 *     You can manually set a format to true or false (e.g., <code>&lt;bold=false&gt;</code>).
 *     This is required if you are using a default text format that is already styled.
 *     For example, if the text is italic by default, <code>&lt;/italic&gt;</code> will only reset it to the default (true),
 *     so you must use <code>&lt;italic=false&gt;</code> to render non-italic text.
 * </p>
 *
 * <h3>Color Options</h3>
 * <p>
 *     Use <code>color</code> to set the text color and <code>outline_color</code> to set the outline color.
 * </p>
 *
 * <h4>Supported Color Formats</h4>
 * <ul>
 *     <li>
 *         <b>Names:</b>
 *         <br><code>black</code>, <code>white</code>, <code>red</code>, <code>green</code>, <code>blue</code>,
 *         <code>yellow</code>, <code>cyan</code>, <code>magenta</code>, <code>orange</code>, <code>pink</code>,
 *         <code>gray</code>, <code>light_gray</code>, <code>dark_gray</code>.
 *         <br>Example: <code>&lt;color=red&gt;</code>
 *     </li>
 *     <li>
 *         <b>Hexadecimal:</b>
 *         <br><code>#RRGGBB</code> or <code>#AARRGGBB</code>
 *         <br>Example: <code>&lt;color=#FF0000&gt;</code>
 *     </li>
 *     <li>
 *         <b>Integer (0-255):</b>
 *         <br><code>rgb(r,g,b)</code> or <code>rgba(r,g,b,a)</code>
 *         <br><b>Note:</b> There must be <b>no spaces</b> between parameters.
 *         <br>Example: <code>&lt;color=rgb(100,200,50)&gt;</code>
 *     </li>
 *     <li>
 *         <b>Float (0.0-1.0):</b>
 *         <br><code>rgbf(r,g,b)</code>, <code>rgbaf(r,g,b,a)</code>, or <code>argbf(a,r,g,b)</code>
 *         <br><b>Note:</b> There must be <b>no spaces</b> between parameters.
 *         <br>Example: <code>&lt;color=rgbf(1.0,0.5,0)&gt;</code>
 *     </li>
 * </ul>
 *
 * <h3>Escaping</h3>
 * <p>
 *     To include a literal angle bracket in the text, use a backslash to escape it.
 *     <br>Example: <code>\&lt;This is not a tag\&gt;</code>
 * </p>
 *
 * <h3>Advanced Usage</h3>
 * <p>
 *     Multiple key-value pairs can be combined in a single tag by separating them with spaces.
 *     <br>Example: <code>&lt;color=red bold italic=true&gt;</code>
 * </p>
 */
@UtilityClass
public class TextParser {

    private static final List<ColorFormat> COLOR_FORMATS = List.of(
            new HexColorFormat(3, value -> Color.fromRGB(value[0], value[1], value[2])),
            new HexColorFormat(4, value -> Color.fromRGBA(value[1], value[2], value[3], value[0])),
            new IntColorFormat("rgb", 3, value -> Color.fromRGB(value[0], value[1], value[2])),
            new IntColorFormat("rgba", 4, value -> Color.fromRGBA(value[0], value[1], value[2], value[3])),
            new IntColorFormat("argb", 4, value -> Color.fromRGBA(value[1], value[2], value[3], value[0])),
            new FloatColorFormat("rgbf", 3, Color::fromRGBF),
            new FloatColorFormat("rgbaf", 4, Color::fromRGBAF),
            new FloatColorFormat("argbf", 4, value -> Color.fromRGBAF(value[1], value[2], value[3], value[0])),
            new StaticColorFormat("black", Color.BLACK),
            new StaticColorFormat("light_gray", Color.LIGHT_GRAY),
            new StaticColorFormat("gray", Color.GRAY),
            new StaticColorFormat("dark_gray", Color.DARK_GRAY),
            new StaticColorFormat("white", Color.WHITE),
            new StaticColorFormat("red", Color.RED),
            new StaticColorFormat("green", Color.GREEN),
            new StaticColorFormat("blue", Color.BLUE),
            new StaticColorFormat("orange", Color.ORANGE),
            new StaticColorFormat("yellow", Color.YELLOW),
            new StaticColorFormat("cyan", Color.CYAN),
            new StaticColorFormat("pink", Color.PINK),
            new StaticColorFormat("magenta", Color.MAGENTA)
    );

    public static TextLine parse(final String text) {
        return parse(text, TextFormat.DEFAULT);
    }

    public static TextLine parse(final String text, final TextFormat defaultFormat) {
        FormatParser parser = new FormatParser(text);
        List<TextSection> sections = new ArrayList<>();
        TextFormat[] currentFormat = {defaultFormat};
        parser.parse((currentText, newOptions) -> {
            if (!currentText.isEmpty()) {
                sections.add(new TextSection(currentText, currentFormat[0]));
            }
            for (FormatParser.Option option : newOptions) {
                currentFormat[0] = applyOption(defaultFormat, currentFormat[0], option);
            }
        });
        return new TextLine(sections);
    }

    private static TextFormat applyOption(final TextFormat defaultFormat, final TextFormat currentFormat, final FormatParser.Option option) {
        return switch (option.name().toLowerCase(Locale.ROOT)) {
            case "color" -> handleColorOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::color, TextFormat::withColor);
            case "outline_color" -> handleColorOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::outlineColor, TextFormat::withOutlineColor);
            case "bold" -> handleBooleanOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::bold, TextFormat::withBold);
            case "italic" -> handleBooleanOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::italic, TextFormat::withItalic);
            case "underlined" -> handleBooleanOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::underlined, TextFormat::withUnderlined);
            case "strikethrough" -> handleBooleanOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::strikethrough, TextFormat::withStrikethrough);
            case "shadow" -> handleBooleanOption(defaultFormat, currentFormat, option.close(), option.value(), TextFormat::shadow, TextFormat::withShadow);
            default -> throw new HandlerException("Unknown option name: " + option.name());
        };
    }

    private static TextFormat handleColorOption(final TextFormat defaultFormat, final TextFormat currentFormat, final boolean close, @Nullable String value, final Function<TextFormat, Color> getter, final BiFunction<TextFormat, Color, TextFormat> setter) {
        if (close) {
            return setter.apply(currentFormat, getter.apply(defaultFormat));
        } else {
            if (value == null) throw new HandlerException("Color option requires a value");
            value = value.toLowerCase(Locale.ROOT);
            Color color = null;
            try {
                for (ColorFormat colorFormat : COLOR_FORMATS) {
                    if (colorFormat.canParse(value)) {
                        color = colorFormat.parse(value);
                        break;
                    }
                }
            } catch (Throwable t) {
                throw new HandlerException("Exception during color parsing: " + value, t);
            }
            if (color == null) {
                throw new HandlerException("Unknown color format: " + value);
            }
            return setter.apply(currentFormat, color);
        }
    }

    private static TextFormat handleBooleanOption(final TextFormat defaultFormat, final TextFormat currentFormat, final boolean close, @Nullable final String value, final Function<TextFormat, Boolean> getter, final BiFunction<TextFormat, Boolean, TextFormat> setter) {
        if (close) {
            return setter.apply(currentFormat, getter.apply(defaultFormat));
        } else if (value == null || value.equalsIgnoreCase("true")) {
            return setter.apply(currentFormat, true);
        } else {
            return setter.apply(currentFormat, false);
        }
    }


    private interface ColorFormat {
        boolean canParse(final String s);

        Color parse(final String s);
    }

    @RequiredArgsConstructor
    private static class HexColorFormat implements ColorFormat {
        private final int channels;
        private final Function<int[], Color> parser;

        @Override
        public boolean canParse(final String s) {
            return s.startsWith("#") && s.length() == (1 + this.channels * 2);
        }

        @Override
        public Color parse(final String s) {
            int[] values = new int[this.channels];
            String hex = s.substring(1);
            for (int i = 0; i < this.channels; i++) {
                values[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            }
            return this.parser.apply(values);
        }
    }

    @RequiredArgsConstructor
    private static class IntColorFormat implements ColorFormat {
        private final String name;
        private final int channels;
        private final Function<int[], Color> parser;

        public boolean canParse(final String s) {
            return s.startsWith(this.name + "(") && s.endsWith(")");
        }

        public Color parse(final String s) {
            int[] values = new int[this.channels];
            String inner = s.substring(this.name.length() + 1, s.length() - 1);
            if (inner.startsWith("#")) {
                String hex = inner.substring(1);
                if (hex.length() != this.channels * 2) {
                    throw new HandlerException("Invalid " + this.name + " hex color length: " + s);
                }
                for (int i = 0; i < this.channels; i++) {
                    values[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
                }
            } else {
                String[] parts = inner.split(",");
                if (parts.length != this.channels) {
                    throw new HandlerException("Invalid " + this.name + " color format: " + s);
                }
                for (int i = 0; i < this.channels; i++) {
                    values[i] = Integer.parseInt(parts[i].trim());
                }
            }
            return this.parser.apply(values);
        }
    }

    @RequiredArgsConstructor
    private static class FloatColorFormat implements ColorFormat {
        private final String name;
        private final int channels;
        private final Function<float[], Color> parser;

        @Override
        public boolean canParse(final String s) {
            return s.startsWith(this.name + "(") && s.endsWith(")");
        }

        @Override
        public Color parse(final String s) {
            float[] values = new float[this.channels];
            String inner = s.substring(this.name.length() + 1, s.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length != this.channels) {
                throw new HandlerException("Invalid " + this.name + " color format: " + s);
            }
            for (int i = 0; i < this.channels; i++) {
                values[i] = Float.parseFloat(parts[i].trim());
            }
            return this.parser.apply(values);
        }
    }

    @RequiredArgsConstructor
    private static class StaticColorFormat implements ColorFormat {
        private final String name;
        private final Color color;

        @Override
        public boolean canParse(final String s) {
            return s.equalsIgnoreCase(this.name);
        }

        @Override
        public Color parse(final String s) {
            return this.color;
        }
    }

}
