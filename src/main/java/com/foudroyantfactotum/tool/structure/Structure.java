package com.foudroyantfactotum.tool.structure;

import com.foudroyantfactotum.tool.structure.coordinates.TransformLAG;
import com.foudroyantfactotum.tool.structure.net.StructureNetwork;
import com.foudroyantfactotum.tool.structure.utility.IStructureDefinitionProvider;
import com.foudroyantfactotum.tool.structure.utility.StructureLogger;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.registries.*;
import javax.annotation.Nullable;
import java.util.Map;


public class Structure
{
    private Structure() {}

    private static IForgeRegistry<IStructureDefinitionProvider> structureRegistry;

    /**
     * Handles the earliest initialization of Structure. Fires the RegisterStructureModIDEvent which
     * implementing mods must handle for structure to work properly.
     */
    public static void configure(String modId, SimpleNetworkWrapper networkChannel, int channelNumber)
    {
        Preconditions.checkNotNull(modId, "The mod ID was not specified.");
        Preconditions.checkNotNull(networkChannel, "The network channel was not provided.");

        StructureLogger.setModId(modId);
        TransformLAG.initStatic();
        StructureNetwork.init(networkChannel, channelNumber);

        //MinecraftForge.EVENT_BUS.register(new Structure());

        structureRegistry = new RegistryBuilder<IStructureDefinitionProvider>()
                .setName(new ResourceLocation(modId, "StructureDefinitionRegistry"))
                .setType(IStructureDefinitionProvider.class)
                .setIDRange(0, Short.MAX_VALUE)
                .add((owner, stage, id, obj, oldObj) -> obj.rebuildStructure())
                .create();
    }

    static int forceLoadRegisteredPatterns()
    {
        int reloadedStructures = 0;
        for (final IStructureDefinitionProvider structure : structureRegistry.getValues())
        {
            structure.rebuildStructure();

            reloadedStructures++;
        }
        return reloadedStructures;
    }

    public static IStructureDefinitionProvider getStructureDefinitionByRegistryName(ResourceLocation resourceLocation)
    {
        final IStructureDefinitionProvider value = structureRegistry.getValue(resourceLocation);

        if (value == null) {
            throw new StructureException("Could not locate Structure " + resourceLocation);
        }
        return value;
    }
}