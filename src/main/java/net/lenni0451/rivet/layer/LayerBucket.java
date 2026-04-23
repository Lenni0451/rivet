package net.lenni0451.rivet.layer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public enum LayerBucket {

    BASE(true),
    OVERLAY(true),
    TOOLTIP(false);

    private final boolean interceptable;

}
