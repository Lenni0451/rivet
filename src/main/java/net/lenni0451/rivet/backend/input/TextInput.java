package net.lenni0451.rivet.backend.input;

import net.lenni0451.rivet.math.Rectangle;

public interface TextInput {

    void start();

    void stop();

    void area(final Rectangle rectangle);

    boolean active();

}
