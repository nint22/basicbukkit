/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicRoleplay.java
 Desc: Manages levels and areas for players. Experience is
 earned through the following events: (experience AND money)
 
 Place block: +1
 Remove block: +1
 Kill player: +50
 Kill mob: +25
 
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
    
    // Save file used for experiance
    private Configuration experianceSave;
    
    // Player experiance map
    private HashMap<String, Long> Experiance;
    
    // Sign radius
    static double SignRadius = 32.0;
    
    // Constructor requires 
    public BasicRoleplay(BasicBukkit plugin, Configuration experianceSave)
    {
        // Save the given plugin
        this.plugin = plugin;
        this.experianceSave = experianceSave;
        
        // Allocate the needed data structures
        Experiance = new HashMap();
        
        // Load all experiances
        experianceSave.load();
        for(String key : experianceSave.getKeys())
        {
            // Get the user's experiance
            long exp = 0;
            String strExp = experianceSave.getString(key + ".experience");
            try {
                exp = Long.parseLong(strExp);
            } catch (Exception e) {
                exp = 0;
            }
            
            //string level = experianceSave.getString(key + ".level", 0);
            
            // Create a sign and add it
            Experiance.put(key, exp);
        }
        
        // How much did we load?
        System.out.println("### BasicBukkit Loaded " + Experiance.size() + " experienced users for roleplay");
    }
    
    // Write to disk each user's wallet
    public void save()
    {
        // Load latest file
        experianceSave.load();
        
        // For all users
        for(String userName : Experiance.keySet())
        {
            // Update experiance
            HashMap<String, Object> Block = new HashMap();
            Block.put("experience", Experiance.get(userName));
            
            // Save this user to the map
            experianceSave.setProperty(userName, Block);
        }
        
        // Save file
        experianceSave.save();
    }
    
    // Get players experiance points
    public long GetExperiance(Player player)
    {
        // Add experiance into the hashmap
        Long experiance = Experiance.get(player.getName());
        if(experiance == null)
            return 0;
        else
            return experiance.intValue();
    }
    
    // Get level for player
    public int GetLevel(Player player)
    {
        // Add experiance into the hashmap
        int level = plugin.users.GetGroupID(player.getName());
        return level;
    }
    
    // Add to a player's experiance
    public void AddExperiance(Player player, long exp)
    {
        // Get current exp and the sum
        long Total 
    }

    void SetExperiance(String UserName, long minExp)
    {
        // Can't add experiance if roleplay is not off
        if(plugin.configuration.getBoolean("roleplay", false) == false)
            return;
        
        // Get the new experiance
        long experiance = GetExperiance(player) + exp;
        int level = GetLevel(player);
        
        // Add experiance into the hashmap
        Experiance.put(player.getName(), experiance);
        
        // For each group...
        for(String group : plugin.users.GetGroupNames())
        {
            // Is this user at a higher level of experiance?
            long minExp = plugin.users.GetGroupExp(group);
            int gid = plugin.users.GetGroupIDByGroup(group);
            if(experiance >= minExp && level < gid)
            {
                player.sendMessage(ChatColor.GREEN + "You have leveled up! You are now level " + ChatColor.RED + gid + ChatColor.GREEN + ", with title \"" + group + "\"");
                plugin.users.SetUserGroup(player.getName(), gid);
                break;
            }
        }
    }
}
