package net.lenni0451.rivet.backend.awt.render;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.threading.ThreadUtils;
import net.lenni0451.rivet.Rivet;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferStrategy;

@Accessors(fluent = true, chain = true, makeFinal = true)
public class RivetCanvas extends Canvas implements Runnable {

    private final Rivet rivet;
    @Getter
    @Setter
    private int targetFps = 60;
    private BufferStrategy bufferStrategy;
    private Thread renderThread;

    public RivetCanvas(final Rivet rivet) {
        this.rivet = rivet;

        this.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (this.renderThread != null) {
                    this.renderThread.interrupt();
                    try {
                        this.renderThread.join(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    this.renderThread = null;
                }
                if (event.getComponent().isDisplayable()) {
                    this.createBufferStrategy(2);
                    this.bufferStrategy = this.getBufferStrategy();
                    this.renderThread = new Thread(this, "Rivet AWT Render Thread");
                    this.renderThread.setDaemon(true);
                    this.renderThread.start();
                }
            }
        });
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (this.bufferStrategy == null) {
                if (!ThreadUtils.sleep(10)) break;
                continue;
            }

            long startTime = System.nanoTime();
            do {
                do {
                    if (this.getWidth() <= 0 || this.getHeight() <= 0) continue;
                    Graphics2D graphics = (Graphics2D) this.bufferStrategy.getDrawGraphics();
                    if (graphics == null) continue;

                    this.configureGraphics2D(graphics);
                    this.renderBackground(graphics);
                    graphics.setColor(Color.WHITE);
//                    RenderListExecutor.INSTANCE.renderList(new AWTRenderer(graphics), this.rivet.render(new DeferredRenderer()).complete());
                    this.rivet.render(new AWTRenderer(graphics));
                    graphics.dispose();
                } while (this.bufferStrategy.contentsRestored());
                this.bufferStrategy.show();
                Toolkit.getDefaultToolkit().sync();
            } while (this.bufferStrategy.contentsLost());

            if (this.targetFps > 0) {
                long frameTime = System.nanoTime() - startTime;
                long targetNanos = 1_000_000_000L / this.targetFps;
                long sleepNanos = targetNanos - frameTime;
                if (sleepNanos > 0) {
                    try {
                        Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    protected void configureGraphics2D(final Graphics2D graphics) {
        // General
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Shapes
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Text
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    protected void renderBackground(final Graphics2D graphics) {
        graphics.setBackground(Color.GRAY.darker().darker().darker().darker());
        graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
    }

}
