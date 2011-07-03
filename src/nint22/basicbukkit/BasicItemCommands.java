/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicItemCommands.java
 Desc: All item commands: /item /give, etc..
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class BasicItemCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Default constructor
    public BasicItemCommands(BasicBukkit plugin)
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
        
        // Get the kit items
        if(command.getName().compareToIgnoreCase("kit") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "kit"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Find all the items in the config file
            List<Object> kit = plugin.configuration.getList("kit");
            for(Object item : kit)
            {
                // Get the item ID and count
                int ItemID = 0;
                int ItemCount = 0;
                
                // Parse string
                String itemString = (String)item;
                String[] items = itemString.split(" ");
                
                // Error check
                if(items == null || items.length != 2 || items[0].length() <= 0 || items[1].length() <= 0)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to generate kit: Invalid data structure");
                    return true;
                }
                
                // convert to int
                try
                {
                    ItemID = Integer.parseInt(items[0]);
                    ItemCount = Integer.parseInt(items[1]);
                }
                catch(Exception e)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to generate kit: Invalid kit strings");
                    return true;
                }
                
                // Make sure the item exists
                if(Material.getMaterial(ItemID) == null)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to generate kit: Invalid Item ID");
                    return true;
                }
                
                // Check item count
                ItemCount = Math.min(ItemCount, 64);
                ItemCount = Math.max(ItemCount, 0);
                
                // Give this item to the player
                player.getInventory().addItem(new ItemStack(ItemID, ItemCount));
            }
            player.sendMessage(ChatColor.GRAY + "You have been given a kit of useful items");
        }
        // Parse each specific command supported
        else if(command.getName().compareToIgnoreCase("item") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "item"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Do we have arguments?
            if(args.length < 1)
                return false;
            
            // Default item is -1
            int ItemID = -1;
            
            // Check if number
            boolean isNumber = true;
            try
            {
                ItemID = Integer.parseInt(args[0]);
            }
            catch(Exception e)
            {
                isNumber = false;
            }
            
            // Is the argument only digits (ie. an item number?)
            if(isNumber)
            {
                // It is an item
                // ItemID already set
            }
            else if(plugin.itemNames.hashmap.containsKey(args[0].toLowerCase()))
            {
                // Known item name
                player.sendMessage(ChatColor.GRAY + "Item: " + args[0]);
                ItemID = Integer.parseInt((String)plugin.itemNames.hashmap.get(args[0]));
            }
            else
            {
                player.sendMessage(ChatColor.GRAY + "Unknown item");
                return true;
            }
            
            // Did we have an item count?
            int count = 64;
            if(args.length > 1)
            {
                try
                {
                    count = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    // Just ignore
                    count = 64;
                    player.sendMessage(ChatColor.GRAY + "Invalid item count");
                    return true;
                }
            }
            
            // Test if item is valid
            if(Material.getMaterial(ItemID) == null)
            {
                // Fail out
                player.sendMessage(ChatColor.GRAY + "Unknown item");
            }
            else
            {
                // Get item easy name
                String ItemName = "(Unknown name)";
                if(plugin.itemNames.hashmap.containsKey(args[0].toLowerCase()))
                    ItemName = "(" + args[0] + ")";

                // Give the user item as needed
                player.getInventory().addItem(new ItemStack(ItemID, count));
                player.sendMessage(ChatColor.GRAY + "Given " + ChatColor.RED + count + ChatColor.GRAY + " of item " + ChatColor.RED + ItemID + " " + ItemName);
            }
        }
        // Parse giving an item from player A to player B
        else if(command.getName().compareToIgnoreCase("give") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "give"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // Do we have arguments?
            if(args.length < 2)
                return false;
            
            // Get player name
            String targetPlayer = args[0];
            
            // Does this player exist on the server?
            Player recieving = plugin.getServer().getPlayer(targetPlayer); 
            if(recieving == null)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to find player \"" + targetPlayer + "\"");
                return true;
            }
            
            // Default item is -1
            int ItemID = -1;
            
            // Check if number
            boolean isNumber = true;
            try
            {
                ItemID = Integer.parseInt(args[2]);
            }
            catch(Exception e)
            {
                isNumber = false;
            }
            
            // Is the argument only digits (ie. an item number?)
            if(isNumber)
            {
                // It is an item
                // ItemID already set
            }
            else if(plugin.itemNames.hashmap.containsKey(args[1].toLowerCase()))
            {
                // Known item name
                player.sendMessage(ChatColor.GRAY + "Item: " + args[1]);
                ItemID = Integer.parseInt((String)plugin.itemNames.hashmap.get(args[1]));
            }
            else
            {
                player.sendMessage(ChatColor.GRAY + "Unknown item");
                return true;
            }
            
            // Did we have an item count?
            int count = 64;
            if(args.length > 1)
            {
                try
                {
                    count = Integer.parseInt(args[2]);
                }
                catch(Exception e)
                {
                    // Just ignore
                    count = 64;
                    player.sendMessage(ChatColor.GRAY + "Invalid item count");
                    return true;
                }
            }
            
            // Test if item is valid
            if(Material.getMaterial(ItemID) == null)
            {
                // Fail out
                player.sendMessage(ChatColor.GRAY + "Unknown item");
            }
            else
            {
                // Get item easy name
                String ItemName = "(Unknown name)";
                if(plugin.itemNames.hashmap.containsKey(args[1].toLowerCase()))
                    ItemName = "(" + args[1] + ")";

                // Give the user item as needed
                recieving.getInventory().addItem(new ItemStack(ItemID, count));
                recieving.sendMessage(ChatColor.GRAY + "Recieved " + ChatColor.RED + count + ChatColor.GRAY + " of item " + ChatColor.RED + ItemID + " " + ItemName + ChatColor.GRAY + " from " + player.getName());
                player.sendMessage(ChatColor.GRAY + "Giving " + ChatColor.RED + targetPlayer + " " + ChatColor.RED + count + ChatColor.GRAY + " of item " + ChatColor.RED + ItemID + " " + ItemName);
            }
        }
        // Clean all inventory for the play
        else if(command.getName().compareToIgnoreCase("clean") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "clean"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            ItemStack[] items = player.getInventory().getContents();
            for(int i = 0; i < items.length; i++)
            {
                // Is item and NOT in lower inventory
                if(items[i] != null && i >= 9)
                        items[i].setAmount(0);
            }
            
            // Set the inventory back
            player.getInventory().setContents(items);
            player.sendMessage(ChatColor.GRAY + "Inventory cleaned");
        }
        // Else, unknown
        else
            return false;
        
        // Done - parsed
        return true;
    }
}
