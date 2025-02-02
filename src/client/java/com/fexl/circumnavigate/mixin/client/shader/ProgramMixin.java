package com.fexl.circumnavigate.mixin.client.shader;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(Program.class)
public class ProgramMixin {
    @ModifyVariable(method = "compileShaderInternal", at = @At(value = "STORE"), ordinal = 2)
    private static String csi(String string, @Local(argsOnly = true) Program.Type type, @Local(argsOnly = true, ordinal = 0) String name) {
        if(true) return string;
        //Programs that should be injected into
        List<String> selected = List.of(
                "rendertype_solid",
                "rendertype_cutout",
                "rendertype_cutout_mipped",
                "rendertype_translucent",
                "rendertype_translucent_moving_block"

                );

        if (type != Program.Type.VERTEX || !selected.contains(name)) return string;

        StringBuilder header = new StringBuilder()
                //Import utilities for calculating curvature
                .append("#moj_import <curvature.glsl>").append("\n")
                //Import uniform which contains world bounds
                .append("uniform vec2 DimensionBounds;");

        StringBuilder mainTail = new StringBuilder()
                //Transform the output position to a curved dimension
                .append("vec3 position = Position;").append("\n")
                .append("\tposition.y -= curve_dimension(DimensionBounds, Position.xz);").append("\n")
                .append("\tPosition = position;");

        //Inject curvature shaders into select shaders
        final List<String> oldString = new ArrayList<>(Arrays.asList(string.split("\n")));
        List<String> newString = new ArrayList<>(oldString);

        boolean voidMain = false;
        for (int i = 0; i < oldString.size(); i++) {
            String currentLine = oldString.get(i);

            if (currentLine.contains("void main()")) {
                newString.add(i, header.toString());
                voidMain = true;
            }

            if (currentLine.contains("{") && voidMain)
                newString.add(i+2, mainTail.toString());
        }

        System.out.println(newString.stream().collect(Collectors.joining("\n")));
        return newString.stream().collect(Collectors.joining("\n"));
    }
}
