package net.lenni0451.rivet.animation;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.commons.animation.Animation;
import net.lenni0451.commons.animation.AnimationMode;

import java.util.List;

@With
@WithBy
public record AnimationConfig(AnimationMode mode, List<AnimationFrameConfig> frames) {

    public AnimationConfig(final AnimationMode mode, final AnimationFrameConfig... frames) {
        this(mode, List.of(frames));
    }

    public AnimationConfig {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("At least one frame is required");
        }
        if (!frames.get(0).fullFrame()) {
            throw new IllegalArgumentException("The first frame of an animation must be a full frame");
        }
        frames = List.copyOf(frames);
    }

    public Animation create() {
        Animation animation = new Animation(this.mode);
        for (AnimationFrameConfig frame : this.frames) {
            animation.frame(f -> {
                if (frame.easingFunction() != null) f.easingFunction(frame.easingFunction());
                if (frame.easingMode() != null) f.easingMode(frame.easingMode());
                if (frame.startValue() != null) f.start(frame.startValue());
                f.end(frame.endValue());
                if (frame.duration() != null) f.duration(frame.duration());
                if (frame.easingBehavior() != null) f.easingBehavior(frame.easingBehavior());
            });
        }
        return animation;
    }

}
