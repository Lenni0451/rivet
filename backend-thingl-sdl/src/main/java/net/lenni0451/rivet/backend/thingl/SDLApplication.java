package net.lenni0451.rivet.backend.thingl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.Rivet;
import net.lenni0451.rivet.backend.Application;
import net.lenni0451.rivet.backend.render.deferred.DeferredRenderer;
import net.lenni0451.rivet.backend.thingl.render.ThinGLRenderer;
import net.lenni0451.rivet.backend.thingl.render.batched.BatchedRenderListExecutor;
import net.lenni0451.rivet.backend.thingl.utils.SDLMapper;
import net.lenni0451.rivet.dragdrop.DragOverEvent;
import net.lenni0451.rivet.dragdrop.DropEvent;
import net.lenni0451.rivet.input.keyboard.CharEvent;
import net.lenni0451.rivet.input.keyboard.KeyEvent;
import net.lenni0451.rivet.input.mouse.MouseButton;
import net.lenni0451.rivet.input.mouse.MouseButtonEvent;
import net.lenni0451.rivet.input.mouse.MouseMoveEvent;
import net.lenni0451.rivet.input.mouse.MouseScrollEvent;
import net.lenni0451.rivet.layout.fullsize.FullSizeLayout;
import net.lenni0451.rivet.math.Size;
import net.raphimc.thingl.ThinGL;
import net.raphimc.thingl.implementation.application.SDLApplicationRunner;
import net.raphimc.thingl.implementation.util.sdl.SdlException;
import org.joml.Matrix4fStack;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(fluent = true, chain = true)
public abstract class SDLApplication extends SDLApplicationRunner implements Application<SDLBackend> {

    private final ThinGLRenderer renderer = new ThinGLRenderer();
    private SDLBackend backend;
    private Rivet rivet;
    private final Set<MouseButton> heldMouseButtons = EnumSet.noneOf(MouseButton.class);
    private final List<File> droppedFiles = new ArrayList<>();
    private float dropX;
    private float dropY;

    public SDLApplication() {
        this(new Configuration());
    }

    public SDLApplication(final Configuration configuration) {
        super(configuration);
    }

    @Override
    @SneakyThrows
    protected void init() {
        super.init();

        ThinGLAssetLoader assetLoader = new ThinGLAssetLoader();
        this.backend = new SDLBackend(this.window, this.createFont(assetLoader), assetLoader);
        this.rivet = new Rivet(this.backend, FullSizeLayout.INSTANCE, new Size(ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight()));
        ThinGL.windowInterface().addFramebufferResizeCallback((width, height) -> this.rivet.size(new Size(width, height)));

        this.updateWindowScale();
        this.init(this.rivet);
    }

    @Override
    protected void handleWindowEvent(final SDL_Event event) {
        super.handleWindowEvent(event);
        if (event.window().windowID() != this.windowId) return;
        switch (event.type()) {
            case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                SDL_MouseMotionEvent mouseMotion = event.motion();
                float[] mouseScale = this.getMouseScale();
                this.rivet.onMouseMove(new MouseMoveEvent(mouseMotion.x() * mouseScale[0], mouseMotion.y() * mouseScale[1], this.heldMouseButtons));
            }
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN -> {
                SDL_MouseButtonEvent mouseButton = event.button();
                float[] mouseScale = this.getMouseScale();
                MouseButtonEvent rivetEvent = SDLMapper.mapMouseButton(mouseButton.x() * mouseScale[0], mouseButton.y() * mouseScale[1], mouseButton.button(), SDLKeyboard.SDL_GetModState());
                if (rivetEvent != null) {
                    this.heldMouseButtons.add(rivetEvent.button());
                    this.rivet.onMouseDown(rivetEvent.withHeldButtons(this.heldMouseButtons));
                }
            }
            case SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> {
                SDL_MouseButtonEvent mouseButton = event.button();
                float[] mouseScale = this.getMouseScale();
                MouseButtonEvent rivetEvent = SDLMapper.mapMouseButton(mouseButton.x() * mouseScale[0], mouseButton.y() * mouseScale[1], mouseButton.button(), SDLKeyboard.SDL_GetModState());
                if (rivetEvent != null) {
                    this.rivet.onMouseUp(rivetEvent.withHeldButtons(this.heldMouseButtons));
                    this.heldMouseButtons.remove(rivetEvent.button());
                }
            }
            case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> {
                SDL_MouseWheelEvent mouseWheel = event.wheel();
                float[] mouseScale = this.getMouseScale();
                float x = mouseWheel.x();
                float y = mouseWheel.y();
                if (mouseWheel.direction() == SDLMouse.SDL_MOUSEWHEEL_FLIPPED) {
                    x = -x;
                    y = -y;
                }
                this.rivet.onMouseScroll(new MouseScrollEvent(mouseWheel.mouse_x() * mouseScale[0], mouseWheel.mouse_y() * mouseScale[1], x, y));
            }
            case SDLEvents.SDL_EVENT_KEY_DOWN -> {
                SDL_KeyboardEvent keyboard = event.key();
                KeyEvent rivetEvent = SDLMapper.mapKeycode(keyboard.key(), keyboard.mod());
                if (rivetEvent != null) {
                    this.rivet.onKeyDown(rivetEvent);
                }
            }
            case SDLEvents.SDL_EVENT_KEY_UP -> {
                SDL_KeyboardEvent keyboard = event.key();
                KeyEvent rivetEvent = SDLMapper.mapKeycode(keyboard.key(), keyboard.mod());
                if (rivetEvent != null) {
                    this.rivet.onKeyUp(rivetEvent);
                }
            }
            case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                SDL_TextInputEvent textInput = event.text();
                String text = textInput.textString();
                if (text != null) {
                    text.codePoints().forEach(c -> this.rivet.onCharTyped(new CharEvent(c)));
                }
            }
            case SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> {
                this.heldMouseButtons.clear();
                this.rivet.unfocus();
            }
            case SDLEvents.SDL_EVENT_WINDOW_DISPLAY_CHANGED, SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED -> {
                this.updateWindowScale();
            }
            case SDLEvents.SDL_EVENT_DROP_BEGIN -> {
                this.droppedFiles.clear();
            }
            case SDLEvents.SDL_EVENT_DROP_POSITION -> {
                SDL_DropEvent drop = event.drop();
                float[] mouseScale = this.getMouseScale();
                this.dropX = drop.x() * mouseScale[0];
                this.dropY = drop.y() * mouseScale[1];
                this.rivet.onDragOver(DragOverEvent.at(this.dropX, this.dropY, List.of()));
            }
            case SDLEvents.SDL_EVENT_DROP_FILE -> {
                SDL_DropEvent drop = event.drop();
                float[] mouseScale = this.getMouseScale();
                this.dropX = drop.x() * mouseScale[0];
                this.dropY = drop.y() * mouseScale[1];
                String path = drop.dataString();
                if (path != null) {
                    this.droppedFiles.add(new File(path));
                }
            }
            case SDLEvents.SDL_EVENT_DROP_COMPLETE -> {
                if (!this.droppedFiles.isEmpty()) {
                    this.rivet.onDrop(DropEvent.at(this.dropX, this.dropY, this.droppedFiles));
                    this.droppedFiles.clear();
                } else {
                    this.rivet.onDragLeave();
                }
            }
        }
    }

    @Override
    protected void render(final Matrix4fStack matrix4fStack) {
        ThinGL.programs().getMsaa().bindInput();
        this.renderBackground(matrix4fStack);
        //this.rivet.render(this.renderer);
        //RenderListExecutor.INSTANCE.renderList(this.renderer, this.rivet.render(new DeferredRenderer()).complete());
        BatchedRenderListExecutor.INSTANCE.renderList(this.renderer, this.rivet.render(new DeferredRenderer()).complete());
        ThinGL.programs().getMsaa().unbindInput();
        ThinGL.programs().getMsaa().renderFullscreen();
        ThinGL.programs().getMsaa().clearInput();
    }

    protected void renderBackground(final Matrix4fStack matrix4fStack) {
        ThinGL.renderer2D().filledRectangle(matrix4fStack, 0, 0, ThinGL.windowInterface().getFramebufferWidth(), ThinGL.windowInterface().getFramebufferHeight(), Color.GRAY.darker().darker().darker().darker());
    }

    private void updateWindowScale() {
        float scale = SDLVideo.SDL_GetWindowDisplayScale(this.window);
        if (scale > 0) {
            this.rivet.scale().automaticScale(scale);
        } else {
            SdlException.check(false, "Failed to get window display scale");
        }
    }

    private float[] getMouseScale() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer windowWidth = stack.mallocInt(1);
            IntBuffer windowHeight = stack.mallocInt(1);
            SdlException.check(SDLVideo.SDL_GetWindowSize(this.window, windowWidth, windowHeight), "Failed to get window size");
            float framebufferWidth = ThinGL.windowInterface().getFramebufferWidth();
            float framebufferHeight = ThinGL.windowInterface().getFramebufferHeight();
            return new float[]{framebufferWidth / windowWidth.get(), framebufferHeight / windowHeight.get()};
        }
    }

}
