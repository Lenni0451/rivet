package net.lenni0451.rivet.backend.thingl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.Font;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public abstract class ThinGLBackend implements Backend {

    private final long window;
    private final Font font;
    private final AssetLoader assetLoader;

    @Override
    public Font font() {
        return this.font;
    }

    @Override
    public AssetLoader assetLoader() {
        return this.assetLoader;
    }

}
