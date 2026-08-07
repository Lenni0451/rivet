package net.lenni0451.rivet.event.listener;

import net.lenni0451.rivet.event.ReturnableMethodContext;

public interface BiReturnableListener<R, A, B> {

    void accept(final ReturnableMethodContext<R> ctx, final A a, final B b);

}
