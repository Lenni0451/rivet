package net.lenni0451.rivet.animation;

import lombok.Builder;
import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;

import javax.annotation.Nullable;

@With
@WithBy
@Builder
public record AnimationFrameConfig(
        @Nullable EasingFunction easingFunction,
        @Nullable EasingMode easingMode,
        @Nullable Float startValue,
        float endValue,
        @Nullable Integer duration,
        @Nullable EasingBehavior easingBehavior
) {

    public boolean fullFrame() {
        return this.easingFunction != null && this.easingMode != null && this.startValue != null && this.duration != null && this.easingBehavior != null;
    }

}
