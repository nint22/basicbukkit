/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicProtectionCommands.java
 Desc: Implements all major protection commands; different from
 "BasicProtection.java" which manages (saves, removes, etc..)
 protection areas.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class BasicProtectionCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;
    
    // Temporary point buffer map
    // Key: username_1 for p1, username_2 for p2
    // 
    HashMap<String, Pair> TempPoints;

    // Default constructor
    public BasicProtectionCommands(BasicBukkit plugin)
    {
        this.plugin = plugin;
        
        // Allocate temp points
        TempPoints = new HashMap();
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
        if(plugin.IsCommand(player, command, args, "p1"))
        {
            // Save point
            Pair currentPos = new Pair(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            TempPoints.put(player.getName() + "_1", currentPos);
            
            player.sendMessage(ChatColor.GRAY + "First point saved for protection: (" + currentPos.x + ", " + currentPos.y + ")");
        }
        else if(plugin.IsCommand(player, command, args, "p2"))
        {
            // Save point
            Pair currentPos = new Pair(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
            TempPoints.put(player.getName() + "_2", currentPos);
            
            player.sendMessage(ChatColor.GRAY + "Second point saved for protection: (" + currentPos.x + ", " + currentPos.y + ")");
        }
        else if(plugin.IsCommand(player, command, args, "protect"))
        {
            // Do we have a name?
            if(args.length != 1)
            {
                player.sendMessage(ChatColor.GRAY + "You must give your protected area a name");
                return true;
            }
            
            // Get protection name
            String name = args[0];
            
            // Does the name already exist?
            if(plugin.protections.GetProtectionOwners(name) != null)
            {
                player.sendMessage(ChatColor.GRAY + "This name already is used for a protection");
                return true;
            }
            
            // Get the two pairs
            Pair p1 = TempPoints.get(player.getName() + "_1");
            Pair p2 = TempPoints.get(player.getName() + "_2");
            
            // Check pair points
            if(p1 == null || p2 == null)
            {
                player.sendMessage(ChatColor.GRAY + "You have yet to define your first and/or second points; use /p1 and/or /p2");
                return true;
            }
            
            // Good to insert
            if(plugin.protections.AddProtection(player, name, p1, p2))
            {
                // Good
                player.sendMessage(ChatColor.GRAY + "Your protected area \"" + name + "\" has been created");
            }
            else
            {
                // Failed
                player.sendMessage(ChatColor.GRAY + "Unable to create a protected area; it is too large! (Max: 128x128)");
            }
        }
        else if(plugin.IsCommand(player, command, args, "protectadd"))
        {
            // Must include name
            if(args.length != 2)
            {
                player.sendMessage(ChatColor.GRAY + "You must include the protected area and user name");
                return true;
            }
            
            // Add user
            LinkedList<String> owners = plugin.protections.GetProtectionOwners(args[0]);
            if(owners == null)
                player.sendMessage(ChatColor.GRAY + "Unable to find protected area \"" + args[0] + "\"");
            else
            {
                // Does the user exist?
                Player target = plugin.getServer().getPlayer(args[1]);
                if(target != null)
                {
                    owners.add(target.getName());
                    player.sendMessage(ChatColor.GRAY + "User \"" + target.getName() + "\" has been added to \"" + args[0] + "\"");
                }
                else
                {
                    owners.add(args[1]);
                    player.sendMessage(ChatColor.GRAY + "User \"" + args[1] + "\" has been added to \"" + args[0] + "\"");
                }
            }
        }
        else if(plugin.IsCommand(player, command, args, "protectrem"))
        {
            // Must include name
            if(args.length != 2)
            {
                player.sendMessage(ChatColor.GRAY + "You must include the protected area and user name");
                return true;
            }
            
            // Remove user
            LinkedList<String> owners = plugin.protections.GetProtectionOwners(args[0]);
            if(owners == null)
                player.sendMessage(ChatColor.GRAY + "Unable to find protected area \"" + args[0] + "\"");
            else
            {
                // Does the user exist?
                Player target = plugin.getServer().getPlayer(args[1]);
                if(target != null)
                {
                    owners.remove(target.getName());
                    player.sendMessage(ChatColor.GRAY + "User \"" + target.getName() + "\" has been removed from \"" + args[0] + "\"");
                }
                else
                {
                    owners.remove(args[1]);
                    player.sendMessage(ChatColor.GRAY + "User \"" + args[1] + "\" has been removed from \"" + args[0] + "\"");
                }
                
                // Do we self delete if there are no owners now?
                owners = plugin.protections.GetProtectionOwners(args[0]);
                if(owners == null || owners.size() <= 0)
                {
                    plugin.protections.RemoveProtection(args[0]);
                }
            }
            
        }
        else if(plugin.IsCommand(player, command, args, "protectdel"))
        {
            // Must include name
            if(args.length != 1)
            {
                player.sendMessage(ChatColor.GRAY + "You must include the protected area and user name");
                return true;
            }
            
            // Remove the entire protection
            LinkedList<String> owners = plugin.protections.GetProtectionOwners(args[0]);
            if(owners == null)
                player.sendMessage(ChatColor.GRAY + "Unable to find protected area \"" + args[0] + "\"");
            else
            {
                plugin.protections.RemoveProtection(args[0]);
                player.sendMessage(ChatColor.GRAY + "Protected area \"" + args[0] + "\" has been removed");
            }
        }
        else if(plugin.IsCommand(player, command, args, "protectinfo"))
        {
            // Are we on a protected area?
            String protection = plugin.protections.GetProtectionName(player.getLocation());
            if(protection != null)
            {
                // Tell the user the name and owners
                LinkedList<String> owners = plugin.protections.GetProtectionOwners(protection);
                String ownersString = "";
                for(int i = 0; i < owners.size(); i++)
                {
                    ownersString += owners.get(i);
                    if(i != owners.size() - 1)
                        ownersString += ", ";
                }
                
                // Tell the user this information
                player.sendMessage(ChatColor.GRAY + "You are in the protected zone \"" + protection + "\"");
                player.sendMessage(ChatColor.GRAY + "Owners: " + ChatColor.WHITE + ownersString);
            }
            else
            {
                // List all protection names
                LinkedList<String> names = plugin.protections.GetProtectedNames();
                String namesString = "";
                for(int i = 0; i < names.size(); i++)
                {
                    namesString += names.get(i);
                    if(i != names.size() - 1)
                        namesString += ", ";
                }
                
                // Tell the user this information
                player.sendMessage(ChatColor.GRAY + "All protected areas: (" + plugin.protections.GetProtectionCount() + ")");
                player.sendMessage(ChatColor.GRAY + "Protected area names: " + ChatColor.WHITE + namesString);
            }
        }
        
        // Done - parsed
        return true;
    }
}
