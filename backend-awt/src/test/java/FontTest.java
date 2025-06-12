import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FontTest {

    public static void main(String[] args) throws Throwable {
        Font font1 = Font.createFont(Font.TRUETYPE_FONT, new File("font1.ttf"));
        Font font2 = Font.createFont(Font.TRUETYPE_FONT, new File("font2.ttf"));

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

}
