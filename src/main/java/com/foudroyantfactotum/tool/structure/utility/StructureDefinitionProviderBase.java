package com.foudroyantfactotum.tool.structure.utility;

import com.foudroyantfactotum.tool.structure.registry.StructureDefinition;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class StructureDefinitionProviderBase implements IStructureDefinitionProvider
{
    private ResourceLocation registryName;
    private StructureDefinition structureDefinition;
    private Map<IBlockState, List<float[]>> collisionBoxCache = Maps.newHashMap();

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
        collisionBoxCache.clear();
    }

    public List<float[]> getCollisionBoxes(IBlockState state) {
        List<float[]> collisionBoxes = collisionBoxCache
                .computeIfAbsent(state, blockState -> structureDefinition.getCollisionBoxes(blockState));
        return collisionBoxes;
    }

    @Override
    public float[] getSelectionBox(IBlockState state)
    {
        return structureDefinition.getSelectionBox(state);
    }

    public StructureDefinition getStructureDefinition() {
        if (this.structureDefinition == null) {
            rebuildStructure();

        }
        return this.structureDefinition;
    }

    @Override
    public Class<IStructureDefinitionProvider> getRegistryType()
    {
        return IStructureDefinitionProvider.class;
    }
}
