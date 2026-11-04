package test.impl;

import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.impl.Label;
import net.lenni0451.rivet.xml.XmlLoader;
import test.TestBase;

import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LiveXml extends TestBase {

    void main() {
        this.run();
    }

    @Override
    public void init(final Rivet rivet) {
        Thread.ofVirtual().start(() -> {
            try {
                Path path = Path.of("ui.xml").normalize().toAbsolutePath();
                WatchService watchService = path.getFileSystem().newWatchService();
                path.getParent().register(watchService, java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    var key = watchService.take();
                    for (var event : key.pollEvents()) {
                        if (event.context().toString().equals(path.getFileName().toString())) {
                            System.out.println("Reloading UI");

                            try {
                                Component loaded = XmlLoader.standard().load(path).load();
                                rivet.tasks().beforeNextFrame(() -> {
                                    rivet.root().clear();
                                    rivet.root().add(loaded);
                                });
                            } catch (Throwable t) {
                                t.printStackTrace();
                                rivet.tasks().beforeNextFrame(() -> {
                                    rivet.root().clear();

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
                                    rivet.root().add(new Label("Failed to load UI:\n" + String.join("\n", lines)), label -> {
                                        label.overflowBehavior().set(Label.OverflowBehavior.WRAP);
                                    });
                                });
                            }
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

}
