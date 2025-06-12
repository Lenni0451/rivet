package net.lenni0451.rivet.text;

import java.util.Set;

// https://github.com/RaphiMC/ThinGL/blob/fd9e6bd95a53b66f23842fac23205187399d22cf/src/main/java/net/raphimc/thingl/text/font/GlyphPredicate.java
@FunctionalInterface
public interface GlyphPredicate {

    static GlyphPredicate all() {
        return codePoint -> true;
    }

    static GlyphPredicate range(final int min, final int max) {
        return codePoint -> codePoint >= min && codePoint <= max;
    }

    static GlyphPredicate any(final Set<Integer> codePoints) {
        return codePoints::contains;
    }

    static GlyphPredicate any(final int... codePoints) {
        return codePoint -> {
            for (int cp : codePoints) {
                if (cp == codePoint) return true;
            }
            return false;
        };
    }


    boolean test(final int codePoint);

    default GlyphPredicate and(final GlyphPredicate other) {
        return codePoint -> this.test(codePoint) && other.test(codePoint);
    }

    default GlyphPredicate or(final GlyphPredicate other) {
        return codePoint -> this.test(codePoint) || other.test(codePoint);
    }

    default GlyphPredicate negate() {
        return codePoint -> !this.test(codePoint);
    }

    default GlyphPredicate exclude(final GlyphPredicate other) {
        return codePoint -> this.test(codePoint) && !other.test(codePoint);
    }

}
