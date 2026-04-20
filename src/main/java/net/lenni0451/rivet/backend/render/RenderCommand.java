package net.lenni0451.rivet.backend.render;

import net.lenni0451.commons.color.Color;
import net.lenni0451.rivet.backend.ShapedText;
import net.lenni0451.rivet.math.Rectangle;
import net.lenni0451.rivet.text.TextOrigin;

public sealed interface RenderCommand permits
        RenderCommand.FillCircle, RenderCommand.OutlineCircle, RenderCommand.FillTriangle, RenderCommand.FillRect,
        RenderCommand.OutlineRect, RenderCommand.FillRoundedRect, RenderCommand.OutlineRoundedRect, RenderCommand.Text,
        RenderCommand.Line {

    Rectangle bounds();


    record FillCircle(float x, float y, float radius, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return new Rectangle(this.x - this.radius, this.y - this.radius, this.radius * 2, this.radius * 2);
        }
    }

    record OutlineCircle(float x, float y, float radius, float outlineWidth, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return new Rectangle(this.x - this.radius, this.y - this.radius, this.radius * 2, this.radius * 2);
        }
    }

    record FillTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            float minX = Math.min(this.x1, Math.min(this.x2, this.x3));
            float minY = Math.min(this.y1, Math.min(this.y2, this.y3));
            float maxX = Math.max(this.x1, Math.max(this.x2, this.x3));
            float maxY = Math.max(this.y1, Math.max(this.y2, this.y3));
            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
    }

    record FillRect(float x, float y, float width, float height, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return new Rectangle(this.x, this.y, this.width, this.height);
        }
    }

    record OutlineRect(float x, float y, float width, float height, float outlineWidth, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return new Rectangle(this.x, this.y, this.width, this.height);
        }
    }

    record FillRoundedRect(float x, float y, float width, float height, float cornerRadius, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return new Rectangle(this.x, this.y, this.width, this.height);
        }
    }

    record OutlineRoundedRect(float x, float y, float width, float height, float cornerRadius, float outlineWidth, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return new Rectangle(this.x, this.y, this.width, this.height);
        }
    }

    record Text(ShapedText shapedText, float x, float y, TextOrigin.Horizontal horizontalOrigin, TextOrigin.Vertical verticalOrigin) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            return this.shapedText.visualSize().at(this.x, this.y);
        }
    }

    record Line(float x1, float y1, float x2, float y2, float width, Color color) implements RenderCommand {
        @Override
        public Rectangle bounds() {
            float minX = Math.min(this.x1, this.x2) - this.width / 2F;
            float minY = Math.min(this.y1, this.y2) - this.width / 2F;
            float maxX = Math.max(this.x1, this.x2) + this.width / 2F;
            float maxY = Math.max(this.y1, this.y2) + this.width / 2F;
            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
    }

}
