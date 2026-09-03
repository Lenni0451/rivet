package net.lenni0451.rivet.parser.impl;

import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.parser.Parser;

import java.util.Locale;

public final class PointParser implements Parser<Point> {

    @Override
    public Point parse(final String s) {
        String[] parts = s.split(" ");
        Point point = this.parseFormat1(parts);
        if (point == null) point = this.parseFormat2(parts);
        return point;
    }

    private Point parseFormat1(final String[] parts) {
        if (parts.length != 2) return null;

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
        return new Point(x, y);
    }

    private Point parseFormat2(final String[] parts) {
        if (parts.length < 1) return null;

        Float x = null;
        Float y = null;
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) throw new IllegalArgumentException("Invalid option: " + part);

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            switch (key.toLowerCase(Locale.ROOT)) {
                case "x" -> x = Float.parseFloat(value);
                case "y" -> y = Float.parseFloat(value);
                default -> throw new IllegalArgumentException("Unknown option: " + part);
            }
        }
        if (x == null) throw new IllegalArgumentException("X is required");
        if (y == null) throw new IllegalArgumentException("Y is required");
        return new Point(x, y);
    }

    @Override
    public String toString(final Point value) {
        return value.x() + " " + value.y();
    }

}
