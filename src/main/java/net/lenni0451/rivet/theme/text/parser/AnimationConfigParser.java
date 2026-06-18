package net.lenni0451.rivet.theme.text.parser;

import net.lenni0451.commons.animation.AnimationMode;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.AnimationFrameConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                if (keyValue.length != 2) throw new IllegalArgumentException("Invalid option: " + option);

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                switch (key.toLowerCase(Locale.ROOT)) {
                    case "easingfunction", "easing_function", "function", "ef" -> easingFunction = this.easingFunctionParser.parse(value);
                    case "easingmode", "easing_mode", "mode", "em" -> easingMode = this.easingModeParser.parse(value);
                    case "startvalue", "start_value", "start", "s" -> startValue = Float.parseFloat(value);
                    case "endvalue", "end_value", "end", "e" -> endValue = Float.parseFloat(value);
                    case "duration", "d" -> duration = Integer.parseInt(value);
                    case "easingbehavior", "easing_behavior", "behavior", "eb" -> easingBehavior = this.behaviorParser.parse(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + key);
                }
            }
        }
        return new AnimationFrameConfig(easingFunction, easingMode, startValue, endValue, duration, easingBehavior);
    }

    @Override
    public String toString(final AnimationConfig value) {
        List<String> lines = new ArrayList<>();
        lines.add(this.modeParser.toString(value.mode()));
        for (AnimationFrameConfig frame : value.frames()) {
            List<String> parts = new ArrayList<>(6);
            if (frame.easingFunction() != null) parts.add("easing_function=" + this.easingFunctionParser.toString(frame.easingFunction()));
            if (frame.easingMode() != null) parts.add("easing_mode=" + this.easingModeParser.toString(frame.easingMode()));
            if (frame.startValue() != null) parts.add("start=" + frame.startValue());
            parts.add("end=" + frame.endValue());
            if (frame.duration() != null) parts.add("duration=" + frame.duration());
            if (frame.easingBehavior() != null) parts.add("easing_behavior=" + this.behaviorParser.toString(frame.easingBehavior()));
            lines.add(String.join(" ", parts));
        }
        return String.join("; ", lines);
    }

}
