package net.lenni0451.rivet.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@NoArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public final class ReturnableMethodContext<R> {

    private boolean cancelled;
    private boolean cancelPropagation;
    private R returnValue;

    public ReturnableMethodContext<R> cancel(final R returnValue) {
        this.cancelled = true;
        this.returnValue = returnValue;
        return this;
    }

    public ReturnableMethodContext<R> cancel(final boolean cancelPropagation, final R returnValue) {
        this.cancelled = true;
        this.cancelPropagation = cancelPropagation;
        this.returnValue = returnValue;
        return this;
    }

}
