/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicWarps.java
 Desc: Load and save all warp data. (Homes, warps, and spawn)
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class BasicWarps
{
    // Plugin interface
    BasicBukkit plugin;
    
    // Configuration file full of warps
    private Configuration warps;
    
    // List of warps (parallel arrays)
    private LinkedList<String> names;
    private LinkedList<Location> locations;
    
    // List of user's homes
    private LinkedList<String> users;
    private LinkedList<Location> homes;
    
    // Spawn location
    private Location spawn;
    
    // Initialize users
    public BasicWarps(BasicBukkit plugin, Configuration warps)
    {
        // Save warps file handle
        this.plugin = plugin;
        this.warps = warps;
        warps.load();
        
        // Allocate list as needed
        names = new LinkedList();
        locations = new LinkedList();
        users = new LinkedList();
        homes = new LinkedList();
        
        // Load all warps
        Map<String, ConfigurationNode> warpData = warps.getNodes("warps");
        if(warpData != null)
        {
            for(ConfigurationNode obj : warpData.values())
            {
                // Is a hash map of "name", "world string", "x", "y", "z"
                Map<String, Object> map = obj.getAll();
                String warpName = (String)map.get("name");
                String worldName = (String)map.get("world");
                int x = Integer.parseInt((String)map.get("x"));
                int y = Integer.parseInt((String)map.get("y"));
                int z = Integer.parseInt((String)map.get("z"));
                
                // Get world
                World targetWorld = plugin.getServer().getWorld(worldName);
                
                // Save to list
                names.add(warpName);
                locations.add(new Location(targetWorld, (double)x, (double)y, (double)z));
            }
        }
        
        // Load all homes
        Map<String, ConfigurationNode> homeData = warps.getNodes("homes");
        if(homeData != null)
        {
            for(ConfigurationNode obj : homeData.values())
            {
                // Is a hash map of "world string", "x", "y", "z"
                Map<String, Object> map = obj.getAll();
                String userName = (String)map.get("user");
                String worldName = (String)map.get("world");
                int x = Integer.parseInt((String)map.get("x"));
                int y = Integer.parseInt((String)map.get("y"));
                int z = Integer.parseInt((String)map.get("z"));
                
                // Get world
                World targetWorld = plugin.getServer().getWorld(worldName);
                
                // Save to list
                users.add(userName);
                homes.add(new Location(targetWorld, (double)x, (double)y, (double)z));
            }
        }
        
        // Load the spawn location if it exists..
        LinkedHashMap map = (LinkedHashMap)warps.getProperty("spawn");
        if(map != null)
        {
            // Load only the first spawn
            String worldName = (String)map.get("world");
            int x = Integer.parseInt((String)map.get("x"));
            int y = Integer.parseInt((String)map.get("y"));
            int z = Integer.parseInt((String)map.get("z"));
            
            // Get world
            World targetWorld = plugin.getServer().getWorld(worldName);
            
            // Save to spawn
            spawn = new Location(targetWorld, (double)x, (double)y, (double)z);
        }
        
        // Print out information
        System.out.println("### BasicBukkit loaded " + names.size() + " warp(s), " + users.size() + " home(s), and " + (spawn == null ? 0 : 1) + " spawn(s)");
    }
    
    // Write out if needed
    public void save()
    {
        // Save spawn as a special case
        if(spawn != null)
        {
            HashMap<String, String> map = new HashMap();
            map.put("world", spawn.getWorld().getName());
            map.put("x", Integer.toString(spawn.getBlockX()));
            map.put("y", Integer.toString(spawn.getBlockY()));
            map.put("z", Integer.toString(spawn.getBlockZ()));
            warps.setProperty("spawn", map);
        }
        
        // For each home
        HashMap<String, HashMap> allHomes = new HashMap();
        for(int i = 0; i < users.size(); i++)
        {
            // Get the location
            Location location = homes.get(i);
            
            // Create a hash map to push out as a property
            HashMap<String, String> map = new HashMap();
            map.put("user", users.get(i));
            map.put("world", location.getWorld().getName());
            map.put("x", Integer.toString(location.getBlockX()));
            map.put("y", Integer.toString(location.getBlockY()));
            map.put("z", Integer.toString(location.getBlockZ()));
            
            // Save this as all warps
            allHomes.put(users.get(i), map);
        }
        
        // Save all warps
        warps.setProperty("homes", allHomes);
        
        // For each warp name
        HashMap<String, HashMap> allWarps = new HashMap();
        for(int i = 0; i < names.size(); i++)
        {
            // Get the location
            Location location = locations.get(i);
            
            // Create a hash map to push out as a property
            HashMap<String, String> map = new HashMap();
            map.put("name", names.get(i));
            map.put("world", location.getWorld().getName());
            map.put("x", Integer.toString(location.getBlockX()));
            map.put("y", Integer.toString(location.getBlockY()));
            map.put("z", Integer.toString(location.getBlockZ()));
            
            // Save this as all warps
            allWarps.put(names.get(i), map);
        }
        
        // Save all warps
        warps.setProperty("warps", allWarps);
        
        // Save file
        warps.save();
    }
    
    // Get a new warp at this location
    public void SetWarp(String name, Location location)
    {
        // If the name exists, just overwrite
        if(names.contains(name))
        {
            locations.set(names.indexOf(name), location);
        }
        else
        {
            names.add(name);
            locations.add(location);
        }
        
        // Done
    }
    
    // Get a warp based on name; returns null on failure
    public Location GetWarp(String[] args)
    {
        // Find the case-corrected warp name
        for(int i = 0; i < names.size(); i++)
        {
            // Get the warp name
            String warpName = (String)names.toArray()[i];
            
            // Do we have a match?
            if(warpName.toLowerCase().startsWith(args[0].toLowerCase()))
            {
                args[0] = warpName;
                break;
            }
        }
        
        int locIndex = names.indexOf(args[0]);
        if(locIndex < 0)
            return null;
        else
            return locations.get(locIndex);
    }
    
    // Remove warp
    public void DelWarp(String name)
    {
        int locIndex = names.indexOf(name);
        if(locIndex >= 0)
        {
            names.remove(locIndex);
            locations.remove(locIndex);
        }
    }
    
    // Set the spawn location
    public void SetSpawn(Location location)
    {
        // Save this spawn
        spawn = location;
        location.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    // Return spawn loction; may return null if not set...
    public Location GetSpawn()
    {
        return spawn;
    }
    
    // Set home for the given user
    public void SetHome(String name, Location location)
    {
        // Does the user already exist? Just update location
        if(users.contains(name))
        {
            homes.set(users.indexOf(name), location);
        }
        else
        {
            users.add(name);
            homes.add(location);
        }
    }
    
    // Get home for the given user; may return null if not set
    public Location GetHome(String name)
    {
        int locIndex = users.indexOf(name);
        if(locIndex < 0)
            return null;
        else
            return homes.get(locIndex);
    }
    
    // Return the list of all the warps
    public LinkedList<String> GetWarpNames()
    {
        return names;
    }
    
    // Given a player (direction), maximum distance, and a step-resolution
    // find a colliding block, or return null upon failure
    // Note that the resolution is the distance applied to the ray, NOT
    // distance relative to block sizes (which are unit 1 size)
    public static Location GetCollision(Player player, double maxDistance, double resolution)
    {
        // Get the ray's source location
        Location raySource = player.getEyeLocation();
        
        // Get the ray's direction (not a vector)
        // Also note, these are degrees
        double yaw = Math.toRadians((player.getLocation().getYaw() + 90) % 360); // +z is yaw 0
        double pitch = Math.toRadians(player.getLocation().getPitch() * -1); // Change to positive up, negative down
        
        // Get world geometry
        World activeWorld = player.getWorld();
        
        // Previous (valid) point
        Location prev = null;
        
        // From distance 0 to maxDistance, check for collision
        for(double d = 2; d < maxDistance; d += resolution)
        {
            // Get the ray offsets sans origin
            double offsetX = d * Math.cos(yaw) * Math.cos(pitch);
            double offsetY = d * Math.sin(pitch);
            double offsetZ = d * Math.sin(yaw) * Math.cos(pitch);
            
            // Add origin and cast to block position
            double posX = raySource.getX() + (int)offsetX;
            double posY = raySource.getY() + (int)offsetY;
            double posZ = raySource.getZ() + (int)offsetZ;
            
            // Material at that block
            Material mat = activeWorld.getBlockAt(new Location(activeWorld, posX, posY, posZ)).getType();
            
            // Is this not air? Return the previous (known air) location
            if(mat != null && mat != Material.AIR && mat != Material.WATER)
                return new Location(activeWorld, posX, posY, posZ, player.getLocation().getYaw(), player.getLocation().getPitch());
        }
        
        // No collisions
        return null;
    }
}
