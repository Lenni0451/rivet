package net.lenni0451.rivet.animation;

import lombok.With;
import lombok.experimental.WithBy;
import net.lenni0451.commons.animation.DynamicAnimation;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;

@With
@WithBy
public record DynamicAnimationConfig(EasingFunction easingFunction, EasingMode easingMode, long duration) {

    public DynamicAnimation create(final float target) {
        return new DynamicAnimation(this.easingFunction, this.easingMode, this.duration, target);
    }

}
