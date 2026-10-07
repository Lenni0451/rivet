package net.lenni0451.rivet.xml.utils;

import lombok.experimental.UtilityClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

}
