package net.lenni0451.rivet.layout.grid;

public enum GridAnchor {

    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    LEFT,
    CENTER,
    RIGHT,
    BOTTOM_LEFT,
    BOTTOM,
    BOTTOM_RIGHT;

    public float offsetX(final float availableWidth, final float width) {
        return switch (this) {
            case TOP_LEFT, LEFT, BOTTOM_LEFT -> 0;
            case TOP, CENTER, BOTTOM -> (availableWidth - width) / 2F;
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> availableWidth - width;
        };
    }

    public float offsetY(final float availableHeight, final float height) {
        return switch (this) {
            case TOP_LEFT, TOP, TOP_RIGHT -> 0;
            case LEFT, CENTER, RIGHT -> (availableHeight - height) / 2F;
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> availableHeight - height;
        };
    }

}
