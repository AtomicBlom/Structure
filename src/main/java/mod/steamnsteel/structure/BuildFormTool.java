/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
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
package mod.steamnsteel.structure;

import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.item.tool.SSToolShovel;
import mod.steamnsteel.library.Material;
import mod.steamnsteel.structure.coordinates.TripleCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BuildFormTool extends SSToolShovel
{
    public BuildFormTool()
    {
        super(Material.STEEL);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            /*final StructureSearchResult result = uberStructureSearch(world, x, y, z);

            if (result != null)
            {
                final int meta = result.orientation.encode() | (result.isMirrored ? SteamNSteelStructureBlock.flagMirrored : 0x0);

                world.setBlock(result.origin.x, result.origin.y, result.origin.z, result.block, meta, 0x2);
                result.block.formStructure(world, result.origin, meta, 0x2);

                updateExternalNeighbours(world, result.origin, result.block.getPattern(), result.orientation, result.isMirrored, true);

                ModNetwork.network.sendToAllAround(
                        new StructurePacket(result.origin.x, result.origin.y, result.origin.z,
                                result.block.getRegHash(), result.orientation, result.isMirrored,
                                StructurePacketOption.BUILD),
                        new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 30)
                );
            }*/
        }

        return false;
    }

    /**
     * Performs complete search on world with at the location
     *
     * @param world target world
     * @param x     x location
     * @param y     y location
     * @param z     z location
     * @return returns {@link mod.steamnsteel.structure.BuildFormTool.StructureSearchResult StructureSearchResult} or null for no result.
     */
    private StructureSearchResult uberStructureSearch(World world, int x, int y, int z)
    {
        //do uber search and build structure todo Threaded? Reduce search space? Reduce memory usage?

        /*for (SteamNSteelStructureBlock ssBlock : StructureRegistry.getStructureList())
        {
            final StructureDefinition sd = ssBlock.getPattern();

            final TripleCoord tl = sd.getToolFormLocation();

            //todo also search mirrored (currently disabled)
            //every Direction nsew
            nextOrientation:
            for (Orientation o : Orientation.values())
            {
                final TripleCoord origin =
                        localToGlobal(
                                -tl.x, -tl.y, -tl.z,
                                x, y, z,
                                o, false, sd.getBlockBounds()
                        );

                final TripleIterator itr = sd.getStructureItr();

                while (itr.hasNext())
                {
                    final TripleCoord local = itr.next();
                    final BlockPos coord = bindLocalToGlobal(origin, local, o, false, sd.getBlockBounds());

                    final Block b = sd.getBlock(local);
                    final int m = sd.getBlockMetadata(local);

                    final Block wb = coord.getBlock(world);
                    final int wm = coord.getMeta(world);

                    if (b == null || b != wb)
                    {
                        if (b instanceof IGeneralBlock)
                        {
                            final IGeneralBlock gb = (IGeneralBlock) b;

                            if (!gb.canBlockBeUsed(wb, coord.getMeta(world), local))
                                continue nextOrientation;
                        } else
                        {
                            continue nextOrientation;
                        }
                    }

                    if (m != -1 && m != wm)
                    {
                        continue nextOrientation;
                    }
                }

                //found match, eeek!
                final StructureSearchResult result = new StructureSearchResult();

                result.block = ssBlock;
                result.origin = origin;
                result.orientation = o;
                result.isMirrored = false; //todo fix mirror state.

                return result;
            }
        }

        //no matches*/
        return null;
    }

    /***
     * final result struct, used to return result from the search.
     */
    private static final class StructureSearchResult
    {
        public SteamNSteelStructureBlock block;
        public EnumFacing orientation;
        public boolean isMirrored;
        public TripleCoord origin;
    }
}
