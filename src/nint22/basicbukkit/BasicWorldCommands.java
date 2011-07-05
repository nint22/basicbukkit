/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicWorldCommands.java
 Desc: Implements all major world-level commands.
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Location;
import org.bukkit.ChatColor;

public class BasicWorldCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Default constructor
    public BasicWorldCommands(BasicBukkit plugin)
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
        if(command.getName().compareToIgnoreCase("tp") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "tp"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Is there only one arg?
            if(args.length == 1)
            {
                // Does this player exist?
                Player target = plugin.getServer().getPlayer(args[0]);
                if(target == null)
                {
                    player.sendMessage(ChatColor.GRAY + "Given player does not exist");
                    return true;
                }
                
                // Change world as needed and location
                player.getLocation().setWorld(target.getWorld());
                player.teleport(target.getLocation());
                
                // Done
                player.sendMessage(ChatColor.GRAY + "You have been teleported to \"" + target.getName() + "\"");
                target.sendMessage(ChatColor.GRAY + "\"" + target.getName() + "\" has teleported to your location");
            }
            // Moving x to y
            if(args.length == 2)
            {
                // Does this player exist?
                Player target1 = plugin.getServer().getPlayer(args[0]);
                Player target2 = plugin.getServer().getPlayer(args[1]);
                if(target1 == null || target2 == null)
                {
                    player.sendMessage(ChatColor.GRAY + "Given player does not exist");
                    return true;
                }
                
                // Change world as needed and location
                target1.getLocation().setWorld(target2.getWorld());
                target1.teleport(target2.getLocation());
                
                // Done
                player.sendMessage(ChatColor.GRAY + "You have teleported \"" + target1.getName() + "\" to \"" + target2.getName() + "\"");
                target1.sendMessage(ChatColor.GRAY + "You have been teleported to \"" + target2.getName() + "\" by \"" + player.getName() + "\"");
                target2.sendMessage(ChatColor.GRAY + "\"" + target1.getName() + "\" has been teleported to you by \"" + player.getName() + "\"");
            }
            else
                return false;
        }
        else if(command.getName().compareToIgnoreCase("warp") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "warp"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Must have at least one argument
            if(args.length != 1)
            {
                // Generate a string of all the warps
                String warps = "";
                for(int i = 0; i < plugin.warps.GetWarpNames().size(); i++)
                {
                    warps += ChatColor.RED + plugin.warps.GetWarpNames().get(i);
                    if(i != plugin.warps.GetWarpNames().size() - 1)
                        warps += ChatColor.GRAY + ", ";
                }
                
                // Print warps..
                player.sendMessage(ChatColor.GRAY + "Available warps:");
                if(warps == null || warps.length() <= 0)
                    player.sendMessage(ChatColor.GRAY + "None");
                else
                    player.sendMessage(warps);
                return true;
            }
            
            // Does this warp exist?
            Location warp = plugin.warps.GetWarp(args[0]);
            if(warp != null)
            {
                player.teleport(warp);
            }
            else
            {
                player.sendMessage(ChatColor.GRAY + "Unable to warp to \"" + args[0] + "\"; warp not found");
                return true;
            }
        }
        else if(command.getName().compareToIgnoreCase("list") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "warp"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Generate a string of all the warps
            String warps = "";
            for(int i = 0; i < plugin.warps.GetWarpNames().size(); i++)
            {
                warps += ChatColor.RED + plugin.warps.GetWarpNames().get(i);
                if(i != plugin.warps.GetWarpNames().size() - 1)
                    warps += ChatColor.GRAY + ", ";
            }
            
            // Print warps..
            player.sendMessage(ChatColor.GRAY + "Available warps: (Type /who for a list of users)");
            if(warps == null || warps.length() <= 0)
                player.sendMessage(ChatColor.GRAY + "None");
            else
                player.sendMessage(warps);
            return true;
        }
        else if(command.getName().compareToIgnoreCase("setwarp") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "setwarp"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Must have at least one argument
            if(args.length != 1)
                return false;
            
            // Save this warp location
            plugin.warps.SetWarp(args[0], player.getLocation());
            player.sendMessage(ChatColor.GRAY + "Warp \"" + args[0] + "\" saved");
        }
        else if(command.getName().compareToIgnoreCase("delwarp") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "delwarp"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Must have at least one argument
            if(args.length != 1)
                return false;
            
            // Save this warp location
            plugin.warps.DelWarp(args[0]);
            player.sendMessage(ChatColor.GRAY + "Warp \"" + args[0] + "\" removed");
        }
        else if(command.getName().compareToIgnoreCase("home") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "home"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Is home defined?
            Location home = plugin.warps.GetHome(player.getName());
            if(home == null)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to warp home; home not set");
            }
            // Move player
            else
            {
                player.teleport(home);
                player.sendMessage(ChatColor.GRAY + "You have warped to your home location");
            }
        }
        else if(command.getName().compareToIgnoreCase("sethome") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "sethome"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Save this as the player's home
            plugin.warps.SetHome(player.getName(), player.getLocation());
            player.sendMessage(ChatColor.GRAY + "You have set your home location");
        }
        else if(command.getName().compareToIgnoreCase("spawn") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "spawn"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Is spawn defined?
            Location spawn = plugin.warps.GetSpawn();
            if(spawn == null)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to warp to spawn; spawn not set");
            }
            // Move player
            else
            {
                player.teleport(spawn);
                player.sendMessage(ChatColor.GRAY + "You have warped to spawn");
            }
        }
        else if(command.getName().compareToIgnoreCase("setspawn") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "setspawn"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Save this as the spawn
            plugin.warps.SetSpawn(player.getLocation());
            player.sendMessage(ChatColor.GRAY + "You have set the spawn location");
        }
        else if(command.getName().compareToIgnoreCase("top") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "top"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Find the highest block...
            int blockY = player.getWorld().getHighestBlockYAt(player.getLocation());
            
            // Change player to top
            Location top = player.getLocation();
            top.setY((double)blockY + 2.0);
            player.teleport(top);
            
            // Say where we now are
            player.sendMessage(ChatColor.GRAY + "You are now at a height of: " + (blockY + 2.0));
        }
        else if(command.getName().compareToIgnoreCase("jump") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "jump"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Find the block the user wants to get to...
            Location jumpLocation = plugin.warps.GetCollision(player, 100.0, 1.0f);
            if(jumpLocation == null)
            {
                // Failed to find target
                player.sendMessage(ChatColor.GRAY + "Unable to jump to location; no collision or too far away");
            }
            else
            {
                // Move up then teleport
                player.teleport(jumpLocation);
                player.sendMessage(ChatColor.GRAY + "Jumped to target location");
            }
        }
        // Else, unknown
        else
            return false;
        
        // Done - parsed
        return true;
    }
}
