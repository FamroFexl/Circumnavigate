#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec2 texCoord0;

//Circumnavigate curvature shader imports
#moj_import <curvature.glsl>
uniform ivec2 DimensionBounds;

void main() {
    //Circumnavigate curvature shader modifications
    vec3 pos = Position;
    pos.y -= curve_dimension(DimensionBounds, pos.xz);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    vertexColor = Color * lightMapColor;

    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
}
