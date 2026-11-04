package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.xml.XmlLoader;
import test.TestBase;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LiveXml extends TestBase {

    private static final Path PATH = Path.of("ui.xml").normalize().toAbsolutePath();

    void main() {
        this.run();
    }

    @Override
    public void init(final Rivet rivet) {
        this.reload();
        Thread.ofVirtual().start(() -> {
            try {
                WatchService watchService = PATH.getFileSystem().newWatchService();
                PATH.getParent().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().equals(PATH.getFileName().toString())) {
                            System.out.println("Reloading UI");
                            this.reload();
                        }
                    }
                    key.reset();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(-1);
            }
        });
    }

    private void reload() {
        var ref = new Object() {
            Component loaded;
        };
        try {
            ref.loaded = XmlLoader.standard().load(PATH).load();
        } catch (Throwable t) {
            t.printStackTrace();
            ref.loaded = this.formatException(t);
        }
        this.rivet().tasks().beforeNextFrame(() -> {
            this.rivet().root().clear();
            this.rivet().root().add(ref.loaded);
        });
    }

    private Label formatException(final Throwable t) {
        List<String> lines = new ArrayList<>();
        Stack<Throwable> causes = new Stack<>();
        causes.push(t);
        while (!causes.isEmpty()) {
            Throwable cause = causes.pop();
            lines.add(cause.getClass().getSimpleName() + ": " + cause.getMessage());
            for (Throwable suppressed : cause.getSuppressed()) {
                causes.push(suppressed);
            }
            if (cause.getCause() != null) {
                causes.push(cause.getCause());
            }
        }

        Label label = new Label("Failed to load UI:\n" + String.join("\n", lines));
        label.overflowBehavior().set(Label.OverflowBehavior.WRAP);
        return label;
    }

}
