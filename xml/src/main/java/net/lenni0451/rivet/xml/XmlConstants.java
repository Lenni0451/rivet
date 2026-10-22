package net.lenni0451.rivet.xml;

import lombok.experimental.UtilityClass;

@UtilityClass
public class XmlConstants {

    // Tags
    public static final String TAG_RIVET_UI = "RivetUi";
    public static final String TAG_ROOT = "Root";
    public static final String TAG_DEFINITIONS = "Definitions";
    public static final String TAG_COMPONENT = "Component";
    public static final String TAG_TEMPLATE = "Template";
    public static final String TAG_PARAMETER = "Parameter";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_IMPORT = "Import";
    public static final String TAG_INCLUDE = "Include";
    public static final String TAG_CONSTANT = "Constant";
    public static final String TAG_CONSTANTS = "Constants";
    public static final String TAG_MAP = "Map";
    public static final String TAG_ENTRY = "Entry";

    // Attributes
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_KEY = "key";
    public static final String ATTRIBUTE_VALUE = "value";
    public static final String ATTRIBUTE_CODEPOINT = "codepoint";
    public static final String ATTRIBUTE_HEX = "hex";
    public static final String ATTRIBUTE_PREFIX = "prefix";
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_SOURCE = "source";
    public static final String ATTRIBUTE_FILE = "file";
    public static final String ATTRIBUTE_REQUIRED = "required";
    public static final String ATTRIBUTE_DEFAULT = "default";
    public static final String ATTRIBUTE_MIN_SIZE = "minSize";
    public static final String ATTRIBUTE_MAX_SIZE = "maxSize";
    public static final String ATTRIBUTE_FIXED_SIZE = "fixedSize";
    public static final String ATTRIBUTE_DISABLED = "disabled";

    // Slot Defaults
    public static final String DEFAULT_SLOT_NAME = "default";

    // Delimiters and Placeholders
    public static final String CONSTANT_PREFIX = "#{";
    public static final String CONSTANT_SUFFIX = "}";
    public static final String VARIABLE_PREFIX = "${";
    public static final String VARIABLE_SUFFIX = "}";
    public static final String TEMPLATE_PARAM_PREFIX = "@{";
    public static final String TEMPLATE_PARAM_SUFFIX = "}";

    // Import Protocols
    public static final String FILE_PREFIX = "file:";
    public static final String CLASSPATH_PREFIX = "classpath:";

}
