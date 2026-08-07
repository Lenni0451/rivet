package net.lenni0451.rivet.event.listener;

import net.lenni0451.rivet.event.VoidMethodContext;

public interface VoidListener<T> {

    void accept(final VoidMethodContext ctx, final T t);

}
