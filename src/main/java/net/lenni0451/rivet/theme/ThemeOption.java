package net.lenni0451.rivet.theme;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class ThemeOption<T> {

    @Getter
    private final Rivet rivet;
    @Getter
    private final ThemeKey<T> key;
    @Nullable
    private final Supplier<T> defaultSupplier;
    private T value;

    public ThemeOption(final Rivet rivet, final ThemeKey<T> key) {
        this(rivet, key, null);
    }

    public ThemeOption(final Rivet rivet, final ThemeKey<T> key, @Nullable final Supplier<T> defaultSupplier) {
        this.rivet = rivet;
        this.key = key;
        this.defaultSupplier = defaultSupplier;
    }

    public T value() {
        if (this.value != null) return this.value;
        T themeValue = this.rivet.getTheme().getOrDefault(this.key, null);
        if (themeValue != null) return themeValue;
        if (this.defaultSupplier != null) return this.defaultSupplier.get();
        return this.rivet.getTheme().get(this.key);
    }

    public void set(@Nullable final T value) {
        this.value = value;
    }

    public void reset() {
        this.value = null;
    }

}
