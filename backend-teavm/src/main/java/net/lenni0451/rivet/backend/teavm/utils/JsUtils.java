package net.lenni0451.rivet.backend.teavm.utils;

import lombok.experimental.UtilityClass;
import org.teavm.jso.JSClass;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSPromise;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.file.Blob;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.webgl.WebGL2RenderingContext;
import org.teavm.jso.webgl.WebGLContextAttributes;
import org.teavm.jso.webgl.WebGLRenderingContext;

import javax.annotation.Nullable;

@UtilityClass
public class JsUtils {

    public static WebGL2RenderingContext getWebGL2Context(final HTMLCanvasElement canvas) {
        WebGLContextAttributes attributes = WebGLContextAttributes.create();
        attributes.setAlpha(false);
        attributes.setAntialias(false);
        attributes.setDepth(false);
        attributes.setStencil(true);
        attributes.setPremultipliedAlpha(true);
        attributes.setPreserveDrawingBuffer(false);
        return (WebGL2RenderingContext) canvas.getContext("webgl2", attributes);
    }

    /**
     * Get a CPU backed 2D context. Uploading a GPU backed canvas to a WebGL texture forces an expensive GPU readback.
     */
    public static CanvasRenderingContext2D getSoftwareContext2D(final HTMLCanvasElement canvas) {
        CanvasContext2DAttributes attributes = JSObjects.create();
        attributes.setWillReadFrequently(true);
        return (CanvasRenderingContext2D) canvas.getContext("2d", attributes);
    }

    public static void bufferSubData(final WebGL2RenderingContext gl, final int target, final ArrayBufferView data, final int length) {
        ((WebGL2Ext) gl).bufferSubData(target, 0, data, 0, length);
    }

    public static void texImage2D(final WebGL2RenderingContext gl, final int target, final CanvasImageSource source) {
        ((WebGL2Ext) gl).texImage2D(target, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, source);
    }

    public static void setPointerCapture(final HTMLElement element, final int pointerId) {
        try {
            ((PointerElement) element).setPointerCapture(pointerId);
        } catch (Throwable ignored) {
        }
    }

    @Nullable
    public static String clipboardText(final Event event) {
        DataTransfer clipboardData = ((ClipboardEvent) event).getClipboardData();
        if (JSObjects.isUndefined(clipboardData) || clipboardData == null) return null;
        return clipboardData.getData("text/plain");
    }

    public static void writeClipboard(final String text) {
        try {
            NavigatorClipboard navigator = ((BrowserWindow) Window.current()).getNavigator();
            if (JSObjects.isUndefined(navigator) || navigator == null) return;
            Clipboard clipboard = navigator.getClipboard();
            if (!JSObjects.isUndefined(clipboard) && clipboard != null) {
                clipboard.writeText(text).catchError(err -> null);
            }
        } catch (Throwable ignored) {
        }
    }

    public static JSPromise<FontFace> loadFontFace(final String name, final ArrayBufferView data) {
        FontFace face = new FontFace(name, data);
        ((FontDocument) HTMLDocument.current()).getFonts().add(face);
        return face.load();
    }

    public static JSPromise<ImageBitmap> createImageBitmap(final ArrayBufferView data) {
        ImageBitmapOptions options = JSObjects.create();
        options.setPremultiplyAlpha("premultiply");
        options.setColorSpaceConversion("none");
        return ((BrowserWindow) Window.current()).createImageBitmap(new Blob(JSArray.of(data)), options);
    }

    public static JSPromise<Response> fetch(final String url) {
        return ((BrowserWindow) Window.current()).fetch(url);
    }

    public static <E extends Event> Runnable listen(final EventTarget target, final String type, final EventListener<E> listener) {
        EventListenerOptions options = JSObjects.create();
        options.setPassive(false);
        ConfigurableEventTarget configurableTarget = (ConfigurableEventTarget) target;
        configurableTarget.addEventListener(type, listener, options);
        return () -> configurableTarget.removeEventListener(type, listener);
    }

    public static void focus(final HTMLElement element) {
        try {
            FocusOptions options = JSObjects.create();
            options.setPreventScroll(true);
            ((FocusableElement) element).focus(options);
        } catch (Throwable e) {
            element.focus();
        }
    }


    public interface BrowserWindow extends JSObject {
        @Nullable
        @JSProperty
        NavigatorClipboard getNavigator();

        JSPromise<ImageBitmap> createImageBitmap(Blob blob, ImageBitmapOptions options);

        JSPromise<Response> fetch(String url);
    }

    public interface CanvasContext2DAttributes extends JSObject {
        @JSProperty
        void setWillReadFrequently(boolean willReadFrequently);
    }

    public interface WebGL2Ext extends WebGL2RenderingContext {
        void bufferSubData(int target, int dstByteOffset, ArrayBufferView srcData, int srcOffset, int length);

        void texImage2D(int target, int level, int internalformat, int format, int type, CanvasImageSource source);
    }

    public interface TexImageSource extends CanvasImageSource {
        @JSProperty
        int getWidth();

        @JSProperty
        int getHeight();
    }

    public interface ImageBitmap extends TexImageSource {
        void close();
    }

    public interface ImageBitmapOptions extends JSObject {
        @JSProperty
        void setPremultiplyAlpha(String premultiplyAlpha);

        @JSProperty
        void setColorSpaceConversion(String colorSpaceConversion);
    }

    public interface Response extends JSObject {
        @JSProperty
        boolean isOk();

        @JSProperty
        int getStatus();

        JSPromise<ArrayBuffer> arrayBuffer();
    }

    @JSClass(name = "FontFace")
    public static class FontFace implements JSObject {
        public FontFace(final String family, final ArrayBufferView source) {
        }

        public native JSPromise<FontFace> load();
    }

    public interface FontFaceSet extends JSObject {
        void add(FontFace fontFace);
    }

    public interface FontDocument extends JSObject {
        @JSProperty
        FontFaceSet getFonts();
    }

    public interface PointerEvent extends MouseEvent {
        @JSProperty
        int getPointerId();
    }

    public interface PointerElement extends JSObject {
        void setPointerCapture(int pointerId);
    }

    public interface CompositionInputEvent extends InputEvent {
        @JSProperty
        boolean isComposing();
    }

    public interface DataTransfer extends JSObject {
        String getData(String format);
    }

    public interface ClipboardEvent extends Event {
        @Nullable
        @JSProperty
        DataTransfer getClipboardData();
    }

    public interface Clipboard extends JSObject {
        JSPromise<JSObject> writeText(String text);
    }

    public interface NavigatorClipboard extends JSObject {
        @Nullable
        @JSProperty
        Clipboard getClipboard();
    }

    public interface EventListenerOptions extends JSObject {
        @JSProperty
        void setPassive(boolean passive);
    }

    public interface ConfigurableEventTarget extends JSObject {
        void addEventListener(String type, EventListener<?> listener, EventListenerOptions options);

        void removeEventListener(String type, EventListener<?> listener);
    }

    public interface FocusOptions extends JSObject {
        @JSProperty
        void setPreventScroll(boolean preventScroll);
    }

    public interface FocusableElement extends JSObject {
        void focus(FocusOptions options);
    }

}
