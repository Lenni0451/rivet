package net.lenni0451.rivet.xml.parser.impl.component;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.animation.AnimationConfig;
import net.lenni0451.rivet.backend.text.Font;
import net.lenni0451.rivet.component.impl.TextField;
import net.lenni0451.rivet.math.Corners;
import net.lenni0451.rivet.math.Padding;
import net.lenni0451.rivet.text.highlight.SyntaxHighlighter;
import net.lenni0451.rivet.xml.parser.XmlParser;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.parser.impl.ElementParserBuilder;
import org.w3c.dom.Node;

import java.util.function.Function;
import java.util.function.Predicate;

public class TextFieldParser implements XmlParser<TextField> {

    private final XmlParser<TextField> parser = ElementParserBuilder.createComponent(ctx -> ctx.attribute("text", String.class)
                    .map(TextField::new)
                    .orElseGet(TextField::new))
            .referenceAttribute("font", Font.class, TextField::font)
            .attribute("text", String.class, TextField::text)
            .attribute("hint", String.class, TextField::hint)
            .referenceAttribute("charReplacer", Function.class, TextField::charReplacer)
            .referenceAttribute("validator", Predicate.class, TextField::validator)
            .referenceAttribute("highlighter", SyntaxHighlighter.class, TextField::highlighter)
            .themeOption("textColor", Color.class, TextField::textColor)
            .themeOption("invalidTextColor", Color.class, TextField::invalidTextColor)
            .themeOption("hintColor", Color.class, TextField::hintColor)
            .themeOption("backgroundColor", Color.class, TextField::backgroundColor)
            .themeOption("outlineColor", Color.class, TextField::outlineColor)
            .themeOption("focusedOutlineColor", Color.class, TextField::focusedOutlineColor)
            .themeOption("invalidOutlineColor", Color.class, TextField::invalidOutlineColor)
            .themeOption("selectionColor", Color.class, TextField::selectionColor)
            .themeOption("cursorColor", Color.class, TextField::cursorColor)
            .themeOption("cursorWidth", Float.class, TextField::cursorWidth)
            .themeOption("outlineWidth", Float.class, TextField::outlineWidth)
            .themeOption("cornerRadius", Corners.class, TextField::cornerRadius)
            .themeOption("innerPadding", Padding.class, TextField::innerPadding)
            .themeOption("passwordChar", Character.class, TextField::passwordChar)
            .themeOption("wordBoundaries", String.class, TextField::wordBoundaries)
            .themeOption("disabledTextColor", Color.class, TextField::disabledTextColor)
            .themeOption("disabledBackgroundColor", Color.class, TextField::disabledBackgroundColor)
            .themeOption("disabledOutlineColor", Color.class, TextField::disabledOutlineColor)
            .themeOption("cursorAnimationConfig", AnimationConfig.class, TextField::cursorAnimationConfig)
            .themeOption("focusAnimationConfig", AnimationConfig.class, TextField::focusAnimationConfig)
            .build();

    @Override
    public TextField parse(final XmlParserContext context, final Node node) {
        return this.parser.parse(context, node);
    }

}
