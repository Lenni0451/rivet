package net.lenni0451.rivet.xml.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.parser.ParserRegistry;

import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class XmlParserContext {

    private final XmlParserRegistry registry;
    private final ParserRegistry stringParserRegistry;
    private final Map<String, Object> variables;

    public Optional<Object> getVariable(final String name) {
        return Optional.ofNullable(this.variables.get(name));
    }

    public <T> Optional<T> getVariable(final String name, final Class<T> type) {
        return Optional.ofNullable(this.variables.get(name)).filter(type::isInstance).map(type::cast);
    }

}
