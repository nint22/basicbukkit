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
 2 - 100: Archer
 3 - 500: Dragoon
 4 - 1000: Knight
 5 - 5000: Bishop
 6 - 10000: King
 
 Types of signs:
 0 - [Kingdom] protects a sphere of 32
 1 - [Store] acts as a buy / selling point
 2 - [Temple] acts as a healing point
 
***************************************************************/

package nint22.basicbukkit;

import java.io.*;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

// Helper class with signs
class SignType
{
    // Sign type
    String SignType;
    
    // Name / argument (i.e. [City]/nDerp)
    String SignArg;
}

// Roleplay command wrappers
public class BasicRoleplay
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // List of all roleplaying signs
    private LinkedList<SignType> Signs;
    
    // Player experiance map
    private HashMap<String, Integer> Experiance;
    
    // Save file used for signs
    private Configuration signSave;
    
    // Save file used for experiance
    private Configuration experianceSave;
    
    // List of kingdoms and its players
    private HashMap<String, LinkedList<String> > Kingdoms;
    
    // Constructor requires 
    public BasicRoleplay(BasicBukkit plugin, Configuration signSave, Configuration experianceSave)
    {
        // TODO...
    }
    
    // Write to disk each user's wallet
    public void save()
    {
        // TODO..
    }
    
    // Get players experiance points
    public void GetExperiance(Player player)
    {
        // TODO...
    }
    
    // Add to a player's experiance
    public void AddExperiance(Player player, int exp)
    {
        // TODO...
        // Note to self: Should rank-up as needed based on the experiance table
    }
    
    // Save a new sign
    public void AddSign(SignType newSign)
    {
        // TODO...
    }
    
    // Remove a sign
    public void RemoveSign(Location loc)
    {
        // TODO...
    }
    
    // Get a kingom sign at the given location
    public void GetKingdomSign(Location loc)
    {
        // TODO...
    }
    
    // Get a store sign at the given location
    public void GetStoreSign(Location loc)
    {
        
    }
    
    // Get a temple sign
    public void GetTempleSign(Location loc)
    {
        
    }
    
    // Get a list of kingdom names
    public String[] GetKingdoms()
    {
        String[] KingdomNames = new String[Kingdoms.size()];
        int index = 0;
        for(String Kingdom : Kingdoms.keySet())
            KingdomNames[index++] = Kingdom;
        return KingdomNames;
    }
}
