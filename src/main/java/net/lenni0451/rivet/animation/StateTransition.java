package net.lenni0451.rivet.animation;

import net.lenni0451.commons.animation.Animation;
import net.lenni0451.rivet.component.Component;

import java.util.function.Supplier;

public class StateTransition<T, S> {

    private final Component component;
    private final Supplier<T> targetSupplier;
    private final Interpolator<T> interpolator;
    private final Supplier<S> stateSupplier;
    private final StateConfigProvider<S> stateConfigProvider;

    private T currentValue;
    private T startValue;
    private T targetValue;
    private S currentState;
    private Animation animation;

    public StateTransition(final Component component, final Supplier<T> targetSupplier, final Interpolator<T> interpolator, final Supplier<S> stateSupplier, final StateConfigProvider<S> stateConfigProvider) {
        this.component = component;
        this.targetSupplier = targetSupplier;
        this.interpolator = interpolator;
        this.stateSupplier = stateSupplier;
        this.stateConfigProvider = stateConfigProvider;

        this.targetValue = targetSupplier.get();
        this.currentValue = this.targetValue;
        this.currentState = stateSupplier.get();
    }

    public T value() {
        if (this.component.rivet() == null) return this.currentValue;

        T newTarget = this.targetSupplier.get();
        S newState = this.stateSupplier.get();
        if (!newState.equals(this.currentState)) {
            this.startValue = this.currentValue;
            this.targetValue = newTarget;
            this.animation = this.stateConfigProvider.get(this.currentState, newState).create().start();
            this.currentState = newState;
        } else if (!newTarget.equals(this.targetValue)) {
            this.startValue = this.currentValue;
            this.targetValue = newTarget;
            this.animation = this.stateConfigProvider.get(this.currentState).create().start();
        }

        if (this.animation != null && this.animation.isRunning()) {
            this.currentValue = this.interpolator.interpolate(this.animation.getValue(), this.startValue, this.targetValue);
        } else {
            this.currentValue = this.targetValue;
            this.animation = null;
        }

        return this.currentValue;
    }


    @FunctionalInterface
    public interface StateConfigProvider<S> {
        AnimationConfig get(final S oldState, final S newState);

        default AnimationConfig get(final S state) {
            return this.get(state, state);
        }
    }

}
