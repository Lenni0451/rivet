#version 300 es
precision highp float;

uniform sampler2D u_texture;
uniform bool u_mask;

in vec2 v_local;
flat in vec4 v_rect;
flat in vec4 v_extra;
flat in vec4 v_extra2;
flat in vec2 v_modeOutline;
flat in vec4 v_color0;
flat in vec4 v_color1;
flat in vec4 v_color2;
flat in vec4 v_color3;

out vec4 fragColor;

// Radii order: top left, bottom left, bottom right, top right (y axis points down)
float sdRoundBox(vec2 p, vec2 halfSize, vec4 radii) {
    float r = p.x > 0.0 ? (p.y > 0.0 ? radii.z : radii.w) : (p.y > 0.0 ? radii.y : radii.x);
    vec2 q = abs(p) - halfSize + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

float sdTriangle(vec2 p, vec2 p0, vec2 p1, vec2 p2) {
    vec2 e0 = p1 - p0;
    vec2 e1 = p2 - p1;
    vec2 e2 = p0 - p2;
    vec2 v0 = p - p0;
    vec2 v1 = p - p1;
    vec2 v2 = p - p2;
    vec2 pq0 = v0 - e0 * clamp(dot(v0, e0) / max(dot(e0, e0), 1e-12), 0.0, 1.0);
    vec2 pq1 = v1 - e1 * clamp(dot(v1, e1) / max(dot(e1, e1), 1e-12), 0.0, 1.0);
    vec2 pq2 = v2 - e2 * clamp(dot(v2, e2) / max(dot(e2, e2), 1e-12), 0.0, 1.0);
    float s = sign(e0.x * e2.y - e0.y * e2.x);
    vec2 d = min(min(vec2(dot(pq0, pq0), s * (v0.x * e0.y - v0.y * e0.x)),
                     vec2(dot(pq1, pq1), s * (v1.x * e1.y - v1.y * e1.x))),
                     vec2(dot(pq2, pq2), s * (v2.x * e2.y - v2.y * e2.x)));
    return -sqrt(d.x) * sign(d.y);
}

void main() {
    int mode = int(v_modeOutline.x + 0.5);
    vec2 uv = clamp((v_local - v_rect.xy) / max(v_rect.zw, vec2(1e-6)), 0.0, 1.0);
    vec4 color = mix(mix(v_color0, v_color3, uv.x), mix(v_color1, v_color2, uv.x), uv.y);
    color.rgb *= color.a;

    float alpha = 1.0;
    if (mode == 0) {
        vec2 halfSize = v_rect.zw * 0.5;
        float d = sdRoundBox(v_local - v_rect.xy - halfSize, halfSize, v_extra);
        float fw = max(fwidth(d), 1e-4);
        alpha = clamp(0.5 - d / fw, 0.0, 1.0);
        float outline = v_modeOutline.y;
        if (outline > 0.0) {
            alpha *= clamp(0.5 + (d + outline) / fw, 0.0, 1.0);
        }
    } else if (mode == 1) {
        color *= texture(u_texture, mix(v_extra.xy, v_extra.zw, uv));
    } else if (mode == 4) {
        // Hard edged rect: Coverage is fully determined by the rasterizer, which never leaves gaps between adjacent quads
        alpha = 1.0;
    } else {
        float d = sdTriangle(v_local, v_extra2.xy, v_extra2.zw, v_extra.xy);
        if (mode == 2) {
            alpha = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
        } else {
            alpha = d <= 0.0 ? 1.0 : 0.0;
        }
    }

    vec4 result = color * alpha;
    if (u_mask ? result.a < 0.5 : result.a <= 0.0) {
        discard;
    }
    fragColor = result;
}
