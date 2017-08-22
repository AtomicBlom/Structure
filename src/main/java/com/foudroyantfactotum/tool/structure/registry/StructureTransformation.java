package com.foudroyantfactotum.tool.structure.registry;

import net.minecraft.util.EnumFacing;

public class StructureTransformation
{
	private static final int[][][] rotationMatrices = {
			{{-1, 0}, {0, -1}}, //south
			{{0, 1}, {-1, 0}}, //west
			{{1, 0}, {0, 1}}, // north
			{{0, -1}, {1, 0}}, //east
	};

	private final StructureDefinition structureDefinition;
	private final EnumFacing facing;
	private final boolean mirrored;
	private final int[][] matrix;

	public StructureTransformation(StructureDefinition structureDefinition, EnumFacing facing, boolean mirrored)
	{
		if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
			facing = EnumFacing.NORTH;
		}

		this.structureDefinition = structureDefinition;
		this.facing = facing;
		this.mirrored = mirrored;

		this.matrix = setupMatrix(facing, mirrored);
	}

	private int[][] setupMatrix(EnumFacing facing, boolean mirrored)
	{
		final int horizontalIndex = facing.getHorizontalIndex();
		final int mirrorMultiplier = mirrored ? -1 : 1;
		int[][] results = {
				{rotationMatrices[horizontalIndex][0][0] * mirrorMultiplier,rotationMatrices[horizontalIndex][0][1] * mirrorMultiplier},
				{rotationMatrices[horizontalIndex][1][0] * mirrorMultiplier,rotationMatrices[horizontalIndex][1][1] * mirrorMultiplier}
		};
		return results;
	}

	public boolean isBlockPresent(int x, int y, int z)
	{
		final int tx = matrix[0][0] * x + matrix[0][1] * z;
		final int tz = matrix[1][0] * x + matrix[1][1] * z;

		return structureDefinition.hasBlockAt(tx, y, tz);
	}
}
