package net.lenni0451.rivet.parser.impl;

import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.parser.Parser;

import java.util.Locale;

public final class RectangleParser implements Parser<Rectangle> {

    @Override
    public Rectangle parse(final String s) {
        String[] parts = s.split(" ");
        Rectangle rectangle = this.parseFormat1(parts);
        if (rectangle == null) rectangle = this.parseFormat2(parts);
        return rectangle;
    }

    private Rectangle parseFormat1(final String[] parts) {
        if (parts.length != 4) return null;

        float x;
        try {
            x = Float.parseFloat(parts[0]);
        } catch (Throwable t) {
            return null;
        }
        float y;
        try {
            y = Float.parseFloat(parts[1]);
        } catch (Throwable t) {
            return null;
        }
        float width;
        try {
            width = Float.parseFloat(parts[2]);
        } catch (Throwable t) {
            return null;
        }
        float height;
        try {
            height = Float.parseFloat(parts[3]);
        } catch (Throwable t) {
            return null;
        }
        return new Rectangle(x, y, width, height);
    }

    private Rectangle parseFormat2(final String[] parts) {
        if (parts.length < 1) return null;

        Float x = null;
        Float y = null;
        Float width = null;
        Float height = null;
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) throw new IllegalArgumentException("Invalid option: " + part);

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            switch (key.toLowerCase(Locale.ROOT)) {
                case "x" -> x = Float.parseFloat(value);
                case "y" -> y = Float.parseFloat(value);
                case "width", "w" -> width = Float.parseFloat(value);
                case "height", "h" -> height = Float.parseFloat(value);
                default -> throw new IllegalArgumentException("Unknown option: " + part);
            }
        }
        if (x == null) throw new IllegalArgumentException("X is required");
        if (y == null) throw new IllegalArgumentException("Y is required");
        if (width == null) throw new IllegalArgumentException("Width is required");
        if (height == null) throw new IllegalArgumentException("Height is required");
        return new Rectangle(x, y, width, height);
    }

    @Override
    public String toString(final Rectangle value) {
        return value.x() + " " + value.y() + " " + value.width() + " " + value.height();
    }

}
