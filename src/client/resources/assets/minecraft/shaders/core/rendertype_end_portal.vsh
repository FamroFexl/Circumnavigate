#version 150

#moj_import <projection.glsl>

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 texProj0;

//Circumnavigate curvature shader imports
#moj_import <curvature.glsl>
uniform ivec2 DimensionBounds;

void main() {
    //Circumnavigate curvature shader modifications
    vec3 pos = Position;
    pos.y -= curve_dimension(DimensionBounds, pos.xz);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    texProj0 = projection_from_position(gl_Position);
}
