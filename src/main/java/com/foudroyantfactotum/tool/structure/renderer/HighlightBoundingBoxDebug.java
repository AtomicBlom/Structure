package com.foudroyantfactotum.tool.structure.renderer;

import com.foudroyantfactotum.tool.structure.IStructure.IStructureTE;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.block.StructureShapeBlock;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class HighlightBoundingBoxDebug {
    //Double.MIN_VALUE is not what you think it is: http://stackoverflow.com/a/3884879
    private static final AxisAlignedBB allEncompassingAxisAlignedBoundingBox = new AxisAlignedBB(
            -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE,
            Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

    @SubscribeEvent
    public void onDrawHighlight(DrawBlockHighlightEvent event) {
        final RayTraceResult target = event.getTarget();
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        final EntityPlayer player = event.getPlayer();
        final ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!heldItem.isEmpty()) {
            return;
        }

        final float partialTicks = event.getPartialTicks();
        final BlockPos targetBlockPos = target.getBlockPos();

        final World world = player.getEntityWorld();
        final IBlockState blockState = world.getBlockState(targetBlockPos);
        final Block block = blockState.getBlock();

        if (block instanceof StructureShapeBlock) {
            highlightBoundingBoxes(targetBlockPos, player, partialTicks, 0.0F, 1.0F, 0.0F, 0.4F);
            final TileEntity tileEntity = world.getTileEntity(targetBlockPos);
            if (tileEntity instanceof IStructureTE) {
                final BlockPos masterBlockLocation = ((IStructureTE) tileEntity).getMasterBlockLocation();
                highlightBoundingBoxes(masterBlockLocation, player, partialTicks, 0.0F, 0.0F, 1.0F, 0.4F);
            }

        }
        if (block instanceof StructureBlock) {
            highlightBoundingBoxes(targetBlockPos, player, partialTicks, 0.0F, 0.0F, 1.0F, 0.4F);
        }
        if (block instanceof StructureShapeBlock) {
            highlightCollisionBoxes(blockState, targetBlockPos, player, partialTicks, 1.0F, 0.0F, 0.0F, 0.4F);
        }
    }

    private void highlightBoundingBoxes(BlockPos blockPos, EntityPlayer player, float partialTicks, float red, float green, float blue, float alpha) {
        final double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.glLineWidth(2.0F);
        RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(blockPos).expandXyz(0.0021D).offset(-x, -y, -z), red, green, blue, alpha);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void highlightCollisionBoxes(IBlockState blockState, BlockPos blockPos, EntityPlayer player, float partialTicks, float red, float green, float blue, float alpha) {
        final World world = player.world;
        final double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        final List<AxisAlignedBB> collisionBoxes = Lists.newArrayList();
        blockState.addCollisionBoxToList(
                world,
                blockPos,
                allEncompassingAxisAlignedBoundingBox,
                collisionBoxes,
                player,
                false);

        if (collisionBoxes.isEmpty()) {
            return;
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        GlStateManager.glLineWidth(1.0F);
        for (final AxisAlignedBB collisionBox : collisionBoxes) {
            RenderGlobal.drawSelectionBoundingBox(collisionBox.expandXyz(0.0021D).offset(-x, -y, -z), red, green, blue, alpha);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
