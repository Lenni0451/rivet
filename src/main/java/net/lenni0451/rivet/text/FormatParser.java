package net.lenni0451.rivet.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class FormatParser {

    private static final char ESCAPE_CHAR = '\\';
    private static final char TAG_START = '<';
    private static final char KEY_VALUE_SEPARATOR = '=';
    private static final char OPTION_SEPARATOR = ' ';
    private static final char TAG_END = '>';
    private static final char CLOSE_OPTION_PREFIX = '/';

    private final char[] chars;
    private int index = 0;

    FormatParser(final String text) {
        this.chars = text.toCharArray();
    }

    public void parse(final Handler handler) {
        StringBuilder currentSection = new StringBuilder();
        while (this.index < this.chars.length) {
            char c = this.chars[this.index++];
            if (c == TAG_START) {
                List<Option> options = this.readTag();
                if (!options.isEmpty()) {
                    handler.handle(currentSection.toString(), options);
                    currentSection.setLength(0);
                }
            } else {
                currentSection.append(c);
            }
        }
        if (!currentSection.isEmpty()) {
            handler.handle(currentSection.toString(), Collections.emptyList());
        }
    }

    private List<Option> readTag() {
        List<Option> options = new ArrayList<>();
        while (this.index < this.chars.length) {
            String name = this.readOptionName();
            boolean close = false;
            if (!name.isEmpty() && name.charAt(0) == CLOSE_OPTION_PREFIX) {
                name = name.substring(1);
                close = true;
            }
            if (this.index >= this.chars.length) break;
            char c = this.chars[this.index++];
            if (c == KEY_VALUE_SEPARATOR) {
                if (close) {
                    throw new IllegalArgumentException("Closing option cannot have a value: " + name);
                }
                String value = this.readOptionValue();
                options.add(new Option(name, value, close));
            } else if (c == OPTION_SEPARATOR) {
                if (!name.isEmpty()) {
                    options.add(new Option(name, null, close));
                }
            } else if (c == TAG_END) {
                if (!name.isEmpty()) {
                    options.add(new Option(name, null, close));
                }
                return options;
            } else {
                throw new IllegalStateException("Invalid character '" + c + "' in tag");
            }
        }
        throw new IllegalStateException("Unclosed tag");
    }

    private String readOptionName() {
        StringBuilder name = new StringBuilder();
        boolean escaped = false;
        while (this.index < this.chars.length) {
            char c = this.chars[this.index++];
            if (!escaped) {
                if (c == ESCAPE_CHAR) {
                    escaped = true;
                    continue;
                } else if (c == KEY_VALUE_SEPARATOR || c == OPTION_SEPARATOR || c == TAG_END) {
                    this.index--; // Step back to re-read this character
                    return name.toString();
                }
            } else {
                escaped = false;
            }
            name.append(c);
        }
        return name.toString();
    }

    private String readOptionValue() {
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        while (this.index < this.chars.length) {
            char c = this.chars[this.index++];
            if (!escaped) {
                if (c == ESCAPE_CHAR) {
                    escaped = true;
                    continue;
                } else if (c == OPTION_SEPARATOR || c == TAG_END) {
                    this.index--; // Step back to re-read this character
                    return value.toString();
                }
            } else {
                escaped = false;
            }
            value.append(c);
        }
        return value.toString();
    }


    public interface Handler {
        void handle(String currentText, List<Option> newOptions);
    }

    public record Option(String name, String value, boolean close) {
    }

}
