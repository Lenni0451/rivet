package net.lenni0451.rivet.event.listener;

import net.lenni0451.rivet.event.VoidMethodContext;

public interface BiVoidListener<A, B> {

    void accept(final VoidMethodContext ctx, final A a, final B b);

}
