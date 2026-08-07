package net.lenni0451.rivet.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@NoArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class VoidMethodContext {

    private boolean cancelled;
    private boolean cancelPropagation;

    public VoidMethodContext cancel() {
        this.cancelled = true;
        return this;
    }

    public VoidMethodContext cancel(final boolean cancelPropagation) {
        this.cancelled = true;
        this.cancelPropagation = cancelPropagation;
        return this;
    }

}
