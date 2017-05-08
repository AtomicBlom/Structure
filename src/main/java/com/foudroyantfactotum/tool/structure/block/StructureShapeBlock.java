/*
 * Copyright (c) 2016 Foudroyant Factotum
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.foudroyantfactotum.tool.structure.block;

import com.foudroyantfactotum.tool.structure.IStructure.ICanMirror;
import com.foudroyantfactotum.tool.structure.IStructure.IStructureTE;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.foudroyantfactotum.tool.structure.tileentity.StructureShapeTE;
import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.xml.dtd.EMPTY;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.foudroyantfactotum.tool.structure.block.StructureBlock.*;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobalCollisionBoxes;

public class StructureShapeBlock extends Block implements ITileEntityProvider, ICanMirror
{
    public static boolean _DEBUG = false;
    public static final String NAME = "structureShape";
    public static final AxisAlignedBB EMPTY_BOUNDS = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    public StructureShapeBlock()
    {
        super(Material.PISTON);
        setSoundType(SoundType.STONE);
        setHardness(0.5f);

        setUnlocalizedName(NAME);

        IBlockState state = this.blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH);

        if (canMirror())
        {
            state = state.withProperty(MIRROR, false);
        }

        setDefaultState(state);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        if (canMirror())
        {
            return new BlockStateContainer(this, BlockHorizontal.FACING, MIRROR);
        }

        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Deprecated
    public IBlockState getStateFromMeta(int meta)
    {
        final EnumFacing facing = EnumFacing.getHorizontal(meta & 0x3);

        IBlockState state = getDefaultState().withProperty(BlockHorizontal.FACING, facing);

        if (canMirror())
        {
            state = state.withProperty(MIRROR, (meta & 0x4) != 0);
        }

        return state;
    }

    public int getMetaFromState(IBlockState state)
    {
        final EnumFacing facing = state.getValue(BlockHorizontal.FACING);
        final boolean mirror = getMirror(state);

        if (canMirror())
        {
            return facing.getHorizontalIndex() | (mirror ? 1 << 2 : 0);
        } else
        {
            return facing.getHorizontalIndex();
        }
    }

    @Override
    public boolean canMirror()
    {
        return true;
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(pos);

        if (te != null)
        {
            if (te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock() != null)
            {
                return te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock().getPickBlock(state, target, world, pos, player);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(pos);

        if (te != null && te.getMasterBlockInstance() != null)
        {
            te.getMasterBlockInstance().onBlockExploded(world, te.getMasterBlockLocation(), explosion);
        }
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(pos);
        if (te != null)
        {
            final BlockPos masterBlockLocation = te.getMasterBlockLocation();
            final IBlockState blockState = world.getBlockState(masterBlockLocation);
            return blockState.getBlock().isBed(blockState, world, masterBlockLocation, player);
        }
        return false;
    }

    @Override
    public boolean isBedFoot(IBlockAccess world, BlockPos pos)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(pos);
        if (te != null)
        {
            final BlockPos masterBlockLocation = te.getMasterBlockLocation();
            final IBlockState blockState = world.getBlockState(masterBlockLocation);
            return blockState.getBlock().isBedFoot(world, masterBlockLocation);
        }
        return false;
    }

    public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied)
    {
        if (world instanceof World)
        {
            final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(pos);
            if (te != null)
            {
                final BlockPos masterBlockLocation = te.getMasterBlockLocation();
                final IBlockState blockState = world.getBlockState(masterBlockLocation);
                blockState.getBlock().setBedOccupied(world, masterBlockLocation, player, occupied);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
            final BlockPos mloc = te.getMasterBlockLocation();
            final StructureBlock sb = te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock();

            StructureDefinition pattern = sb.getStructureDefinitionProvider().getStructureDefinition();
            //FIXME: GetActualState?
            float[] selectionBox = pattern.getSelectionBox(world.getBlockState(mloc));
            if (sb == null || selectionBox == null)
            {
                return EMPTY_BOUNDS;
            }

            final List<AxisAlignedBB> axisAlignedBBS = localToGlobalCollisionBoxes(
                    pos,
                    mloc.getX() - pos.getX(), mloc.getY() - pos.getY(), mloc.getZ() - pos.getZ(),
                    null, null, Lists.newArrayList(selectionBox),
                    state.getValue(BlockHorizontal.FACING), getMirror(state)

            );
            if (!axisAlignedBBS.isEmpty())
            {
                return axisAlignedBBS.get(0);
            }
        }

        return EMPTY_BOUNDS;
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, @Nullable Entity entityIn, boolean p_185477_7_)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
            final BlockPos mloc = te.getMasterBlockLocation();
            final StructureBlock sb = te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock();

            StructureDefinition pattern = sb.getStructureDefinitionProvider().getStructureDefinition();
            //FIXME: GetActualState?
            List<float[]> collisionBoxes = pattern.getCollisionBoxes(world.getBlockState(mloc));
            if (sb == null || collisionBoxes == null)
            {
                return;
            }

            localToGlobalCollisionBoxes(
                    pos,
                    mloc.getX() - pos.getX(), mloc.getY() - pos.getY(), mloc.getZ() - pos.getZ(),
                    mask, list, collisionBoxes,
                    state.getValue(BlockHorizontal.FACING), getMirror(state)
            );
        }
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    @Deprecated
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        return EnumPushReaction.BLOCK;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new StructureShapeTE();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager particleManager)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);

        if (te != null)
        {
            final StructureBlock block = te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock();

            if (block != null)
            {
                return block.addDestroyEffects(world, te.getMasterBlockLocation(), particleManager);
            }
        }

        return true; //No Destroy Effects
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        final IStructureTE te = (IStructureTE) world.getTileEntity(pos);
        final boolean isPlayerCreative = player != null && player.capabilities.isCreativeMode;
        final boolean isPlayerSneaking = player != null && player.isSneaking();

        final StructureBlock sb = te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock();

        if (sb != null)
        {
            boolean shouldDecompose = sb.shouldDecompose() && (!isPlayerCreative || isPlayerSneaking);

            sb.breakStructure(world,
                    te.getMasterBlockLocation(),
                    state.getValue(BlockHorizontal.FACING),
                    getMirror(state),
                    shouldDecompose
            );
            updateExternalNeighbours(world,
                    te.getMasterBlockLocation(),
                    sb.getStructureDefinitionProvider(),
                    state.getValue(BlockHorizontal.FACING),
                    getMirror(state),
                    false
            );

        } else
        {
            world.setBlockToAir(pos);
        }

        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        final StructureShapeTE te = (StructureShapeTE) worldIn.getTileEntity(pos);

        if (te != null)
        {
            final StructureBlock block = te.getStructureDefinitionProvider().getStructureDefinition().getMasterBlock();

            if (block != null)
            {
                return block.onStructureBlockActivated(worldIn, te.getMasterBlockLocation(), playerIn, hand, pos, side, te.getLocal(), hitX, hitY, hitZ);
            }
        }

        worldIn.setBlockToAir(pos);

        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos neighbourPos)
    {
        onSharedNeighbourBlockChange(world, pos,
                ((StructureShapeTE) world.getTileEntity(pos)).getStructureDefinitionProvider(),
                blockIn,
                state
        );
    }

    //======================================
    //      V i s u a l   D e b u g
    //======================================


    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return _DEBUG ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }
}
