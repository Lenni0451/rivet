package net.lenni0451.rivet.math;

import lombok.With;

@With
public record Padding(float left, float top, float right, float bottom) {

    public static final Padding EMPTY = new Padding(0, 0, 0, 0);

}
