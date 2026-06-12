package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.rivet.animation.DynamicAnimationConfig;

public class DynamicAnimationConfigParser implements Parser<DynamicAnimationConfig> {

    private final EasingFunctionParser easingFunctionParser = new EasingFunctionParser();
    private final EnumParser<EasingMode> easingModeParser = new EnumParser<>(EasingMode.values());

    @Override
    public DynamicAnimationConfig parse(final String s) {
        String[] parts = s.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Expected format: <easing function> <easing mode> <duration>");
        }
        return new DynamicAnimationConfig(
                this.easingFunctionParser.parse(parts[0]),
                this.easingModeParser.parse(parts[1]),
                Long.parseLong(parts[2])
        );
    }

}
