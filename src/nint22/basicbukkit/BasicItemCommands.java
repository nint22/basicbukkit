/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicItemCommands.java
 Desc: All item commands: /item /give, etc..
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

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
        if(plugin.IsCommand(player, command, args, "kit"))
        {
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
        else if(plugin.IsCommand(player, command, args, "item") || plugin.IsCommand(player, command, args, "i"))
        {
            // We must have between 1-2 args
            if(args.length < 1)
                return false;
            
            // Create items
            ItemStack newItems = CreateItems(player, args);
            if(newItems == null)
                return true;
            
            // Give items to player
            player.getInventory().addItem(newItems);
            player.sendMessage(ChatColor.GRAY + "Given " + ChatColor.RED + newItems.getAmount() + ChatColor.GRAY + " of item " + ChatColor.RED + newItems.getType().getId() + " (" + newItems.getType().name() + ")");

            // All done...
            return true;
        }
        // Parse giving an item from player A to player B
        else if(plugin.IsCommand(player, command, args, "give"))
        {
            // We must have at least 2 args
            if(args.length < 2)
                return false;
            
            // Does the target player exist?
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null)
            {
                player.sendMessage(ChatColor.GRAY + "Player \"" + args[0] + "\" does not exist");
                return true;
            }
            
            // Create items
            ItemStack newItems = CreateItems(player, target, args);
            if(newItems == null)
                return true;
            
            // Give items to player
            target.getInventory().addItem(newItems);
            player.sendMessage(ChatColor.GRAY + "Gave \"" + target.getName() + "\" " + ChatColor.RED + newItems.getAmount() + ChatColor.GRAY + " of item " + ChatColor.RED + newItems.getType().getId() + " (" + newItems.getType().name() + ")");
            target.sendMessage(ChatColor.GRAY + "Recieved from \"" + player.getName() + "\" " + ChatColor.RED + newItems.getAmount() + ChatColor.GRAY + " of item " + ChatColor.RED + newItems.getType().getId() + " (" + newItems.getType().name() + ")");
            
            // All done...
            return true;
        }
        // Clean all inventory for the play
        else if(plugin.IsCommand(player, command, args, "clean"))
        {
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
        // Clean all inventory
        else if(plugin.IsCommand(player, command, args, "cleanall"))
        {
            ItemStack[] items = player.getInventory().getContents();
            for(int i = 0; i < items.length; i++)
                if(items[i] != null)
                    items[i].setAmount(0);
            
            // Set the inventory back
            player.getInventory().setContents(items);
            player.sendMessage(ChatColor.GRAY + "Entire inventory cleaned");
        }
        
        // Done - parsed
        return true;
    }
    
    // Forwards to the longer CreateItems function with the reciever options
    // Written short-hand function for /item (since it doesn't have a receiver,
    // the reciever IS the caller
    private ItemStack CreateItems(Player caller, String[] args)
    {
        return CreateItems(caller, null, args);
    }
    
    // Private function that creates an item stack; this function was
    // create to help merge the two code blocks found in /item and /give
    // Returns null on error; writes the error to the player itself
    // Does NOT actually give items, it is up to the specific /item and /give blocks
    // Note: if doing /item, reciever MUST be null
    private ItemStack CreateItems(Player caller, Player reciever, String[] args)
    {
        // If we have a reciever, shift all args back once
        if(reciever != null)
        {
            String[] newArgs = new String[args.length - 1];
            for(int i = 1; i < args.length; i++)
                newArgs[i-1] = args[i];
            args = newArgs;
        }
        
        // Item ID we want (ItemID[:MetaID])
        // Note that MetaID, when used (i.e. non zero) is set via
        // the "setDurability" since items are essentially unions
        // of block data types (which also use Meta)
        int ItemID = -1;
        int MetaID = 0;
        String SimpleName = "Unknown Name";
        
        // The first arg is either the item name OR the item number
        // Is item num,ber
        if(args[0].matches("\\d+") || args[0].matches("\\d+:\\d+"))
        {
            // Attempt to get integers
            try
            {
                // Is it an item id AND meta
                if(args[0].indexOf(":") >= 0)
                {
                    String[] split = args[0].split(":");
                    ItemID = Integer.parseInt(split[0]);
                    MetaID = Integer.parseInt(split[1]);
                }
                // Just item ID with default meta ID
                else
                {
                    ItemID = Integer.parseInt(args[0]);
                    MetaID = 0;
                }
            }
            catch(Exception e)
            {
                caller.sendMessage(ChatColor.GRAY + "Unable to item ID; assuming it is integer");
                return null;
            }

            // Attempt to find the short name
            SimpleName = plugin.itemNames.FindItem(ItemID, MetaID);
        }
        // Very likely an item string name
        else
        {
            // Get the full item name
            String ItemString = plugin.itemNames.FindItem(args[0].toLowerCase());
            
            // Does this item exist?
            if(ItemString != null)
            {
                // Get the item data information which is encoded as "ItemID:MetaID"
                SimpleName = args[0].toLowerCase();
                String[] StringData = ItemString.split(":");
                
                // Attempt to convert to Item and Meta ID
                try
                {
                    ItemID = Integer.parseInt(StringData[0]);
                    MetaID = Integer.parseInt(StringData[1]);
                }
                catch(Exception e)
                {
                    caller.sendMessage(ChatColor.GRAY + "Unable to parse the Item and Meta ID of your object from items.csv");
                    return null;
                }
            }
            // Else, this string is simply unknown
            else
            {
                caller.sendMessage(ChatColor.GRAY + "Unknown item name or reference");
                return null;
            }
        }
        
        // Done getting the Item and Meta ID
        // Check for anything bizzare
        if(ItemID <= 0)
        {
            caller.sendMessage(ChatColor.GRAY + "Cannot create this item");
            return null;
        }

        
        // Does this item exist at all?
        if(Material.getMaterial(ItemID) == null)
        {
            caller.sendMessage(ChatColor.GRAY + "Item does not exist");
            return null;
        }
        
        // Did we have an item count?
        int count = 64;
        if(args.length > 1)
        {
            try
            {
                // Cast to get count
                count = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                caller.sendMessage(ChatColor.GRAY + "Invalid item count");
                return null;
            }
        }
        
        // Can this user use banned item?
        if(!plugin.users.CanUseItem(ItemID, MetaID, caller.getName()))
        {
            caller.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(caller.getName()) + ", " + plugin.users.GetGroupName(caller.getName()) + ") cannot create banned blocks.");
            return null;
        }
        
        // If we are giving blocks, can the reciever use the items?
        if(reciever != null && !plugin.users.CanUseItem(ItemID, MetaID, reciever.getName()))
        {
            caller.sendMessage(ChatColor.RED + "You cannot give banned items to \"" + reciever.getName() + "\" because their group (GID " + plugin.users.GetGroupID(reciever.getName()) + ", " + plugin.users.GetGroupName(reciever.getName()) + ") cannot use banned blocks.");
            return null;
        }
        
        // Create the item stack...
        ItemStack newItems = new ItemStack(ItemID, count);
        
        // Set meta as needed
        if(MetaID != 0)
            newItems.setDurability((short)MetaID);

        // All done... give new stack
        return newItems;
    }
}
