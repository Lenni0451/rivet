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

public class AWTAssetLoader implements AssetLoader {

    @Override
    public Font loadFont(final InputStream inputStream, final int size) throws IOException {
        try {
            return new AWTFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream).deriveFont((float) size));
        } catch (FontFormatException e) {
            throw new IOException("Failed to load font", e);
        }
    }

    @Override
    public Texture loadTexture(final InputStream inputStream) throws IOException {
        return new AWTTexture(ImageIO.read(inputStream));
    }

}
