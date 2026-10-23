package net.lenni0451.rivet.text.highlight;

import net.lenni0451.rivet.text.model.TextBlock;
import net.lenni0451.rivet.text.model.TextFormat;
import net.lenni0451.rivet.text.model.TextLine;
import net.lenni0451.rivet.text.model.TextSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HighlightUtils {

    public static TextBlock spansToTextBlock(final String text, final List<HighlightSpan> spans, final TextFormat defaultFormat) {
        if (text == null || text.isEmpty()) {
            return new TextBlock(new TextLine(new TextSection("", defaultFormat)));
        }
        if (spans == null || spans.isEmpty()) {
            return SyntaxHighlighter.defaultHighlight(text, defaultFormat);
        }

        List<HighlightSpan> sortedSpans = spans.stream()
                .filter(s -> s.startOffset() < text.length() && s.endOffset() > s.startOffset())
                .map(s -> new HighlightSpan(
                        Math.max(0, s.startOffset()),
                        Math.min(text.length(), s.endOffset()),
                        s.format()
                ))
                .sorted(Comparator.comparingInt(HighlightSpan::startOffset))
                .toList();

        List<TextSection> flatSections = new ArrayList<>();
        int lastIndex = 0;
        for (HighlightSpan span : sortedSpans) {
            if (span.startOffset() < lastIndex) {
                if (span.endOffset() <= lastIndex) {
                    continue;
                }
                flatSections.add(new TextSection(text.substring(lastIndex, span.endOffset()), span.format()));
                lastIndex = span.endOffset();
                continue;
            }
            if (span.startOffset() > lastIndex) {
                flatSections.add(new TextSection(text.substring(lastIndex, span.startOffset()), defaultFormat));
            }
            flatSections.add(new TextSection(text.substring(span.startOffset(), span.endOffset()), span.format()));
            lastIndex = span.endOffset();
        }
        if (lastIndex < text.length()) {
            flatSections.add(new TextSection(text.substring(lastIndex), defaultFormat));
        }

        return sectionsToTextBlock(flatSections);
    }

    public static TextBlock sectionsToTextBlock(final List<TextSection> sections) {
        List<TextLine> lines = new ArrayList<>();
        List<TextSection> currentSections = new ArrayList<>();

        for (TextSection section : sections) {
            String content = section.text();
            if (content.contains("\n")) {
                String[] parts = content.split("\n", -1);
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        lines.add(new TextLine(currentSections));
                        currentSections = new ArrayList<>();
                    }
                    if (!parts[i].isEmpty()) {
                        currentSections.add(new TextSection(parts[i], section.format()));
                    }
                }
            } else if (!content.isEmpty()) {
                currentSections.add(section);
            }
        }
        lines.add(new TextLine(currentSections));
        return new TextBlock(lines);
    }

}
