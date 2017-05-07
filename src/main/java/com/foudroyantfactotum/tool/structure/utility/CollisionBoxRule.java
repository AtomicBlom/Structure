package com.foudroyantfactotum.tool.structure.utility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import java.util.function.Function;

public class CollisionBoxRule
{
    final Function<IBlockState, Boolean> rule;
    private final float[][] collisionBoxes;

    CollisionBoxRule(Function<IBlockState, Boolean> rule, float[]... collisionBoxes) {
        this.rule = rule;
        this.collisionBoxes = collisionBoxes;
    }

    public boolean matches(IBlockState state) {
        return rule.apply(state);
    }


    void adjustToMasterBlock(BlockPos masterPosition)
    {
        for (final float[] bb : collisionBoxes)
        {
            bb[0] -= masterPosition.getX(); bb[3] -= masterPosition.getX();
            bb[1] -= masterPosition.getY(); bb[4] -= masterPosition.getY();
            bb[2] -= masterPosition.getZ(); bb[5] -= masterPosition.getZ();
        }
    }

    public float[][] getCollisionBoxes()
    {
        return collisionBoxes;
    }
}
