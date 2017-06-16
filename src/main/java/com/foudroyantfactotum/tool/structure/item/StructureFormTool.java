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

import com.foudroyantfactotum.tool.structure.IStructure.IPartBlockState;
import com.foudroyantfactotum.tool.structure.StructureRegistry;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.net.StructureNetwork;
import com.foudroyantfactotum.tool.structure.net.StructurePacket;
import com.foudroyantfactotum.tool.structure.net.StructurePacketOption;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static com.foudroyantfactotum.tool.structure.block.StructureBlock.MIRROR;
import static com.foudroyantfactotum.tool.structure.block.StructureBlock.updateExternalNeighbours;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.*;

public class StructureFormTool extends Item
{
    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    protected static final EnumFacing[][] orientationPriority = {
            {EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST}, //south
            {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH}, //west
            {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST}, //north
            {EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST}, //east
    };
    protected static final boolean[][] mirrorPriority = {
            {false, true},
            {true, false},
    };

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote || playerIn == null)
        {
            return EnumActionResult.SUCCESS;
        }

        final EnumFacing[] orientation = orientationPriority[MathHelper.floor(playerIn.rotationYaw * 4.0f / 360.0f + 0.5) & 3];
        final boolean[] mirror = mirrorPriority[playerIn.isSneaking()?1:0];

        doSearch(worldIn, pos, orientation, mirror, StructureRegistry.getStructureList());

        return EnumActionResult.SUCCESS;
    }

    protected void doSearch(World world, BlockPos pos, EnumFacing[] orientation, boolean[] mirror, Collection<StructureBlock> sd)
    {
        final List<Future<SearchResult>> searchJobFuture = new ArrayList<>(sd.size());

        //search currently ignores multiple matches and take the first match available.
        for (final StructureBlock sb : StructureRegistry.getStructureList())
        {
            searchJobFuture.add(pool.submit(new SearchJob(sb, world, pos, orientation, mirror)));
        }

        SearchResult result = null;

        for (final Future<SearchResult> res : searchJobFuture)
        {
            try
            {
                if (result == null)
                {
                    result = res.get();
                } else {
                    res.cancel(true);
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        searchJobFuture.clear();

        if (result != null)
        {
            IBlockState state = result.block.getDefaultState()
                    .withProperty(BlockHorizontal.FACING, result.orientation);

            if (result.block.canMirror())
            {
                state = state.withProperty(MIRROR, result.mirror);
            }

            world.setBlockState(result.origin, state, 0x2);
            result.block.formStructure(world, result.origin, state, 0x2);

            updateExternalNeighbours(world, result.origin, result.block.getPattern(), result.orientation, result.mirror, true);

            StructureNetwork.network.sendToAllAround(
                    new StructurePacket(result.origin, result.block.getRegHash(), result.orientation, result.mirror, StructurePacketOption.BUILD),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), result.origin.getX(), result.origin.getY(), result.origin.getZ(), 30)
            );
        }
    }

    /**
     * Performs complete search on world at the location
     */
    private static class SearchJob implements Callable<SearchResult>
    {
        final StructureBlock ssBlock;
        final World world;
        final BlockPos pos;

        final EnumFacing[] orientationOrder;
        final boolean[] mirrorOrder;

        SearchJob(StructureBlock ssBlock, World world, BlockPos pos, EnumFacing[] orientationOrder, boolean[] mirrorOrder)
        {
            this.ssBlock = ssBlock;
            this.world = world;
            this.pos = pos;
            this.orientationOrder = orientationOrder;
            this.mirrorOrder = ssBlock.canMirror()? mirrorOrder : new boolean[]{false};
        }

        @Override
        public SearchResult call() throws Exception
        {
            final StructureDefinition sd = ssBlock.getPattern();
            final BlockPos tl = sd.getToolFormLocation();

            nextOrientation:
            for (final EnumFacing o: orientationOrder)
            {
                nextMirror:
                for (final boolean mirror : mirrorOrder)
                {
                    final BlockPos origin =
                            localToGlobal(
                                    -tl.getX(), -tl.getY(), -tl.getZ(),
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    o, mirror, sd.getBlockBounds()
                            );

                    for (final MutableBlockPos local : sd.getStructureItr())
                    {
                        final IPartBlockState pb = sd.getBlock(local);
                        final IBlockState b = pb.getBlockState();

                        //alter local coord var and changes it to world coord.
                        mutLocalToGlobal(local, origin, o, mirror, sd.getBlockBounds());

                        final IBlockState ncwb = world.getBlockState(local);

                        final IBlockState wb = ncwb.getActualState(world, local);

                        if (b != null && (b.getBlock() != wb.getBlock() || !doBlockStatesMatch(pb, localToGlobal(b, o, mirror), wb)))
                        {
                            if (mirrorOrder.length <= 1) //is last mirror
                            {
                                continue nextOrientation;
                            }
                            else
                            {
                                if (mirror == mirrorOrder[1])
                                {
                                    continue nextOrientation;
                                } else {
                                    continue nextMirror;
                                }
                            }
                        }
                    }

                    //found match, eeek!
                    final SearchResult result = new SearchResult();

                    result.block = ssBlock;
                    result.origin = origin;
                    result.orientation = o;
                    result.mirror = mirror;

                    return result;
                }
            }

            //no matches for this structure
            return null;
        }
    }

    /***
     * final result struct, used to return result from the search.
     */
    private static final class SearchResult
    {
        public StructureBlock block;
        public EnumFacing orientation;
        public boolean mirror;
        public BlockPos origin;
    }
}
