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
package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.IStructure.IPartBlockState;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.block.StructureShapeBlock;
import com.foudroyantfactotum.tool.structure.coordinates.BlockPosUtil;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StructureDefinitionBuilder
{
    private BitSet sbLayout;
    private BlockPos sbLayoutSize;

    private BlockPos masterPosition;
    private BlockPos toolFormPosition;

    private IPartBlockState[][][] states;
    private List<CollisionBoxRule> collisionBoxes = Lists.newArrayList();
    private List<CollisionBoxRule> selectionBoxRules = Lists.newArrayList();
    private StructureBlock masterBlock;
    private StructureShapeBlock shapeBlock;

    public StructureDefinition build()
    {
        if (masterBlock == null) {
            throw new StructureDefinitionError("Missing master block");
        }

        if (shapeBlock == null) {
            throw new StructureDefinitionError("Missing shape block");
        }

        if(conBlocks == null)
        {
            throw new StructureDefinitionError("Missing block states");
        }

        //blocks jagged map test
        for (final IBlockState[][] b: conBlocks)
        {
            if (b.length != conBlocks[0].length)
            {
                throw new StructureDefinitionError("Construction map jagged");
            }

            for (final IBlockState[] bb: b)
            {
                if (bb.length != b[0].length)
                {
                    throw new StructureDefinitionError("Construction map jagged");
                }
            }
        }

        //state jagged map test
        if (state != null)
        {
            for (final String[][] s : state)
            {
                if (s.length != state[0].length)
                {
                    throw new StructureDefinitionError("Construction map jagged");
                }

                for (final String[] ss : s)
                {
                    if (ss.length != s[0].length)
                    {
                        throw new StructureDefinitionError("Construction map jagged");
                    }
                }
            }

            if (!(
                    conBlocks.length == state.length &&
                            conBlocks[0].length == state[0].length &&
                            conBlocks[0][0].length == state[0][0].length
            ))
                throw new StructureDefinitionError("block/state sizing mismatch");
        }

        if (toolFormPosition == null)
        {
            throw new StructureDefinitionError("tool form location missing");
        }

        //-------------------------------------------------------
        //Correct data and align it to the inner data structures.
        //-------------------------------------------------------
        final int xsz = conBlocks.length;
        final int ysz = conBlocks[0].length;
        final int zsz = conBlocks[0][0].length;

        states = new IPartBlockState[xsz][ysz][zsz];

        for (final MutableBlockPos local : BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            states[local.getX()][local.getY()][local.getZ()] = getBlockState(local);
        }

        //correct collision bounds.
        for (final CollisionBoxRule collisionBox : collisionBoxes)
        {
            collisionBox.adjustToMasterBlock(masterPosition);
        }
        for (final CollisionBoxRule selectionBox : selectionBoxRules) {
            selectionBox.adjustToMasterBlock(masterPosition);
        }

        //correct tool form location
        toolFormPosition = toolFormPosition.subtract(masterPosition);

        return new StructureDefinition(
                sbLayout,
                sbLayoutSize,
                masterPosition,
                toolFormPosition,
                states,
                collisionBoxes,
                selectionBoxRules,
                masterBlock,
                shapeBlock);
    }

    /**
     * Gets the clean error checked block state
     * @param local local coords of the block within the map
     * @return block state
     */
    private IPartBlockState getBlockState(MutableBlockPos local)
    {
        final IBlockState block = conBlocks[local.getX()][local.getY()][local.getZ()];

        if (block == null) return PartBlockState.of();
        if (state == null) return PartBlockState.of(block);

        final String blockState = state[local.getX()][local.getY()][local.getZ()];

        if (blockState == null) return PartBlockState.of(block);

        return PartBlockState.of(block, blockState);
    }

    private ImmutableMap<Character, IBlockState> conDef = ImmutableMap.of();
    private IBlockState[][][] conBlocks;

    /**
     * Define what each character represents within the block map
     * @param representation char to unlocal.getZ()ed block name map
     * @exception NullPointerException thrown if block doesn't exist.
     */
    public void assignConstructionDef(ImmutableMap<Character, String> representation)
    {
        Builder<Character, IBlockState> builder = ImmutableMap.builder();

        for (final Character c: representation.keySet())
        {
            final String blockName = representation.get(c);
            final Block block = Block.getBlockFromName(blockName);

            checkNotNull(block, "assignConstructionDef.Block does not exist " + blockName);

            builder.put(c, block.getDefaultState());
        }

        //default
        builder.put(' ', Blocks.AIR.getDefaultState());

        conDef = builder.build();
    }

    /**
     * builds the block array using the representation map and the layout(String[]...)
     * String = x-line
     * String[] = z-line
     * String[]... = y-line
     * @param layer the layout of the blocks.
     * @exception NullPointerException the layout is missing a map
     */
    public void assignConstructionBlocks(String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        conBlocks = new IBlockState[xsz][ysz][zsz];

        for (final MutableBlockPos local : BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            final char c = layer[local.getY()][local.getZ()].charAt(local.getX());

            if (!conDef.containsKey(c) && c != '-')
            {
                throw new StructureDefinitionError("Map missing '" + c + "' @" + local);
            }

            conBlocks[local.getX()][local.getY()][local.getZ()] = c == '-' ? null : conDef.get(c);
        }
    }

    /**
     * Configures the location of the blocks.
     * M => Master block location. Specify only once
     * - => Block position
     *   => No block
     *
     * @param shift translation of S(C).origin to S(F).origin
     * @param layer
     */
    public void setConfiguration(BlockPos shift, String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        sbLayoutSize = BlockPosUtil.of(xsz, ysz, zsz);
        sbLayout = new BitSet(xsz * ysz * zsz);

        for (final MutableBlockPos local : BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            final char c = Character.toUpperCase(layer[local.getY()][local.getZ()].charAt(local.getX()));

            switch (c)
            {
                case 'M': // Master block location
                    if (masterPosition == null)
                    {
                        masterPosition = BlockPosUtil.of(
                                local.getX() + shift.getX(),
                                local.getY() + shift.getY(),
                                local.getZ() + shift.getZ()
                        );
                    } else
                    {
                        throw new StructureDefinitionError("setConfiguration.Master position defined more then once.");
                    }

                case ' ':
                case '-':
                    sbLayout.set(
                            local.getX() + local.getZ() * xsz + local.getY() *zsz*xsz,
                            c != ' ');
                    break;
                default:
                {
                    throw new StructureDefinitionError("setConfiguration.Unknown char '" + c + '\'');
                }
            }
        }

        if (masterPosition == null)
        {
            throw new StructureDefinitionError("setConfiguration.Master position not defined");
        }
    }

    private ImmutableMap<Character, String> repState = ImmutableMap.of();
    private String[][][] state;

    /**
     * Define what each character represents within the state map
     * @param representation char to "equivelent json" state map
     * @exception NullPointerException thrown if block doesn't exist.
     */
    public void assignConstructionStateDef(ImmutableMap<Character, String> representation)
    {
        repState = representation;
    }

    /**
     * builds the state array using the representation map and the layout(String[]...)
     * String = x-line
     * String[] = z-line
     * String[]... = y-line
     * @param layer the layout of the states.
     * @exception NullPointerException the layout is missing a map
     */
    public void assignConstructionStateBlocks(String[]... layer)
    {
        final int xsz = layer[0][0].length();
        final int ysz = layer.length;
        final int zsz = layer[0].length;

        state = new String[xsz][ysz][zsz];

        for (final MutableBlockPos local : BlockPos.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(xsz-1, ysz-1, zsz-1)))
        {
            final char c = layer[local.getY()][local.getZ()].charAt(local.getX());

            if (!repState.containsKey(c) && c != ' ')
            {
                throw new StructureDefinitionError("Map missing '" + c + "' @" + local);
            }

            state[local.getX()][local.getY()][local.getZ()] = repState.get(c);
        }
    }

    public void assignToolFormPosition(BlockPos toolFormPosition)
    {
        this.toolFormPosition = toolFormPosition;
    }

    public void setCollisionBoxRule(Function<IBlockState, Boolean> condition, float[]... collisionBoxes) {
        this.collisionBoxes.add(new CollisionBoxRule(condition, collisionBoxes));
    }
    public void setSelectionBoxRule(Function<IBlockState, Boolean> condition, float[] selectionBox) {
        this.selectionBoxRules.add(new CollisionBoxRule(condition, selectionBox));
    }

    /**
     * set collision boxes of structure
     * @param collisionBoxes arrays of collision. must have a length of 6 l=lower left back u=upper right front [lx, ly, lz, ux, uy, uz]
     */
    public void setCollisionBoxes(float[]... collisionBoxes)
    {
        this.collisionBoxes.add(new CollisionBoxRule((blockState) -> true, collisionBoxes));
    }
    public void setSelectionBox(float[] selectionBox) {
        this.selectionBoxRules.add(new CollisionBoxRule((blockState) -> true, selectionBox));
    }

    public void setMasterBlock(StructureBlock block)
    {
        this.masterBlock = block;
    }

    public StructureBlock getMasterBlock() {
        return this.masterBlock;
    }

    public void setShapeBlock(StructureShapeBlock block)
    {
        this.shapeBlock = block;
    }

    public StructureShapeBlock getShapeBlock() {
        return this.shapeBlock;
    }

    public static class StructureDefinitionError extends Error
    {
        public StructureDefinitionError(String msg)
        {
            super(msg);
        }
    }

}
