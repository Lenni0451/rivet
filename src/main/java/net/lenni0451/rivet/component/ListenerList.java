package net.lenni0451.rivet.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

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

    public boolean call(final Predicate<L> listenerInvoker, final BooleanSupplier task) {
        for (L listener : this.listeners) {
            boolean value = listenerInvoker.test(listener);
            if (value) return true;
        }
        return task.getAsBoolean();
    }

}
