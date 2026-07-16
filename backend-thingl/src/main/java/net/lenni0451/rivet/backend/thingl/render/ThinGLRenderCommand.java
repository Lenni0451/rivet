package net.lenni0451.rivet.backend.thingl.render;

import net.lenni0451.rivet.backend.render.deferred.RenderCommand;
import net.lenni0451.rivet.math.Rectangle;
import org.joml.Matrix4fStack;

import java.util.function.Consumer;

public sealed interface ThinGLRenderCommand extends RenderCommand.Custom permits ThinGLRenderCommand.Custom {

    record Custom(Consumer<Matrix4fStack> renderFunction, Rectangle bounds) implements ThinGLRenderCommand {
    }

}
