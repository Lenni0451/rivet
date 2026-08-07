package net.lenni0451.rivet.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PrePostListenerList<PRE, POST> {

    private final List<PRE> preListeners = new ArrayList<>();
    private final List<POST> postListeners = new ArrayList<>();

    public PrePostListenerList<PRE, POST> add(final PRE listener) {
        return this.addPre(listener);
    }

    public PrePostListenerList<PRE, POST> remove(final PRE listener) {
        return this.removePre(listener);
    }

    public PrePostListenerList<PRE, POST> addPre(final PRE listener) {
        this.preListeners.add(listener);
        return this;
    }

    public PrePostListenerList<PRE, POST> removePre(final PRE listener) {
        this.preListeners.remove(listener);
        return this;
    }

    public PrePostListenerList<PRE, POST> addPost(final POST listener) {
        this.postListeners.add(listener);
        return this;
    }

    public PrePostListenerList<PRE, POST> removePost(final POST listener) {
        this.postListeners.remove(listener);
        return this;
    }

    public void call(final Consumer<PRE> preInvoker, final Consumer<POST> postInvoker, final Runnable code) {
        for (PRE listener : this.preListeners) {
            preInvoker.accept(listener);
        }
        code.run();
        for (POST postListener : this.postListeners) {
            postInvoker.accept(postListener);
        }
    }

    public void callVoid(final BiConsumer<PRE, VoidMethodContext> preInvoker, final Consumer<POST> postInvoker, final Runnable code) {
        VoidMethodContext context = new VoidMethodContext();
        for (PRE preListener : this.preListeners) {
            preInvoker.accept(preListener, context);
            if (context.cancelled() && context.cancelPropagation()) {
                return;
            }
        }
        if (!context.cancelled()) {
            code.run();
            for (POST postListener : this.postListeners) {
                postInvoker.accept(postListener);
            }
        }
    }

    public <R> R callWithReturnValue(final BiConsumer<PRE, ReturnableMethodContext<R>> preInvoker, final Consumer<POST> postInvoker, final Supplier<R> code) {
        ReturnableMethodContext<R> context = new ReturnableMethodContext<>();
        for (PRE preListener : this.preListeners) {
            preInvoker.accept(preListener, context);
            if (context.cancelled() && context.cancelPropagation()) {
                break;
            }
        }
        if (context.cancelled()) {
            return context.returnValue();
        } else {
            R returnValue = code.get();
            for (POST postListener : this.postListeners) {
                postInvoker.accept(postListener);
            }
            return returnValue;
        }
    }

}
