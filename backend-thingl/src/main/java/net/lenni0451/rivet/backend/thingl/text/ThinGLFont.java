package net.lenni0451.rivet.backend.thingl.text;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.text.model.TextSection;
import net.raphimc.thingl.resource.font.face.FontFace;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.shaping.ShapedTextLine;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true, chain = true)
public class ThinGLFont implements Font {

    private final FontInstanceSet fontInstanceSet;
    private final List<FontFace> fontFaces;

    public ThinGLFont(final FontInstanceSet fontInstanceSet, final List<FontFace> fontFaces) {
        this.fontInstanceSet = fontInstanceSet;
        this.fontFaces = new ArrayList<>(fontFaces);
    }

    @Override
    public int size() {
        return this.fontInstanceSet.getMainInstance().getSize();
    }

    @Override
    public float height() {
        return this.fontInstanceSet.getMainInstance().getHeight();
    }

    @Override
    public Font derive(final int size) {
        return new ThinGLFont(this.fontInstanceSet.getScaledInstanceSet(size), this.fontFaces);
    }

    @Override
    public ShapedText shapeText(final String text, final Color color) {
        TextStyle style = new TextStyle(color, 0, Color.TRANSPARENT);
        ShapedTextLine shapedTextLine = net.raphimc.thingl.text.TextLine.fromString(this.fontInstanceSet, text, style).shape();
        return new ThinGLShapedText(shapedTextLine);
    }

    @Override
    public ShapedText shapeText(final net.lenni0451.rivet.text.model.TextLine line) {
        return new ThinGLShapedText(this.toThinGL(line).shape());
    }

    @Override
    public void close() {
        this.fontFaces.forEach(FontFace::free);
        this.fontFaces.clear();
        this.fontInstanceSet.free();
    }

    private net.raphimc.thingl.text.TextLine toThinGL(final net.lenni0451.rivet.text.model.TextLine line) {
        net.raphimc.thingl.text.TextLine textLine = new net.raphimc.thingl.text.TextLine();
        for (TextSection section : line.sections()) {
            int flags = TextStyle.buildFlags(
                    section.format().shadow(),
                    section.format().bold(),
                    section.format().italic(),
                    section.format().underlined(),
                    section.format().strikethrough()
            );
            TextStyle style = new TextStyle(section.format().color(), flags, section.format().outlineColor());
            textLine.runs().addAll(net.raphimc.thingl.text.TextLine.fromString(this.fontInstanceSet, section.text(), style).runs());
        }
        textLine.compact();
        return textLine;
    }

}
