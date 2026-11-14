package net.lenni0451.rivet.backend.teavm.reflection;

import net.lenni0451.rivet.theme.Theme;
import org.teavm.extension.spi.reflection.SimpleReflectionPolicy;

public final class ThemeReflectionPolicy extends SimpleReflectionPolicy {

    private static final String HOLDER_PREFIX = Theme.class.getName() + "$";

    @Override
    protected void setup() {
        this.selectClasses(cls -> cls.name().startsWith(HOLDER_PREFIX)).reflectableFields(STATIC);
    }

}
