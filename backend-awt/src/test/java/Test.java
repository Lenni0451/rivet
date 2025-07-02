import lombok.SneakyThrows;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.awt.AWTBackend;
import net.lenni0451.rivet.backend.awt.Graphics2DRenderer;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.impl.Button;
import net.lenni0451.rivet.constants.MouseConstants;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.text.FontSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

public class Test extends Canvas {

    static {
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("glass.win.uiScale", "100%");
        System.setProperty("prism.allowhidpi", "false");
    }

    public static void main(String[] args) throws Throwable {
        new Test().run();
    }


    private JFrame frame;
    private Rivet rivet;

    public Test() {
        this.initFrame();
        this.initUI();
        this.initListeners();
    }

    private void initFrame() {
        this.frame = new JFrame("Rivet Test");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(500, 500);
        this.frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout());
        this.frame.setContentPane(root);
        root.add(this, BorderLayout.CENTER);
        this.frame.setVisible(true);
    }

    @SneakyThrows
    private void initUI() {
        AWTBackend backend = new AWTBackend(null);
        Font font = backend.loadFont(this.getClass().getResourceAsStream("/Roboto-Regular.ttf"), 48);

        AbsoluteContainer rootContainer = new AbsoluteContainer();
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        rootContainer.add(button, 50, 50);
        this.rivet = new Rivet(backend, new FontSet(font), rootContainer, this.frame.getWidth(), this.frame.getHeight());
    }

    private void initListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int button = switch (e.getButton()) {
                    case MouseEvent.BUTTON1 -> MouseConstants.BUTTON_LEFT;
                    case MouseEvent.BUTTON2 -> MouseConstants.BUTTON_MIDDLE;
                    case MouseEvent.BUTTON3 -> MouseConstants.BUTTON_RIGHT;
                    default -> -1;
                };
                if (button == -1) return;

                int modifiers = 0;
                if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_SHIFT;
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_CONTROL;
                if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_ALT;
                if ((e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_META;

                Test.this.rivet.onMouseDown(e.getX(), e.getY(), button, modifiers);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int button = switch (e.getButton()) {
                    case MouseEvent.BUTTON1 -> MouseConstants.BUTTON_LEFT;
                    case MouseEvent.BUTTON2 -> MouseConstants.BUTTON_MIDDLE;
                    case MouseEvent.BUTTON3 -> MouseConstants.BUTTON_RIGHT;
                    default -> -1;
                };
                if (button == -1) return;

                int modifiers = 0;
                if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_SHIFT;
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_CONTROL;
                if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_ALT;
                if ((e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0) modifiers |= MouseConstants.MODIFIER_META;

                Test.this.rivet.onMouseDown(e.getX(), e.getY(), button, modifiers);
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Test.this.rivet.onMouseMove(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Test.this.rivet.onMouseMove(e.getX(), e.getY());
            }
        });
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Test.this.rivet.onMouseScroll(e.getX(), e.getY(), 0, (float) -e.getPreciseWheelRotation());
            }
        });
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                Test.this.rivet.onCharTyped(e.getKeyChar());
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    private void run() {
        this.createBufferStrategy(2);
        BufferStrategy bufferStrategy = this.getBufferStrategy();
        while (!Thread.currentThread().isInterrupted()) {
            do {
                do {
                    final Graphics graphics = bufferStrategy.getDrawGraphics();
                    if (graphics == null) continue;
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
                    ((AWTBackend) this.rivet.getBackend()).setRenderer(new Graphics2DRenderer(g2d));
                    this.rivet.render();
                    graphics.dispose();
                } while (bufferStrategy.contentsRestored());
                bufferStrategy.show();
            } while (bufferStrategy.contentsLost());
        }
    }

}
