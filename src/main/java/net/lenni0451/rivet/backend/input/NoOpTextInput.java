package net.lenni0451.rivet.backend.input;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.math.Rectangle;

@Getter
@Accessors(fluent = true, chain = true, makeFinal = true)
public class NoOpTextInput implements TextInput {

    private boolean active;

    @Override
    public void start() {
        this.active = true;
    }

    @Override
    public void stop() {
        this.active = false;
    }

    @Override
    public void area(final Rectangle rectangle) {
    }

}
