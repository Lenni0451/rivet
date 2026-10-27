package net.lenni0451.rivet.backend.awt;

import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.Texture;
import net.lenni0451.rivet.backend.awt.text.AWTFont;
import net.lenni0451.rivet.backend.awt.texture.AWTTexture;
import net.lenni0451.rivet.backend.text.Font;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AWTAssetLoader implements AssetLoader {

    @Override
    public Font loadFont(final int size, final InputStream... inputStreams) throws IOException {
        List<java.awt.Font> fonts = new ArrayList<>();
        for (InputStream inputStream : inputStreams) {
            try {
                fonts.add(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream).deriveFont((float) size));
            } catch (FontFormatException e) {
                throw new IOException("Failed to load font", e);
            }
        }
        return new AWTFont(fonts);
    }

    @Override
    public Texture loadTexture(final InputStream inputStream) throws IOException {
        return new AWTTexture(ImageIO.read(inputStream));
    }

}
