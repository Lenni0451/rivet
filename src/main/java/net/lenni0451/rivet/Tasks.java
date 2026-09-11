package net.lenni0451.rivet;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public final class Tasks {

    private final Queue<Runnable> tasks = new LinkedBlockingQueue<>();

    public void beforeNextFrame(final Runnable task) {
        this.tasks.offer(task);
    }

    public CompletableFuture<Void> beforeNextFrameFuture(final Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.tasks.offer(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    void runTasks() {
        Runnable task;
        while ((task = this.tasks.poll()) != null) task.run();
    }

}
