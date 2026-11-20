#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

out vec4 vertexColor;
out vec2 localPos;
flat out vec3 shape;   // half width, half height, radius (px)
flat out vec3 params;  // border width/8, soft/32, mode/2

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    localPos = UV0;
    shape = vec3(float(UV1.x) / 16.0, float(UV1.y) / 16.0, float(UV2.x) / 16.0);
    params = Normal;
}
