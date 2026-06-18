package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.rivet.math.Corners;

import javax.annotation.Nullable;
import java.util.Locale;

public final class CornersParser implements Parser<Corners> {

    @Nullable
    @Override
    public Corners parse(final String s) {
        String[] parts = s.split(" ");
        Corners corners = this.parseFormat1(s);
        if (corners == null) corners = this.parseFormat2(parts);
        if (corners == null) corners = this.parseFormat3(parts);
        return corners;
    }

    private Corners parseFormat1(final String s) {
        if (!s.contains(" ") && !s.contains("=")) {
            try {
                return new Corners(Float.parseFloat(s));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Nullable
    private Corners parseFormat2(final String[] parts) {
        if (parts.length != 4) return null;
        float topLeft;
        try {
            topLeft = Float.parseFloat(parts[0]);
        } catch (Throwable t) {
            return null;
        }
        float bottomLeft;
        try {
            bottomLeft = Float.parseFloat(parts[1]);
        } catch (Throwable t) {
            return null;
        }
        float bottomRight;
        try {
            bottomRight = Float.parseFloat(parts[2]);
        } catch (Throwable t) {
            return null;
        }
        float topRight;
        try {
            topRight = Float.parseFloat(parts[3]);
        } catch (Throwable t) {
            return null;
        }
        return new Corners(topLeft, bottomLeft, bottomRight, topRight);
    }

    private Corners parseFormat3(final String[] parts) {
        if (parts.length > 0) {
            float topLeft = 0;
            float bottomLeft = 0;
            float bottomRight = 0;
            float topRight = 0;
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length != 2) throw new IllegalArgumentException("Invalid option: " + part);

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                switch (key.toLowerCase(Locale.ROOT)) {
                    case "topleft", "top_left", "tl" -> topLeft = Float.parseFloat(value);
                    case "bottomleft", "bottom_left", "bl" -> bottomLeft = Float.parseFloat(value);
                    case "bottomright", "bottom_right", "br" -> bottomRight = Float.parseFloat(value);
                    case "topright", "top_right", "tr" -> topRight = Float.parseFloat(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + part);
                }
            }
            return new Corners(topLeft, bottomLeft, bottomRight, topRight);
        }
        return null;
    }

    @Override
    public String toString(final Corners value) {
        return "top_left=" + value.topLeft()
                + " bottom_left=" + value.bottomLeft()
                + " bottom_right=" + value.bottomRight()
                + " top_right=" + value.topRight();
    }

}
