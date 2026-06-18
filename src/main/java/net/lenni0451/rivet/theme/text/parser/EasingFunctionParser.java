package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.commons.animation.easing.EasingFunction;

import javax.annotation.Nullable;
import java.util.Locale;

public final class EasingFunctionParser implements Parser<EasingFunction> {

    @Nullable
    @Override
    public EasingFunction parse(final String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "linear" -> EasingFunction.LINEAR;
            case "sine" -> EasingFunction.SINE;
            case "quad" -> EasingFunction.QUAD;
            case "cubic" -> EasingFunction.CUBIC;
            case "quart" -> EasingFunction.QUART;
            case "quint" -> EasingFunction.QUINT;
            case "expo" -> EasingFunction.EXPO;
            case "circ" -> EasingFunction.CIRC;
            case "back" -> EasingFunction.BACK;
            case "elastic" -> EasingFunction.ELASTIC;
            case "bounce" -> EasingFunction.BOUNCE;
            default -> null;
        };
    }

    @Nullable
    @Override
    public String toString(final EasingFunction value) {
        if (value == EasingFunction.LINEAR) return "linear";
        if (value == EasingFunction.SINE) return "sine";
        if (value == EasingFunction.QUAD) return "quad";
        if (value == EasingFunction.CUBIC) return "cubic";
        if (value == EasingFunction.QUART) return "quart";
        if (value == EasingFunction.QUINT) return "quint";
        if (value == EasingFunction.EXPO) return "expo";
        if (value == EasingFunction.CIRC) return "circ";
        if (value == EasingFunction.BACK) return "back";
        if (value == EasingFunction.ELASTIC) return "elastic";
        if (value == EasingFunction.BOUNCE) return "bounce";
        return null;
    }

}
