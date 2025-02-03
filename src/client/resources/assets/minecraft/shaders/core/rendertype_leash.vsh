#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in ivec2 UV2;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;
uniform int FogShape;

out float vertexDistance;
flat out vec4 vertexColor;

//Circumnavigate curvature shader imports
#moj_import <curvature.glsl>
uniform ivec2 DimensionBounds;

void main() {
    //Circumnavigate curvature shader modifications
    vec3 pos = Position;
    pos.y -= curve_dimension(DimensionBounds, pos.xz);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color * ColorModulator * texelFetch(Sampler2, UV2 / 16, 0);
}
