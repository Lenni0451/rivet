package net.lenni0451.rivet.backend.awt;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.WithBy;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Application;
import net.lenni0451.rivet.backend.awt.render.RivetCanvas;
import net.lenni0451.rivet.backend.awt.utils.AWTMapper;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Size;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true, chain = true)
public abstract class AWTApplication implements Application<AWTBackend> {

    private final Configuration configuration;
    private final CompletableFuture<Void> launchFuture = new CompletableFuture<>();
    private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();
    private final Set<MouseButton> heldMouseButtons = EnumSet.noneOf(MouseButton.class);
    private AWTBackend backend;
    private Rivet rivet;
    private JFrame frame;
    private RivetCanvas canvas;

    public AWTApplication(final String title, final int width, final int height) {
        this(Configuration.DEFAULT.withTitle(title).withWidth(width).withHeight(height));
    }

    public AWTApplication(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            if (EventQueue.isDispatchThread()) {
                this.launch();
            } else {
                EventQueue.invokeAndWait(this::launch);
            }
            this.launchFuture.complete(null);
            if (EventQueue.isDispatchThread()) {
                SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
                this.closeFuture.whenComplete((result, failure) -> loop.exit());
                loop.enter();
            } else {
                this.closeFuture.join();
            }
        } catch (Throwable t) {
            this.launchFuture.completeExceptionally(t);
            this.closeFuture.completeExceptionally(t);
            if (t instanceof RuntimeException re) throw re;
            throw new RuntimeException(t);
        }
    }

    @Override
    public void runAsync(final boolean waitForLaunch) {
        new Thread(this, this.getClass().getSimpleName() + " Main Thread").start();
        if (waitForLaunch) {
            try {
                this.launchFuture.join();
            } catch (CancellationException ignored) {
            }
        }
    }

    @Override
    public void close(final boolean waitForClose) {
        if (this.frame != null) {
            EventQueue.invokeLater(() -> {
                if (this.canvas != null) {
                    this.canvas.stop();
                }
                this.frame.dispose();
                this.closeFuture.complete(null);
            });
        } else {
            this.closeFuture.complete(null);
        }
        if (waitForClose) {
            try {
                this.closeFuture.join();
            } catch (CancellationException ignored) {
            }
        }
    }

    @SneakyThrows
    protected void launch() {
        AWTAssetLoader assetLoader = new AWTAssetLoader();
        this.backend = new AWTBackend(this.createFont(assetLoader), assetLoader);
        this.rivet = new Rivet(this.backend, FullSizeLayout.INSTANCE, new Size(this.configuration.width(), this.configuration.height()));
        this.canvas = this.initCanvas();
        this.frame = this.initFrame();
        this.initCanvasEvents(this.canvas);
        this.initWindowEvents(this.frame);
        this.init(this.rivet);
        this.frame.setVisible(true);
        this.canvas.requestFocusInWindow();
    }

    protected RivetCanvas initCanvas() {
        RivetCanvas canvas = new RivetCanvas(this.rivet, this.configuration);
        canvas.setFocusable(true);
        canvas.setFocusTraversalKeysEnabled(false);
        return canvas;
    }

    protected JFrame initFrame() {
        JFrame frame = new JFrame(this.configuration.title());
        frame.setLayout(new BorderLayout());
        frame.add(this.canvas);
        frame.setResizable(this.configuration.resizable());
        frame.pack();
        frame.setSize(this.configuration.width(), this.configuration.height());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        if (this.configuration.centered()) {
            frame.setLocationRelativeTo(null);
        }
        return frame;
    }

    protected void initCanvasEvents(final Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                component.requestFocusInWindow();
                MouseButtonEvent event = AWTMapper.mapMouseButton(e.getX(), e.getY(), e.getButton(), e.getModifiersEx());
                if (event != null) {
                    AWTApplication.this.heldMouseButtons.add(event.button());
                    AWTApplication.this.rivet.onMouseDown(event.withHeldButtons(AWTApplication.this.heldMouseButtons));
                    e.consume();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                MouseButtonEvent event = AWTMapper.mapMouseButton(e.getX(), e.getY(), e.getButton(), e.getModifiersEx());
                if (event != null) {
                    AWTApplication.this.rivet.onMouseUp(event.withHeldButtons(AWTApplication.this.heldMouseButtons));
                    AWTApplication.this.heldMouseButtons.remove(event.button());
                    e.consume();
                }
            }
        });
        component.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                AWTApplication.this.rivet.onMouseMove(new MouseMoveEvent(e.getX(), e.getY(), AWTApplication.this.heldMouseButtons));
                e.consume();
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                AWTApplication.this.rivet.onMouseMove(new MouseMoveEvent(e.getX(), e.getY(), AWTApplication.this.heldMouseButtons));
                e.consume();
            }
        });
        component.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                // Java doesn't support horizontal scrolling
                float verticalScroll = (float) e.getPreciseWheelRotation();
                // And it also doesn't care about the OS scroll direction
                verticalScroll *= -1;
                AWTApplication.this.rivet.onMouseScroll(new MouseScrollEvent(e.getX(), e.getY(), 0, verticalScroll));
                e.consume();
            }
        });
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                net.lenni0451.rivet.input.keyboard.KeyEvent event = AWTMapper.mapKeycode(e.getExtendedKeyCode(), e.getModifiersEx());
                if (event != null) {
                    AWTApplication.this.backend.heldKeys().add(event.key());
                    AWTApplication.this.rivet.onKeyDown(event);
                    e.consume();
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                net.lenni0451.rivet.input.keyboard.KeyEvent event = AWTMapper.mapKeycode(e.getExtendedKeyCode(), e.getModifiersEx());
                e.consume();
                if (event != null) {
                    AWTApplication.this.backend.heldKeys().remove(event.key());
                    AWTApplication.this.rivet.onKeyUp(event);
                    e.consume();
                }
            }

            @Override
            public void keyTyped(final KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (keyChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar)) {
                    AWTApplication.this.rivet.onCharTyped(new CharEvent(keyChar));
                    e.consume();
                }
            }
        });
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent event) {
                int width = event.getComponent().getWidth();
                int height = event.getComponent().getHeight();
                AWTApplication.this.rivet.size(new Size(width, height));
            }
        });
        new DropTarget(component, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetListener() {
            @Override
            public void dragEnter(final DropTargetDragEvent event) {
                this.handleDrag(event);
            }

            @Override
            public void dragOver(final DropTargetDragEvent event) {
                this.handleDrag(event);
            }

            @Override
            public void dropActionChanged(final DropTargetDragEvent event) {
                this.handleDrag(event);
            }

            private void handleDrag(final DropTargetDragEvent event) {
                if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    event.rejectDrag();
                    return;
                }

                List<File> files = new ArrayList<>();
                try {
                    Object data = event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (data instanceof List<?> list) {
                        list.stream().filter(File.class::isInstance).map(File.class::cast).forEach(files::add);
                    }
                } catch (Throwable ignored) {
                }

                Point location = event.getLocation();
                boolean accepted = AWTApplication.this.rivet.onDragOver(DragOverEvent.at(location.x, location.y, files));
                if (accepted) {
                    int action = event.getDropAction();
                    if (action == DnDConstants.ACTION_NONE) {
                        action = DnDConstants.ACTION_COPY;
                    }
                    event.acceptDrag(action);
                } else {
                    event.rejectDrag();
                }
            }

            @Override
            public void dragExit(final DropTargetEvent event) {
                AWTApplication.this.rivet.onDragLeave();
            }

            @Override
            public void drop(final DropTargetDropEvent event) {
                if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    event.rejectDrop();
                    return;
                }

                int action = event.getDropAction();
                if (action == DnDConstants.ACTION_NONE) {
                    action = DnDConstants.ACTION_COPY;
                }
                event.acceptDrop(action);

                try {
                    Object data = event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (data instanceof List<?> list) {
                        List<File> files = list.stream().filter(File.class::isInstance).map(File.class::cast).toList();
                        Point location = event.getLocation();
                        boolean handled = AWTApplication.this.rivet.onDrop(DropEvent.at(location.x, location.y, files));
                        event.dropComplete(handled);
                        return;
                    }
                } catch (Throwable ignored) {
                }
                event.dropComplete(false);
            }
        }, true);
    }

    protected void initWindowEvents(final JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                AWTApplication.this.close(false);
            }
        });
        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(final WindowEvent e) {
                AWTApplication.this.heldMouseButtons.clear();
                AWTApplication.this.backend.heldKeys().clear();
                AWTApplication.this.rivet.unfocus();
            }
        });
    }


    @With
    @WithBy
    public record Configuration(
            String title, int width, int height,
            boolean centered, boolean resizable, int targetFps, boolean antiAliasing, boolean textAntiAliasing,
            Color backgroundColor
    ) {
        public static final Configuration DEFAULT = new Configuration(
                "Rivet Application", 800, 600,
                true, true, 60, false, true,
                Color.GRAY.darker().darker().darker().darker()
        );
    }

}
