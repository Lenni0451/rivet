import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.component.impl.Button;
import net.lenni0451.rivet.container.impl.AbsoluteContainer;
import net.lenni0451.rivet.renderer.awt.Graphics2DRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Test {

    public static void main(String[] args) throws Throwable {
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AbsoluteContainer rootContainer = new AbsoluteContainer();
        Button button = new Button("Testing", mouseButton -> System.out.println("CLICKED! Button: " + mouseButton));
        rootContainer.add(button, 50, 50);
        Rivet rivet = new Rivet(new Graphics2DRenderer(g2d), rootContainer, image.getWidth(), image.getHeight());
        rivet.render();

        ImageIO.write(image, "png", new File("test.png"));
    }

}
