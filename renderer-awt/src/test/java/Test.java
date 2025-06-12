import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.impl.Button;
import net.lenni0451.rivet.constants.MouseConstants;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.renderer.awt.Graphics2DRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;

public class Test extends Canvas {

    static {
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("glass.win.uiScale", "100%");
        System.setProperty("prism.allowhidpi", "false");
    }

    public static void main(String[] args) throws Throwable {
        new Test().run();
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AbsoluteContainer rootContainer = new AbsoluteContainer();
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        rootContainer.add(button, 50, 50);
        Rivet rivet = new Rivet(new Graphics2DRenderer(g2d), rootContainer, image.getWidth(), image.getHeight());
        rivet.render();

        ImageIO.write(image, "png", new File("test.png"));
    }


    private JFrame frame;
    private Rivet rivet;
    private Graphics2D g2d;

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

    private void initUI() {
        AbsoluteContainer rootContainer = new AbsoluteContainer();
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        rootContainer.add(button, 50, 50);
        this.rivet = new Rivet(new Graphics2DRenderer(() -> this.g2d), rootContainer, this.frame.getWidth(), this.frame.getHeight());
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
    }

    private void run() {
        this.createBufferStrategy(2);
        BufferStrategy bufferStrategy = this.getBufferStrategy();
        while (!Thread.currentThread().isInterrupted()) {
            do {
                do {
                    final Graphics graphics = bufferStrategy.getDrawGraphics();
                    if (graphics == null) continue;
                    this.g2d = (Graphics2D) graphics;
                    this.rivet.render();
                    graphics.dispose();
                } while (bufferStrategy.contentsRestored());
                bufferStrategy.show();
            } while (bufferStrategy.contentsLost());
        }
    }

}
