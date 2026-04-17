package net.lenni0451.rivet.backend.render;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.text.TextOrigin;

public sealed interface RenderCommand permits
        RenderCommand.FillCircle, RenderCommand.OutlineCircle, RenderCommand.FillTriangle, RenderCommand.FillRect,
        RenderCommand.OutlineRect, RenderCommand.FillRoundedRect, RenderCommand.OutlineRoundedRect, RenderCommand.Text {

    record FillCircle(float x, float y, float radius, Color color) implements RenderCommand {
    }

    record OutlineCircle(float x, float y, float radius, float outlineWidth, Color color) implements RenderCommand {
    }

    record FillTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) implements RenderCommand {
    }

    record FillRect(float x, float y, float width, float height, Color color) implements RenderCommand {
    }

    record OutlineRect(float x, float y, float width, float height, float outlineWidth, Color color) implements RenderCommand {
    }

    record FillRoundedRect(float x, float y, float width, float height, float cornerRadius, Color color) implements RenderCommand {
    }

    record OutlineRoundedRect(float x, float y, float width, float height, float cornerRadius, float outlineWidth, Color color) implements RenderCommand {
    }

    record Text(ShapedText shapedText, float x, float y, TextOrigin.Horizontal horizontalOrigin, TextOrigin.Vertical verticalOrigin) implements RenderCommand {
    }

}
