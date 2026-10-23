package net.lenni0451.rivet.xml.parser;

import net.lenni0451.rivet.component.Component;
import net.lenni0451.rivet.component.container.*;
import net.lenni0451.rivet.component.container.tabcontainer.TabContainer;
import net.lenni0451.rivet.component.impl.*;
import net.lenni0451.rivet.component.impl.slider.RangeSlider;
import net.lenni0451.rivet.component.impl.slider.Slider;
import net.lenni0451.rivet.component.impl.slider.SliderTicks;
import net.lenni0451.rivet.layout.absolute.AbsoluteLayout;
import net.lenni0451.rivet.layout.absolute.AbsoluteOptions;
import net.lenni0451.rivet.layout.anchor.AnchorLayout;
import net.lenni0451.rivet.layout.anchor.AnchorOptions;
import net.lenni0451.rivet.layout.border.BorderLayout;
import net.lenni0451.rivet.layout.border.BorderPosition;
import net.lenni0451.rivet.layout.dock.DockLayout;
import net.lenni0451.rivet.layout.dock.DockPosition;
import net.lenni0451.rivet.layout.flex.FlexLayout;
import net.lenni0451.rivet.layout.flex.FlexOptions;
import net.lenni0451.rivet.layout.flow.HorizontalFlowLayout;
import net.lenni0451.rivet.layout.flow.VerticalFlowLayout;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.layout.grid.GridLayout;
import net.lenni0451.rivet.layout.grid.GridOptions;
import net.lenni0451.rivet.layout.list.HorizontalListLayout;
import net.lenni0451.rivet.layout.list.VerticalListLayout;
import net.lenni0451.rivet.layout.tile.TileLayout;
import net.lenni0451.rivet.layout.tile.TileOptions;
import net.lenni0451.rivet.xml.parser.impl.component.*;
import net.lenni0451.rivet.xml.parser.impl.layout.*;
import net.lenni0451.rivet.xml.parser.impl.property.*;
import net.lenni0451.rivet.xml.utils.XmlUtils;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmlParserRegistry {

    public static XmlParserRegistry standard() {
        XmlParserRegistry registry = new XmlParserRegistry();

        // Components
        registry.register("Arrow", Arrow.class, new ArrowParser());
        registry.register("Button", Button.class, new ButtonParser());
        registry.register("Checkbox", Checkbox.class, new CheckboxParser());
        registry.register("CollapsibleContainer", CollapsibleContainer.class, new CollapsibleContainerParser());
        registry.register("ColorPicker", ColorPicker.class, new ColorPickerParser());
        registry.register("ComboBox", ComboBox.class, new ComboBoxParser());
        registry.register("Container", Container.class, new ContainerParser());
        registry.register("DecoratedContainer", DecoratedContainer.class, new DecoratedContainerParser());
        registry.register("DragNumberInput", DragNumberInput.class, new DragNumberInputParser());
        registry.register("DynamicListContainer", DynamicListContainer.class, new DynamicListContainerParser());
        registry.register("FormattedLabel", FormattedLabel.class, new FormattedLabelParser());
        registry.register("Image", Image.class, new ImageParser());
        registry.register("Label", Label.class, new LabelParser());
        registry.register("PaddedContainer", PaddedContainer.class, new PaddedContainerParser());
        registry.register("ProgressBar", ProgressBar.class, new ProgressBarParser());
        registry.register("RangeSlider", RangeSlider.class, new RangeSliderParser());
        registry.register("ReorderableContainer", ReorderableContainer.class, new ReorderableContainerParser());
        registry.register("ScrollBar", ScrollBar.class, new ScrollBarParser());
        registry.register("ScrollContainer", ScrollContainer.class, new ScrollContainerParser());
        registry.register("Separator", Separator.class, new SeparatorParser());
        registry.register("Slider", Slider.class, new SliderParser());
        registry.register("SolidColor", SolidColor.class, new SolidColorParser());
        registry.register("TabContainer", TabContainer.class, new TabContainerParser());
        registry.register("Tab", TabParser.TabDefinition.class, new TabParser());
        registry.register("TextArea", TextArea.class, new TextAreaParser());
        registry.register("TextField", TextField.class, new TextFieldParser());
        registry.register("ToggleSwitch", ToggleSwitch.class, new ToggleSwitchParser());

        // Layouts
        registry.register("AbsoluteLayout", AbsoluteLayout.class, new AbsoluteLayoutParser());
        registry.register("AnchorLayout", AnchorLayout.class, new AnchorLayoutParser());
        registry.register("BorderLayout", BorderLayout.class, new BorderLayoutParser());
        registry.register("DockLayout", DockLayout.class, new DockLayoutParser());
        registry.register("FlexLayout", FlexLayout.class, new FlexLayoutParser());
        registry.register("FullSizeLayout", FullSizeLayout.class, new FullSizeLayoutParser());
        registry.register("GridLayout", GridLayout.class, new GridLayoutParser());
        registry.register("HorizontalFlowLayout", HorizontalFlowLayout.class, new HorizontalFlowLayoutParser());
        registry.register("HorizontalListLayout", HorizontalListLayout.class, new HorizontalListLayoutParser());
        registry.register("TileLayout", TileLayout.class, new TileLayoutParser());
        registry.register("VerticalFlowLayout", VerticalFlowLayout.class, new VerticalFlowLayoutParser());
        registry.register("VerticalListLayout", VerticalListLayout.class, new VerticalListLayoutParser());

        // Properties & Layout Options
        registry.register("Capabilities", Component.Capabilities.class, new CapabilitiesParser());
        registry.register("AbsoluteOptions", AbsoluteOptions.class, new AbsoluteOptionsParser());
        registry.register("AnchorOptions", AnchorOptions.class, new AnchorOptionsParser());
        registry.register("BorderPosition", BorderPosition.class, new BorderPositionParser());
        registry.register("BorderOptions", BorderPosition.class, new BorderPositionParser());
        registry.register("DockPosition", DockPosition.class, new DockPositionParser());
        registry.register("DockOptions", DockPosition.class, new DockPositionParser());
        registry.register("FlexOptions", FlexOptions.class, new FlexOptionsParser());
        registry.register("GridOptions", GridOptions.class, new GridOptionsParser());
        registry.register("TileOptions", TileOptions.class, new TileOptionsParser());
        registry.register("SliderTicks", SliderTicks.class, new SliderTicksParser());

        return registry;
    }


    private final Map<Class<?>, XmlParser<?>> xmlParsers = new LinkedHashMap<>();
    private final Map<String, Class<?>> tagNames = new LinkedHashMap<>();
    private final Map<String, XmlParser<?>> parsersByTag = new LinkedHashMap<>();

    public XmlParserRegistry() {
    }

    public XmlParserRegistry(final XmlParserRegistry other) {
        this.xmlParsers.putAll(other.xmlParsers);
        this.tagNames.putAll(other.tagNames);
        this.parsersByTag.putAll(other.parsersByTag);
    }

    public XmlParserRegistry copy() {
        return new XmlParserRegistry(this);
    }

    public <T> XmlParserRegistry register(final Class<T> type, final XmlParser<? extends T> parser) {
        this.xmlParsers.put(XmlUtils.box(type), parser);
        return this;
    }

    public <T> XmlParserRegistry register(final String tagName, final Class<T> type, final XmlParser<? extends T> parser) {
        this.tagNames.put(tagName, XmlUtils.box(type));
        return this.register(type, parser);
    }

    public <T> XmlParserRegistry registerByTag(final String tagName, final Class<T> type, final XmlParser<? extends T> parser) {
        this.tagNames.put(tagName, XmlUtils.box(type));
        this.parsersByTag.put(tagName, parser);
        return this;
    }

    public boolean supports(final Class<?> type) {
        return this.xmlParsers.containsKey(XmlUtils.box(type));
    }

    public boolean supports(final String tagName) {
        return this.tagNames.containsKey(tagName);
    }

    public Class<?> getTypeByTag(final String tagName) {
        return this.tagNames.get(tagName);
    }

    public XmlParser<?> getParserByTag(final String tagName) {
        return this.parsersByTag.get(tagName);
    }

    public Object parseNode(final Node node, final XmlParserContext context) {
        String tagName = node.getNodeName();
        Class<?> type = this.tagNames.get(tagName);
        if (type == null) {
            throw new IllegalArgumentException("Unknown tag: " + tagName);
        }
        return this.parse(type, node, context);
    }

    public <T> T parse(final Class<T> type, final Node node, final XmlParserContext context) {
        Class<T> boxedType = XmlUtils.box(type);
        ResolvedParser resolved = this.resolveParser(boxedType, node);
        if (resolved == null) {
            throw new UnsupportedOperationException("Unsupported value type: " + type.getTypeName());
        }
        Object parsed;
        try {
            parsed = resolved.parser().parse(context, node);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to parse node as " + resolved.targetType().getTypeName(), e);
        }
        if (parsed == null) {
            throw new IllegalArgumentException("Unable to parse node as " + resolved.targetType().getTypeName());
        }
        if (!boxedType.isInstance(parsed)) {
            throw new IllegalStateException("Parser for " + resolved.targetType().getTypeName() + " returned " + parsed.getClass().getTypeName());
        }
        return boxedType.cast(parsed);
    }

    private <T> ResolvedParser resolveParser(final Class<T> type, final Node node) {
        Class<?> tagType = null;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String tagName = node.getNodeName();
            tagType = this.tagNames.get(tagName);
            XmlParser<?> tagParser = this.parsersByTag.get(tagName);
            if (tagParser != null && tagType != null && type.isAssignableFrom(tagType)) {
                return new ResolvedParser(tagParser, tagType);
            }
        }
        XmlParser<?> parser = this.xmlParsers.get(type);
        if (parser != null) {
            return new ResolvedParser(parser, type);
        }
        if (tagType != null && type.isAssignableFrom(tagType)) {
            XmlParser<?> tagTypeParser = this.xmlParsers.get(tagType);
            if (tagTypeParser != null) {
                return new ResolvedParser(tagTypeParser, tagType);
            }
        }
        return null;
    }


    private record ResolvedParser(XmlParser<?> parser, Class<?> targetType) {
    }

}
