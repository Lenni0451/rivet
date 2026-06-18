package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.rivet.math.Padding;

import javax.annotation.Nullable;
import java.util.Locale;

public final class PaddingParser implements Parser<Padding> {

    @Nullable
    @Override
    public Padding parse(final String s) {
        String[] parts = s.split(" ");
        Padding padding = this.parseFormat1(s);
        if (padding == null) padding = this.parseFormat2(parts);
        if (padding == null) padding = this.parseFormat3(parts);
        return padding;
    }

    @Nullable
    private Padding parseFormat1(final String s) {
        if (!s.contains(" ") && !s.contains("=")) {
            try {
                return new Padding(Float.parseFloat(s));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Nullable
    private Padding parseFormat2(final String[] parts) {
        if (parts.length != 4) return null;
        float left;
        try {
            left = Float.parseFloat(parts[0]);
        } catch (Throwable t) {
            return null;
        }
        float top;
        try {
            top = Float.parseFloat(parts[1]);
        } catch (Throwable t) {
            return null;
        }
        float right;
        try {
            right = Float.parseFloat(parts[2]);
        } catch (Throwable t) {
            return null;
        }
        float bottom;
        try {
            bottom = Float.parseFloat(parts[3]);
        } catch (Throwable t) {
            return null;
        }
        return new Padding(left, top, right, bottom);
    }

    private Padding parseFormat3(final String[] parts) {
        if (parts.length > 0) {
            float left = 0;
            float top = 0;
            float right = 0;
            float bottom = 0;
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length != 2) throw new IllegalArgumentException("Invalid option: " + part);

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                switch (key.toLowerCase(Locale.ROOT)) {
                    case "left", "l" -> left = Float.parseFloat(value);
                    case "top", "t" -> top = Float.parseFloat(value);
                    case "right", "r" -> right = Float.parseFloat(value);
                    case "bottom", "b" -> bottom = Float.parseFloat(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + key);
                }
            }
            return new Padding(left, top, right, bottom);
        }
        return null;
    }

    @Override
    public String toString(final Padding value) {
        return "left=" + value.left() + " top=" + value.top() + " right=" + value.right() + " bottom=" + value.bottom();
    }

}
