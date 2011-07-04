/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicAdminCommands.java
 Desc: Lists all commands the player can execute as well as
 does some of the misc commands such as motd, etc..
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.ChatColor;
import java.util.*;

public class BasicMiscCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Default constructor
    public BasicMiscCommands(BasicBukkit plugin)
    {
        this.plugin = plugin;
    }
    
    // Help was called
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // Get player
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        
        // Parse each specific command supported
        if(command.getName().compareToIgnoreCase("help") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "help"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Get LinkedHashMap of the commands
            PluginDescriptionFile pdfFile = plugin.getDescription();
            LinkedHashMap Map = (LinkedHashMap)pdfFile.getCommands();
            
            // Get help list
            Collection collection = Map.values();
            
            // How many pages of help do we have?
            int Count = collection.size();
            int Pages = Count / 5;
            int PageIndex = 0;
            
            // Convert arg to int
            try
            {
                PageIndex = Integer.parseInt(args[0]) - 1;
            }
            catch(Exception e)
            {
                // Just default back to 0
                PageIndex = 0;
            }
            
            // To lower to the lowest page count..
            PageIndex = Math.min(Pages, PageIndex);
            
            // How page count and command count
            // Note the +1 offset so we are human friendly (i.e. we are 1..n rather than 0..n-1)
            player.sendMessage(ChatColor.WHITE + "Page " + ChatColor.RED + "[" + (PageIndex + 1) + "]" + ChatColor.WHITE + " of " + ChatColor.RED + "[" + (Pages+1) + "]" + ChatColor.WHITE +"; " + Count + " total commands");
            
            // Print off 5 commands for this page
            for(int i = PageIndex * 5; i < Math.min(Count, PageIndex * 5 + 5); i++)
            {
                // Command struct
                LinkedHashMap CommandDetails = (LinkedHashMap)collection.toArray()[i];
                
                // Print out info
                String name = CommandDetails.values().toArray()[1].toString();
                String description = CommandDetails.values().toArray()[0].toString();
                player.sendMessage(ChatColor.GRAY + "#" + (i+1) + ": " + ChatColor.RED + name + ChatColor.GRAY + " - " + description);
            }
        }
        else if(command.getName().compareToIgnoreCase("motd") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "motd"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Get the motd string
            String[] motd = plugin.configuration.getString("motd").split("\n");
            for(int i = 0; i < motd.length; i++)
            {
                // Colorize and print
                motd[i] = motd[i].replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
                player.sendMessage(motd[i]);
            }
        }
        else if(command.getName().compareToIgnoreCase("clear") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "clear"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Send enough empty lines to the client to
            // make sure we clear out the user's screen buffer
            for(int i = 0; i < 20; i++)
                player.sendMessage("");
        }
        else if(command.getName().compareToIgnoreCase("where") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "where"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Print to the player where her or she is at
            player.sendMessage(ChatColor.GRAY + "Your location: <" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + ">");
            
            String protectionName = plugin.protections.GetProtectionName(new Pair(player.getLocation().getBlockX(), player.getLocation().getBlockZ()));
            if(protectionName != null)
                player.sendMessage(ChatColor.GRAY + "You are in the protected area named \"" + protectionName + "\"");
        }
        // Unknown command
        else
            return false;
        
        // Done - parsed
        return true;
    }
}