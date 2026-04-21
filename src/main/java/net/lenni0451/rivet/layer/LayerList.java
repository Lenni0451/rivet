package net.lenni0451.rivet.layer;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.component.Container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Accessors(fluent = true)
public class LayerList {

    @Getter
    private final Layer baseLayer;
    private final List<Layer> overlays = new ArrayList<>();
    private List<Layer> allLayers = new ArrayList<>();

    public LayerList(final Container baseContainer) {
        this.baseLayer = new Layer(baseContainer, Layer.BASE_LAYER);
        this.updateAllLayers();
    }

    public List<Layer> get() {
        return Collections.unmodifiableList(this.allLayers);
    }

    public void add(final Layer layer) {
        if (layer.priority() == Layer.BASE_LAYER) {
            throw new IllegalArgumentException("Cannot add a layer with base layer priority (" + Layer.BASE_LAYER + "), base layer is already set");
        }
        this.overlays.add(layer);
        this.overlays.sort(Comparator.comparingInt(Layer::priority));
        this.updateAllLayers();
    }

    public boolean remove(final Layer layer) {
        if (this.overlays.remove(layer)) {
            this.updateAllLayers();
            return true;
        }
        return false;
    }

    private void updateAllLayers() {
        List<Layer> newAllLayers = new ArrayList<>(this.overlays.size() + 1);
        newAllLayers.add(this.baseLayer);
        newAllLayers.addAll(this.overlays);
        this.allLayers = newAllLayers;
    }

}
