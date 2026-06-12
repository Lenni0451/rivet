package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.commons.animation.easing.EasingFunction;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Locale;

@ApiStatus.Internal
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

}
