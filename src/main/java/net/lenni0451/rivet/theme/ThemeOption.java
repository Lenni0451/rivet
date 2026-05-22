package net.lenni0451.rivet.theme;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public final class ThemeOption<T> {

    @Getter
    private final Rivet rivet;
    @Getter
    private final ThemeKey<T> key;
    @Nullable
    private Supplier<T> value;

    public ThemeOption(final Rivet rivet, final ThemeKey<T> key) {
        this.rivet = rivet;
        this.key = key;
    }

    public T value() {
        if (this.value != null) return this.value.get();
        return this.rivet.theme().get(this.key);
    }

    public void set(@Nullable final T value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = () -> value;
        }
    }

    public void set(@Nullable final Supplier<T> valueSupplier) {
        this.value = valueSupplier;
    }

    public void reset() {
        this.value = null;
    }

}
