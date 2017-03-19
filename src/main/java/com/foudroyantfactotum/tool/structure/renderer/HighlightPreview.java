package com.foudroyantfactotum.tool.structure.renderer;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.utility.StructureQuery;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class HighlightPreview {
    private static final EnumFacing[] modelSources = {
            EnumFacing.UP,
            EnumFacing.DOWN,
            EnumFacing.NORTH,
            EnumFacing.SOUTH,
            EnumFacing.EAST,
            EnumFacing.WEST,
            null
    };

    @SubscribeEvent
    public void onDrawHighlight(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);

        if (heldItem == null || heldItem.stackSize == 0) {
            return;
        }

        Item item = heldItem.getItem();

        if (!(item instanceof ItemBlock)) {
            return;
        }
        Block block = ((ItemBlock) item).getBlock();
        if (!(block instanceof StructureBlock)) {
            return;
        }
        StructureBlock structureBlock = (StructureBlock)block;

        highlightFutureBlock(event, structureBlock);
    }

    private void highlightFutureBlock(DrawBlockHighlightEvent event, StructureBlock structureBlock) {
        RayTraceResult target = event.getTarget();
        EntityPlayer player = event.getPlayer();
        BlockPos blockPos = target.getBlockPos();
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        World world = player.worldObj;

        BlockPos potentialPlaceLocation = blockPos;
        if (!world.getBlockState(blockPos).getBlock().isReplaceable(world, blockPos)) {
            potentialPlaceLocation = blockPos.offset(target.sideHit);
        }

        float partialTicks = event.getPartialTicks();
        if (world.getWorldBorder().contains(potentialPlaceLocation))
        {
            List<BlockPos.MutableBlockPos> badLocations = Lists.newArrayList();

            final EnumFacing orientation = EnumFacing.getHorizontal(MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5) & 3);
            final boolean mirror = structureBlock.canMirror() && player.isSneaking();

            boolean canPlace = StructureQuery.canPlaceStructure(structureBlock, world, potentialPlaceLocation, orientation, mirror, badLocations);
            IBlockState blockState = structureBlock.getStateForPlacement(world, potentialPlaceLocation, target.sideHit, (float)target.hitVec.xCoord, (float)target.hitVec.yCoord, (float)target.hitVec.zCoord, heldItem.getMetadata(), player, heldItem);

            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

            renderBadBlockOutline(badLocations, x, y, z);

            renderPlacement(structureBlock, potentialPlaceLocation, world, partialTicks, canPlace, blockState, x, y, z);
        }
    }

    private void renderPlacement(StructureBlock structureBlock, BlockPos blockpos, World world, float partialTicks, boolean canPlace, IBlockState blockState, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x, -y, -z);
        GlStateManager.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());
        GlStateManager.enableTexture2D();

        EnumBlockRenderType renderType = blockState.getRenderType();
        if (renderType == EnumBlockRenderType.MODEL) {
            BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
            IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(blockState);

            renderPlacement(blockState, model, canPlace);
        } else if (renderType == EnumBlockRenderType.ENTITYBLOCK_ANIMATED) {
            TileEntityRendererDispatcher.instance.renderTileEntityAt(structureBlock.createTileEntity(world, blockState), 0, 0, 0, partialTicks);
        }

        GlStateManager.popMatrix();
    }

    private void renderBadBlockOutline(List<BlockPos.MutableBlockPos> badLocations, double x, double y, double z) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        for (BlockPos badLocation : badLocations) {
            RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(badLocation).expandXyz(0.0020000000949949026D).offset(-x, -y, -z), 1.0F, 0.0F, 0.0F, 0.4F);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void renderPlacement(IBlockState state, IBakedModel model, boolean canPlace) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        VertexBuffer vertexBuffer = new ReusableVertexBuffer(2097152);
        renderModelToVertexBuffer(vertexBuffer, state, model, canPlace);

        WorldVertexBufferUploader worldVertexBufferUploader = new WorldVertexBufferUploader();

        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.5f);

        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.colorMask(false, false, false, false);

        worldVertexBufferUploader.draw(vertexBuffer);

        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.colorMask(true, true, true, true);
        worldVertexBufferUploader.draw(vertexBuffer);

        GlStateManager.disableCull();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }

    private static void renderModelToVertexBuffer(VertexBuffer vertexBuffer, IBlockState state, IBakedModel model, boolean canPlace) {
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        int color = (0x80000000 | (canPlace ? 0xFFFFFF : 0xFF5555));
        for (EnumFacing value : modelSources) {
            for (BakedQuad quad : model.getQuads(state, value, 0)) {
                LightUtil.renderQuadColor(vertexBuffer, quad, color);
            }
        }
        vertexBuffer.finishDrawing();
    }

}
