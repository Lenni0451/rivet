package net.lenni0451.rivet.text;

import net.lenni0451.commons.color.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextParserTest {

    @Test
    void basicFormats() {
        List<TextSection> sections = TextParser.parse("<italic bold underlined strikethrough>Test");
        assertEquals(1, sections.size());
        assertEquals("Test", sections.get(0).text());
        TextFormat format = sections.get(0).format();
        assertTrue(format.bold());
        assertTrue(format.italic());
        assertTrue(format.underlined());
        assertTrue(format.strikethrough());
    }

    @Test
    void defaultFormat() {
        TextFormat format = new TextFormat(Color.RED, Color.GREEN, true, true, true, true, true);
        List<TextSection> sections = TextParser.parse("Test", format);
        assertEquals(1, sections.size());
        assertEquals("Test", sections.get(0).text());
        assertEquals(format, sections.get(0).format());
    }

    @Test
    void disableDefaultFormat() {
        TextFormat format = new TextFormat(Color.RED, Color.GREEN, true, true, true, true, true);
        List<TextSection> sections = TextParser.parse("<italic=false bold=false>Test", format);
        assertEquals(1, sections.size());
        assertEquals("Test", sections.get(0).text());
        TextFormat sectionFormat = sections.get(0).format();
        assertFalse(sectionFormat.bold());
        assertFalse(sectionFormat.italic());
        assertTrue(sectionFormat.underlined());
        assertTrue(sectionFormat.strikethrough());
    }

    @Test
    void closeFormats() {
        List<TextSection> sections = TextParser.parse("<bold italic>Test 1</bold>Test 2");
        assertEquals(2, sections.size());
        assertEquals("Test 1", sections.get(0).text());
        TextFormat format1 = sections.get(0).format();
        assertTrue(format1.bold());
        assertTrue(format1.italic());
        assertEquals("Test 2", sections.get(1).text());
        TextFormat format2 = sections.get(1).format();
        assertFalse(format2.bold());
        assertTrue(format2.italic());
    }

    @Test
    void testAllFormats() {
        List<TextSection> sections = TextParser.parse(String.join("",
                "<color=red>Red Color</color>",
                "<outline_color=blue>Blue Outline</outline_color>",
                "<bold>Bold Text</bold>",
                "<italic>Italic Text</italic>",
                "<underlined>Underlined Text</underlined>",
                "<strikethrough>Strikethrough Text</strikethrough>"
        ));
        assertEquals(6, sections.size());
        assertEquals(Color.RED, sections.get(0).format().color());
        assertEquals(Color.BLUE, sections.get(1).format().outlineColor());
        assertTrue(sections.get(2).format().bold());
        assertTrue(sections.get(3).format().italic());
        assertTrue(sections.get(4).format().underlined());
        assertTrue(sections.get(5).format().strikethrough());
    }

    @Test
    void testAllColorFormats() {
        List<TextSection> sections = TextParser.parse(String.join("",
                "<color=#FF0000>Red RGB Hex</color>",
                "<color=#FF00FF00>Green ARGB Hex</color>",
                "<color=rgb(0,0,255)>Blue RGB</color>",
                "<color=rgba(255,255,0,255)>Yellow RGBA</color>",
                "<color=argb(255,0,255,255)>Cyan ARGB</color>",
                "<color=rgbf(1.0,0.0,0.0)>Red RGB Float</color>",
                "<color=rgbaf(0.0,1.0,0.0,1.0)>Green RGBA Float</color>",
                "<color=argbf(1.0,0.0,0.0,1.0)>Blue ARGB Float</color>",
                "<color=black>black</color>",
                "<color=light_gray>light_gray</color>",
                "<color=gray>gray</color>",
                "<color=dark_gray>dark_gray</color>",
                "<color=white>white</color>",
                "<color=red>red</color>",
                "<color=green>green</color>",
                "<color=blue>blue</color>",
                "<color=orange>orange</color>",
                "<color=yellow>yellow</color>",
                "<color=cyan>cyan</color>",
                "<color=pink>pink</color>",
                "<color=magenta>magenta</color>"
        ));
        assertEquals(21, sections.size());
        assertEquals(Color.RED, sections.get(0).format().color());
        assertEquals(Color.GREEN, sections.get(1).format().color());
        assertEquals(Color.BLUE, sections.get(2).format().color());
        assertEquals(Color.YELLOW, sections.get(3).format().color());
        assertEquals(Color.CYAN, sections.get(4).format().color());
        assertEquals(Color.RED, sections.get(5).format().color());
        assertEquals(Color.GREEN, sections.get(6).format().color());
        assertEquals(Color.BLUE, sections.get(7).format().color());
        assertEquals(Color.BLACK, sections.get(8).format().color());
        assertEquals(Color.LIGHT_GRAY, sections.get(9).format().color());
        assertEquals(Color.GRAY, sections.get(10).format().color());
        assertEquals(Color.DARK_GRAY, sections.get(11).format().color());
        assertEquals(Color.WHITE, sections.get(12).format().color());
        assertEquals(Color.RED, sections.get(13).format().color());
        assertEquals(Color.GREEN, sections.get(14).format().color());
        assertEquals(Color.BLUE, sections.get(15).format().color());
        assertEquals(Color.ORANGE, sections.get(16).format().color());
        assertEquals(Color.YELLOW, sections.get(17).format().color());
        assertEquals(Color.CYAN, sections.get(18).format().color());
        assertEquals(Color.PINK, sections.get(19).format().color());
        assertEquals(Color.MAGENTA, sections.get(20).format().color());
    }

    @Test
    void testEscape() {
        List<TextSection> sections = TextParser.parse("\\<italic>Not Italic\\</italic>");
        assertEquals(1, sections.size());
        assertEquals("<italic>Not Italic</italic>", sections.get(0).text());
        TextFormat format = sections.get(0).format();
        assertFalse(format.italic());
    }

}
