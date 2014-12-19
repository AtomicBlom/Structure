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
package mod.steamnsteel.block.structure;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.block.SteamNSteelStructureBlock;
import mod.steamnsteel.tileentity.StructureShapeTE;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.StructurePattern;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.List;

public class StructureShapeBlock extends SteamNSteelStructureBlock implements ITileEntityProvider
{
    public static final String NAME = "structureShape";

    public StructureShapeBlock()
    {
        setBlockName(NAME);
    }

    @Override
    public StructurePattern getPattern()
    {
        //should be unreachable
        throw new AssertionError("Pattern call on non-pattern block");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        final StructureShapeTE te = (StructureShapeTE)world.getTileEntity(x,y,z);

        if (te.hasMaster())
        {
            final Vec3 loc = te.getMasterLocation();
            final Block block = te.getMasterBlock();

            if (block instanceof SteamNSteelStructureBlock && !(block instanceof StructureShapeBlock))
                return block.getSelectedBoundingBoxFromPool(world, (int)loc.xCoord,(int)loc.yCoord,(int)loc.zCoord);
        }

        return AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1);
    }

    @Override
    protected Vec3 getMasterBlock(World world, int x, int y, int z)
    {
        final StructureShapeTE te = (StructureShapeTE) world.getTileEntity(x,y,z);

        return te.getMasterLocation();
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {
        final TileEntity te = world.getTileEntity(x,y,z);
        final Block block = ((StructureShapeTE) te).getMasterBlock();

        if (block instanceof SteamNSteelStructureBlock)
        {
            final Vec3 ml = ((StructureShapeTE) te).getMasterLocation();

            block.addCollisionBoxesToList(world, (int)ml.xCoord, (int)ml.yCoord, (int)ml.zCoord, aabb, boundingBoxList, entityColliding);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new StructureShapeTE();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack)
    {
        //noop
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {//TODO remove test code
        final StructureShapeTE te = (StructureShapeTE)world.getTileEntity(x,y,z);
        final SteamNSteelStructureBlock block = (SteamNSteelStructureBlock)te.getMasterBlock();

        if (!world.isRemote){
            player.addChatComponentMessage(new ChatComponentText("Cleaned the recipe : " + block.getLocalizedName() + " : BlockID " + te.getBlockID()));
        }
        block.cleanPattern();

        if (!world.isRemote)
        {
            String s = "\n";
            for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            {
                if (te.hasNeighbour(d)) s += "\n" + d;
            }

            if (!world.isRemote) Logger.info(s);

            return true;
        }


        return true;
    }
}
