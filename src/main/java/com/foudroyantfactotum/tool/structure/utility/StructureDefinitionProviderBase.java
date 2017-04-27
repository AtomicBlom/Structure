package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import net.minecraft.util.ResourceLocation;
import javax.annotation.Nullable;

public abstract class StructureDefinitionProviderBase implements IStructureDefinitionProvider
{
    private ResourceLocation registryName;
    private StructureDefinition structureDefinition;

    @Override
    public IStructureDefinitionProvider setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    public abstract StructureDefinitionBuilder getStructureBuild();

    public void rebuildStructure() {
        structureDefinition = getStructureBuild().build();
        structureDefinition.getMasterBlock().setStructureDefinitionProvider(this);
    }

    public StructureDefinition getStructureDefinition() {
        if (this.structureDefinition == null) {
            rebuildStructure();

        }
        return this.structureDefinition;
    }

    @Override
    public Class<? super IStructureDefinitionProvider> getRegistryType()
    {
        return IStructureDefinitionProvider.class;
    }
}
