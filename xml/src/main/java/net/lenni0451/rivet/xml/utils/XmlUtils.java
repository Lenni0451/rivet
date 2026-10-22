package net.lenni0451.rivet.xml.utils;

import lombok.experimental.UtilityClass;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class XmlUtils {

    public static Document parseDocument(final InputStream stream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(stream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse XML document", e);
        }
    }

    public static Document parseDocument(final String xml) {
        return parseDocument(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public static List<Element> childElements(final Element parent) {
        NodeList children = parent.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    public static boolean isTemplateParameter(final String value) {
        return value != null && value.startsWith("@{") && value.endsWith("}");
    }

    public static String extractTemplateParameterName(final String value) {
        return value.substring(2, value.length() - 1);
    }

    public static boolean isVariableReference(final String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    public static String extractVariableReferenceName(final String value) {
        return value.substring(2, value.length() - 1);
    }

    public static boolean isConstantReference(final String value) {
        return value != null && value.startsWith("#{") && value.endsWith("}");
    }

    public static String extractConstantReferenceName(final String value) {
        return value.substring(2, value.length() - 1);
    }

    public static String unescapeJavaString(final String s) {
        if (s == null || s.indexOf('\\') == -1) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'u' -> {
                        if (i + 4 >= s.length()) {
                            throw new IllegalArgumentException("Incomplete unicode escape in string: " + s);
                        }
                        String hex = s.substring(i + 1, i + 5);
                        try {
                            int codePoint = Integer.parseInt(hex, 16);
                            sb.append((char) codePoint);
                            i += 4;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid unicode escape '\\u" + hex + "' in string: " + s, e);
                        }
                    }
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case '\\' -> sb.append('\\');
                    case '\'' -> sb.append('\'');
                    case '\"' -> sb.append('\"');
                    default -> {
                        sb.append('\\');
                        sb.append(next);
                    }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String parseCodepoint(final String codepointStr) {
        if (codepointStr == null || codepointStr.isBlank()) {
            throw new IllegalArgumentException("Codepoint string cannot be null or empty");
        }
        String trimmed = codepointStr.trim();
        int radix = 10;
        String raw = trimmed;
        if (raw.startsWith("0x") || raw.startsWith("0X")) {
            raw = raw.substring(2);
            radix = 16;
        } else if (raw.startsWith("U+") || raw.startsWith("u+")) {
            raw = raw.substring(2);
            radix = 16;
        } else {
            boolean hasHexChar = false;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                    hasHexChar = true;
                    break;
                }
            }
            if (hasHexChar) {
                radix = 16;
            }
        }
        try {
            int codePoint = Integer.parseInt(raw, radix);
            if (!Character.isValidCodePoint(codePoint)) {
                throw new IllegalArgumentException("Invalid Unicode codepoint: " + codepointStr);
            }
            return Character.toString(codePoint);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid codepoint number: " + codepointStr, e);
        }
    }

    public static String parseHexCodepoint(final String hexStr) {
        if (hexStr == null || hexStr.isBlank()) {
            throw new IllegalArgumentException("Hex string cannot be null or empty");
        }
        String raw = hexStr.trim();
        if (raw.startsWith("0x") || raw.startsWith("0X") || raw.startsWith("U+") || raw.startsWith("u+")) {
            raw = raw.substring(2);
        }
        try {
            int codePoint = Integer.parseInt(raw, 16);
            if (!Character.isValidCodePoint(codePoint)) {
                throw new IllegalArgumentException("Invalid Unicode codepoint: " + hexStr);
            }
            return Character.toString(codePoint);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex codepoint: " + hexStr, e);
        }
    }

    public static String resolvePlaceholders(final String value, final String prefix, final String suffix, final Function<String, String> lookup) {
        if (value == null || !value.contains(prefix)) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        int i = 0;
        int len = value.length();
        int prefixLen = prefix.length();
        int suffixLen = suffix.length();

        while (i < len) {
            if (i > 0 && value.charAt(i - 1) == '\\' && value.startsWith(prefix, i)) {
                // Escaped placeholder, e.g. \#{name} -> remove the backslash and output #{name}
                sb.deleteCharAt(sb.length() - 1);
                int end = value.indexOf(suffix, i + prefixLen);
                if (end != -1) {
                    sb.append(value, i, end + suffixLen);
                    i = end + suffixLen;
                } else {
                    sb.append(value, i, len);
                    i = len;
                }
                continue;
            }

            if (value.startsWith(prefix, i)) {
                int end = value.indexOf(suffix, i + prefixLen);
                if (end == -1) {
                    throw new IllegalArgumentException("Unclosed placeholder in string: " + value);
                }
                String token = value.substring(i + prefixLen, end);
                String replacement = lookup.apply(token);
                if (replacement == null) {
                    throw new IllegalArgumentException("Unknown constant: " + token);
                }
                sb.append(replacement);
                i = end + suffixLen;
            } else {
                sb.append(value.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    public static void substituteConstants(final Node rootNode, final Map<String, String> constants) {
        Map<String, String> safeConstants = constants == null ? Map.of() : constants;
        substituteConstantsNode(rootNode, safeConstants);
    }

    private static void substituteConstantsNode(final Node node, final Map<String, String> constants) {
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String val = attr.getNodeValue();
                if (val != null && val.contains("#{")) {
                    attr.setNodeValue(resolvePlaceholders(val, "#{", "}", constants::get));
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            short childType = child.getNodeType();
            if (childType == Node.TEXT_NODE || childType == Node.CDATA_SECTION_NODE) {
                String val = child.getNodeValue();
                if (val != null && val.contains("#{")) {
                    child.setNodeValue(resolvePlaceholders(val, "#{", "}", constants::get));
                }
            } else if (childType == Node.ELEMENT_NODE) {
                substituteConstantsNode(child, constants);
            }
        }
    }

}
