package net.lenni0451.rivet.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ListenerList<L> {

    private final List<L> listeners = new ArrayList<>();

    public ListenerList<L> add(final L listener) {
        this.listeners.add(listener);
        return this;
    }

    public ListenerList<L> remove(final L listener) {
        this.listeners.remove(listener);
        return this;
    }

    public <R> R call(final Function<L, R> listenerInvoker, final Supplier<R> task) {
        for (L listener : this.listeners) {
            R value = listenerInvoker.apply(listener);
            if (value != null) return value;
        }
        return task.get();
    }

}
