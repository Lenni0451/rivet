package net.lenni0451.rivet.backend.teavm.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.lenni0451.rivet.backend.TextInput;
import net.lenni0451.rivet.backend.teavm.utils.JsUtils;
import net.lenni0451.rivet.math.Rectangle;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLTextAreaElement;

import java.util.function.Function;

/**
 * Text input using a hidden textarea.<br>
 * Focusing a real text element is required for IME composition and on-screen keyboards on mobile devices.
 */
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true, makeFinal = true)
public class TeaVMTextInput implements TextInput {

    private final HTMLElement canvas;
    @Getter
    private final HTMLTextAreaElement textArea;
    private final Function<Rectangle, Rectangle> toCssRect;
    @Getter
    private boolean active;

    @Override
    public void start() {
        this.active = true;
        this.textArea.setValue("");
        JsUtils.focus(this.textArea);
    }

    @Override
    public void stop() {
        this.active = false;
        this.textArea.setValue("");
        JsUtils.focus(this.canvas);
    }

    @Override
    public void area(final Rectangle rectangle) {
        Rectangle css = this.toCssRect.apply(rectangle);
        CSSStyleDeclaration style = this.textArea.getStyle();
        style.setProperty("left", css.x() + "px");
        style.setProperty("top", css.y() + "px");
        style.setProperty("height", Math.max(1, css.height()) + "px");
    }

}
