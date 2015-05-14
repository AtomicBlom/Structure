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

package mod.steamnsteel.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import mod.steamnsteel.client.renderer.item.*;
import mod.steamnsteel.client.renderer.tileentity.*;
import mod.steamnsteel.library.ModBlock;
import mod.steamnsteel.tileentity.*;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

@SuppressWarnings({"MethodMayBeStatic", "WeakerAccess"})
public class ClientRenderProxy extends RenderProxy
{
    @Override
    public void init()
    {
        registerItemRenderers();
        registerTESRs();
    }

    @Override
    public int addNewArmourRenderers(String armor)
    {
        return RenderingRegistry.addNewArmourRendererPrefix(armor);
    }

    private void registerItemRenderers()
    {
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlock.ballMill), new BallMillItemRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlock.blastFurnace), new BlastFurnaceItemRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlock.boiler), new BoilerItemRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlock.cupola), new CupolaItemRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlock.chestPlotonium), new PlotoniumChestItemRenderer());
    }

    private void registerTESRs()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(BallMillTE.class, new BallMillTESR());
        ClientRegistry.bindTileEntitySpecialRenderer(BlastFurnaceTE.class, new BlastFurnaceTESR());
        ClientRegistry.bindTileEntitySpecialRenderer(BoilerTE.class, new BoilerTESR());
        ClientRegistry.bindTileEntitySpecialRenderer(CupolaTE.class, new CupolaTESR());
        ClientRegistry.bindTileEntitySpecialRenderer(PlotoniumChestTE.class, new PlotoniumChestTESR());

        ClientRegistry.bindTileEntitySpecialRenderer(ExampleTE.class, new ExampleTESR());
    }
}
