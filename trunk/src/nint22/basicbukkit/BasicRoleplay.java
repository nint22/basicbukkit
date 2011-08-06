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
    private HashMap<String, Integer> Level;
    
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
        Level = new HashMap();
        
        // Load all signs
        signSave.load();
        for(String key : signSave.getKeys())
        {
            // Get the sign arg and location
            String SignType = signSave.getString(key + ".SignType", "");
            String SignArg = signSave.getString(key + ".SignArg", "");
            
            double x = signSave.getInt(key + ".x", 0);
            double y = signSave.getInt(key + ".y", 0);
            double z = signSave.getInt(key + ".z", 0);
            String World = signSave.getString(key + ".World", "");
            
            // Create a sign and add it
            BasicSignType Sign = new BasicSignType(SignType, SignArg, new Location(plugin.getServer().getWorld(World), x, y, z));
            Signs.add(Sign);
        }
        
        // Load all experiances
        experianceSave.load();
        for(String key : experianceSave.getKeys())
        {
            // Get the user's experiance
            int exp = experianceSave.getInt(key + ".experience", 0);
            int level = experianceSave.getInt(key + ".level", 0);
            
            // Create a sign and add it
            Experiance.put(key, exp);
        }
        
        // How much did we load?
        System.out.println("### BasicBukkit Loaded " + Signs.size() + " signs and " + Experiance.size() + " experienced users");
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
            // Update experiance
            HashMap<String, Object> Block = new HashMap();
            Block.put("experience", Experiance.get(userName));
            Block.put("level", Level.get(userName));
            
            // Save this user to the map
            experianceSave.setProperty(userName, Block);
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
    
    // Get level for player
    public int GetLevel(Player player)
    {
        // Add experiance into the hashmap
        Integer level = Level.get(player.getName());
        if(level == null)
            return 0;
        else
            return level.intValue();
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
        Integer level = Level.get(player.getName());
        if(level == null)
            level = new Integer(0);
        
        // Get user's GID so we don't level down if an op explcitily set us
        int UserGID = plugin.users.GetGroupID(player.getName());
        
        // Did we level up?
        // Based on the header table
        if(experiance >= 10 && level.intValue() < 1 && UserGID < 1)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 1);
            plugin.users.SetUserGroup(player.getName(), 1);
            Level.put(player.getName(), 1);
        }
        else if(experiance >= 100 && level.intValue() < 2 && UserGID < 2)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 2);
            plugin.users.SetUserGroup(player.getName(), 2);
            Level.put(player.getName(), 2);
        }
        else if(experiance >= 500 && level.intValue() < 3 && UserGID < 3)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 3);
            plugin.users.SetUserGroup(player.getName(), 3);
            Level.put(player.getName(), 3);
        }
        else if(experiance >= 1000 && level.intValue() < 4 && UserGID < 4)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 4);
            plugin.users.SetUserGroup(player.getName(), 4);
            Level.put(player.getName(), 4);
        }
        else if(experiance >= 5000 && level.intValue() < 5 && UserGID < 5)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 5);
            plugin.users.SetUserGroup(player.getName(), 5);
            Level.put(player.getName(), 5);
        }
        else if(experiance >= 10000 && level.intValue() < 6 && UserGID < 6)
        {
            player.sendMessage(ChatColor.GRAY + "You have leveled up! You are now level " + ChatColor.RED + 6);
            plugin.users.SetUserGroup(player.getName(), 6);
            Level.put(player.getName(), 6);
        }
        
        // Add cash based on level AND experiance
        plugin.economy.GiveMoney(player, exp * (level.intValue() + 1));
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
                    // Ignore if not in same world
                    if(Sign.SignLoc.getWorld() != newSign.SignLoc.getWorld())
                        continue;
                    
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
                // Ignore if not in same world
                if(Sign.SignLoc.getWorld() != newSign.SignLoc.getWorld())
                    continue;
                
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
                // Ignore if not in same world
                if(Sign.SignLoc.getWorld() != newSign.SignLoc.getWorld())
                    continue;
                
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
            // Ignore if not in same world
            if(Sign.SignLoc.getWorld() != location.getWorld())
                continue;
            
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
    
    // Return the kingdom name the player is in
    public String GetKingdom(Location loc)
    {
        // For each sign
        for(BasicSignType Sign : Signs)
        {
            // Ignore if not in same world
            if(Sign.SignLoc.getWorld() != loc.getWorld())
                continue;
            
            // We only care if it is a kingdom sign
            if(Sign.SignType.equalsIgnoreCase("[kingdom]") && Sign.SignLoc.distance(loc) <= SignRadius)
                return Sign.SignArg;
        }
        
        // never found
        return null;
    }

    // Returns true if the player is in his/her kingdom-owned temple
    boolean CanBeHealed(Player player)
    {
        // What is this player's kingdom?
        String PlayerKingdom = plugin.users.GetSpecialTitle(player);
        String InKingdom = GetKingdom(player.getLocation());
        
        // Do we have a match?
        if(!PlayerKingdom.equalsIgnoreCase(InKingdom))
            return false;
        
        // Since we match, is there any temple's near by AND within the same kingdom
        for(BasicSignType Sign : Signs)
        {
            // Ignore if not in same world
            if(Sign.SignLoc.getWorld() != player.getLocation().getWorld())
                continue;
            
            if(Sign.SignType.equalsIgnoreCase("[temple]") && Sign.SignLoc.distance(player.getLocation()) <= SignRadius && GetKingdom(Sign.SignLoc).equalsIgnoreCase(InKingdom))
                return true;
        }
        
        // Nothing was found...
        return false;
    }
    
    // Returns true if the player can buy or sell
    boolean CanTrade(Player player)
    {
        // Is this user near his or her own kingdom?
        String UserKingdom = plugin.users.GetSpecialTitle(player);
        String InKingdom = plugin.roleplay.GetKingdom(player.getLocation());

        // Are they in the same kingdom?
        if(!UserKingdom.equalsIgnoreCase(InKingdom))
            return false;
        
        // Is this user near a store sign?
        for(BasicSignType Sign : Signs)
        {
            // Ignore if not in same world
            if(Sign.SignLoc.getWorld() != player.getLocation().getWorld())
                continue;
            
            if(Sign.SignType.equalsIgnoreCase("[store]") && Sign.SignLoc.distance(player.getLocation()) <= SignRadius && GetKingdom(Sign.SignLoc).equalsIgnoreCase(InKingdom))
                return true;
        }
        
        // Never found
        return false;
    }

    // Returns true if the is a valid kingdom
    boolean IsKingdom(String Kingdom)
    {
        // Linear search and comparison
        String[] kingdoms = GetKingdoms();
        for(String name : kingdoms)
            if(name.equalsIgnoreCase(Kingdom))
                return true;
        return false;
    }
}
