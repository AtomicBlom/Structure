package com.foudroyantfactotum.tool.structure;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import java.util.List;

/***
 * Command (class) for reloading the structures in-game after jvm hot swap
 * Should only be enabled for development purposes.
 */
public class CommandReloadStructures implements ICommand
{
    private static final String COMMAND = "RELOAD_STRUCTURES";
    private static final List<String> aliases = Lists.newArrayList(COMMAND);

    @Override
    public String getName()
    {
        return COMMAND;
    }

    @Override
    public String getUsage(ICommandSender player)
    {
        return COMMAND;
    }

    @Override
    public List<String> getAliases()
    {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        int structuresReloaded = Structure.forceLoadRegisteredPatterns();
        sender.sendMessage(new TextComponentString("Reconstructed " + structuresReloaded + " structures"));
    }

    //restrict usage of development function
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender instanceof EntityPlayer)
        {
            final EntityPlayer ep = (EntityPlayer) sender;
            return ep.capabilities.isCreativeMode;
        }

        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

    @Override
    public int compareTo(ICommand iCommand)
    {
        return 0;
    }
}
