package net.lenni0451.rivet.component;

public interface MouseListener {

    void mouseEnter();

    void mouseLeave();

    void mouseDown(final float mouseX, final float mouseY, final int button);

    void mouseUp(final float mouseX, final float mouseY, final int button);

    void mouseMove(final float mouseX, final float mouseY);

}
