package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import java.util.List;

public interface IStructureDefinitionProvider extends IForgeRegistryEntry<IStructureDefinitionProvider>
{
    StructureDefinition getStructureDefinition();

    void rebuildStructure();

    List<float[]> getCollisionBoxes(IBlockState state);
    float[] getSelectionBox(IBlockState state);
}
