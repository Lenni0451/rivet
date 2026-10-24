package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.impl.TextArea;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.text.highlight.SyntaxHighlighter;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.function.Predicate;

public class TextAreaParser implements XmlParser<TextArea> {

    private final XmlParser<TextArea> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("text", String.class)
                    .map(TextArea::new)
                    .orElseGet(TextArea::new))
            .referenceAttribute("font", Font.class, TextArea::font)
            .attribute("text", String.class, TextArea::text)
            .attribute("hint", String.class, TextArea::hint)
            .referenceAttribute("validator", Predicate.class, TextArea::validator)
            .referenceAttribute("highlighter", SyntaxHighlighter.class, TextArea::highlighter)
            .themeOption("tabWidth", Integer.class, TextArea::tabWidth)
            .themeOption("textColor", Color.class, TextArea::textColor)
            .themeOption("invalidTextColor", Color.class, TextArea::invalidTextColor)
            .themeOption("hintColor", Color.class, TextArea::hintColor)
            .themeOption("backgroundColor", Color.class, TextArea::backgroundColor)
            .themeOption("outlineColor", Color.class, TextArea::outlineColor)
            .themeOption("focusedOutlineColor", Color.class, TextArea::focusedOutlineColor)
            .themeOption("invalidOutlineColor", Color.class, TextArea::invalidOutlineColor)
            .themeOption("selectionColor", Color.class, TextArea::selectionColor)
            .themeOption("cursorColor", Color.class, TextArea::cursorColor)
            .themeOption("cursorWidth", Float.class, TextArea::cursorWidth)
            .themeOption("outlineWidth", Float.class, TextArea::outlineWidth)
            .themeOption("cornerRadius", Corners.class, TextArea::cornerRadius)
            .themeOption("innerPadding", Padding.class, TextArea::innerPadding)
            .themeOption("disabledTextColor", Color.class, TextArea::disabledTextColor)
            .themeOption("disabledBackgroundColor", Color.class, TextArea::disabledBackgroundColor)
            .themeOption("disabledOutlineColor", Color.class, TextArea::disabledOutlineColor)
            .themeOption("cursorAnimationConfig", AnimationConfig.class, TextArea::cursorAnimationConfig)
            .themeOption("focusAnimationConfig", AnimationConfig.class, TextArea::focusAnimationConfig)
            .build();

    @Override
    public TextArea parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
