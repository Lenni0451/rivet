package net.lenni0451.rivet.backend.thingl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.Backend;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public abstract class ThinGLBackend implements Backend {

    private final long window;
    private final ThinGLFont font;

    @Override
    public Font font() {
        return this.font;
    }

}
