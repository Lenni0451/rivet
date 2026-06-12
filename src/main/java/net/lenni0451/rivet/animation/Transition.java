package net.lenni0451.rivet.animation;

import net.lenni0451.commons.animation.Animation;
import net.lenni0451.rivet.component.Component;

import java.util.function.Supplier;

public class Transition<T> {

    private final Component component;
    private final Supplier<T> targetSupplier;
    private final Supplier<AnimationConfig> configSupplier;
    private final Interpolator<T> interpolator;

    private T currentValue;
    private T startValue;
    private T targetValue;
    private Animation animation;

    public Transition(final Component component, final Supplier<T> targetSupplier, final Supplier<AnimationConfig> configSupplier, final Interpolator<T> interpolator) {
        this.component = component;
        this.targetSupplier = targetSupplier;
        this.configSupplier = configSupplier;
        this.interpolator = interpolator;

        this.targetValue = targetSupplier.get();
        this.currentValue = this.targetValue;
    }

    public T getValue() {
        if (this.component.rivet() == null) return this.currentValue;

        T newTarget = this.targetSupplier.get();
        if (!newTarget.equals(this.targetValue)) {
            this.startValue = this.currentValue;
            this.targetValue = newTarget;
            this.animation = this.configSupplier.get().create().start();
        }

        if (this.animation != null && this.animation.isRunning()) {
            this.currentValue = this.interpolator.interpolate(this.animation.getValue(), this.startValue, this.targetValue);
        } else {
            this.currentValue = this.targetValue;
            this.animation = null;
        }

        return this.currentValue;
    }

}
