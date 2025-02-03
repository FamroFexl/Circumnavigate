#version 150

//CoordinateConstants.DISABLING_CHUNK_POS*16 as a block position for rendering
#define DISABLING_BLOCK_POS 60322112

float curve_dimension(ivec2 dimension_size, vec2 pos) {
    vec2 modPos = pos;
    //Disabled dimension curvature on x-axis
    if(dimension_size.x == DISABLING_BLOCK_POS)
        modPos.x = 0.0;

    //Disabled dimension curvature on z-axis
    if(dimension_size.y == DISABLING_BLOCK_POS)
        modPos.y = 0.0;

    return ((modPos.x * modPos.x)/(dimension_size.x) + (modPos.y * modPos.y)/(dimension_size.y));
}