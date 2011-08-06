/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicEconomy.java
 Desc: The general economy wrapper which contains each user's wallet
 data as well as data for 
 
***************************************************************/

package nint22.basicbukkit;

import java.io.*;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

// Economy command wrappers
public class BasicEconomy
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // Map of item IDs and item values; defined in "prices.yml"
    private HashMap<Integer, Integer> Prices;
    
    // Map of each player's wallet
    private HashMap<String, Integer> Wallets;
    
    // The save file used for saving each player's balance
    private Configuration bankSave;
    
    // Constructor requires 
    public BasicEconomy(BasicBukkit plugin, File priceFile, Configuration bankSave)
    {
        // Save given plugin
        this.plugin = plugin;
        this.bankSave = bankSave;
        
        // Allocate prices and wallet system
        Prices = new HashMap();
        Wallets = new HashMap();
        
        // Load file and parse items names
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(priceFile));
            while(reader.ready())
            {
                // Get line, format: itemID,priceID
                String line = reader.readLine();
                String data[] = line.split(",");
                
                // Get key-value pairs
                String ItemID = data[0].trim();
                String ItemPrice = data[2].trim();
                
                // Put into dictionary
                Prices.put(new Integer(ItemID), new Integer(ItemPrice));
            }
            reader.close();
        }
        catch(Exception e)
        {
            System.out.println("### BasicBukkit is unable to read the items.csv file: " + e.getMessage());
            System.exit(0);
        }
        
        // Read from the bank
        // For each user?
        for(String key : bankSave.getKeys())
        {
            // Get the player cash ammount (Defaults to 200Z)
            int Cash = bankSave.getInt(key + ".wallet", 200);
            
            // Save to the wallets list
            Wallets.put(key, Cash);
        }
        
        // Print out how much we loaded
        System.out.println("### BasicBucket loaded economy data for " + Wallets.size() + " users and " + Prices.size() + " prices");
    }
    
    // Write to disk each user's wallet
    public void save()
    {
        // Get the freshest users file
        bankSave.load();
        
        // For all users
        for(String username : Wallets.keySet())
        {
            // Update user
            HashMap<String, Object> WalletValue = new HashMap();
            WalletValue.put("wallet", Wallets.get(username));
            
            // Save this user to the map
            bankSave.setProperty(username, WalletValue);
        }
        
        // Save file
        bankSave.save();
    }
    
    // Attempt to buy an item
    public void BuyItem(Player player, int ItemID, int Ammount)
    {
        // Ignore if the player is null
        if(player == null)
            return;
        
        // Attempt to find the itemID
        Integer Price = Prices.get(ItemID);
        if(Price == null)
        {
            player.sendMessage(ChatColor.GRAY + "Unable to find item ID \"" + ItemID + "\"");
            return;
        }
        
        // Cannot buy items without a price
        if(Price < 0)
        {
            player.sendMessage(ChatColor.GRAY + "Given item ID \"" + ItemID + "\" is not for sale");
            return;
        }
        
        // Can this user use banned item?
        if(!plugin.users.CanUseItem(ItemID, 0, player.getName()))
        {
            player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot create banned blocks.");
            return;
        }
        
        // Does this user have enough chash
        int UserCash = Wallets.get(player.getName());
        int TotalPrice = Price.intValue() * Ammount;
        if(UserCash < TotalPrice)
        {
            player.sendMessage(ChatColor.GRAY + "Cannot buy " + Ammount + " of item ID \"" + ItemID + "\"; total cost is more than you can afford");
            return;
        }
        
        // Does the user have enough slots?
        int SlotCount = (int)Math.ceil((double)Ammount / 64.0);
        int OpenSlots = 0;
        
        ItemStack[] items = player.getInventory().getContents();
        for(int i = 0; i < items.length; i++)
        {
            // Add open slot count
            if(items[i] == null)
                OpenSlots++;
        }
        
        // Do we have enough slots?
        if(OpenSlots < SlotCount)
        {
            player.sendMessage(ChatColor.GRAY + "You must have at least " + SlotCount + " open slots to recieve this item; you currently have " + OpenSlots + " open slots");
            return;
        }
        
        // All good, make the purchase
        Wallets.put(player.getName(), UserCash - TotalPrice);
        
        // Create the item stack and give to player
        ItemStack newItems = new ItemStack(ItemID, Ammount);
        player.getInventory().addItem(newItems);
        player.sendMessage(ChatColor.GRAY + "You have purchased " + Ammount + " of item ID \"" + ItemID + "\"; current balance: " + (UserCash - TotalPrice) + "Z");
    }
    
    // Attempt to sell an item
    public void SellItem(Player player, int ItemID, int Ammount)
    {
        player.sendMessage(ChatColor.RED + "NOT YET IMPLEMENTED");
    }
    
    // Get the given player's wallet / cash ammount
    public int GetMoney(Player player)
    {
        Integer money = Wallets.get(player.getName());
        if(money == null)
            return 0;
        else
            return money.intValue();
    }
    
    // Give money silentry
    public void GiveMoney(Player player, int ammount)
    {
        Integer OriginalAmmount = Wallets.get(player.getName());
        if(OriginalAmmount == null)
            OriginalAmmount = new Integer(0);
        
        Wallets.put(player.getName(), ammount + OriginalAmmount.intValue());
    }
    
    // Give the player money for a reason
    public void GiveMoney(Player player, int ammount, String reason)
    {
        Integer OriginalAmmount = Wallets.get(player.getName());
        if(OriginalAmmount == null)
            OriginalAmmount = new Integer(0);
        
        Wallets.put(player.getName(), ammount + OriginalAmmount.intValue());
        player.sendMessage(ChatColor.GRAY + "Given " + ammount + " for \"" + reason + "\"");
    }
    
    // Returns the price of the item, returning -1 if not found
    int GetPrice(int ItemID)
    {
        Integer Price = Prices.get(ItemID);
        if(Price == null)
            return -1;
        else
            return Price.intValue();
    }
}
