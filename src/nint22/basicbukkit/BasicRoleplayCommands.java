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
            long Experiance = plugin.roleplay.GetExperiance(player);
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
                long exp = plugin.users.GetGroupExp(group);
                player.sendMessage(ChatColor.GRAY + "Rank #" + index++ + ": \"" + group + ChatColor.GRAY + "\", required exp: " + ChatColor.RED + exp);
            }
        }
        else if(plugin.IsCommand(player, command, args, "addexp"))
        {
            // Check arg count
            if(args.length != 2)
                return false;
            
            // Attempt to find the player
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null)
            {
                target.sendMessage(ChatColor.GRAY + "Unable to find player \"" + args[0] + "\"");
                return true;
            }
            
            // Parse the experiance count
            long exp = 0;
            try
            {
                // Parse the given integer
                exp = Long.parseLong(args[1]);
            }
            catch(Exception e)
            {
                target.sendMessage(ChatColor.GRAY + "Unable to parse the given integer for the experience value");
                return true;
            }
            
            // Add experiance
            plugin.roleplay.AddExperiance(target, exp);
            player.sendMessage(ChatColor.GRAY + "You have added " + exp + " exp. points to \"" + target.getName() + "\"");
            target.sendMessage(ChatColor.GRAY + "You have been given " + exp + " exp. points by \"" + player.getName() + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "remexp"))
        {
            // Check arg count
            if(args.length != 2)
                return false;
            
            // Attempt to find the player
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null)
            {
                target.sendMessage(ChatColor.GRAY + "Unable to find player \"" + args[0] + "\"");
                return true;
            }
            
            // Parse the experiance count
            long exp = 0;
            try
            {
                // Parse the given integer
                exp = Long.parseLong(args[1]);
            }
            catch(Exception e)
            {
                target.sendMessage(ChatColor.GRAY + "Unable to parse the given integer for the experience value");
                return true;
            }
            
            // Invert the signs so it is always negative
            if(exp > 0)
                exp = -exp;
            
            // Remove experiance
            plugin.roleplay.AddExperiance(target, exp);
            player.sendMessage(ChatColor.GRAY + "You have removed " + exp + " exp. points from \"" + target.getName() + "\"");
            target.sendMessage(ChatColor.GRAY + "You have been removed " + exp + " exp. points by \"" + player.getName() + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "setexp"))
        {
            // Check arg count
            if(args.length != 2)
                return false;
            
            // Attempt to find the player
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null)
            {
                target.sendMessage(ChatColor.GRAY + "Unable to find player \"" + args[0] + "\"");
                return true;
            }
            
            // Parse the experiance count
            long exp = 0;
            try
            {
                // Parse the given integer
                exp = Long.parseLong(args[1]);
            }
            catch(Exception e)
            {
                target.sendMessage(ChatColor.GRAY + "Unable to parse the given integer for the experience value");
                return true;
            }
            
            // Remove experiance
            plugin.roleplay.SetExperiance(target.getName(), exp);
            player.sendMessage(ChatColor.GRAY + "\"" + target.getName() + "\"'s exp. points set to " + exp);
            target.sendMessage(ChatColor.GRAY + "Your exp. points are now set to " + exp + " by \"" + player.getName() + "\"");
        }
        
        // Done - parsed
        return true;
    }
}
