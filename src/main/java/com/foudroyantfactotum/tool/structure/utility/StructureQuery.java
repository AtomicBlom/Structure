package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.localToGlobal;
import static com.foudroyantfactotum.tool.structure.coordinates.TransformLAG.mutLocalToGlobal;

public final class StructureQuery {
    private StructureQuery() {}

    public static boolean canPlaceStructure(StructureBlock block, World world, BlockPos pos, EnumFacing orientation, boolean mirror, @Nullable List<BlockPos.MutableBlockPos> badLocations) {
        //find master block location
        StructureDefinition pattern = block.getPattern();
        final BlockPos hSize = pattern.getHalfBlockBounds();
        final BlockPos ml = pattern.getMasterLocation();

        BlockPos blockBounds = pattern.getBlockBounds();
        BlockPos origin
                = localToGlobal(
                -hSize.getX() + ml.getX(), ml.getY(), -hSize.getZ() + ml.getZ(),
                pos.getX(), pos.getY(), pos.getZ(),
                orientation, mirror, blockBounds);

        boolean isValid = true;
        //check block locations
        for (final BlockPos.MutableBlockPos local : pattern.getStructureItr())
        {
            if (!pattern.hasBlockAt(local))
            {
                continue;
            }

            mutLocalToGlobal(local, origin, orientation, mirror, blockBounds);

            if (!world.getBlockState(local).getBlock().isReplaceable(world, local))
            {
                isValid = false;
                if (badLocations != null) {
                    badLocations.add(new BlockPos.MutableBlockPos(local));
                }
            }
        }
        return isValid;
    }
}
