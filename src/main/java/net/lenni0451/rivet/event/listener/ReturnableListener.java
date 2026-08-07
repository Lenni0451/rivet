package net.lenni0451.rivet.event.listener;

import net.lenni0451.rivet.event.ReturnableMethodContext;

public interface ReturnableListener<R, T> {

    void accept(final ReturnableMethodContext<R> ctx, final T t);

}
