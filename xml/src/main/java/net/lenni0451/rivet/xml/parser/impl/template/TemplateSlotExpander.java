package net.lenni0451.rivet.xml.parser.impl.template;

import lombok.RequiredArgsConstructor;
import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.xml.parser.XmlParserContext;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;

@RequiredArgsConstructor
public class TemplateSlotExpander {

    private final String templateName;
    private final Element templateRootElement;
    private final XmlParserContext callerContext;

    public Element expand(final Element callerElement) {
        Map<String, List<Element>> suppliedSlots = this.collectSuppliedSlots(callerElement);

        Element templateClone = (Element) this.templateRootElement.cloneNode(true);
        Set<String> declaredSlots = new HashSet<>();
        this.processSlots(templateClone, suppliedSlots, declaredSlots);

        for (String suppliedName : suppliedSlots.keySet()) {
            if (!declaredSlots.contains(suppliedName)) {
                throw new IllegalArgumentException("Unknown slot '" + suppliedName + "' supplied for component <" + this.templateName + ">");
            }
        }
        return templateClone;
    }

    private Map<String, List<Element>> collectSuppliedSlots(final Element callerElement) {
        Map<String, List<Element>> suppliedSlots = new LinkedHashMap<>();
        List<Element> defaultSlotElements = new ArrayList<>();
        boolean explicitDefaultSlot = false;

        for (Element child : XmlUtils.childElements(callerElement)) {
            String tagName = child.getNodeName();
            if ("Slot".equals(tagName)) {
                this.validateSlotAttributes(child);
                String slotName = child.hasAttribute("name") ? child.getAttribute("name") : "default";
                if (slotName.isEmpty()) {
                    throw new IllegalArgumentException("Slot name cannot be empty on component <" + this.templateName + ">");
                }
                if ("default".equals(slotName)) {
                    if (explicitDefaultSlot || !defaultSlotElements.isEmpty()) {
                        throw new IllegalArgumentException("Duplicate slot supplied: default for component <" + this.templateName + ">");
                    }
                    explicitDefaultSlot = true;
                } else if (suppliedSlots.containsKey(slotName)) {
                    throw new IllegalArgumentException("Duplicate slot supplied: " + slotName + " for component <" + this.templateName + ">");
                }
                suppliedSlots.put(slotName, XmlUtils.childElements(child));
            } else {
                Class<?> type = this.callerContext.registry().getTypeByTag(tagName);
                if (type != null && Component.class.isAssignableFrom(type)) {
                    if (explicitDefaultSlot) {
                        throw new IllegalArgumentException("Duplicate slot supplied: default for component <" + this.templateName + ">");
                    }
                    defaultSlotElements.add(child);
                }
            }
        }

        if (!defaultSlotElements.isEmpty()) {
            suppliedSlots.put("default", defaultSlotElements);
        }
        return suppliedSlots;
    }

    private void processSlots(final Element parent, final Map<String, List<Element>> suppliedSlots, final Set<String> declaredSlots) {
        List<Element> children = XmlUtils.childElements(parent);
        for (Element child : children) {
            if ("Slot".equals(child.getNodeName())) {
                Node slotParent = child.getParentNode();
                boolean isArgumentToTemplate = slotParent instanceof Element parentElem
                        && this.callerContext.registry().getParserByTag(parentElem.getNodeName()) instanceof TemplateComponentParser;
                if (!isArgumentToTemplate) {
                    this.replaceSlot(child, suppliedSlots, declaredSlots);
                    continue;
                }
            }
            this.processSlots(child, suppliedSlots, declaredSlots);
        }
    }

    private void replaceSlot(final Element slotElement, final Map<String, List<Element>> suppliedSlots, final Set<String> declaredSlots) {
        this.validateSlotAttributes(slotElement);
        String slotName = slotElement.hasAttribute("name") ? slotElement.getAttribute("name") : "default";
        if (slotName.isEmpty()) {
            throw new IllegalArgumentException("Slot name cannot be empty in template <" + this.templateName + ">");
        }
        if (!declaredSlots.add(slotName)) {
            throw new IllegalArgumentException("Duplicate slot declared in template <" + this.templateName + ">: " + slotName);
        }

        boolean isSupplied = suppliedSlots.containsKey(slotName);
        List<Element> replacements;
        if (isSupplied) {
            replacements = suppliedSlots.get(slotName);
        } else {
            this.processSlots(slotElement, suppliedSlots, declaredSlots);
            replacements = XmlUtils.childElements(slotElement);
        }

        Node parentNode = slotElement.getParentNode();
        if (parentNode == null) {
            throw new IllegalStateException("<Slot> element cannot be root of template <" + this.templateName + ">");
        }
        Document doc = parentNode.getOwnerDocument();
        for (Element replacement : replacements) {
            Element imported = (Element) doc.importNode(replacement, true);
            if (isSupplied) {
                this.interpolateParameters(imported);
            }
            parentNode.insertBefore(imported, slotElement);
        }
        parentNode.removeChild(slotElement);
    }

    private void interpolateParameters(final Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            String value = attr.getNodeValue();
            if (value != null && value.contains("#{")) {
                value = this.callerContext.resolveConstants(value);
                attr.setNodeValue(value);
            }
            if (XmlUtils.isTemplateParameter(value)) {
                attr.setNodeValue(this.callerContext.resolveTemplateParameter(value));
            }
        }
        for (Element child : XmlUtils.childElements(element)) {
            this.interpolateParameters(child);
        }
    }

    private void validateSlotAttributes(final Element slotElement) {
        NamedNodeMap attributes = slotElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            if (!"name".equals(attr.getNodeName())) {
                throw new IllegalArgumentException("Unknown attribute '" + attr.getNodeName() + "' on <Slot>");
            }
        }
    }

}
