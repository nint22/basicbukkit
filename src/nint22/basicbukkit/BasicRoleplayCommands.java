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
        
        if(plugin.IsCommand(player, command, args, "level") || plugin.IsCommand(player, command, args, "exp"))
        {
            // Get the player's experiance
            int Experiance = plugin.roleplay.GetExperiance(player);
            int Level = plugin.users.GetGroupID(player.getName());
            player.sendMessage(ChatColor.GRAY + "Level " + ChatColor.RED + Level + ChatColor.GRAY + "; experiance points: " + ChatColor.RED + String.format("%,d", Experiance));
        }
        else if(plugin.IsCommand(player, command, args, "ranks"))
        {
            // List all ranks
            int index = 0;
            for(String group : plugin.users.GetGroupNames())
            {
                // Get this groups exp
                int exp = plugin.users.GetGroupExp(group);
                player.sendMessage(ChatColor.GRAY + "Rank #" + index++ + ": \"" + group + ChatColor.GRAY + "\", required exp: " + ChatColor.RED + exp);
            }
        }
        
        // Done - parsed
        return true;
    }
}
