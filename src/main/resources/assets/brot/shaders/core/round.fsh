#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 localPos;
flat in vec3 shape;
flat in vec3 params;

out vec4 fragColor;

float sdRoundBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    float r = min(shape.z, min(shape.x, shape.y));
    float sd = sdRoundBox(localPos, shape.xy, r);
    float aa = max(fwidth(sd), 0.0001);       // one screen pixel in GUI units
    int mode = int(params.z * 2.0 + 0.5);
    float cov;
    if (mode == 1) {                          // outline ring
        float bw = params.x * 8.0;
        float d = abs(sd + bw * 0.5) - bw * 0.5;
        cov = clamp(0.5 - d / aa, 0.0, 1.0);
    } else if (mode == 2) {                   // soft shadow
        float s = max(params.y * 32.0, 0.5);
        cov = 1.0 - smoothstep(-s * 0.25, s, sd);
        cov *= cov;
    } else {                                  // filled shape
        cov = clamp(0.5 - sd / aa, 0.0, 1.0);
    }
    vec4 c = vertexColor * ColorModulator;
    c.a *= cov;
    if (c.a <= 0.003) discard;
    fragColor = c;
}
