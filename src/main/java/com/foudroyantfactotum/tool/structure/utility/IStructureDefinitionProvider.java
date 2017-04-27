package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public interface IStructureDefinitionProvider extends IForgeRegistryEntry<IStructureDefinitionProvider>
{
    StructureDefinition getStructureDefinition();

    void rebuildStructure();
}
