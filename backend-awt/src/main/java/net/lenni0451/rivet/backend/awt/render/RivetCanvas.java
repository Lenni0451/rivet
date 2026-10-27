package net.lenni0451.rivet.backend.awt.render;

import lombok.experimental.Accessors;
import net.lenni0451.commons.threading.ThreadUtils;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.awt.AWTApplication;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferStrategy;

/**
 * AWT Canvas can be quite slow, so you might need to toy around with JVM arguments to get the best performance.<br>
 * Try playing around with the following arguments:<br>
 * <ul>
 *     <li>-Dsun.java2d.opengl=false</li>
 *     <li>-Dsun.java2d.d3d=false</li>
 *     <li>-Dsun.java2d.xrender=false</li>
 * </ul>
 */
@Accessors(fluent = true, chain = true, makeFinal = true)
public class RivetCanvas extends Canvas implements Runnable {

    private final Rivet rivet;
    private final int targetFps;
    private final boolean antiAliasing;
    private final boolean textAntiAliasing;
    private final Color backgroundColor;
    private BufferStrategy bufferStrategy;
    private Thread renderThread;

    public RivetCanvas(final Rivet rivet, final AWTApplication.Configuration configuration) {
        this.rivet = rivet;
        this.targetFps = configuration.targetFps();
        this.antiAliasing = configuration.antiAliasing();
        this.textAntiAliasing = configuration.textAntiAliasing();
        this.backgroundColor = configuration.backgroundColor().toAWT();

        this.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (this.renderThread != null) {
                    this.stop();
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

    public void stop() {
        if (this.renderThread != null) {
            this.renderThread.interrupt();
            try {
                this.renderThread.join(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Render thread did not exit after 1s", e);
            }
            this.renderThread = null;
        }
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
                    this.render(graphics);
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
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, this.antiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Shapes
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Text
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, this.textAntiAliasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    protected void renderBackground(final Graphics2D graphics) {
        graphics.setBackground(this.backgroundColor);
        graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
    }

    protected void render(final Graphics2D graphics) {
//        RenderListExecutor.INSTANCE.renderList(new AWTRenderer(graphics), this.rivet.render(new DeferredRenderer()).complete());
        this.rivet.render(new AWTRenderer(graphics));
    }

}
