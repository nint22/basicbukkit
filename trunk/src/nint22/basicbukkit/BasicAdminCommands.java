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
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "op"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
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
            
            // Get the target player's data
            Player targetPlayer = plugin.getServer().getPlayer(args[0]);
            if(targetPlayer == null)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to find player named \"" + targetPlayer.getName() + "\"");
                return true;
            }
            
            // Attempt to change group now
            else
            {
                if(plugin.users.SetUserGroup(targetPlayer.getName(), GroupID))
                    player.sendMessage(ChatColor.GRAY + "You have set \"" + targetPlayer.getName() + "\" to group ID " + GroupID + ", " + plugin.users.GetGroupName(targetPlayer.getName()) + "");
                else
                    player.sendMessage(ChatColor.GRAY + "Unable to assign \"" + targetPlayer.getName() + "\" to group ID " + GroupID);
            }
        }
        else if(command.getName().compareToIgnoreCase("kick") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "kick"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Just a name
            if(args.length == 1)
            {
                // Find player and click if neeeded
                Player toKick = plugin.getServer().getPlayer(args[0]);
                if(toKick != null)
                {
                    // Can only ban members of groups <= caller's groups
                    int banGroupID = plugin.users.GetGroupID(toKick.getName());
                    int myGroupID = plugin.users.GetGroupID(player.getName());
                    if(banGroupID > myGroupID)
                    {
                        plugin.users.UserBan(toKick.getName(), "Cannot ban users with a higher group ID (theirs: " + banGroupID + ", yours: " + myGroupID + ")");
                        return true;
                    }
                    
                    toKick.kickPlayer("Kicked from the server by \"" + player.getName() + "\".");
                    player.sendMessage("You have kicked player \"" + toKick.getName() + "\"");
                }
                else
                    player.sendMessage("Unable to kick; cannot find player \"" + toKick.getName() + "\"");
            }
            // Just a name and minutes
            else if(args.length == 2)
            {
                // Get total time (in minutes)
                int KickTime = 0;
                try
                {
                    KickTime = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    player.sendMessage("Unable to kick; unable parse time argument");
                    return true;
                }
                
                // Find player and click if neeeded
                Player toKick = plugin.getServer().getPlayer(args[0]);
                if(toKick != null)
                {
                    toKick.kickPlayer("Kicked from the server by \"" + player.getName() + "\" for " + KickTime + " minute(s).");
                    player.sendMessage("You have kicked player \"" + toKick.getName() + "\" for " + KickTime + " minute(s).");
                }
                else
                    player.sendMessage("Unable to kick; cannot find player \"" + toKick.getName() + "\"");
                
                // Save the time when the user can come back
                plugin.users.UserSetKickTime(player.getName(), KickTime);
            }
            // Else, fail
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("ban") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "ban"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Must have a user name and reason
            if(args.length == 1)
            {
                // Get player
                Player banPlayer = plugin.getServer().getPlayer(args[0]);
                
                if(banPlayer == null)
                    player.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\"");
                else
                {
                    // Can only ban members of groups <= caller's groups
                    int banGroupID = plugin.users.GetGroupID(banPlayer.getName());
                    int myGroupID = plugin.users.GetGroupID(player.getName());
                    if(banGroupID > myGroupID)
                    {
                        plugin.users.UserBan(banPlayer.getName(), "Cannot ban users with a higher group ID (theirs: " + banGroupID + ", yours: " + myGroupID + ")");
                        return true;
                    }
                    
                    plugin.users.UserBan(banPlayer.getName(), "No defined ban reason");
                    banPlayer.kickPlayer("Banned from the server by \"" + player.getName() + "\".");
                }
            }
            else if(args.length == 2)
            {
                // Get player
                Player banPlayer = plugin.getServer().getPlayer(args[0]);
                if(banPlayer == null)
                    player.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\"");
                else
                {
                    // Can only ban members of groups <= caller's groups
                    int banGroupID = plugin.users.GetGroupID(banPlayer.getName());
                    int myGroupID = plugin.users.GetGroupID(player.getName());
                    if(banGroupID > myGroupID)
                    {
                        plugin.users.UserBan(banPlayer.getName(), "Cannot ban users with a higher group ID (theirs: " + banGroupID + ", yours: " + myGroupID + ")");
                        return true;
                    }
                    
                    plugin.users.UserBan(banPlayer.getName(), args[1]);
                    banPlayer.kickPlayer("Banned from the server by \"" + player.getName() + "\".");
                }
            }
            // Else, fail
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("unban") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "unban"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Must have a user name
            if(args.length == 1)
            {
                // Remove player if found
                Player banPlayer = plugin.getServer().getPlayer(args[0]);
                if(banPlayer == null)
                    player.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\"");
                else
                    plugin.users.UserUnban(player.getName());
            }
            // Else, fail
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("who") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "who"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
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
            // Else, just list all players
            else
            {
                // Form users list
                String allPlayers = "";
                for(int i = 0; i < plugin.getServer().getOnlinePlayers().length; i++)
                {
                    String playerName = plugin.getServer().getOnlinePlayers()[i].getDisplayName();
                    allPlayers += playerName;
                    if(i != plugin.getServer().getOnlinePlayers().length - 1)
                        allPlayers += ", ";
                }
                
                // Replace colors..
                allPlayers = allPlayers.toString().replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
                
                // Print all
                player.sendMessage(ChatColor.GRAY + "Online Players:");
                player.sendMessage(allPlayers);
            }
        }
        else if(command.getName().compareToIgnoreCase("time") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "time"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
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
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "weather"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
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
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "kill"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
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
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "say"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
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
                plugin.getServer().broadcastMessage(ChatColor.RED + player.getName() + " says:" + message);
            }
        }
        else if(command.getName().compareToIgnoreCase("god") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "god"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Get current god state
            boolean IsGod = plugin.users.IsGod(player.getName());
            
            // Invert
            if(IsGod == true)
                IsGod = false;
            else
                IsGod = true;
            
            // Save god mode
            plugin.users.SetGod(player.getName(), IsGod);
            
            // Tell the player if it is on or off
            player.sendMessage(ChatColor.GRAY + "God mode has been turned " + (IsGod ? "on" : "off"));
        }
        // Else, unknown
        else
            return false;
        
        // Done - parsed
        return true;
    }
}
