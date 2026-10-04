package net.lenni0451.rivet.xml.parser;

import org.w3c.dom.Node;
import javax.annotation.Nullable;

public interface XmlParser<T> {

    @Nullable
    T parse(final XmlParserContext context, final Node node);

}
