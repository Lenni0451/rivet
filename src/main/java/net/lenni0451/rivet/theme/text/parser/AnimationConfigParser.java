package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.commons.animation.AnimationMode;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.AnimationFrameConfig;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class AnimationConfigParser implements Parser<AnimationConfig> {

    private final EasingFunctionParser easingFunctionParser = new EasingFunctionParser();
    private final EnumParser<EasingMode> easingModeParser = new EnumParser<>(EasingMode.values());
    private final EnumParser<AnimationMode> modeParser = new EnumParser<>(AnimationMode.values());
    private final EnumParser<EasingBehavior> behaviorParser = new EnumParser<>(EasingBehavior.values());

    @Override
    public AnimationConfig parse(final String s) {
        List<String> lines = new ArrayList<>();
        for (String line : s.split(";")) {
            line = line.trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }
        if (lines.size() < 2) {
            throw new IllegalArgumentException("At least animation mode and one frame is required");
        }
        AnimationMode mode = this.modeParser.parse(lines.get(0));
        List<AnimationFrameConfig> frames = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            frames.add(this.parseFrame(lines.get(i)));
        }
        return new AnimationConfig(mode, frames);
    }

    private AnimationFrameConfig parseFrame(final String s) {
        EasingFunction easingFunction = null;
        EasingMode easingMode = null;
        Float startValue = null;
        float endValue = 0;
        Integer duration = null;
        EasingBehavior easingBehavior = null;

        String[] options = s.split(" ");
        if (options.length == 6 && !s.contains("=")) {
            easingFunction = this.easingFunctionParser.parse(options[0]);
            easingMode = this.easingModeParser.parse(options[1]);
            startValue = Float.parseFloat(options[2]);
            endValue = Float.parseFloat(options[3]);
            duration = Integer.parseInt(options[4]);
            easingBehavior = this.behaviorParser.parse(options[5]);
        } else {
            for (String option : options) {
                String[] keyValue = option.split("=", 2);
                if (keyValue.length != 2) {
                    throw new IllegalArgumentException("Invalid option: " + option);
                }
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                switch (key) {
                    case "easingFunction" -> easingFunction = this.easingFunctionParser.parse(value);
                    case "easingMode" -> easingMode = this.easingModeParser.parse(value);
                    case "startValue" -> startValue = Float.parseFloat(value);
                    case "endValue" -> endValue = Float.parseFloat(value);
                    case "duration" -> duration = Integer.parseInt(value);
                    case "easingBehavior" -> easingBehavior = this.behaviorParser.parse(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + key);
                }
            }
        }
        return new AnimationFrameConfig(easingFunction, easingMode, startValue, endValue, duration, easingBehavior);
    }

}
