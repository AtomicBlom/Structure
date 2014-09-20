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

package mod.steamnsteel.block.resource.structure;

import mod.steamnsteel.TheMod;
import mod.steamnsteel.block.SteamNSteelBlock;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.utility.Vector;
import mod.steamnsteel.utility.log.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class PlotoniumRuinWall extends SteamNSteelBlock
{
    public static final String NAME = "ruinWallPlotonium";

	private HashMap<Integer, IIcon> icons2 = new HashMap<Integer, IIcon>();

	final int DEFAULT = 0;
	final int TOP = 1;
	final int BOTTOM = 2;
	final int LEFT = 4;
	final int RIGHT = 8;

	int[][] ROTATION_MATRIX = {
			{0,0,0,0,0,0,6},
			{0,0,0,0,0,0,6},
			{5,4,2,3,0,1,6},
			{4,5,3,2,0,1,6},
			{2,3,4,5,0,1,6},
			{3,2,5,4,0,1,6}
	};


	final int TOP_EDGE = 1;
	final int BOTTOM_EDGE = 2;
	final int LEFT_EDGE = 4;
	final int RIGHT_EDGE = 8;
	final int SINGLE = LEFT_EDGE | RIGHT_EDGE;

	final int NO_FEATURE = 0;
	final int PLATE_FEATURE = 16;
	final int PIPE_FEATURE = 32;
	final int FEATURE_MASK = PLATE_FEATURE | PIPE_FEATURE;

	final int LEFT_FEATURE_EDGE = 64;
	final int RIGHT_FEATURE_EDGE = 128;
	final int TOP_FEATURE_EDGE = 256;
	final int BOTTOM_FEATURE_EDGE = 512;

	final int MISSING_TEXTURE = Integer.MAX_VALUE;
	final int ERASE_FEATURE_DATA = TOP_EDGE | BOTTOM_EDGE | LEFT_EDGE | RIGHT_EDGE;

	public void registerBlockIcons(IIconRegister iconRegister) {
		icons2.clear();
		icons2.put(TOP, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-Top"));
		icons2.put(TOP | LEFT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-TopL"));
		icons2.put(TOP | RIGHT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-TopR"));
		icons2.put(TOP | SINGLE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-TopSingle"));

		icons2.put(DEFAULT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall"));
		icons2.put(LEFT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall"));
		icons2.put(RIGHT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall"));
		icons2.put(LEFT | RIGHT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall"));

		icons2.put(BOTTOM, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-Bottom"));
		icons2.put(BOTTOM | LEFT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-BottomL"));
		icons2.put(BOTTOM | RIGHT, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-BottomR"));
		icons2.put(BOTTOM | SINGLE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-BottomSingle"));

		icons2.put(PLATE_FEATURE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall"));
		icons2.put(PLATE_FEATURE | LEFT_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateL"));
		icons2.put(PLATE_FEATURE | LEFT_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateL"));
		icons2.put(PLATE_FEATURE | RIGHT_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateR"));
		icons2.put(PLATE_FEATURE | RIGHT_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateR"));
		icons2.put(PLATE_FEATURE | TOP_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateT"));
		icons2.put(PLATE_FEATURE | BOTTOM_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateB"));

		icons2.put(PLATE_FEATURE | LEFT_FEATURE_EDGE | BOTTOM_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateBL"));
		icons2.put(PLATE_FEATURE | LEFT_EDGE | BOTTOM_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateBL"));
		icons2.put(PLATE_FEATURE | RIGHT_FEATURE_EDGE | BOTTOM_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateBR"));
		icons2.put(PLATE_FEATURE | RIGHT_EDGE | BOTTOM_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateBR"));
		icons2.put(PLATE_FEATURE | RIGHT_EDGE | BOTTOM_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateTR"));
		icons2.put(PLATE_FEATURE | LEFT_FEATURE_EDGE | TOP_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateTL"));
		icons2.put(PLATE_FEATURE | RIGHT_FEATURE_EDGE | TOP_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateTR"));
		icons2.put(PLATE_FEATURE | RIGHT_EDGE | TOP_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateTR"));
		icons2.put(PLATE_FEATURE | RIGHT_EDGE | TOP_FEATURE_EDGE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-PlateTR"));

		icons2.put(MISSING_TEXTURE, iconRegister.registerIcon(TheMod.MOD_ID + ":" + "blockPlotoniumWall-Missing"));
	}

	@Override
	public void onBlockClicked(World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_, EntityPlayer p_149699_5_) {
		super.onBlockClicked(p_149699_1_, p_149699_2_, p_149699_3_, p_149699_4_, p_149699_5_);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		int blockProperties = getTexturePropertiesForSide(world, x, y, z, side);
		String description = describeTextureProperties(blockProperties);
		player.addChatComponentMessage(new ChatComponentText(description));
		return super.onBlockActivated(world, x, y, z, player, side, p_149727_7_, p_149727_8_, p_149727_9_);
	}

	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		//Logger.info("%d, %d, %d, %d", );

		int blockProperties = getTexturePropertiesForSide(blockAccess, x, y, z, side);

		if (!icons2.containsKey(blockProperties)) {
			String blockPropertiesDescription = describeTextureProperties(blockProperties);

			Logger.warning("Unknown Ruin Wall bitmap: %d (%s) - %s @ (%d, %d, %d) - %d", blockProperties, Integer.toBinaryString(blockProperties), blockPropertiesDescription, x, y, z, side);

			blockProperties = MISSING_TEXTURE;
		}
		return icons2.get(blockProperties);
	}

	private String describeTextureProperties(int blockProperties) {
		StringBuilder sb = new StringBuilder();
		if ((blockProperties & TOP_EDGE) == TOP_EDGE) {
			sb.append("TE,");
		}
		if ((blockProperties & BOTTOM_EDGE) == BOTTOM_EDGE) {
			sb.append("BE,");
		}
		if ((blockProperties & LEFT_EDGE) == LEFT_EDGE) {
			sb.append("LE,");
		}
		if ((blockProperties & RIGHT_EDGE) == RIGHT_EDGE) {
			sb.append("RE,");
		}

		if ((blockProperties & PLATE_FEATURE) == PLATE_FEATURE) {
			sb.append("Plate,");
		}
		if ((blockProperties & PIPE_FEATURE) == PIPE_FEATURE) {
			sb.append("Pipe,");
		}
		if ((blockProperties & LEFT_FEATURE_EDGE) == LEFT_FEATURE_EDGE) {
			sb.append("LFE,");
		}
		if ((blockProperties & RIGHT_FEATURE_EDGE) == RIGHT_FEATURE_EDGE) {
			sb.append("RFE,");
		}
		if ((blockProperties & TOP_FEATURE_EDGE) == TOP_FEATURE_EDGE) {
			sb.append("TFE,");
		}
		if ((blockProperties & BOTTOM_FEATURE_EDGE) == BOTTOM_FEATURE_EDGE) {
			sb.append("BFE,");
		}
		return sb.toString();
	}

	private int getTexturePropertiesForSide(IBlockAccess blockAccess, int x, int y, int z, int side) {
		try {
			ForgeDirection orientation = ForgeDirection.getOrientation(side);
			if (orientation == ForgeDirection.UP || orientation == ForgeDirection.DOWN) {
				return DEFAULT;
			}


			int[] rotationMatrix = ROTATION_MATRIX[side];

			ForgeDirection left = ForgeDirection.getOrientation(rotationMatrix[0]);
			ForgeDirection right = ForgeDirection.getOrientation(rotationMatrix[1]);
			ForgeDirection back = ForgeDirection.getOrientation(rotationMatrix[2]);
			ForgeDirection forward = ForgeDirection.getOrientation(rotationMatrix[3]);

			ForgeDirection above = ForgeDirection.getOrientation(rotationMatrix[5]);
			ForgeDirection below = ForgeDirection.getOrientation(rotationMatrix[4]);


			Block blockLeft = blockAccess.getBlock(x + left.offsetX, y + left.offsetY, z + left.offsetZ);
			Block blockRight = blockAccess.getBlock(x + right.offsetX, y + right.offsetY, z + right.offsetZ);
			Block blockAbove = blockAccess.getBlock(x + above.offsetX, y + above.offsetY, z + above.offsetZ);
			Block blockBelow = blockAccess.getBlock(x + below.offsetX, y + below.offsetY, z + below.offsetZ);
			Block blockBackAndUp = blockAccess.getBlock(x + above.offsetX + back.offsetX, y + above.offsetY + back.offsetY, z + above.offsetZ + back.offsetZ);
			Block blockBackAndLeft = blockAccess.getBlock(x + left.offsetX + back.offsetX, y + left.offsetY + back.offsetY, z + left.offsetZ + back.offsetZ);
			Block blockBackAndRight = blockAccess.getBlock(x + right.offsetX + back.offsetX, y + right.offsetY + back.offsetY, z + right.offsetZ + back.offsetZ);

			int blockProperties = 0;
			if (!blockAbove.getMaterial().isOpaque() || blockBackAndUp.getMaterial().isOpaque()) {
				blockProperties |= TOP_EDGE;
			}
			if (blockBelow != ModBlock.ruinWallPlotonium && blockBelow.getMaterial().isOpaque()) {
				blockProperties = BOTTOM_EDGE;
			}
			if (blockLeft != ModBlock.ruinWallPlotonium || blockBackAndLeft.getMaterial().isOpaque()) {
				blockProperties |= LEFT_EDGE;
			}

			if (blockRight != ModBlock.ruinWallPlotonium || blockBackAndRight.getMaterial().isOpaque()) {
				blockProperties |= RIGHT_EDGE;
			}

			int featureId = getSideFeature(x, y, z);
			if (featureId != NO_FEATURE) {
				blockProperties |= featureId;
				if (getSideFeature(x + left.offsetX, y + left.offsetY, z + left.offsetZ) != featureId) {
					blockProperties |= LEFT_FEATURE_EDGE;
				}
				if (getSideFeature(x + right.offsetX, y + right.offsetY, z + right.offsetZ) != featureId) {
					blockProperties |= RIGHT_FEATURE_EDGE;
				}
				if (getSideFeature(x + above.offsetX, y + above.offsetY, z + above.offsetZ) != featureId) {
					blockProperties |= TOP_FEATURE_EDGE;
				}
				if (getSideFeature(x + below.offsetX, y + below.offsetY, z + below.offsetZ) != featureId) {
					blockProperties |= BOTTOM_FEATURE_EDGE;
				}

				if ((blockProperties & (TOP_FEATURE_EDGE | BOTTOM_FEATURE_EDGE)) == (TOP_FEATURE_EDGE | BOTTOM_FEATURE_EDGE)) {
					blockProperties &= ERASE_FEATURE_DATA;
				}

				if ((blockProperties & (LEFT_FEATURE_EDGE | RIGHT_FEATURE_EDGE)) == (LEFT_FEATURE_EDGE | RIGHT_FEATURE_EDGE)) {
					blockProperties &= ERASE_FEATURE_DATA;
				}
			}

			return blockProperties;
		} catch (Exception e) {
			e.printStackTrace();
			return MISSING_TEXTURE;
		}
	}

	HashMap<Vector<Integer>, double[]> cachedNoiseGens = new HashMap<Vector<Integer>, double[]>();
	NoiseGeneratorOctaves noiseGen = new NoiseGeneratorOctaves(new Random(1L), 5);

	//x, y, z in world coordinates
	private double[] getNoiseGen(int x, int y, int z) {
		x = (x >> 4) << 4;
		z = (z >> 4) << 4;
		final Vector<Integer> integerVector = new Vector<Integer>(x, 0, z);
		double[] noiseData = cachedNoiseGens.get(integerVector);
		if (noiseData == null) {
			/**
			 * pars:(par2,3,4=noiseOffset ; so that adjacent noise segments connect) (pars5,6,7=x,y,zArraySize),(pars8,10,12 =
			 * x,y,z noiseScale)
			 */
			noiseData = new double[16*256*16];
			noiseGen.generateNoiseOctaves(noiseData, x, 0, z, 16, 256, 16, 3, 3, 3);
			cachedNoiseGens.put(integerVector, noiseData);
		}
		return noiseData;
	}

	private int getSideFeature(int x, int y, int z) {
		double[] noiseData = getNoiseGen(x, y, z);
		x = x & 16;
		z = z & 16;

		//Flat[x + WIDTH * (y + DEPTH * z)] = Original[x, y, z]
		final int i = y + 16 * (x + 16 * z);
		double featureNoise = noiseData[i];

		if (featureNoise > -17.8877 && featureNoise < -10.6177) {
			return PLATE_FEATURE;
		} else if (featureNoise >= -2.9167 && featureNoise < 0.403) {
			return PLATE_FEATURE;
		}
		return 0;
	}

	public PlotoniumRuinWall()
    {
        super(Material.rock);
        setBlockName(NAME);


    }
}
