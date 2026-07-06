package net.lenni0451.rivet.component;

import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.math.Size;
import net.lenni0451.rivet.utils.ContainerMouseHandler;

public abstract class ParentContainer extends Component implements Parent {

    protected abstract ContainerMouseHandler<?> mouseHandler();

    @Override
    protected void onComponentAdded() {
        this.children().forEach(c -> c.setRivet(this.rivet(), this));
    }

    @Override
    protected void onComponentRemoved() {
        this.children().forEach(c -> c.setRivet(null, null));
        this.mouseHandler().unsafeClear();
    }

    @Override
    protected void onComponentDisabled() {
        this.children().forEach(c -> c.disabled(true));
        this.mouseHandler().unsafeClear();
    }

    @Override
    protected void onComponentEnabled() {
        this.children().forEach(c -> c.disabled(false));
    }

    @Override
    public void onThemeChanged() {
        this.children().forEach(Component::onThemeChanged);
    }

    @Override
    protected void onComponentMouseLeave() {
        this.mouseHandler().onMouseLeave();
    }

    @Override
    protected boolean onComponentMouseDown(final MouseButtonEvent event, final Size size) {
        return this.mouseHandler().onMouseDown(this.rivet(), event, size).handled();
    }

    @Override
    protected boolean onComponentMouseUp(final MouseButtonEvent event, final Size size) {
        return this.mouseHandler().onMouseUp(this.rivet(), event, size).handled();
    }

    @Override
    protected boolean onComponentMouseMove(final MouseMoveEvent event, final Size size) {
        return this.mouseHandler().onMouseMove(event, size).handled();
    }

    @Override
    protected boolean onComponentMouseScroll(final MouseScrollEvent event, final Size size) {
        return this.mouseHandler().onMouseScroll(event, size).handled();
    }

    @Override
    protected boolean onComponentDrop(final DropEvent event, final Size size) {
        return this.mouseHandler().onDrop(event, size).handled();
    }

    @Override
    protected boolean onComponentDragOver(final DragOverEvent event, final Size size) {
        return this.mouseHandler().onDragOver(event, size).handled();
    }

    @Override
    protected void onComponentDragLeave() {
        this.mouseHandler().onDragLeave();
    }

    @Override
    public void requestLayoutRecalculation() {
        if (this.parent() != null) this.parent().requestLayoutRecalculation();
    }

}
