/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicWorldCommands.java
 Desc: Implements all major world-level commands.
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
        if(plugin.IsCommand(player, command, args, "tp"))
        {
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
                target.sendMessage(ChatColor.GRAY + "\"" + player.getName() + "\" has teleported to your location");
            }
            // Moving x to y
            else if(args.length == 2)
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
        else if(plugin.IsCommand(player, command, args, "warp"))
        {
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
                player.sendMessage(ChatColor.GRAY + "Available warps: (" + plugin.warps.GetWarpNames().size() + ")");
                if(warps == null || warps.length() <= 0)
                    player.sendMessage(ChatColor.GRAY + "None");
                else
                    player.sendMessage(warps);
                return true;
            }
            else
            {
                // Does this warp exist?
                Location warp = plugin.warps.GetWarp(args);
                if(warp != null)
                {
                    player.teleport(warp);
                    player.sendMessage(ChatColor.GRAY + "You have been warped to \"" + args[0] + "\"");
                }
                else
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to warp to \"" + args[0] + "\"; warp not found");
                    return true;
                }
            }
        }
        else if(plugin.IsCommand(player, command, args, "list"))
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
            player.sendMessage(ChatColor.GRAY + "Available warps: (Type /who for a list of users)");
            if(warps == null || warps.length() <= 0)
                player.sendMessage(ChatColor.GRAY + "None");
            else
                player.sendMessage(warps);
            return true;
        }
        else if(plugin.IsCommand(player, command, args, "setwarp"))
        {
            // Must have at least one argument
            if(args.length != 1)
                return false;
            
            // Save this warp location
            plugin.warps.SetWarp(args[0], player.getLocation());
            player.sendMessage(ChatColor.GRAY + "Warp \"" + args[0] + "\" saved");
        }
        else if(plugin.IsCommand(player, command, args, "delwarp"))
        {
            // Must have at least one argument
            if(args.length != 1)
                return false;
            
            // Save this warp location
            plugin.warps.DelWarp(args[0]);
            player.sendMessage(ChatColor.GRAY + "Warp \"" + args[0] + "\" removed");
        }
        else if(plugin.IsCommand(player, command, args, "home"))
        {
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
        else if(plugin.IsCommand(player, command, args, "sethome"))
        {
            // Save this as the player's home
            plugin.warps.SetHome(player.getName(), player.getLocation());
            player.sendMessage(ChatColor.GRAY + "You have set your home location");
        }
        else if(plugin.IsCommand(player, command, args, "spawn"))
        {
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
        else if(plugin.IsCommand(player, command, args, "setspawn"))
        {
            // Save this as the spawn
            plugin.warps.SetSpawn(player.getLocation());
            player.sendMessage(ChatColor.GRAY + "You have set the spawn location");
        }
        else if(plugin.IsCommand(player, command, args, "top"))
        {
            // Find the highest block...
            int blockY = GetHighestBlock(player.getLocation());
            
            // Change player to top
            Location top = player.getLocation();
            top.setY((double)blockY + 2.0);
            player.teleport(top);
            
            // Say where we now are
            player.sendMessage(ChatColor.GRAY + "You are now at a height of: " + (blockY + 2.0));
        }
        else if(plugin.IsCommand(player, command, args, "jump"))
        {
            // Find the block the user wants to get to...
            Location jumpLocation = plugin.warps.GetCollision(player, 150.0, 1.0f);
            if(jumpLocation == null)
            {
                // Failed to find target
                player.sendMessage(ChatColor.GRAY + "Unable to jump to location; no collision or too far away");
            }
            else
            {
                // Get block y
                int blockY = GetHighestBlock(jumpLocation);
                jumpLocation.setY(blockY + 2.0);
                
                // Get distance
                double Distance = jumpLocation.distance(player.getLocation());
                
                // Move up then teleport
                player.teleport(jumpLocation);
                player.sendMessage(ChatColor.GRAY + "Jumped to target location, distance: " + String.format("%.2f", Distance) + " blocks");
            }
        }
        
        // Done - parsed
        return true;
    }
    
    // Return the highest valid y position at the given location
    // I.e. searches for the highest non-air block from the top
    private int GetHighestBlock(Location location)
    {
        // Find the best "highest"
        int HighestY = location.getWorld().getHighestBlockYAt(location);
        
        // Is this glass or water? Keep moving up (i.e. y++) if needed
        while(true)
        {
            // Check this block
            Block block = location.getWorld().getBlockAt(location.getBlockX(), HighestY, location.getBlockZ());
            if(block.getType() == Material.GLASS || block.getType() == Material.WATER)
                HighestY++;
            else
                break;
        }
        
        // Best top position
        return HighestY;
    }
}
