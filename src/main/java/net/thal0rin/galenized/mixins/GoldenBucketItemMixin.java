package net.thal0rin.galenized.mixins;

import com.teamabnormals.caverns_and_chasms.common.item.GoldenBucketItem;
import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.thal0rin.galenized.init.GItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(value = GoldenBucketItem.class, priority = 99999)
public abstract class GoldenBucketItemMixin {

    @Unique
    private static final Map<Fluid, Supplier<Item>> FLUID_TO_BUCKET_MAP = new HashMap<>();

    static {
        FLUID_TO_BUCKET_MAP.put(ModFluids.LUMISENE_FLUID.get(), GItems.GOLDEN_LUMISENE_BUCKET);
        FLUID_TO_BUCKET_MAP.put(Fluids.WATER, CCItems.GOLDEN_WATER_BUCKET);
        FLUID_TO_BUCKET_MAP.put(Fluids.LAVA, CCItems.GOLDEN_LAVA_BUCKET);
    }

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void grabFluid(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        cir.cancel();
        GoldenBucketItem self = (GoldenBucketItem) (Object) this;

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        int bucketLevel = tag.contains("FluidLevel") ? tag.getInt("FluidLevel") : 0;

        BlockHitResult result = galenized$calculateHitResult(level, player, self, bucketLevel);
        BlockPos pos = result.getBlockPos();
        BlockState sourceState = level.getBlockState(pos);
        Fluid fluid = sourceState.getFluidState().getType();

        if (galenized$handleFluidPickup(level, player, hand, cir, stack, tag, bucketLevel, pos, fluid)) {
            return;
        }

        if (galenized$handleFluidPlace(level, player, hand, cir, stack, tag, bucketLevel, result, pos)) {
            return;
        }

        if (!tag.contains("FluidLevel")) {
            player.setItemInHand(hand, new ItemStack(CCItems.GOLDEN_BUCKET.get()));
        }

        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
    }

    @Unique
    private BlockHitResult galenized$calculateHitResult(Level level, Player player, GoldenBucketItem self, int bucketLevel) {
        boolean isCrouching = player.isCrouching();
        boolean canPickupFluid = self.getFluid() != Fluids.EMPTY && bucketLevel >= 2 || isCrouching && self.getFluid() != Fluids.EMPTY;
        return galenized$getPlayerPOVHitResult(level, player, canPickupFluid ? net.minecraft.world.level.ClipContext.Fluid.NONE : net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY);
    }

    @Unique
    private boolean galenized$handleFluidPickup(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack stack, CompoundTag tag, int bucketLevel, BlockPos pos, Fluid fluid) {
        if (FLUID_TO_BUCKET_MAP.containsKey(fluid)) {
            if (level.mayInteract(player, pos)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);

                ItemStack newBucket = FLUID_TO_BUCKET_MAP.get(fluid).get().getDefaultInstance();
                int newFluidLevel;

                if (!tag.contains("FluidLevel")) {
                    newFluidLevel = 0;
                } else {
                    newFluidLevel = Math.min(bucketLevel + 1, 2);
                }

                newBucket.getOrCreateTag().putInt("FluidLevel", newFluidLevel);

                player.setItemInHand(hand, newBucket);

                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                cir.setReturnValue(InteractionResultHolder.sidedSuccess(newBucket, level.isClientSide()));
            } else {
                cir.setReturnValue(InteractionResultHolder.fail(stack));
            }
            return true;
        }
        return false;
    }

    @Unique
    private boolean galenized$handleFluidPlace(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack stack, CompoundTag tag, int bucketLevel, BlockHitResult result, BlockPos pos) {
        GoldenBucketItem self = (GoldenBucketItem) (Object) this;
        Fluid fluid = self.getFluid();

        if (FLUID_TO_BUCKET_MAP.containsKey(fluid) && bucketLevel > -1) {
            Direction direction = result.getDirection();
            BlockPos targetPos = pos.relative(direction);

            if (level.mayInteract(player, pos) && player.mayUseItemAt(targetPos, direction, stack)) {
                BlockState state = level.getBlockState(targetPos);

                boolean isValidSurface = false;
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = targetPos.relative(dir);
                    BlockState neighborState = level.getBlockState(neighborPos);
                    if (!neighborState.isAir()) {
                        isValidSurface = true;
                        break;
                    }
                }

                if (isValidSurface) {
                    level.setBlock(targetPos, fluid.defaultFluidState().createLegacyBlock(), 11);

                    int newFluidLevel = bucketLevel;
                    if (!player.isCreative()) {
                        newFluidLevel = bucketLevel - 1;
                        tag.putInt("FluidLevel", newFluidLevel);
                    }

                    if (newFluidLevel < 0) {
                        player.setItemInHand(hand, new ItemStack(CCItems.GOLDEN_BUCKET.get()));
                    }
                    level.playSound(null, targetPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);

                    cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
                } else {
                    cir.setReturnValue(InteractionResultHolder.fail(stack));
                }
            } else {
                cir.setReturnValue(InteractionResultHolder.fail(stack));
            }
            return true;
        }
        return false;
    }

    @Unique
    private static BlockHitResult galenized$getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluid) {
        float xRot = player.getXRot();
        float yRot = player.getYRot();
        Vec3 eyePosition = player.getEyePosition();
        float cos = Mth.cos(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float sin = Mth.sin(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-xRot * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-xRot * ((float) Math.PI / 180F));
        float f6 = sin * f4;
        float f7 = cos * f4;
        double d0 = player.getBlockReach();
        Vec3 vec31 = eyePosition.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
        return level.clip(new ClipContext(eyePosition, vec31, net.minecraft.world.level.ClipContext.Block.OUTLINE, fluid, player));
    }
}
