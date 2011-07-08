/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicAdminCommands.java
 Desc: Implements all major admin commands; these are commands called
 by special op groups and via the server's console interface.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

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
        /*** CONSOLE COMMANDS ***/
        if (sender instanceof ConsoleCommandSender)
        {
            // Say we are a console
            if(command.getName().equalsIgnoreCase("op"))
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
                        // Failed to parse as int, attempt to get by group name
                        GroupID = plugin.users.GetGroupIDByGroup(args[1]);
                        
                        // Just bug out if not valid
                        if(GroupID < 0)
                        {
                            sender.sendMessage(ChatColor.GRAY + "Unable to op: Invalid group ID or name");
                            return true;
                        }
                    }
                }
                
                // Get the target player's data
                Player targetPlayer = plugin.getServer().getPlayer(args[0]);
                if(targetPlayer == null)
                {
                    sender.sendMessage(ChatColor.GRAY + "Unable to find player named \"" + targetPlayer.getName() + "\"");
                    return true;
                }

                // Attempt to change group now
                else
                {
                    if(plugin.users.SetUserGroup(targetPlayer.getName(), GroupID))
                    {
                        sender.sendMessage(ChatColor.GRAY + "You have set \"" + targetPlayer.getName() + "\" to group ID " + GroupID + ", \"" + plugin.users.GetGroupName(targetPlayer.getName()) + "\"");
                        targetPlayer.sendMessage(ChatColor.GRAY + "You have been moved to group \"" + plugin.users.GetGroupName(targetPlayer.getName()) + "\", GID " + GroupID + "");
                    }
                    else
                        sender.sendMessage(ChatColor.GRAY + "Unable to assign \"" + targetPlayer.getName() + "\" to group ID " + GroupID);
                }
            }
            else if(command.getName().equalsIgnoreCase("kick"))
            {
                // Find player and click if neeeded
                Player toKick = plugin.getServer().getPlayer(args[0]);
                if(toKick != null)
                {
                    toKick.kickPlayer("Kicked from the server by the server administrator.");
                    sender.sendMessage("You have kicked player \"" + toKick.getName() + "\"");
                }
                else
                    sender.sendMessage("Unable to kick; cannot find player \"" + toKick.getName() + "\"");
            }
            else if(command.getName().equalsIgnoreCase("ban"))
            {
                // Must have a user name and reason
                if(args.length >= 1)
                {
                    // Get the ban reason
                    String reason = "No defined ban reason";
                    if(args.length > 1)
                    {
                        reason = "";
                        for(int i = 1; i < args.length; i++)
                            reason += args[i];
                    }
                    
                    // Attempt to kick
                    Player banPlayer = plugin.getServer().getPlayer(args[0]);
                    if(banPlayer == null)
                    {
                        sender.sendMessage("User \"" + args[0] + "\" banned but not kicked: user not found");
                        plugin.users.SetBan(args[0], reason);
                    }
                    else
                    {
                        sender.sendMessage("User \"" + banPlayer.getName() + "\" banned and kicked");
                        banPlayer.kickPlayer("Banned from the server by the server administrator's console.");
                        plugin.users.SetBan(banPlayer.getName(), reason);
                    }
                }
                // Else, fail
                else
                    return false;
            }
            else if(command.getName().equalsIgnoreCase("unban"))
            {
                // Must have a user name
                if(args.length == 1)
                {
                    // Remove player if found
                    if(plugin.users.SetUnban(args[0]))
                        sender.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\"");
                    else
                        sender.sendMessage(ChatColor.GRAY + "Player \"" + args[0] + "\" is now unbaned");
                }
            }
            else if(command.getName().equalsIgnoreCase("who"))
            {
                // Do we have an arg?
                if(args.length > 0)
                {
                    // For each arg
                    for(int i = 0; i < args.length; i++)
                    {
                        // Get player and get player
                        Player whoTarget = plugin.getServer().getPlayer(args[i]);
                        sender.sendMessage(ChatColor.GRAY + whoTarget.getName() + ": " + whoTarget.getAddress().getAddress().getHostAddress());
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
                    allPlayers = plugin.ColorString(allPlayers);

                    // Print all
                    sender.sendMessage(ChatColor.GRAY + "Online Players:");
                    sender.sendMessage(allPlayers);
                }
            }
            else if(command.getName().equalsIgnoreCase("say"))
            {
                // Send this message to all players
                if(args.length < 1)
                {
                    sender.sendMessage(ChatColor.GRAY + "You must say something!");
                }
                // Send to all
                else
                {
                    // Form total string
                    String message = " ";

                    for(int i = 0; i < args.length; i++)
                        message += " " + args[i];

                    // Send to all
                    message = plugin.ColorString(message);
                    plugin.BroadcastMessage("Server admin says:" + message);
                }
            }
            
            // Execute console command
            return true;
        }
        /*** PLAYER COMMANDS ***/
        else if (sender instanceof Player)
        {
            // Get player object
            Player player = (Player) sender;

            // Parse each specific command supported
            if(plugin.IsCommand(player, command, args, "op"))
            {
                // Command format: /op <player> [op level, defaults to 0]
                // There can only be either 1 or 2 args
                if(args.length < 1 || args.length > 2)
                {
                    // List groups
                    String groups = "";
                    for(int i = 0; i < plugin.users.GetGroupIDs().size(); i++)
                    {
                        groups += plugin.users.GetGroupIDs().get(i) + ": \"" + plugin.users.GetGroupNames().get(i) + "\"";
                        if(i != plugin.users.GetGroupIDs().size() - 1)
                            groups += ", ";
                    }
                    player.sendMessage(ChatColor.GRAY + "Existing groups: " + groups);
                    return false;
                }

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
                        // Failed to parse as int, attempt to get by group name
                        GroupID = plugin.users.GetGroupIDByGroup(args[1]);
                        
                        // Just bug out if not valid
                        if(GroupID < 0)
                        {
                            player.sendMessage(ChatColor.GRAY + "Unable to op: Invalid group ID or name");
                            return true;
                        }
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
                    {
                        player.sendMessage(ChatColor.GRAY + "You have set \"" + targetPlayer.getName() + "\" to group ID " + GroupID + ", \"" + plugin.users.GetGroupName(targetPlayer.getName()) + "\"");
                        targetPlayer.sendMessage(ChatColor.GRAY + "You have been moved to group \"" + plugin.users.GetGroupName(targetPlayer.getName()) + "\", GID " + GroupID + "");
                    }
                    else
                        player.sendMessage(ChatColor.GRAY + "Unable to assign \"" + targetPlayer.getName() + "\" to group ID " + GroupID);
                }
            }
            else if(plugin.IsCommand(player, command, args, "kick"))
            {
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
                            plugin.users.SetBan(toKick.getName(), "Cannot ban users with a higher group ID (theirs: " + banGroupID + ", yours: " + myGroupID + ")");
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
                    plugin.users.SetKickTime(player.getName(), KickTime);
                }
                // Else, fail
                else
                    return false;
            }
            else if(plugin.IsCommand(player, command, args, "ban"))
            {
                // Must have a user name and reason
                if(args.length >= 1)
                {
                    // Get the ban reason
                    String reason = "No defined ban reason";
                    if(args.length > 1)
                    {
                        reason = "";
                        for(int i = 1; i < args.length; i++)
                            reason += args[i];
                    }
                    
                    // Attempt to kick
                    plugin.users.SetBan(args[0], reason);
                    Player banPlayer = plugin.getServer().getPlayer(args[0]);
                    if(banPlayer == null)
                    {
                        player.sendMessage(ChatColor.GRAY + "User \"" + args[0] + "\" banned but not kicked: user not found");
                        plugin.users.SetBan(args[0], reason);
                    }
                    else
                    {
                        player.sendMessage(ChatColor.GRAY + "User \"" + banPlayer.getName() + "\" banned and kicked");
                        banPlayer.kickPlayer("Banned from the server by " + player.getName());
                        plugin.users.SetBan(banPlayer.getName(), reason);
                    }
                }
                // Else, fail
                else
                    return false;
            }
            else if(plugin.IsCommand(player, command, args, "unban"))
            {
                // Must have a user name
                if(args.length == 1)
                {
                    // Remove player if found
                    if(plugin.users.SetUnban(args[0]))
                        sender.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\"");
                    else
                        sender.sendMessage(ChatColor.GRAY + "Player \"" + args[0] + "\" is now unbaned");
                }
                // Else, fail
                else
                    return false;
            }
            else if(plugin.IsCommand(player, command, args, "who"))
            {
                // Do we have an arg?
                if(args.length > 0)
                {
                    // For each arg
                    for(int i = 0; i < args.length; i++)
                    {
                        // Get player and get player
                        Player whoTarget = plugin.getServer().getPlayer(args[i]);
                        player.sendMessage(ChatColor.GRAY + whoTarget.getName() + ": " + whoTarget.getAddress().getAddress().getHostAddress());
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
                    allPlayers = plugin.ColorString(allPlayers);

                    // Print all
                    player.sendMessage(ChatColor.GRAY + "Online Players:");
                    player.sendMessage(allPlayers);
                }
            }
            else if(plugin.IsCommand(player, command, args, "time"))
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
                plugin.BroadcastMessage(ChatColor.GRAY + "Time set to " + time.toLowerCase());
            }
            else if(plugin.IsCommand(player, command, args, "weather"))
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
                plugin.BroadcastMessage(ChatColor.GRAY + "Weather set to " + weatherType.toLowerCase());
            }
            else if(plugin.IsCommand(player, command, args, "kill"))
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
            else if(plugin.IsCommand(player, command, args, "say"))
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
                    message = plugin.ColorString(message);
                    plugin.BroadcastMessage(ChatColor.RED + player.getName() + " says:" + message);
                }
            }
            else if(plugin.IsCommand(player, command, args, "god") || plugin.IsCommand(player, command, args, "pvp"))
            {
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
            else if(plugin.IsCommand(player, command, args, "iclean"))
            {
                // Get the current world
                World world = player.getWorld();
                List<Entity> worldEntities = world.getEntities();
                
                int TotalRemoved = 0;
                for(Entity entity : worldEntities)
                {
                    // If craft item, remove
                    // Though this is not a good method, it prevents the
                    // need to include a new library (i.e. org.craftbukkit)
                    if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftItem"))
                    {
                        entity.remove();
                        TotalRemoved++;
                    }
                    // Remove vehicles
                    else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftBoat"))
                    {
                        entity.remove();
                        TotalRemoved++;
                    }
                    else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftMinecart"))
                    {
                        entity.remove();
                        TotalRemoved++;
                    }
                    else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftPoweredMinecart"))
                    {
                        entity.remove();
                        TotalRemoved++;
                    }
                    else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftStorageMinecart"))
                    {
                        entity.remove();
                        TotalRemoved++;
                    }
                }
                
                // How many did we remove?
                player.sendMessage(ChatColor.GRAY + "Removed a total of " + TotalRemoved + " items in this world");
            }
            
            // Done - parsed
            return true;
        }
        
        // Unknown sender...
        return true;
    }
}
