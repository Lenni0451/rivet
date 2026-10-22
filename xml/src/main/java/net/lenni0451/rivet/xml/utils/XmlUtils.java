package net.lenni0451.rivet.xml.utils;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.xml.XmlConstants;
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
        return value != null && value.startsWith(XmlConstants.TEMPLATE_PARAM_PREFIX) && value.endsWith(XmlConstants.TEMPLATE_PARAM_SUFFIX);
    }

    public static String extractTemplateParameterName(final String value) {
        return value.substring(XmlConstants.TEMPLATE_PARAM_PREFIX.length(), value.length() - XmlConstants.TEMPLATE_PARAM_SUFFIX.length());
    }

    public static boolean isVariableReference(final String value) {
        return value != null && value.startsWith(XmlConstants.VARIABLE_PREFIX) && value.endsWith(XmlConstants.VARIABLE_SUFFIX);
    }

    public static String extractVariableReferenceName(final String value) {
        return value.substring(XmlConstants.VARIABLE_PREFIX.length(), value.length() - XmlConstants.VARIABLE_SUFFIX.length());
    }

    public static boolean isConstantReference(final String value) {
        return value != null && value.startsWith(XmlConstants.CONSTANT_PREFIX) && value.endsWith(XmlConstants.CONSTANT_SUFFIX);
    }

    public static String extractConstantReferenceName(final String value) {
        return value.substring(XmlConstants.CONSTANT_PREFIX.length(), value.length() - XmlConstants.CONSTANT_SUFFIX.length());
    }

    public static String unescapeJavaString(final String s) {
        if (s == null || s.indexOf('\\') == -1) {
            return s;
        }
        StringBuilder out = new StringBuilder(s.length());
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
                            out.append((char) codePoint);
                            i += 4;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid unicode escape '\\u" + hex + "' in string: " + s, e);
                        }
                    }
                    case 'n' -> out.append('\n');
                    case 't' -> out.append('\t');
                    case 'r' -> out.append('\r');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case '\\' -> out.append('\\');
                    case '\'' -> out.append('\'');
                    case '\"' -> out.append('\"');
                    default -> {
                        out.append('\\');
                        out.append(next);
                    }
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String parseCodepoint(final String codepointString) {
        if (codepointString == null || codepointString.isBlank()) {
            throw new IllegalArgumentException("Codepoint string cannot be null or empty");
        }
        String trimmed = codepointString.trim();
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
                throw new IllegalArgumentException("Invalid Unicode codepoint: " + codepointString);
            }
            return Character.toString(codePoint);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid codepoint number: " + codepointString, e);
        }
    }

    public static String parseHexCodepoint(final String hexString) {
        if (hexString == null || hexString.isBlank()) {
            throw new IllegalArgumentException("Hex string cannot be null or empty");
        }
        String raw = hexString.trim();
        if (raw.startsWith("0x") || raw.startsWith("0X") || raw.startsWith("U+") || raw.startsWith("u+")) {
            raw = raw.substring(2);
        }
        try {
            int codePoint = Integer.parseInt(raw, 16);
            if (!Character.isValidCodePoint(codePoint)) {
                throw new IllegalArgumentException("Invalid Unicode codepoint: " + hexString);
            }
            return Character.toString(codePoint);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex codepoint: " + hexString, e);
        }
    }

    public static String resolvePlaceholders(final String value, final String prefix, final String suffix, final Function<String, String> lookup) {
        if (value == null || !value.contains(prefix)) {
            return value;
        }
        StringBuilder out = new StringBuilder(value.length());
        int i = 0;
        int length = value.length();
        int prefixLength = prefix.length();
        int suffixLength = suffix.length();

        while (i < length) {
            if (i > 0 && value.charAt(i - 1) == '\\' && value.startsWith(prefix, i)) {
                // Escaped placeholder, e.g. \#{name} -> remove the backslash and output #{name}
                out.deleteCharAt(out.length() - 1);
                int end = value.indexOf(suffix, i + prefixLength);
                if (end != -1) {
                    out.append(value, i, end + suffixLength);
                    i = end + suffixLength;
                } else {
                    out.append(value, i, length);
                    i = length;
                }
                continue;
            }

            if (value.startsWith(prefix, i)) {
                int end = value.indexOf(suffix, i + prefixLength);
                if (end == -1) {
                    throw new IllegalArgumentException("Unclosed placeholder in string: " + value);
                }
                String token = value.substring(i + prefixLength, end);
                String replacement = lookup.apply(token);
                if (replacement == null) {
                    throw new IllegalArgumentException("Unknown constant: " + token);
                }
                out.append(replacement);
                i = end + suffixLength;
            } else {
                out.append(value.charAt(i));
                i++;
            }
        }
        return out.toString();
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
                Node attribute = attributes.item(i);
                String value = attribute.getNodeValue();
                if (value != null && value.contains(XmlConstants.CONSTANT_PREFIX)) {
                    attribute.setNodeValue(resolvePlaceholders(value, XmlConstants.CONSTANT_PREFIX, XmlConstants.CONSTANT_SUFFIX, constants::get));
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            short childType = child.getNodeType();
            if (childType == Node.TEXT_NODE || childType == Node.CDATA_SECTION_NODE) {
                String value = child.getNodeValue();
                if (value != null && value.contains(XmlConstants.CONSTANT_PREFIX)) {
                    child.setNodeValue(resolvePlaceholders(value, XmlConstants.CONSTANT_PREFIX, XmlConstants.CONSTANT_SUFFIX, constants::get));
                }
            } else if (childType == Node.ELEMENT_NODE) {
                substituteConstantsNode(child, constants);
            }
        }
    }

}
