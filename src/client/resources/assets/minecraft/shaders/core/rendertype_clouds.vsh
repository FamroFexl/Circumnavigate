#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;

//Circumnavigate curvature shader imports
#moj_import <curvature.glsl>
uniform ivec2 DimensionBounds;

void main() {
    //Circumnavigate curvature shader modifications
    vec3 pos1 = Position;
    pos1.y -= curve_dimension(DimensionBounds, pos1.xz);

    gl_Position = ProjMat * ModelViewMat * vec4(pos1, 1.0);

    vec4 pos = ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexDistance = fog_distance(pos.xyz, FogShape);
    vertexColor = Color;
}
