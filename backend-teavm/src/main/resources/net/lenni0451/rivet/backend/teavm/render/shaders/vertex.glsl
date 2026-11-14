#version 300 es
precision highp float;

layout(location = 0) in vec4 a_matrix;
layout(location = 1) in vec4 a_data;
layout(location = 2) in vec4 a_rect;
layout(location = 3) in vec4 a_extra;
layout(location = 4) in vec4 a_extra2;
layout(location = 5) in vec4 a_color0;
layout(location = 6) in vec4 a_color1;
layout(location = 7) in vec4 a_color2;
layout(location = 8) in vec4 a_color3;

uniform vec2 u_viewport;

out vec2 v_local;
flat out vec4 v_rect;
flat out vec4 v_extra;
flat out vec4 v_extra2;
flat out vec2 v_modeOutline;
flat out vec4 v_color0;
flat out vec4 v_color1;
flat out vec4 v_color2;
flat out vec4 v_color3;

void main() {
    vec2 corner = vec2(float(gl_VertexID & 1), float(gl_VertexID >> 1));
    mat2 m = mat2(a_matrix.xy, a_matrix.zw);
    float mode = a_data.z;

    // Expand anti aliased primitives by one device pixel so the smoothed edge is not cut off
    float margin = 0.0;
    if (mode == 0.0 || mode == 2.0) {
        float det = abs(m[0][0] * m[1][1] - m[1][0] * m[0][1]);
        margin = det > 0.0 ? 1.0 / sqrt(det) : 0.0;
    }
    vec2 local = a_rect.xy - margin + corner * (a_rect.zw + 2.0 * margin);
    vec2 device = m * local + a_data.xy;
    gl_Position = vec4(device / u_viewport * vec2(2.0, -2.0) + vec2(-1.0, 1.0), 0.0, 1.0);

    v_local = local;
    v_rect = a_rect;
    v_extra = a_extra;
    v_extra2 = a_extra2;
    v_modeOutline = a_data.zw;
    v_color0 = a_color0;
    v_color1 = a_color1;
    v_color2 = a_color2;
    v_color3 = a_color3;
}
