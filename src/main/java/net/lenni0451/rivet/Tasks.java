package net.lenni0451.rivet;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

public final class Tasks {

    private final Queue<Runnable> tasks = new LinkedBlockingDeque<>();

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
        int count = this.tasks.size();
        for (int i = 0; i < count; i++) {
            Runnable task = this.tasks.poll();
            if (task == null) break;
            task.run();
        }
    }

}
