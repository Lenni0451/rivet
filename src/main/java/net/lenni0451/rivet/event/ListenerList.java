package net.lenni0451.rivet.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ListenerList<I> {

    private final List<I> listeners = new ArrayList<>();

    public List<I> listeners() {
        return List.copyOf(this.listeners);
    }

    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    public ListenerList<I> add(final I listener) {
        this.listeners.add(listener);
        return this;
    }

    public ListenerList<I> remove(final I listener) {
        this.listeners.remove(listener);
        return this;
    }

    public void call(final Consumer<I> invoker) {
        if (this.isEmpty()) return;

        for (I listener : this.listeners) {
            invoker.accept(listener);
        }
    }

    public void callVoid(final BiConsumer<I, VoidMethodContext> invoker, final Runnable code) {
        if (this.isEmpty()) {
            code.run();
            return;
        }

        VoidMethodContext context = new VoidMethodContext();
        for (I listener : this.listeners) {
            invoker.accept(listener, context);
            if (context.cancelled() && context.cancelPropagation()) {
                break;
            }
        }
        if (!context.cancelled()) {
            code.run();
        }
    }

    public <R> R callWithReturnValue(final BiConsumer<I, ReturnableMethodContext<R>> invoker, final Supplier<R> code) {
        if (this.isEmpty()) return code.get();

        ReturnableMethodContext<R> context = new ReturnableMethodContext<>();
        for (I listener : this.listeners) {
            invoker.accept(listener, context);
            if (context.cancelled() && context.cancelPropagation()) {
                break;
            }
        }
        if (context.cancelled()) {
            return context.returnValue();
        } else {
            return code.get();
        }
    }

}
