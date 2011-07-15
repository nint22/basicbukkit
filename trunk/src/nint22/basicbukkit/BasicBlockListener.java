/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicBlockListener.java
 Desc: The basic block listener for events such as palcement,
 drop, etc...
 
***************************************************************/

package nint22.basicbukkit;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;

public class BasicBlockListener extends BlockListener
{
    // Current working plugin
    private final BasicBukkit plugin;
    
    // Three types of flows
    boolean LavaFlows, WaterFlows, FireFlows;
    
    // Constructor saves plugin handle
    public BasicBlockListener(BasicBukkit instance)
    {
        // Save plugin
        plugin = instance;
        
        // Save the three types of flow
        LavaFlows = plugin.configuration.getBoolean("lavaflows", false);
        WaterFlows = plugin.configuration.getBoolean("waterflows", false);
        FireFlows = plugin.configuration.getBoolean("fireflows", false);
    }
    
    // When a block is placed
    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        /*** Ban Check ***/
        
        // Can the player place this block?
        Player player = event.getPlayer();
        int BlockID = event.getBlock().getTypeId();
        
        // Can this user build?
        if(!plugin.users.CanBuild(player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") does not have build permissions.");
            event.setCancelled(true);
            return;
        }
        
        // If we cannot place banned items...
        // Note that slabs create block ID 0 before going double slab
        if(BlockID != 0 && !plugin.users.CanUseBannedItem(BlockID, player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot place banned blocks.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        event.setCancelled(!CheckProtection(event.getPlayer(), event.getBlock().getLocation(), event));
    }
    
    // Did break?
    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
        // Get player if this was a player-based event
        if(event.getPlayer() != null)
        {
            Player player = event.getPlayer();

            // Can this user build?
            if(!plugin.users.CanBuild(player.getName()))
            {
                plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") does not have build permissions.");
                event.setCancelled(true);
                return;
            }
        }
        
        /*** Protection Check ***/
        event.setCancelled(!CheckProtection(event.getPlayer(), event.getBlock().getLocation(), event));
    }
    
    // Does spread?
    @Override
    public void onBlockFromTo(BlockFromToEvent event)
    {
        // Get block ID
        Material BlockID = event.getBlock().getType();
        
        // Check for lava
        if((BlockID == Material.LAVA || BlockID == Material.STATIONARY_LAVA) && !LavaFlows)
        {
            event.setCancelled(true);
        }
        else if((BlockID == Material.WATER || BlockID == Material.STATIONARY_WATER) && !WaterFlows)
        {
            event.setCancelled(true);
        }
        else if(BlockID == Material.FIRE && !FireFlows)
        {
            event.setCancelled(true);
        }
    }
    
    // Fire protection
    @Override
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        // Is this fire?
        if((event.getCause().equals(BlockIgniteEvent.IgniteCause.SPREAD)) && !FireFlows)
        {
            event.setCancelled(true);
        }
    }
    
    // Fire protection
    @Override
    public void onBlockBurn(BlockBurnEvent event)
    {
        // Is this a fire?
        if(!FireFlows)
            event.setCancelled(true);
    }
    
    // Custom check placement / break code of the given location, not the player's location
    // Returns true if valid, false if not valid protection
    private boolean CheckProtection(Player player, Location location, BlockEvent event)
    {
        // Get the owners of this protection area
        String protectionName = plugin.protections.GetProtectionName(location);
        
        // Is there a location?
        if(protectionName != null)
        {
            // Are we in the owner's list?
            LinkedList<String> protectionOwners = plugin.protections.GetProtectionOwners(protectionName);
            if(!protectionOwners.contains(player.getName()))
            {
                // Not in list, cancel
                plugin.SendMessage(player, ChatColor.RED + "You are not allowed to modify this protected land, named \"" + protectionName + "\"");
                return false;
            }
        }
        
        // Else, all good
        return true;
    }
}
