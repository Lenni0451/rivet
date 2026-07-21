package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.math.Rectangle;

public interface TextInput {

    TextInput NOOP = new TextInput() {
        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void area(final Rectangle rectangle) {
        }
    };


    void start();

    void stop();

    void area(final Rectangle rectangle);

}
