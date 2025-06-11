package net.lenni0451.rivet.component;

public interface MouseListener {

    void onMouseEnter();

    void onMouseLeave();

    void onMouseDown(final float mouseX, final float mouseY, final int button);

    void onMouseUp(final float mouseX, final float mouseY, final int button);

    void onMouseMove(final float mouseX, final float mouseY);

}
