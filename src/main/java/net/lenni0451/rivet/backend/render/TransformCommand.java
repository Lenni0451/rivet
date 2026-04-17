package net.lenni0451.rivet.backend.render;

public sealed interface TransformCommand permits
        TransformCommand.Translate, TransformCommand.Scissor, TransformCommand.Scale {

    record Translate(float x, float y) implements TransformCommand {
    }

    record Scissor(float x, float y, float width, float height) implements TransformCommand {
    }

    record Scale(float x, float y) implements TransformCommand {
    }

}
