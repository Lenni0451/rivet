package net.lenni0451.rivet.backend.thingl.util;

import lombok.experimental.UtilityClass;
import net.lenni0451.rivet.math.Point;
import net.lenni0451.rivet.math.Transform2D;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MathUtil {

    public static Matrix4f toMatrix(final Transform2D transform) {
        return new Matrix4f(
                transform.m00(), transform.m10(), 0, 0,
                transform.m01(), transform.m11(), 0, 0,
                0, 0, 1, 0,
                transform.m02(), transform.m12(), 0, 1
        );
    }

    public static List<Vector2f> convert(final Point[] points) {
        List<Vector2f> vectors = new ArrayList<>(points.length);
        for (Point point : points) {
            vectors.add(new Vector2f(point.x(), point.y()));
        }
        return vectors;
    }

}
