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
package com.foudroyantfactotum.tool.structure.waila.structure;

/*public class WailaStructureShapeBlock implements IWailaDataProvider
{
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return new ItemStack(te.getMasterBlockInstance());
            }
        }

        return null;
    }

    @Override
    public ITipList getWailaHead(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return te.getMasterBlockInstance().getWailaHead(itemStack, currenttip, accessor, config);
            }
        }

        currenttip.clear();

        return currenttip;
    }

    @Override
    public ITipList getWailaBody(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return te.getMasterBlockInstance().getWailaBody(itemStack, currenttip, accessor, config);
            }
        }

        currenttip.clear();

        return currenttip;
    }

    @Override
    public ITipList getWailaTail(ItemStack itemStack, ITipList currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if (accessor.getTileEntity() instanceof StructureShapeTE)
        {
            final StructureShapeTE te = (StructureShapeTE) accessor.getTileEntity();

            if (te.getMasterBlockInstance() != null)
            {
                return te.getMasterBlockInstance().getWailaTail(itemStack, currenttip, accessor, config);
            }
        }

        currenttip.clear();

        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(TileEntity te, NBTTagCompound tag, IWailaDataAccessorServer accessor)
    {
        return tag;
    }
}*/
