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
package com.foudroyantfactotum.tool.structure.tileentity;

import com.foudroyantfactotum.tool.structure.IStructure.IStructureTE;
import com.foudroyantfactotum.tool.structure.Structure;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.coordinates.BlockPosUtil;
import com.foudroyantfactotum.tool.structure.utility.IStructureDefinitionProvider;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import static com.foudroyantfactotum.tool.structure.block.StructureBlock.getMirror;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;
import static com.foudroyantfactotum.tool.structure.tileentity.StructureTE.BLOCK_INFO;
import static com.foudroyantfactotum.tool.structure.tileentity.StructureTE.BLOCK_PATTERN_NAME;

public class StructureShapeTE extends TileEntity implements IStructureTE
{
    private BlockPos local = BlockPos.ORIGIN;
    private IStructureDefinitionProvider structureDefinition;

    private java.util.Optional<BlockPos> masterLocation = java.util.Optional.empty();
    private Optional<StructureTE> originTE = Optional.absent();
    private boolean hasNotAttemptedAcquisitionOfOriginTE = true;

    public StructureTE getOriginTE()
    {
        return originTE.get();
    }

    public boolean hasOriginTE()
    {
        if (originTE.isPresent())
        {
            if (!originTE.get().isInvalid())
            {
                return true;
            }
        }

        //get te and test
        final TileEntity te = getWorld().getTileEntity(getMasterBlockLocation());

        if (hasNotAttemptedAcquisitionOfOriginTE && te instanceof StructureTE)
        {
            if (!te.isInvalid())
            {
                originTE = Optional.of((StructureTE) te);
                return true;
            }
        }

        hasNotAttemptedAcquisitionOfOriginTE = false;
        return false;
    }

    //================================================================
    //                 S T R U C T U R E   C O N F I G
    //================================================================


    @Override
    public BlockPos getMasterBlockLocation()
    {
        if (!masterLocation.isPresent())
        {
            final IBlockState state = getWorld().getBlockState(pos);
            final StructureBlock sb = structureDefinition.getStructureDefinition().getMasterBlock();

            if (sb == null)
            {
                return pos;
            }

            masterLocation = java.util.Optional.of(localToGlobal(
                    -local.getX(), -local.getY(), -local.getZ(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    state.getValue(BlockHorizontal.FACING), getMirror(state),
                    sb.getStructureDefinitionProvider().getStructureDefinition().getBlockBounds()));
        }

        return masterLocation.get();
    }

    public IStructureDefinitionProvider getStructureDefinition()
    {
        return structureDefinition;
    }

    @Override
    public void configureBlock(BlockPos local, IStructureDefinitionProvider structureDefinition)
    {
        this.local = local;
        this.structureDefinition = structureDefinition;
    }

    @Override
    public BlockPos getLocal()
    {
        return local;
    }


    //================================================================
    //                            N B T
    //================================================================

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(super.getUpdateTag());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        final int blockInfo = nbt.getInteger(BLOCK_INFO);

        local = BlockPosUtil.fromInt(blockInfo);
        String definitionName = nbt.getString(BLOCK_PATTERN_NAME);
        structureDefinition = Structure.getStructureDefinitionByRegistryName(new ResourceLocation(definitionName));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setInteger(BLOCK_INFO, BlockPosUtil.toInt(local));
        nbt.setString(BLOCK_PATTERN_NAME, structureDefinition.getRegistryName().toString());

        return nbt;
    }

    //================================================================
    //                          C l a s s
    //================================================================

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("local", local)
                .add("structureRegistryName", structureDefinition.getRegistryName())
                .add("masterLocation", masterLocation)
                .add("originTE", originTE)
                .add("hasNotAttemptedAcquisitionOfOriginTE", hasNotAttemptedAcquisitionOfOriginTE)
                .toString();
    }
}
