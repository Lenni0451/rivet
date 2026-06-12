package net.lenni0451.rivet.animation;

import net.lenni0451.commons.color.Color;

@FunctionalInterface
public interface Interpolator<T> {

    Interpolator<Float> FLOAT = (progress, start, end) -> start + (end - start) * progress;
    Interpolator<Color> COLOR = Color::interpolate;

    T interpolate(final float progress, final T start, final T end);

}
