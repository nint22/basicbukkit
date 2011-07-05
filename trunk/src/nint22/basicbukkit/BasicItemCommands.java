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
import java.util.Map.Entry;

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
        else if(command.getName().compareToIgnoreCase("item") == 0 || command.getName().compareToIgnoreCase("i") == 0)
        {
            // Security check
            if(!plugin.users.CanExecute(player.getName(), "item"))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot use this command.");
                return true;
            }
            
            // We must have between 1-2 args
            if(args.length < 1)
                return false;
            
            // Item ID we want
            int ItemID = -1;
            int MetaID = 0;
            String SimpleName = "Unknown Name";
            
            // The first arg is either the item name OR the item number
            if(args[0].matches("\\d+"))
            {
                // Arg is integer
                try
                {
                    ItemID = Integer.parseInt(args[0]);
                }
                catch(Exception e)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to parse args[0] integer");
                    return true;
                }
                
                // Attempt to get the short-name
                Set<Entry<String, String>> AllData = plugin.itemNames.hashmap.entrySet();
                for(Object obj : AllData)
                {
                    // Value matches...
                    if(((Entry<String, String>)obj).getValue().equalsIgnoreCase(ItemID + "_0"))
                    {
                        SimpleName = ((Entry<String, String>)obj).getKey();
                        break;
                    }
                }
            }
            else
            {
                // Arg is item string
                if(plugin.itemNames.hashmap.containsKey(args[0].toLowerCase()))
                {
                    String ItemString = plugin.itemNames.hashmap.get(args[0].toLowerCase());
                    SimpleName = args[0].toLowerCase();
                    String[] StringData = ItemString.split("_");
                    try
                    {
                        ItemID = Integer.parseInt(StringData[0]);
                        MetaID = Integer.parseInt(StringData[1]);
                    }
                    catch(Exception e)
                    {
                        player.sendMessage(ChatColor.GRAY + "Unable to parse args[0] and args[1] integers");
                        return true;
                    }
                }
                else
                {
                    player.sendMessage(ChatColor.GRAY + "Unknown item reference");
                    return true;
                }
            }
            
            // Does this item exist at all?
            if(Material.getMaterial(ItemID) == null)
            {
                player.sendMessage(ChatColor.GRAY + "Item does not exist");
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
            
            player.getInventory().addItem(new ItemStack(ItemID, count));
            player.sendMessage(ChatColor.GRAY + "Given " + ChatColor.RED + count + ChatColor.GRAY + " of item " + ChatColor.RED + ItemID + " (" + SimpleName + ")");
            
            // All done...
            return true;
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
            
            // We must have between 2-3 args
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
            
            // Item ID we want
            int ItemID = -1;
            int MetaID = 0;
            String SimpleName = "Unknown Name";
            
            // The first arg is either the item name OR the item number
            if(args[1].matches("\\d+"))
            {
                // Arg is integer
                try
                {
                    ItemID = Integer.parseInt(args[1]);
                }
                catch(Exception e)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to parse args[1] integer");
                    return true;
                }
                
                // Attempt to get the short-name
                Set<Entry<String, String>> AllData = plugin.itemNames.hashmap.entrySet();
                for(Object obj : AllData)
                {
                    // Value matches...
                    if(((Entry<String, String>)obj).getValue().equalsIgnoreCase(ItemID + "_0"))
                    {
                        SimpleName = ((Entry<String, String>)obj).getKey();
                        break;
                    }
                }
            }
            else
            {
                // Arg is item string
                if(plugin.itemNames.hashmap.containsKey(args[1].toLowerCase()))
                {
                    String ItemString = plugin.itemNames.hashmap.get(args[1].toLowerCase());
                    SimpleName = args[1].toLowerCase();
                    String[] StringData = ItemString.split("_");
                    try
                    {
                        ItemID = Integer.parseInt(StringData[0]);
                        MetaID = Integer.parseInt(StringData[1]);
                    }
                    catch(Exception e)
                    {
                        player.sendMessage(ChatColor.GRAY + "Unable to parse args[1] and args[2] integers");
                        return true;
                    }
                }
                else
                {
                    player.sendMessage(ChatColor.GRAY + "Unknown item reference");
                    return true;
                }
            }
            
            // Does this item exist at all?
            if(Material.getMaterial(ItemID) == null)
            {
                player.sendMessage(ChatColor.GRAY + "Item does not exist");
                return true;
            }
            
            // Did we have an item count?
            int count = 64;
            if(args.length > 2)
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
            
            player.getInventory().addItem(new ItemStack(ItemID, count));
            player.sendMessage(ChatColor.GRAY + "Gave " + ChatColor.RED + count + ChatColor.GRAY + " of item " + ChatColor.RED + ItemID + " (" + SimpleName + ")" + ChatColor.GRAY + " to " + recieving.getName());
            recieving.sendMessage(ChatColor.GRAY + "Recieved " + ChatColor.RED + count + ChatColor.GRAY + " of item " + ChatColor.RED + ItemID + " (" + SimpleName + ")" + ChatColor.GRAY + " from " + player.getName());
            
            // All done...
            return true;
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
