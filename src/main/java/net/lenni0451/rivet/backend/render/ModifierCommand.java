package net.lenni0451.rivet.backend.render;

public sealed interface ModifierCommand permits
        ModifierCommand.Translate, ModifierCommand.ComponentBounds, ModifierCommand.Scissor, ModifierCommand.Scale, ModifierCommand.Custom,
        ModifierCommand.Stencil {

    record Translate(float x, float y) implements ModifierCommand {
    }

    record ComponentBounds(float x, float y, float width, float height) implements ModifierCommand {
    }

    record Scissor(float x, float y, float width, float height) implements ModifierCommand {
    }

    record Scale(float x, float y) implements ModifierCommand {
    }

    record Stencil(RenderList mask) implements ModifierCommand {
    }

    non-sealed interface Custom extends ModifierCommand {
    }

}
