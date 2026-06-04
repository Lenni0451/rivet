package net.lenni0451.rivet.utils;

public class MathUtils {

    public static float roundMin(final float value, final float min) {
        return Math.max(Math.round(value), min);
    }

}
