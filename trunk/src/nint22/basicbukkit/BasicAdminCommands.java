/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicAdminCommands.java
 Desc: Implements all major admin commands.
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.World;
import java.util.*;

public class BasicAdminCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Default constructor
    public BasicAdminCommands(BasicBukkit plugin)
    {
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
        if(command.getName().compareToIgnoreCase("op") == 0)
        {
            // Command format: /op <player> [op level, defaults to 0]
            // There can only be either 1 or 2 args
            if(args.length < 1 || args.length > 2)
                return false;
            
            // Target group
            int GroupID = 0;
            
            // Change group ID
            if(args.length == 2)
            {
                try
                {
                    GroupID = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    // Just bug out
                    player.sendMessage(ChatColor.GRAY + "Unable to op: Invalid integer");
                    return true;
                }
            }
            
            // What is this players ID? Can ONLY op groups lower or equal to self
            int SelfGroupID = plugin.users.GetGroupID(player.getName());
            if(GroupID > SelfGroupID)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to op: You cannot op players to a higher ranking group than your own (" + SelfGroupID + ")");
                return true;
            }
            
            // Attempt to change group now
            plugin.users.SetUser(player.getName(), GroupID);
            player.sendMessage(ChatColor.GRAY + "You have set \"" + player.getName() + "\" to group #" + GroupID);
        }
        else if(command.getName().compareToIgnoreCase("kick") == 0)
        {
            // Do we have an arg?
            if(args.length > 0)
            {
                // For each arg
                for(int i = 0; i < args.length; i++)
                {
                    // Get player and kill if target
                    Player[] targetPlayer = plugin.getServer().getOnlinePlayers();
                    for(int j = 0; j < targetPlayer.length; j++)
                    {
                        // Ban + kick player
                        if(targetPlayer[j].getName().compareTo(args[i]) == 0)
                        {
                            targetPlayer[j].kickPlayer("Kicked from server.");
                        }
                    }
                }
            }
            // Else, fail
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("ban") == 0)
        {
            // Do we have an arg?
            if(args.length > 0)
            {
                // For each arg
                for(int i = 0; i < args.length; i++)
                {
                    // Get player and kill if target
                    Player[] targetPlayer = plugin.getServer().getOnlinePlayers();
                    for(int j = 0; j < targetPlayer.length; j++)
                    {
                        // Ban + kick player
                        if(targetPlayer[j].getName().compareTo(args[i]) == 0)
                        {
                            targetPlayer[j].kickPlayer("Kicked from server.");
                        }
                    }
                }
            }
            // Else, fail
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("who") == 0)
        {
            // Do we have an arg?
            if(args.length > 0)
            {
                // For each arg
                for(int i = 0; i < args.length; i++)
                {
                    // Get player and kill if target
                    Player[] targetPlayer = plugin.getServer().getOnlinePlayers();
                    for(int j = 0; j < targetPlayer.length; j++)
                    {
                        // Print IP
                        if(targetPlayer[j].getName().compareTo(args[i]) == 0)
                            player.sendMessage(ChatColor.GRAY + targetPlayer[j].getName() + ": " + targetPlayer[j].getAddress().getAddress().getHostAddress());
                    }
                }
            }
            // Else, fail
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("time") == 0)
        {
            // What are we setting to?
            String time = "";
            if(args.length > 0 && args[0] != null)
                time = args[0];
            
            // Get worlds list
            List<World> worlds = plugin.getServer().getWorlds();
            
            // Get arg if exists
            for(World world : worlds)
            {
                if(time.compareToIgnoreCase("dawn") == 0)
                    world.setTime(0);
                else if(time.compareToIgnoreCase("day") == 0)
                    world.setTime(6000);
                else if(time.compareToIgnoreCase("dusk") == 0)
                    world.setTime(12000);
                else if(time.compareToIgnoreCase("night") == 0)
                    world.setTime(37700);
                else
                    return false; // Failed
            }
            
            // Say we changed the weather
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "time set to " + time.toLowerCase());
        }
        else if(command.getName().compareToIgnoreCase("weather") == 0)
        {
            // What are we setting to?
            String weatherType = "";
            if(args.length > 0 && args[0] != null)
                weatherType = args[0];
            
            // Get worlds list
            List<World> worlds = plugin.getServer().getWorlds();
            
            // Get arg if exists
            for(World world : worlds)
            {
                if(weatherType.compareToIgnoreCase("dry") == 0)
                    world.setStorm(false);
                else if(weatherType.compareToIgnoreCase("wet") == 0)
                    world.setStorm(true);
                else
                    return false; // Failed
            }
            
            // Say we changed the weather
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "Weather set to " + weatherType.toLowerCase());
        }
        else if(command.getName().compareToIgnoreCase("kill") == 0)
        {
            // Do we have an arg?
            if(args.length > 0)
            {
                // For each arg
                for(int i = 0; i < args.length; i++)
                {
                    // Get player and kill if target
                    Player[] targetPlayer = plugin.getServer().getOnlinePlayers();
                    for(int j = 0; j < targetPlayer.length; j++)
                    {
                        if(targetPlayer[j].getName().compareTo(args[i]) == 0)
                        {
                            targetPlayer[j].setHealth(0);
                            targetPlayer[j].sendMessage(ChatColor.GRAY + "You have been killed by " + player.getName());
                        }
                    }
                }
            }
            // Else, kill self
            else
                player.setHealth(0);
        }
        else if(command.getName().compareToIgnoreCase("say") == 0)
        {
            // Send this message to all players
            if(args.length < 1)
            {
                player.sendMessage(ChatColor.GRAY + "You must say something!");
            }
            // Send to all
            else
            {
                // Form total string
                String message = " ";
                
                for(int i = 0; i < args.length; i++)
                    message += " " + args[i];
                
                // Send to all
                message = message.toString().replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
                plugin.getServer().broadcastMessage(ChatColor.RED + "Server:" + message);
            }
        }
        // Else, unknown
        else
            return false;
        
        // Done - parsed
        return true;
    }
}
