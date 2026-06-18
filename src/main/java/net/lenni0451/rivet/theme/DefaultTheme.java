package net.lenni0451.rivet.theme;

import lombok.experimental.UtilityClass;
import net.lenni0451.commons.animation.AnimationMode;
import net.lenni0451.commons.animation.EasingBehavior;
import net.lenni0451.commons.animation.easing.EasingFunction;
import net.lenni0451.commons.animation.easing.EasingMode;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.animation.AnimationFrameConfig;

@UtilityClass
public class DefaultTheme {

    public static final AnimationConfig HOVER_ANIMATION = new AnimationConfig(
            AnimationMode.DEFAULT,
            new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_IN_OUT, 0F, 1F, 150, EasingBehavior.KEEP)
    );
    public static final AnimationConfig CLICK_ANIMATION = new AnimationConfig(
            AnimationMode.DEFAULT,
            new AnimationFrameConfig(EasingFunction.SINE, EasingMode.EASE_IN_OUT, 0F, 1F, 100, EasingBehavior.KEEP)
    );

}
