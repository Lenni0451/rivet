package net.lenni0451.rivet.backend.thingl;

import net.lenni0451.rivet.backend.AssetLoader;
import net.lenni0451.rivet.backend.thingl.text.ThinGLFont;
import net.lenni0451.rivet.backend.thingl.texture.ThinGLCPUTexture;
import net.raphimc.thingl.resource.font.face.FontFace;
import net.raphimc.thingl.resource.font.face.impl.FreeTypeFontFace;
import net.raphimc.thingl.resource.font.instance.FontInstance;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import net.raphimc.thingl.resource.image.impl.StbByteImage2D;
import net.raphimc.thingl.text.util.GlyphPredicate;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

public class ThinGLAssetLoader implements AssetLoader {

    @Override
    public ThinGLFont loadFont(final InputStream inputStream, final int size) throws IOException {
        return this.loadFont(size, inputStream);
    }

    public ThinGLFont loadFont(final int size, final InputStream... inputStreams) throws IOException {
        List<FontFace> fontFaces = new ArrayList<>();
        SequencedMap<FontInstance, GlyphPredicate> instances = new LinkedHashMap<>();
        for (InputStream inputStream : inputStreams) {
            FreeTypeFontFace fontFace = new FreeTypeFontFace(inputStream.readAllBytes());
            fontFaces.add(fontFace);
            instances.put(fontFace.getInstance(size), GlyphPredicate.all());
        }
        return new ThinGLFont(new FontInstanceSet(instances), fontFaces);
    }

    @Override
    public ThinGLCPUTexture loadTexture(final InputStream inputStream) throws IOException {
        return this.loadTexture(inputStream, GL11C.GL_LINEAR);
    }

    public ThinGLCPUTexture loadTexture(final InputStream inputStream, final int filter) throws IOException {
        return new ThinGLCPUTexture(new StbByteImage2D(inputStream.readAllBytes()), filter);
    }

}
