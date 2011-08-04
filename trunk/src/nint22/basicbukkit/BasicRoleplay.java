/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicRoleplay.java
 Desc: Manages levels and areas for players. Experience is
 earned through the following events: (experiance AND money)
 
 Place block: +1
 Remove block: +1
 Kill player: +50
 Kill mob: +25
 
 Level distribution is based on powers of 10 / 2:
 0 - 0: Beggar
 1 - 10: Builder
 2 - 100: Soldier
 3 - 500: Dragoon
 4 - 1000: Knight
 5 - 5000: Bishop
 6 - 10000: King
 
 Types of signs:
 0 - [Kingdom] protects a sphere of 32
 1 - [Store] acts as a buy / selling point
 2 - [Temple] acts as a healing point
 
 The more populated a kingdom is, that adds as a price multiplyer
 to the st
 
 Kingdoms automatically have mobs removed as a bonus.
 
***************************************************************/

package nint22.basicbukkit;

import java.io.*;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

// Roleplay command wrappers
public class BasicRoleplay
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // Save file used for signs
    private Configuration signSave;
    
    // Save file used for experiance
    private Configuration experianceSave;
    
    // List of all roleplaying signs
    private LinkedList<BasicSignType> Signs;
    
    // Player experiance map
    private HashMap<String, Integer> Experiance;
    
    // Sign radius
    static double SignRadius = 32.0;
    
    // Constructor requires 
    public BasicRoleplay(BasicBukkit plugin, Configuration signSave, Configuration experianceSave)
    {
        // Save the given plugin
        this.plugin = plugin;
        this.signSave = signSave;
        this.experianceSave = experianceSave;
        
        // Allocate the needed data structures
        Signs = new LinkedList();
        Experiance = new HashMap();
        
        // Load data from the configuration files
    }
    
    // Write to disk each user's wallet
    public void save()
    {
        /*** SIGNS ***/
        
        // Load latest file
        signSave.load();
        
        // For all signs
        int index = 0;
        for(BasicSignType sign : Signs)
        {
            // Update user
            HashMap<String, Object> Block = new HashMap();
            Block.put("SignType", sign.SignType);
            Block.put("SignArg", sign.SignArg);
            Block.put("x", sign.SignLoc.getBlockX());
            Block.put("y", sign.SignLoc.getBlockY());
            Block.put("z", sign.SignLoc.getBlockZ());
            Block.put("World", sign.SignLoc.getWorld().getName());
            
            // Save this user to the map
            signSave.setProperty("sign_" + index++, Block);
        }
        
        // Save file
        signSave.save();
        
        /*** EXPERIANCE ***/
        
        
        // Load latest file
        experianceSave.load();
        
        // For all users
        for(String userName : Experiance.keySet())
        {
            // Save this user to the map
            signSave.setProperty(userName, Experiance.get(userName));
        }
        
        // Save file
        experianceSave.save();
    }
    
    // Get players experiance points
    public int GetExperiance(Player player)
    {
        // Add experiance into the hashmap
        Integer experiance = Experiance.get(player.getName());
        if(experiance == null)
            return 0;
        else
            return experiance.intValue();
    }
    
    // Add to a player's experiance
    public void AddExperiance(Player player, int exp)
    {
        // Add experiance into the hashmap
        Integer experiance = Experiance.get(player.getName());
        if(experiance == null)
            Experiance.put(player.getName(), exp);
        else
            Experiance.put(player.getName(), experiance.intValue() + exp);
        
        // Get the new experiance
        experiance = Experiance.get(player.getName());
        
        // Did we level up?
        // Based on the header table
        if(experiance >= 10)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 1);
            plugin.users.SetUserGroup(player.getName(), 1);
        }
        else if(experiance >= 100)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 2);
            plugin.users.SetUserGroup(player.getName(), 2);
        }
        else if(experiance >= 500)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 3);
            plugin.users.SetUserGroup(player.getName(), 3);
        }
        else if(experiance >= 1000)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 4);
            plugin.users.SetUserGroup(player.getName(), 4);
        }
        else if(experiance >= 5000)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 5);
            plugin.users.SetUserGroup(player.getName(), 5);
        }
        else if(experiance >= 10000)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 6);
            plugin.users.SetUserGroup(player.getName(), 6);
        }
    }
    
    // Save a  new sign; if it failes, return false
    // Otherwise return true on success
    public boolean AddSign(Player player, BasicSignType newSign)
    {
        // Is this player of the highest rank?
        int GID = plugin.users.GetGroupID(player.getName());
        if(plugin.users.GetGroupIDs().indexOf(GID) != plugin.users.GetGroupIDs().size() - 1)
        {
            // Not server owner; cancel
            player.sendMessage(ChatColor.GRAY + "You cannot create a roleplaying signs; your GroupID is not high enough");
            return false;
        }
        
        // New kingdom / kingdom grown sign
        // Can only be placed by the highest rank on the server
        if(newSign.SignType.equalsIgnoreCase("[kingdom]"))
        {
            // Make sure we have a kingdom name
            if(newSign.SignArg == null || newSign.SignArg.length() <= 0)
            {
                // Not server owner; cancel
                player.sendMessage(ChatColor.GRAY + "You cannot create a new kingdom, kingdom name must be on the second line");
                return false;
            }
            // Create the kingdom
            else
            {
                // Added: Is this the first sign for this kingdom?
                boolean IsUnique = true;
                
                // Is this connected to any other kingdom sign of the ame
                boolean IsConnected = false;
                
                // Are we coliding with any other kingdoms? Then cancel
                for(BasicSignType Sign : Signs)
                {
                    // We only care if it is a kingdom sign
                    if(Sign.SignType.equalsIgnoreCase("[kingdom]") == false)
                        continue;
                    
                    // Are we coliding?
                    if(Sign.SignLoc.distance(newSign.SignLoc) <= SignRadius)
                    {
                        // If this sign is of another kingdom: fail out
                        if(Sign.SignArg.equalsIgnoreCase(newSign.SignArg) == false)
                        {
                            player.sendMessage(ChatColor.GRAY + "You cannot create a new kingdom; the sign intersects with a different kingdom");
                            return false;
                        }
                        
                        // Are we touching a sign of the same kingdom?
                        else
                            IsConnected = true;
                    }
                    
                    // Check for uniqueness
                    if(Sign.SignArg.equalsIgnoreCase(newSign.SignArg))
                        IsUnique = false;
                }
                
                // If this is the first kingdom sign ever:
                if(IsUnique)
                {
                    // Push into the list
                    Signs.add(newSign);
                    player.sendMessage(ChatColor.GRAY + "You have created a new kingdom!");
                    plugin.BroadcastMessage(ChatColor.BLUE + "A new kingdom has been created! All hail \"" + newSign.SignArg + "\"");
                }
                // If this is growing, make sre we are connected
                else if(IsConnected)
                {
                    // Push into the list
                    Signs.add(newSign);
                    player.sendMessage(ChatColor.GRAY + "You have grown your kingdom of \"" + newSign.SignArg + "\"");
                }
                // Error check
                else
                {
                    player.sendMessage(ChatColor.GRAY + "Your kingdom sign must be connected to the same kingdom and be close enough");
                    return false;
                }
            }
        }
        
        // Store?
        else if(newSign.SignType.equalsIgnoreCase("[store]"))
        {
            // Is this sign connected to a kingdom?
            boolean IsConnected = false;
            String Kingdom = "";
            
            // Are we coliding with any other kingdoms? Then cancel
            for(BasicSignType Sign : Signs)
            {
                // We only care if it is a kingdom sign
                if(Sign.SignType.equalsIgnoreCase("[kingdom]") == false)
                    continue;
                
                // Are we coliding?
                if(Sign.SignLoc.distance(newSign.SignLoc) <= SignRadius)
                {
                    // No need to keep searching
                    Kingdom = Sign.SignArg;
                    IsConnected = true;
                    break;
                }
            }
            
            // If we are connected, place, otherwise, fail out
            if(IsConnected)
            {
                player.sendMessage(ChatColor.GRAY + "You have created a store for the kingdom \"" + Kingdom + "\"");
                Signs.add(newSign);
            }
            else
            {
                player.sendMessage(ChatColor.GRAY + "Unable to create a store sign, you are not in any kingdom");
                return false;
            }
        }
        
        // Church / temple
        else if(newSign.SignType.equalsIgnoreCase("[temple]"))
        {
            // Is this sign connected to a kingdom?
            boolean IsConnected = false;
            String Kingdom = "";
            
            // Are we coliding with any other kingdoms? Then cancel
            for(BasicSignType Sign : Signs)
            {
                // We only care if it is a kingdom sign
                if(Sign.SignType.equalsIgnoreCase("[kingdom]") == false)
                    continue;
                
                // Are we coliding?
                if(Sign.SignLoc.distance(newSign.SignLoc) <= SignRadius)
                {
                    // No need to keep searching
                    Kingdom = Sign.SignArg;
                    IsConnected = true;
                    break;
                }
            }
            
            // If we are connected, place, otherwise, fail out
            if(IsConnected)
            {
                player.sendMessage(ChatColor.GRAY + "You have created a temple for the kingdom \"" + Kingdom + "\"");
                Signs.add(newSign);
            }
            else
            {
                player.sendMessage(ChatColor.GRAY + "Unable to create a temple sign, you are not in any kingdom");
                return false;
            }
        }
        
        // All good
        return true;
    }
    
    // Return a list of all signs within a radius that affects the given user
    public LinkedList<BasicSignType> GetSigns(Location location)
    {
        // Create a new linked list..
        LinkedList<BasicSignType> close = new LinkedList();
        
        for(BasicSignType Sign : Signs)
        {
            // Are we coliding?
            if(Sign.SignLoc.distance(location) <= SignRadius)
                close.add(Sign);
        }
        
        // Return the nearest signs
        return close;
    }
    
    // Get a list of kingdom names
    public String[] GetKingdoms()
    {
        // Hash map for all unique kingdom signs
        HashMap<String, String> Kingdoms = new HashMap();
        
        // For all the signs
        for(BasicSignType Sign : Signs)
        {
            if(Sign.SignType.equalsIgnoreCase("[kingdom]"))
                Kingdoms.put(Sign.SignArg, Sign.SignArg);
        }
        
        // Convert to a list of strings
        String[] temp = new String[Kingdoms.size()];
        int index = 0;
        for(String key : Kingdoms.keySet())
            temp[index++] = key;
        return temp;
    }
}
