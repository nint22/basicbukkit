/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicLocks.java
 Desc: Series of lock and unlock code for chests and doors.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

// Lock data structure
class Lock
{
    public int x;
    public int y;
    public int z;
    public String owner;
    
    public Lock(int newx, int newy, int newz, String newowner)
    {
        x = newx;
        y = newy;
        z = newz;
        owner = newowner;
    }
}

// Global locking system for items
public class BasicLocks
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // Internal config handler
    private Configuration locks;
    
    // List of locks, where the key is the chest / item
    // position and the data is the owner's name
    private HashMap<String, Lock> lockedObjects;
    
    // Initialize locks interface
    public BasicLocks(BasicBukkit plugin, Configuration locks)
    {
        // Save plugin handle and locks
        this.plugin = plugin;
        this.locks = locks;
        
        // Open incase the given handle was yet initialized
        locks.load();
        
        // Create new hash map
        lockedObjects = new HashMap();
        
        // For each user?
        for(String key : locks.getKeys())
        {
            // Get the location
            int x = locks.getInt(key + ".x", 0);
            int y = locks.getInt(key + ".y", 0);
            int z = locks.getInt(key + ".z", 0);
            
            // Get the owner's name
            String owner = locks.getString(key + ".owner", "");
            
            // If no owner, ignore
            if(owner.length() > 0)
                lockedObjects.put(x + "," + y + "," + z, new Lock(x, y, z, owner));
        }
        
        // How many did we load?
        System.out.println("### BasicBukkit loaded " + lockedObjects.size() + " locked objects");
    }
    
    // Write out if needed
    public void save()
    {
        // Get the freshest users file
        locks.load();
        
        // For each pair in the hash map
        int lockIndex = 0;
        for(Lock lock : lockedObjects.values())
        {
            // Create an object to place
            HashMap<String, Object> map = new HashMap();
            
            // Save the location and owner name
            map.put("x", new Integer(lock.x));
            map.put("y", new Integer(lock.y));
            map.put("z", new Integer(lock.z));
            map.put("owner", lock.owner);
            
            // Place into locks
            locks.setProperty("lock_" + lockIndex, map);
            
            // Grow lock index
            lockIndex++;
        }
        
        // Save file
        locks.save();
    }
    
    // User attempts a lock
    public void Lock(Player player)
    {
        // Find the coliding object
        Location collision = BasicWarps.GetCollision(player, 4.0, 0.25);
        
        // Did we find anything?
        if(collision == null)
        {
            player.sendMessage(ChatColor.GRAY + "Unable to find what you are attempting to lock");
            return;
        }
        
        // Target block material
        Material targetBlock = collision.getWorld().getBlockAt(collision).getType();
        if(targetBlock == Material.CHEST || targetBlock == Material.WOODEN_DOOR || targetBlock == Material.TRAP_DOOR)
        {
            // Is it already owned by something?
            String hashKey = collision.getBlockX() + "," + collision.getBlockY() + "," + collision.getBlockZ();
            Lock lock = lockedObjects.get(hashKey);
            
            if(lock != null)
                player.sendMessage(ChatColor.GRAY + "This has already been locked by player \"" + lock.owner + "\"");
            else
            {
                // Lock chest and give to user
                lockedObjects.put(hashKey, new Lock(collision.getBlockX(), collision.getBlockY(), collision.getBlockZ(), player.getName()));
                player.sendMessage(ChatColor.GRAY + "You have created a new lock for this " + targetBlock.name().toLowerCase().replace("_", " "));
                
                // Special case: chests may be combined?
                if(targetBlock == Material.CHEST)
                {
                    if(collision.clone().add(1, 0, 0).getBlock().getType() == Material.CHEST)
                        lockedObjects.put((collision.getBlockX() + 1) + "," + collision.getBlockY() + "," + (collision.getBlockZ() + 0), new Lock(collision.getBlockX() + 1, collision.getBlockY(), collision.getBlockZ(), player.getName()));
                    else if(collision.clone().add(-1, 0, 0).getBlock().getType() == Material.CHEST)
                        lockedObjects.put((collision.getBlockX() - 1) + "," + collision.getBlockY() + "," + (collision.getBlockZ() + 0), new Lock(collision.getBlockX() - 1, collision.getBlockY(), collision.getBlockZ(), player.getName()));
                    else if(collision.clone().add(0, 0, 1).getBlock().getType() == Material.CHEST)
                        lockedObjects.put((collision.getBlockX() + 0) + "," + collision.getBlockY() + "," + (collision.getBlockZ() + 1), new Lock(collision.getBlockX(), collision.getBlockY(), collision.getBlockZ() + 1, player.getName()));
                    else if(collision.clone().add(0, 0, -1).getBlock().getType() == Material.CHEST)
                        lockedObjects.put((collision.getBlockX() + 0) + "," + collision.getBlockY() + "," + (collision.getBlockZ() - 1), new Lock(collision.getBlockX(), collision.getBlockY(), collision.getBlockZ() - 1, player.getName()));
                }
                // Doors are either one up or one down
                else if(targetBlock == Material.WOODEN_DOOR)
                {
                    if(collision.clone().add(0, 1, 0).getBlock().getType() == Material.WOODEN_DOOR)
                        lockedObjects.put(collision.getBlockX() + "," + (collision.getBlockY() + 1) + "," + collision.getBlockZ(), new Lock(collision.getBlockX(), collision.getBlockY() + 1, collision.getBlockZ(), player.getName()));
                    else if(collision.clone().add(0, -1, 0).getBlock().getType() == Material.WOODEN_DOOR)
                        lockedObjects.put(collision.getBlockX() + "," + (collision.getBlockY() - 1) + "," + collision.getBlockZ(), new Lock(collision.getBlockX(), collision.getBlockY() - 1, collision.getBlockZ(), player.getName()));
                }
                
            }
        }
        else
            player.sendMessage(ChatColor.GRAY + "Cannot lock this item; only chests and doors are lockable, not \"" + targetBlock.name().toLowerCase().replace("_", " ") + "\"");
        
        // Done locking
    }
    
    // User attempts to remove a lock
    public void Unlock(Player player)
    {
        // Find the coliding object
        Location collision = BasicWarps.GetCollision(player, 4.0, 0.25);
        
        // Did we find anything?
        if(collision == null)
        {
            player.sendMessage(ChatColor.GRAY + "Unable to find what you are attempting to unlock");
            return;
        }
        
        // Target block material
        Material targetBlock = collision.getWorld().getBlockAt(collision).getType();
        if(targetBlock == Material.CHEST || targetBlock == Material.WOODEN_DOOR || targetBlock == Material.TRAP_DOOR)
        {
            // Is it already owned by something?
            String hashKey = collision.getBlockX() + "," + collision.getBlockY() + "," + collision.getBlockZ();
            Lock lock = lockedObjects.get(hashKey);
            
            if(lock == null)
            {
                player.sendMessage(ChatColor.GRAY + "This is not locked");
            }
            else if(lock.owner.equals(player.getName()) || plugin.users.IsSuperuser(player))
            {
                // Unlock source
                lockedObjects.remove(hashKey);
                player.sendMessage(ChatColor.GRAY + "You have removed the lock for this " + targetBlock.name().toLowerCase().replace("_", " "));
                
                // Special case: chests may be combined?
                if(targetBlock == Material.CHEST)
                {
                    if(collision.clone().add(1, 0, 0).getBlock().getType() == Material.CHEST)
                        lockedObjects.remove((collision.getBlockX() + 1) + "," + collision.getBlockY() + "," + (collision.getBlockZ() + 0));
                    else if(collision.clone().add(-1, 0, 0).getBlock().getType() == Material.CHEST)
                        lockedObjects.remove((collision.getBlockX() - 1) + "," + collision.getBlockY() + "," + (collision.getBlockZ() + 0));
                    else if(collision.clone().add(0, 0, 1).getBlock().getType() == Material.CHEST)
                        lockedObjects.remove((collision.getBlockX() + 0) + "," + collision.getBlockY() + "," + (collision.getBlockZ() + 1));
                    else if(collision.clone().add(0, 0, -1).getBlock().getType() == Material.CHEST)
                        lockedObjects.remove((collision.getBlockX() + 0) + "," + collision.getBlockY() + "," + (collision.getBlockZ() - 1));
                }
                // Doors are either one up or one down
                else if(targetBlock == Material.WOODEN_DOOR)
                {
                    if(collision.clone().add(0, 1, 0).getBlock().getType() == Material.WOODEN_DOOR)
                        lockedObjects.remove(collision.getBlockX() + "," + (collision.getBlockY() + 1) + "," + collision.getBlockZ());
                    else if(collision.clone().add(0, -1, 0).getBlock().getType() == Material.WOODEN_DOOR)
                        lockedObjects.remove(collision.getBlockX() + "," + (collision.getBlockY() - 1) + "," + collision.getBlockZ());
                }
            }
            else
                player.sendMessage(ChatColor.GRAY + "Cannot unlock; Owned by \"" + lock.owner + "\"");
        }
        else
            player.sendMessage(ChatColor.GRAY + "Cannot unlock this item; only chests and doors are lockable");
        
        // Done locking
    }
    
    // Returns the owner of the given block
    // May return null
    public String LockOwner(Location location)
    {
        String hashKey = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        Lock lock = lockedObjects.get(hashKey);
        if(lock == null)
            return null;
        else
            return lock.owner;
    }    
}
