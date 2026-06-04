package net.lenni0451.rivet.theme;

import net.lenni0451.rivet.Rivet;

import java.util.function.Function;

public record ThemeKey<T>(String name, Class<T> type, Function<Rivet, T> defaultValue) {
}
