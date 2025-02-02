#version 150

//CoordinateConstants.DISABLING_CHUNK_POS*16 as a block position for rendering
//#define DISABLING_BLOCK_POS (30161056)

float curve_dimension(vec2 dimension_size, vec2 pos) {
    //Disabled dimension curvature on x-axis
    //if(dimension_size.x == DISABLING_BLOCK_POS) {}

    //Disabled dimension curvature on z-axis
    //if(dimension_size.y == DISABLING_BLOCK_POS) {}

    return dot(pos, pos)/256;
}