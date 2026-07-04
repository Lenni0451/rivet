package net.lenni0451.rivet.backend.thingl.util;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Rectangle;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.Rectanglef;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MathUtil {

    public static Rectanglef convert(final Rectangle rectangle) {
        return new Rectanglef(rectangle.x(), rectangle.y(), rectangle.maxX(), rectangle.maxY());
    }

    public static Rectanglef transform(final Rectanglef rectangle, final Matrix4f matrix) {
        final Vector3f p1 = new Vector3f(rectangle.minX, rectangle.minY, 0F);
        final Vector3f p2 = new Vector3f(rectangle.maxX, rectangle.minY, 0F);
        final Vector3f p3 = new Vector3f(rectangle.maxX, rectangle.maxY, 0F);
        final Vector3f p4 = new Vector3f(rectangle.minX, rectangle.maxY, 0F);
        matrix.transformPosition(p1);
        matrix.transformPosition(p2);
        matrix.transformPosition(p3);
        matrix.transformPosition(p4);
        rectangle.minX = Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x));
        rectangle.minY = Math.min(Math.min(p1.y, p2.y), Math.min(p3.y, p4.y));
        rectangle.maxX = Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x));
        rectangle.maxY = Math.max(Math.max(p1.y, p2.y), Math.max(p3.y, p4.y));
        return rectangle;
    }

    public static List<Vector2f> convert(final Point[] points) {
        List<Vector2f> vectors = new ArrayList<>(points.length);
        for (Point point : points) {
            vectors.add(new Vector2f(point.x(), point.y()));
        }
        return vectors;
    }

}
