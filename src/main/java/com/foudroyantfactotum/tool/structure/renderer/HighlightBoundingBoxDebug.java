package com.foudroyantfactotum.tool.structure.renderer;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.block.StructureShapeBlock;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
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
        RayTraceResult target = event.getTarget();
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!(heldItem == null || heldItem.getItem() == null)) {
            return;
        }

        World world = event.getPlayer().getEntityWorld();
        IBlockState blockState = world.getBlockState(target.getBlockPos());
        Block block = blockState.getBlock();
        if (block instanceof StructureBlock || block instanceof StructureShapeBlock) {
            highlightBoundingBoxes(event, blockState);
        }
    }

    private void highlightBoundingBoxes(DrawBlockHighlightEvent event, IBlockState blockState) {
        RayTraceResult result = event.getTarget();
        EntityPlayer player = event.getPlayer();
        World world = player.worldObj;
        float partialTicks = event.getPartialTicks();
        double x = event.getPlayer().lastTickPosX + (event.getPlayer().posX - event.getPlayer().lastTickPosX) * (double)partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

        List<AxisAlignedBB> collisionBoxes = Lists.newArrayList();
        blockState.addCollisionBoxToList(
                world,
                result.getBlockPos(),
                allEncompassingAxisAlignedBoundingBox,
                collisionBoxes,
                player);

        if (collisionBoxes.size() == 0) {
            return;
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(event.getTarget().getBlockPos()).expandXyz(0.0020000000949949026D).offset(-x, -y, -z), 0.0F, 1.0F, 0.0F, 0.4F);
        GlStateManager.glLineWidth(1.0F);
        for (AxisAlignedBB collisionBox : collisionBoxes) {
            RenderGlobal.drawSelectionBoundingBox(collisionBox.expandXyz(0.0020000000949949026D).offset(-x, -y, -z), 1.0F, 0.0F, 0.0F, 0.4F);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
