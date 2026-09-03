package net.lenni0451.rivet.parser.impl;

import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.parser.Parser;

import java.util.Locale;

public final class SizeParser implements Parser<Size> {

    @Override
    public Size parse(final String s) {
        String[] parts = s.split(" ");
        Size size = this.parseFormat1(parts);
        if (size == null) size = this.parseFormat2(parts);
        return size;
    }

    private Size parseFormat1(final String[] parts) {
        if (parts.length != 2) return null;

        float width;
        try {
            width = Float.parseFloat(parts[0]);
        } catch (Throwable t) {
            return null;
        }
        float height;
        try {
            height = Float.parseFloat(parts[1]);
        } catch (Throwable t) {
            return null;
        }
        return new Size(width, height);
    }

    private Size parseFormat2(final String[] parts) {
        if (parts.length < 1) return null;

        Float width = null;
        Float height = null;
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) throw new IllegalArgumentException("Invalid option: " + part);

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            switch (key.toLowerCase(Locale.ROOT)) {
                case "width", "w" -> width = Float.parseFloat(value);
                case "height", "h" -> height = Float.parseFloat(value);
                default -> throw new IllegalArgumentException("Unknown option: " + part);
            }
        }
        if (width == null) throw new IllegalArgumentException("Width is required");
        if (height == null) throw new IllegalArgumentException("Height is required");
        return new Size(width, height);
    }

    @Override
    public String toString(final Size value) {
        return value.width() + " " + value.height();
    }

}
