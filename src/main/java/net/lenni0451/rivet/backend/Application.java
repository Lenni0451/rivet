package net.lenni0451.rivet.backend;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.text.Font;

public interface Application<B extends Backend> extends Runnable {

    B backend();

    Rivet rivet();

    @Override
    void run();

    default void runAsync() {
        this.runAsync(true);
    }

    void runAsync(final boolean waitForLaunch);

    default void close() {
        this.close(true);
    }

    void close(final boolean waitForClose);

    Font createFont(final AssetLoader assetLoader) throws Exception;

    void init(final Rivet rivet);

}
