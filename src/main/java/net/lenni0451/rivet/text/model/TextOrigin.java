package net.lenni0451.rivet.text.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextOrigin {

    @RequiredArgsConstructor
    public enum Horizontal {
        LOGICAL_LEFT(width -> 0),
        VISUAL_LEFT(width -> 0),
        VISUAL_CENTER(width -> width / 2F),
        VISUAL_RIGHT(width -> width);

        private final Mapper mapper;

        public float position(final float width) {
            return this.mapper.map(width);
        }
    }

    @RequiredArgsConstructor
    public enum Vertical {
        BASELINE(height -> height / 2F),
        LOGICAL_TOP(height -> 0),
        LOGICAL_CENTER(height -> height / 2F),
        LOGICAL_BOTTOM(height -> height),
        VISUAL_TOP(height -> 0),
        VISUAL_CENTER(height -> height / 2F),
        VISUAL_BOTTOM(height -> height);

        private final Mapper mapper;

        public float position(final float height) {
            return this.mapper.map(height);
        }
    }

    @FunctionalInterface
    private interface Mapper {
        float map(final float f);
    }

}
