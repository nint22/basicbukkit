/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicRoleplayCommands.java
 Desc: Implements all roleplay commands ranging from land control
 to money and bank management.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class BasicRoleplayCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;
    
    // Default constructor
    public BasicRoleplayCommands(BasicBukkit plugin)
    {
        // Save plugin handler
        this.plugin = plugin;
    }
    
    // General admin commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // Get player
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        
        // Parse each specific command supported
        if(plugin.IsCommand(player, command, args, "buy"))
        {
            // TODO...
            return false;
        }
        else if(plugin.IsCommand(player, command, args, "sell"))
        {
            // TODO...
            return false;
        }
        else if(plugin.IsCommand(player, command, args, "money"))
        {
            // TODO...
            return false;
        }
        else if(plugin.IsCommand(player, command, args, "level"))
        {
            // TODO...
            return false;
        }
                
        else if(plugin.IsCommand(player, command, args, "kjoin"))
        {
            // TODO...
            return false;
        }
        else if(plugin.IsCommand(player, command, args, "kleave"))
        {
            // TODO...
            return false;
        }
        else if(plugin.IsCommand(player, command, args, "kkick"))
        {
            // TODO...
            return false;
        }
        
        // Done - parsed
        return true;
    }
}
