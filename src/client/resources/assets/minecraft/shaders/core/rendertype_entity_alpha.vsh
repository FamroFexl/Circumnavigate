#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV1;
in vec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec2 texCoord2;

//Circumnavigate curvature shader imports
#moj_import <curvature.glsl>
uniform ivec2 DimensionBounds;

void main() {
    //Circumnavigate curvature shader modifications
    vec3 pos = Position;
    pos.y -= curve_dimension(DimensionBounds, pos.xz);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexColor = Color;
    texCoord0 = UV0;
    texCoord1 = UV1;
    texCoord2 = UV2;
}
