package net.lenni0451.rivet.theme;

import net.lenni0451.rivet.Rivet;

import javax.annotation.Nullable;

public class ThemeOption<T> {

    private final Rivet rivet;
    private final ThemeKey<T> key;
    private T value;

    public ThemeOption(final Rivet rivet, final ThemeKey<T> key) {
        this.rivet = rivet;
        this.key = key;
    }

    public Rivet rivet() {
        return this.rivet;
    }

    public ThemeKey<T> key() {
        return this.key;
    }

    public T value() {
        if (this.value == null) {
            return this.rivet.getTheme().get(this.key);
        }
        return this.value;
    }

    public void set(@Nullable final T value) {
        this.value = value;
    }

    public void reset() {
        this.value = null;
    }

}
