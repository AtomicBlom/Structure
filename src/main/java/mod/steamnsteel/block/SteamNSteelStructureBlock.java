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
package mod.steamnsteel.block;

import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.tileentity.SteamNSteelStructureTE;
import mod.steamnsteel.utility.Orientation;
import mod.steamnsteel.utility.log.Logger;
import mod.steamnsteel.utility.structure.*;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class SteamNSteelStructureBlock extends SteamNSteelMachineBlock implements IStructurePatternBlock
{
    public static final int flagMirrored = 1 << 2;
    public static final int flagInvalid = 1 << 3;

    private Optional<StructurePattern> strucPatt= Optional.absent();

    @Override
    public StructurePattern getPattern()
    {
        if (!strucPatt.isPresent())
            strucPatt = Optional.of(StructurePattern.getPattern(getUnlocalizedName().hashCode()));

        return strucPatt.get();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack)
    {
        super.onBlockPlacedBy(world, x, y, z, entity, itemStack);

        int meta = world.getBlockMetadata(x, y, z);
        final boolean mirror = entity.isSneaking();
        final Orientation o = Orientation.getdecodedOrientation(meta);

        if (mirror) {
            meta |= flagMirrored;
            world.setBlockMetadataWithNotify(x,y,z,meta,0);
        }

        final StructureBlockIterator itr = new StructureBlockIterator(getPattern(), Vec3.createVectorHelper(x, y, z), o, mirror);

        while (itr.hasNext())
        {
            final StructureBlockCoord block = itr.next();

            if (!block.isMasterBlock())
                block.setBlock(world, ModBlock.structureShape,meta, 2);

            final IStructureTE ssBlock = (IStructureTE) block.getTileEntity(world);

            if (ssBlock != null) {
                ssBlock.setBlockPattern(getUnlocalizedName());
                ssBlock.configureBlock(block);
            } else {
                Logger.info("ERROR: " + block);
            }
        }
    }

    @Override
    public Vec3 getMasterBlockLocation(SteamNSteelStructureTE te)
    {
        return Vec3.createVectorHelper(te.xCoord,te.yCoord,te.yCoord);
    }

    protected SteamNSteelStructureBlock getMasterBlock(SteamNSteelStructureTE te)
    {
        return this;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);
        final int meta = world.getBlockMetadata(x,y,z);

        if ((meta & flagInvalid) != 0) {Logger.info("neighbor invalid :" + Vec3.createVectorHelper(x,y,z));return;}
        if (te == null) {Logger.info("te is null now :" + Vec3.createVectorHelper(x,y,z));return;}

        for (ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
        {
            if (te.hasNeighbour(d))
            {
                final int nx = x + d.offsetX;
                final int ny = y + d.offsetY;
                final int nz = z + d.offsetZ;

                final Block nBlock = world.getBlock(nx,ny,nz);
                final int nMeta = world.getBlockMetadata(nx, ny, nz);

                if (!(nBlock instanceof SteamNSteelStructureBlock) || meta != nMeta )
                {
                    Logger.info("nber des: " + Vec3.createVectorHelper(x,y,z));
                    world.setBlock(x, y, z, te.getTransmutedBlock(),te.getTransmutedMeta(),0x3);
                }
            }
        }
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
    {
        world.spawnParticle("hugeexplosion", x, y, z, 1,1,1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);
        final Vec3 mLoc = getMasterBlockLocation(te);

        final float sAjt = 0.05f;

        final StructureBlockIterator itr = new StructureBlockIterator(
                te.getPattern(),
                Vec3.createVectorHelper((int) mLoc.xCoord,(int) mLoc.yCoord,(int) mLoc.zCoord),
                Orientation.getdecodedOrientation(meta),
                isMirrored(meta)
        );

        while (itr.hasNext())
        {
            final StructureBlockCoord coord = itr.next();

            float xSpeed = 0.0f;
            float ySpeed = 0.0f;
            float zSpeed = 0.0f;
            int count = 0;

            for(ForgeDirection d: ForgeDirection.VALID_DIRECTIONS)
            {
                if (!coord.hasGlobalNeighbour(d)) {
                    xSpeed += d.offsetX;
                    ySpeed += d.offsetY;
                    zSpeed += d.offsetZ;
                }
            }

            if (++count > 5) continue;

            spawnBreakParticle(world, te, coord.getX() + 0.5f, coord.getY() + 0.5f, coord.getZ() + 0.5f, xSpeed*sAjt, ySpeed*sAjt, zSpeed*sAjt);
        }

        return true; //No Destroy Effects
    }

    private static void spawnBreakParticle(World world, SteamNSteelStructureTE te, float x, float y, float z, float sx, float sy, float sz)
    {
        world.spawnParticle("explode", x-0.5, y, z-0.5, sx, sy, sz);
        world.spawnParticle("explode", x, y, z, sx, sy, sz);
        world.spawnParticle("explode", x+0.5, y, z+0.5, sx, sy, sz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
        return true; //No Digging Effects
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {//TODO consider caching AABB?
        final Vec3 size = getPattern().getSize().addVector(-0.5, 0, -0.5);
        final Vec3 b = Vec3.createVectorHelper(-0.5,0,-0.5);
        final float rot = (float) Orientation.
                getdecodedOrientation(world.getBlockMetadata(x, y, z)).
                getRotationValue();

        size.rotateAroundY(rot);
        b.rotateAroundY(rot);

        return AxisAlignedBB.getBoundingBox(
                x + 0.5 + min(b.xCoord, size.xCoord),
                y,
                z + 0.5 + min(b.zCoord, size.zCoord),

                x + 0.5 + max(b.xCoord, size.xCoord),
                y + size.yCoord,
                z + 0.5 + max(b.zCoord, size.zCoord));
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List boundingBoxList, Entity entityColliding)
    {//TODO cache mby?
        final int meta = world.getBlockMetadata(x, y, z);
        final Orientation o = Orientation.getdecodedOrientation(meta);
        final float[][] collB = getPattern().getCollisionBoxes();

        final Vec3 trans = getPattern().getHalfSize().addVector(-0.5, 0, -0.5);
        final boolean isMirrored = isMirrored(meta);
        final float rot = (float) o.getRotationValue();

        trans.rotateAroundY(rot);
        for (final float[] f: collB)
        {
            final Vec3 lower = Vec3.createVectorHelper(f[0], f[1], f[2] * (isMirrored?-1:1));
            final Vec3 upper = Vec3.createVectorHelper(f[3], f[4], f[5] * (isMirrored?-1:1));

            lower.rotateAroundY(rot);
            upper.rotateAroundY(rot);

            final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
                    x + min(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + lower.yCoord, z + min(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5,
                    x + max(lower.xCoord, upper.xCoord) + trans.xCoord + 0.5, y + upper.yCoord, z + max(lower.zCoord, upper.zCoord) + trans.zCoord + 0.5);

            if (aabb.intersectsWith(bb))
            {
                boundingBoxList.add(bb);
            }
        }
    }

    public static boolean isMirrored(int meta)
    {
        return (meta & flagMirrored) != 0;
    }

    @Override
    public int quantityDropped(Random rnd)
    {
        return 0;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        if ((meta & flagInvalid) != 0) return;
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE) world.getTileEntity(x,y,z);
        final Vec3 mLoc = getMasterBlockLocation(te);

        final StructureBlockIterator itr = new StructureBlockIterator(
                te.getPattern(),
                Vec3.createVectorHelper((int) mLoc.xCoord,(int) mLoc.yCoord,(int) mLoc.zCoord),
                Orientation.getdecodedOrientation(meta),
                isMirrored(meta)
        );

        while (itr.hasNext())
        {
            final StructureBlockCoord coord = itr.next();

            coord.setMeta(world, flagInvalid, 0x2);
            coord.setBlock(world, te.getPattern().getBlock(coord), 0, 0x2);
            coord.removeTileEntity(world);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {//TODO remove test code
        final SteamNSteelStructureTE te = (SteamNSteelStructureTE)world.getTileEntity(x,y,z);

        if (!world.isRemote)
        {
            String s = "\n";
            s += te.getMasterLocation();
            s += " : te(" + te.xCoord + ","+te.yCoord +","+te.zCoord+") - M";
            s += te.blockID;

            Logger.info(s);
        }

        return true;
    }
}
