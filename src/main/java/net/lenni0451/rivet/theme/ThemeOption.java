package net.lenni0451.rivet.theme;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.event.ListenerList;
import net.lenni0451.rivet.property.ObjectProperty;

import javax.annotation.Nullable;

@Accessors(fluent = true, chain = true, makeFinal = true)
public final class ThemeOption<T> {

    @Getter
    private final Component component;
    @Getter
    private final ThemeKey<T> key;
    private final ObjectProperty<T> value = new ObjectProperty<>();

    public ThemeOption(final Component component, final ThemeKey<T> key) {
        this.component = component;
        this.key = key;

        // The value can be null if it can't be resolved (e.g. the component is not attached), keep the last value instead
        this.value.addValidator(newValue -> newValue != null ? newValue : this.value.get());
        this.reset();
    }

    public T get() {
        T value = this.value.get();
        if (value == null) {
            throw new IllegalStateException("Unable to get theme value for key " + this.key.name() + " without a rivet instance");
        }
        return value;
    }

    public ThemeOption<T> set(@Nullable final T value) {
        if (value == null) return this.reset();
        this.value.unregisterSync();
        this.value.set(value);
        return this;
    }

    public ThemeOption<T> reset() {
        return this.bind(this.key);
    }

    public ThemeOption<T> bind(final ThemeKey<T> themeKey) {
        return this.bind(new ThemeValueObservable<>(this.component, themeKey));
    }

    public ThemeOption<T> bind(final ObjectProperty<T> property) {
        this.value.unregisterSync();
        property.registerSync(this.value);
        return this;
    }

    public ThemeOption<T> bind(final ObjectProperty.Observable<T> observable) {
        this.value.unregisterSync();
        this.value.registerSync(observable);
        return this;
    }

    public ListenerList<ObjectProperty.Setter<T>> changeListener() {
        return this.value.changeListener();
    }


    private static class ThemeValueObservable<T> implements ObjectProperty.Observable<T> {
        private final Component component;
        private final ThemeKey<T> key;
        private final ListenerList<ObjectProperty.Setter<T>> changeListener = new ListenerList<>();
        private final Runnable updater = this::update;

        private ThemeValueObservable(final Component component, final ThemeKey<T> key) {
            this.component = component;
            this.key = key;
        }

        @Override
        @Nullable
        public T get() {
            Rivet rivet = this.component.rivet();
            return rivet == null ? null : rivet.theme().get(this.key);
        }

        @Override
        public void set(final T value) {
            // Theme values are read-only
        }

        @Override
        public void addChangeListener(final ObjectProperty.Setter<T> listener) {
            if (this.changeListener.isEmpty()) {
                this.component.addedListener().add(this.updater);
                this.component.themeChangedListener().add(this.updater);
            }
            this.changeListener.add(listener);
        }

        @Override
        public void removeChangeListener(final ObjectProperty.Setter<T> listener) {
            this.changeListener.remove(listener);
            if (this.changeListener.isEmpty()) {
                this.component.addedListener().remove(this.updater);
                this.component.themeChangedListener().remove(this.updater);
            }
        }

        private void update() {
            T value = this.get();
            if (value != null) {
                this.changeListener.call(listener -> listener.set(value));
            }
        }
    }

}
