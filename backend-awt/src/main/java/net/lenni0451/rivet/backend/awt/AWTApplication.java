package net.lenni0451.rivet.backend.awt;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.awt.utils.AWTMapper;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Size;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;
import java.util.Set;

@Getter
@Accessors(fluent = true, chain = true)
public abstract class AWTApplication {

    private AWTBackend backend;
    private Rivet rivet;
    private final Set<MouseButton> heldMouseButtons = EnumSet.noneOf(MouseButton.class);

    public AWTApplication(final String title, final int width, final int height) {
        this.initRivet(width, height);
        this.initFrame(title, width, height);
    }

    @SneakyThrows
    protected void initRivet(final int width, final int height) {
        AWTAssetLoader assetLoader = new AWTAssetLoader();
        this.backend = new AWTBackend(this.createFont(assetLoader), assetLoader);
        this.rivet = new Rivet(this.backend, FullSizeLayout.INSTANCE, new Size(width, height));
        this.initRivet(this.rivet);
    }

    protected void initFrame(final String title, final int width, final int height) {
        RivetCanvas canvas = new RivetCanvas(this.rivet);
        JFrame frame = new JFrame(title);
        frame.setLayout(new BorderLayout());
        frame.add(canvas);
        frame.pack();
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.initComponentEvents(canvas);
        this.initWindowEvents(frame);
        this.initFrame(frame);
        frame.setVisible(true);
    }

    protected void initComponentEvents(final Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
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
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                net.lenni0451.rivet.input.keyboard.KeyEvent event = AWTMapper.mapKeycode(e.getExtendedKeyCode(), e.getModifiersEx());
                if (event != null) {
                    AWTApplication.this.backend.heldKeys().remove(event.key());
                    AWTApplication.this.rivet.onKeyUp(event);
                }
            }

            @Override
            public void keyTyped(final KeyEvent e) {
                char keyChar = e.getKeyChar();
                if (keyChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar)) {
                    AWTApplication.this.rivet.onCharTyped(new CharEvent(keyChar));
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
    }

    protected void initWindowEvents(final JFrame frame) {
        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(final WindowEvent e) {
                AWTApplication.this.heldMouseButtons.clear();
                AWTApplication.this.backend.heldKeys().clear();
                AWTApplication.this.rivet.unfocus();
            }
        });
    }

    protected abstract Font createFont(final AWTAssetLoader assetLoader) throws Exception;

    protected abstract void initRivet(final Rivet rivet);

    protected void initFrame(final JFrame frame) {
    }

}
