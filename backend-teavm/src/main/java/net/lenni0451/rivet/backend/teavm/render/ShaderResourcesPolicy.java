package net.lenni0451.rivet.backend.teavm.render;

import org.teavm.extension.spi.resources.ResourcesPolicy;

import java.util.Collection;

public final class ShaderResourcesPolicy implements ResourcesPolicy {

    @Override
    public String[] supplyResources(final Collection<? extends String> classNames) {
        if (!classNames.contains(Shaders.class.getName())) {
            return null;
        }
        return new String[]{Shaders.VERTEX_FILE, Shaders.FRAGMENT_FILE};
    }

}
