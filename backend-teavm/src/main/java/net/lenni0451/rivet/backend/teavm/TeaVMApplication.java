package net.lenni0451.rivet.backend.teavm;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.WithBy;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Application;
import net.lenni0451.rivet.backend.render.deferred.DeferredRenderer;
import net.lenni0451.rivet.backend.render.deferred.RenderList;
import net.lenni0451.rivet.backend.teavm.input.DomInputMapper;
import net.lenni0451.rivet.backend.teavm.input.TeaVMTextInput;
import net.lenni0451.rivet.backend.teavm.render.WebGLRenderer;
import net.lenni0451.rivet.backend.teavm.utils.JsUtils;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.Key;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.keyboard.ModifierKey;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.math.Size;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.webgl.WebGL2RenderingContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Runs a Rivet UI in the browser using WebGL 2.<br>
 * The application is single threaded: input events, layout and rendering all happen on the browser's main thread.
 * {@link #run()} returns as soon as the application was launched, the render loop is driven by {@code requestAnimationFrame}.
 */
@Getter
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public abstract class TeaVMApplication implements Application<TeaVMBackend> {

    private static final float WHEEL_PIXELS_PER_LINE = 100;
    private static final float WHEEL_LINES_PER_STEP = 3;

    private final Configuration configuration;
    private final Set<MouseButton> heldMouseButtons = EnumSet.noneOf(MouseButton.class);
    @Getter(AccessLevel.NONE)
    private final List<Runnable> eventListeners = new ArrayList<>();
    private TeaVMBackend backend;
    private Rivet rivet;
    private HTMLCanvasElement canvas;
    @Getter(AccessLevel.NONE)
    private boolean canvasCreated;
    private HTMLTextAreaElement textArea;
    private TeaVMTextInput textInput;
    private WebGL2RenderingContext gl;
    private WebGLRenderer renderer;
    private int pixelWidth;
    private int pixelHeight;
    private double pixelRatio;
    private int animationFrame = -1;
    private boolean running;
    @Nullable
    private KeyEvent pendingPaste;

    public TeaVMApplication() {
        this(Configuration.DEFAULT);
    }

    /**
     * Launch the application.<br>
     * This has to be called from a TeaVM thread (e.g. the {@code main} method) since loading assets blocks until they are loaded.
     */
    @Override
    public void run() {
        if (this.running) throw new IllegalStateException("The application is already running");
        this.launch();
    }

    @Override
    public void runAsync(final boolean waitForLaunch) {
        this.run();
    }

    @Override
    public void close(final boolean waitForClose) {
        if (!this.running) return;
        this.running = false;
        if (this.animationFrame >= 0) {
            Window.cancelAnimationFrame(this.animationFrame);
            this.animationFrame = -1;
        }
        // Without removing the listeners a relaunched application would receive every event twice
        for (Runnable eventListener : this.eventListeners) {
            eventListener.run();
        }
        this.eventListeners.clear();
        this.heldMouseButtons.clear();
        this.pendingPaste = null;
        this.rivet.dispose();
        this.renderer.close();
        this.textArea.delete();
        if (this.canvasCreated) {
            this.canvas.delete();
            this.canvasCreated = false;
        }
    }

    @SneakyThrows
    protected void launch() {
        this.canvas = this.initCanvas();
        this.gl = JsUtils.getWebGL2Context(this.canvas);
        if (this.gl == null) throw new IllegalStateException("WebGL 2 is not supported by this browser");
        this.renderer = new WebGLRenderer(this.gl);
        this.textArea = this.initTextArea();
        this.textInput = new TeaVMTextInput(this.canvas, this.textArea, this::toCssRect);

        TeaVMAssetLoader assetLoader = new TeaVMAssetLoader(this.gl);
        this.backend = new TeaVMBackend(this.createFont(assetLoader), assetLoader, this.textInput);
        this.updateCanvasSize();
        this.rivet = new Rivet(this.backend, FullSizeLayout.INSTANCE, new Size(this.pixelWidth, this.pixelHeight));
        this.rivet.scale().automaticScale((float) this.pixelRatio);
        this.initEvents();
        this.init(this.rivet);

        this.running = true;
        this.animationFrame = Window.requestAnimationFrame(this::frame);
    }

    protected HTMLCanvasElement initCanvas() {
        HTMLDocument document = HTMLDocument.current();
        HTMLCanvasElement canvas = null;
        if (this.configuration.canvasId() != null) {
            canvas = (HTMLCanvasElement) document.getElementById(this.configuration.canvasId());
            if (canvas == null) throw new IllegalStateException("Canvas with id '" + this.configuration.canvasId() + "' not found");
        }
        if (canvas == null) {
            canvas = (HTMLCanvasElement) document.createElement("canvas");
            CSSStyleDeclaration bodyStyle = document.getBody().getStyle();
            bodyStyle.setProperty("margin", "0");
            bodyStyle.setProperty("overflow", "hidden");
            CSSStyleDeclaration canvasStyle = canvas.getStyle();
            canvasStyle.setProperty("position", "fixed");
            canvasStyle.setProperty("left", "0");
            canvasStyle.setProperty("top", "0");
            canvasStyle.setProperty("width", "100%");
            canvasStyle.setProperty("height", "100%");
            document.getBody().appendChild(canvas);
            this.canvasCreated = true;
        }
        CSSStyleDeclaration style = canvas.getStyle();
        style.setProperty("display", "block");
        style.setProperty("outline", "none");
        style.setProperty("touch-action", "none");
        canvas.setAttribute("tabindex", "0");
        return canvas;
    }

    protected HTMLTextAreaElement initTextArea() {
        HTMLDocument document = HTMLDocument.current();
        HTMLTextAreaElement textArea = (HTMLTextAreaElement) document.createElement("textarea");
        textArea.setAttribute("autocomplete", "off");
        textArea.setAttribute("autocorrect", "off");
        textArea.setAttribute("autocapitalize", "off");
        textArea.setAttribute("spellcheck", "false");
        textArea.setAttribute("aria-hidden", "true");
        CSSStyleDeclaration style = textArea.getStyle();
        style.setProperty("position", "fixed");
        style.setProperty("left", "0");
        style.setProperty("top", "0");
        style.setProperty("width", "1px");
        style.setProperty("height", "1px");
        style.setProperty("opacity", "0");
        style.setProperty("padding", "0");
        style.setProperty("border", "0");
        style.setProperty("resize", "none");
        style.setProperty("pointer-events", "none");
        style.setProperty("font-size", "16px"); // Prevents zooming on iOS
        document.getBody().appendChild(textArea);
        return textArea;
    }

    protected void renderFrame() {
        RenderList renderList = this.rivet.render(new DeferredRenderer()).complete();
        this.renderer.render(renderList, this.pixelWidth, this.pixelHeight, this.configuration.backgroundColor());
    }

    private void frame(final double timestamp) {
        if (!this.running) return;
        this.animationFrame = Window.requestAnimationFrame(this::frame);
        try {
            if (this.updateCanvasSize()) {
                this.rivet.size(new Size(this.pixelWidth, this.pixelHeight));
                this.rivet.scale().automaticScale((float) this.pixelRatio);
            }
            this.renderFrame();
        } catch (Throwable t) {
            System.err.println("Error while rendering Rivet frame: " + t);
            t.printStackTrace();
        }
    }

    private boolean updateCanvasSize() {
        double pixelRatio = Window.current().getDevicePixelRatio();
        if (pixelRatio <= 0 || Double.isNaN(pixelRatio)) pixelRatio = 1;
        int width = Math.max(1, (int) Math.round(this.canvas.getClientWidth() * pixelRatio));
        int height = Math.max(1, (int) Math.round(this.canvas.getClientHeight() * pixelRatio));
        if (width == this.pixelWidth && height == this.pixelHeight && pixelRatio == this.pixelRatio) return false;
        this.pixelWidth = width;
        this.pixelHeight = height;
        this.pixelRatio = pixelRatio;
        this.canvas.setWidth(width);
        this.canvas.setHeight(height);
        return true;
    }

    private Rectangle toCssRect(final Rectangle rectangle) {
        float scale = this.rivet.scale().scaleFactor() / (float) this.pixelRatio;
        TextRectangle bounds = this.canvas.getBoundingClientRect();
        return new Rectangle(
                bounds.getLeft() + rectangle.x() * scale, bounds.getTop() + rectangle.y() * scale,
                rectangle.width() * scale, rectangle.height() * scale
        );
    }

    private float pointerX(final MouseEvent event) {
        return (float) (event.getOffsetX() * this.pixelRatio);
    }

    private float pointerY(final MouseEvent event) {
        return (float) (event.getOffsetY() * this.pixelRatio);
    }

    private void initEvents() {
        HTMLElement canvas = this.canvas;
        this.listen(canvas, "contextmenu", Event::preventDefault);
        this.listen(canvas, "pointerdown", (JsUtils.PointerEvent event) -> {
            event.preventDefault(); // Prevents the browser from moving the focus
            JsUtils.setPointerCapture(canvas, event.getPointerId());
            this.onPointerDown(event);
            JsUtils.focus(this.textInput.active() ? this.textArea : canvas);
        });
        this.listen(canvas, "pointerup", this::onPointerUp);
        this.listen(canvas, "pointermove", (MouseEvent event) -> {
            MouseButton button = DomInputMapper.mapMouseButton(event.getButton());
            if (button == null) {
                this.rivet.onMouseMove(new MouseMoveEvent(this.pointerX(event), this.pointerY(event), this.heldMouseButtons));
            } else if ((event.getButtons() & DomInputMapper.buttonMask(button)) != 0) {
                // pointerdown/pointerup are only fired for the first/last button, other button changes are reported by pointermove
                this.onPointerDown(event);
                JsUtils.focus(this.textInput.active() ? this.textArea : canvas);
            } else {
                this.onPointerUp(event);
            }
        });
        this.listen(canvas, "pointercancel", event -> {
            // The browser took over the pointer (e.g. for a touch gesture), there will be no pointerup event
            this.heldMouseButtons.clear();
            this.rivet.unfocus();
        });
        this.listen(canvas, "wheel", (WheelEvent event) -> {
            event.preventDefault();
            float scale = switch (event.getDeltaMode()) {
                case 0 -> 1 / WHEEL_PIXELS_PER_LINE; // Pixels
                case 1 -> 1 / WHEEL_LINES_PER_STEP; // Lines
                default -> 1; // Pages
            };
            this.rivet.onMouseScroll(new MouseScrollEvent(this.pointerX(event), this.pointerY(event),
                    (float) -event.getDeltaX() * scale, (float) -event.getDeltaY() * scale));
        });

        this.listen(canvas, "keydown", (KeyboardEvent event) -> this.onKeyDown(event, false));
        this.listen(canvas, "keyup", (KeyboardEvent event) -> this.onKeyUp(event));
        this.listen(this.textArea, "keydown", (KeyboardEvent event) -> this.onKeyDown(event, true));
        this.listen(this.textArea, "keyup", (KeyboardEvent event) -> this.onKeyUp(event));
        this.listen(this.textArea, "input", (JsUtils.CompositionInputEvent event) -> {
            if (!event.isComposing()) this.flushTextArea();
        });
        this.listen(this.textArea, "compositionend", event -> this.flushTextArea());
        this.listen(HTMLDocument.current(), "paste", event -> {
            KeyEvent pasteEvent = this.pendingPaste;
            this.pendingPaste = null;
            HTMLElement activeElement = HTMLDocument.current().getActiveElement();
            if (activeElement != this.canvas && activeElement != this.textArea) return; // The paste is meant for another element of the page
            String text = JsUtils.clipboardText(event);
            if (text == null) return;
            event.preventDefault();
            this.backend.clipboard(text);
            this.rivet.onKeyDown(pasteEvent != null ? pasteEvent : new KeyEvent(Key.V, Set.of(ModifierKey.CONTROL)));
        });
        this.listen(Window.current(), "blur", event -> {
            this.backend.heldKeys().clear();
            this.heldMouseButtons.clear();
            this.pendingPaste = null;
            this.rivet.unfocus();
        });
    }

    private <E extends Event> void listen(final EventTarget target, final String type, final EventListener<E> listener) {
        this.eventListeners.add(JsUtils.listen(target, type, listener));
    }

    private void onPointerDown(final MouseEvent event) {
        MouseButton button = DomInputMapper.mapMouseButton(event.getButton());
        if (button == null) return;
        this.heldMouseButtons.add(button);
        this.rivet.onMouseDown(new MouseButtonEvent(this.pointerX(event), this.pointerY(event), button, DomInputMapper.mapModifiers(event), this.heldMouseButtons));
    }

    private void onPointerUp(final MouseEvent event) {
        MouseButton button = DomInputMapper.mapMouseButton(event.getButton());
        if (button == null || !this.heldMouseButtons.contains(button)) return;
        this.rivet.onMouseUp(new MouseButtonEvent(this.pointerX(event), this.pointerY(event), button, DomInputMapper.mapModifiers(event), this.heldMouseButtons));
        this.heldMouseButtons.remove(button);
    }

    private void onKeyDown(final KeyboardEvent event, final boolean fromTextArea) {
        if (DomInputMapper.isComposing(event)) return; // The key is consumed by the IME
        Key key = DomInputMapper.mapKey(event);
        Set<ModifierKey> modifiers = DomInputMapper.mapModifiers(event);
        boolean shortcut = (event.isCtrlKey() && !event.isAltKey()) || event.isMetaKey();
        String typed = event.getKey();
        boolean printable = !shortcut && typed != null && typed.codePointCount(0, typed.length()) == 1 && typed.codePointAt(0) >= 0x20;

        if (key != null) {
            this.backend.heldKeys().add(key);
            if (shortcut && key == Key.V) {
                // The clipboard can only be read in the paste event, which is fired after this event
                Set<ModifierKey> pasteModifiers = EnumSet.noneOf(ModifierKey.class);
                pasteModifiers.addAll(modifiers);
                pasteModifiers.add(ModifierKey.CONTROL);
                this.pendingPaste = new KeyEvent(key, pasteModifiers);
                return;
            }
            boolean handled = this.rivet.onKeyDown(new KeyEvent(key, modifiers));
            // Printable keys in the textarea must not be cancelled, otherwise no input event is fired
            if ((handled || DomInputMapper.isNavigationKey(key)) && !(fromTextArea && printable)) {
                event.preventDefault();
            }
        }
        if (printable && !fromTextArea) {
            // Without an active text input there is no input event, so characters are taken directly from the key event
            this.rivet.onCharTyped(new CharEvent(typed.codePointAt(0)));
            event.preventDefault();
        }
    }

    private void onKeyUp(final KeyboardEvent event) {
        if (DomInputMapper.isComposing(event)) return;
        Key key = DomInputMapper.mapKey(event);
        if (key == null) return;
        this.backend.heldKeys().remove(key);
        if (this.rivet.onKeyUp(new KeyEvent(key, DomInputMapper.mapModifiers(event)))) {
            event.preventDefault();
        }
    }

    private void flushTextArea() {
        String text = this.textArea.getValue();
        if (text == null || text.isEmpty()) return;
        this.textArea.setValue("");
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            if (codePoint >= 0x20) {
                this.rivet.onCharTyped(new CharEvent(codePoint));
            }
            i += Character.charCount(codePoint);
        }
    }


    @With
    @WithBy
    public record Configuration(@Nullable String canvasId, Color backgroundColor) {
        public static final Configuration DEFAULT = new Configuration(null, Color.GRAY.darker().darker().darker().darker());
    }

}
