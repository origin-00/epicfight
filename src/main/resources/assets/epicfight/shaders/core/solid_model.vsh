#version 330

// Flat, lit vertex-colored model (datapack editor and skill previews). Since 1.21.5 shaders read
// their matrices and light directions from uniform blocks instead of loose uniforms.

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec3 Normal;

out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color) * ColorModulator;
}
