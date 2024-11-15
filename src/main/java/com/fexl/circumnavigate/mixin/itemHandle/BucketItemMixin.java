package com.fexl.circumnavigate.mixin.itemHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    // TODO fix hand not doing the place motion when placing outside of bounds
    //  fix play sound not playing (at least on client which places the bucket) when placing outside of bounds
    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getBlockPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos wrapBlockPos(BlockHitResult instance, Operation<BlockPos> original, @Local(argsOnly = true) Level level) {
        WorldTransformer transformer = level.getTransformer();
        BlockPos result = original.call(instance);
        return transformer.translateBlockToBounds(result);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos wrapRelative(BlockPos instance, Direction direction, Operation<BlockPos> original, @Local(argsOnly = true) Level level) {
        WorldTransformer transformer = level.getTransformer();
        BlockPos result = original.call(instance, direction);
        return transformer.translateBlockToBounds(result);
    }

// why it doesnt play sound no idea...
//    @WrapMethod(method = "playEmptySound")
//    private void wrapPlayEmptySound(Player player, LevelAccessor level, BlockPos pos, Operation<Void> original) {
//        int unWrappedPosX = (int) level.getTransformer().xTransformer.getDeltaBetween(level.getTransformer().xTransformer.wrapCoordToLimit(pos.getX()), player.getX());
//        int unWrappedPosZ = (int) level.getTransformer().zTransformer.getDeltaBetween(level.getTransformer().zTransformer.wrapCoordToLimit(pos.getZ()), player.getZ());
//        BlockPos unwrappedPos = new BlockPos((int) (player.getX() - unWrappedPosX), pos.getY(), (int) (player.getZ() - unWrappedPosZ));
//        original.call(player, level, unwrappedPos);
//    }
}
