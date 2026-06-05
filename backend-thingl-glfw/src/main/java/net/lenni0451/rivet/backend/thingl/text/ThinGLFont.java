package net.lenni0451.rivet.backend.thingl.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.backend.text.ShapedText;
import net.lenni0451.rivet.backend.text.ShapedTextBlock;
import net.lenni0451.rivet.text.model.TextSection;
import net.raphimc.thingl.resource.font.instance.FontInstanceSet;
import net.raphimc.thingl.text.TextStyle;
import net.raphimc.thingl.text.shaping.ShapedTextLine;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class ThinGLFont implements Font {

    private final FontInstanceSet fontInstanceSet;

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
        return new ThinGLFont(this.fontInstanceSet.getScaledInstanceSet(size));
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
    public ShapedTextBlock shapeText(final net.lenni0451.rivet.text.model.TextBlock block) {
        net.raphimc.thingl.text.TextBlock thinglBlock = new net.raphimc.thingl.text.TextBlock();
        for (net.lenni0451.rivet.text.model.TextLine line : block.lines()) {
            thinglBlock.add(this.toThinGL(line));
        }
        return new ThinGLShapedTextBlock(thinglBlock.shape());
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
