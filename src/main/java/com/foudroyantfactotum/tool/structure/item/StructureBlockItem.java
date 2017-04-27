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
package com.foudroyantfactotum.tool.structure.item;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.mutLocalToGlobal;

public class StructureBlockItem extends ItemBlock
{
    public StructureBlockItem(Block block)
    {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        Preconditions.checkNotNull(player);
        final StructureBlock structureBlock = (StructureBlock)block;
        newState = getInitialStateForSubItems(stack, newState);

        final EnumFacing orientation = newState.getValue(BlockHorizontal.FACING);
        boolean mirror = false;
        if (structureBlock.canMirror()) {
            mirror = newState.getValue(StructureBlock.MIRROR);
        }

        //find master block location
        final StructureDefinition structureDefinition = structureBlock.getStructureDefinitionProvider().getStructureDefinition();
        final BlockPos hSize = structureDefinition.getHalfBlockBounds();
        final BlockPos ml = structureDefinition.getMasterLocation();

        final BlockPos origin
                = localToGlobal(
                -hSize.getX() + ml.getX(), ml.getY(), -hSize.getZ() + ml.getZ(),
                pos.getX(), pos.getY(), pos.getZ(),
                orientation, mirror, structureDefinition.getBlockBounds());

        //check block locations
        for (final MutableBlockPos local : structureDefinition.getStructureItr())
        {
            if (!structureDefinition.hasBlockAt(local))
            {
                continue;
            }

            mutLocalToGlobal(local, origin, orientation, mirror, structureDefinition.getBlockBounds());

            if (!world.getBlockState(local).getBlock().isReplaceable(world, local))
            {
                return false;
            }
        }

        world.setBlockState(origin, newState, 0x2);
        structureBlock.onBlockPlacedBy(world, origin, newState, player, stack);

        return true;
    }

    protected IBlockState getInitialStateForSubItems(ItemStack stack, IBlockState newState)
    {
        return newState;
    }
}
