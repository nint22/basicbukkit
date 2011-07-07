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

import java.util.LinkedList;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;

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
        
        // If we cannot place banned items...
        if(!plugin.users.CanUseBannedItem(BlockID, player.getName()))
        {
            player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot place banned blocks.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        event.setCancelled(!CheckProtection(event.getPlayer(), event));
    }
    
    // Did break?
    @Override
    public void onBlockBreak(BlockBreakEvent event)
    {
        /*** Protection Check ***/
        event.setCancelled(!CheckProtection(event.getPlayer(), event));
    }
    
    // Does spread?
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        // Get block ID
        int BlockID = event.getBlock().getTypeId();
        
        // Check for lava
        if(BlockID == org.bukkit.Material.LAVA.getId() && !LavaFlows)
        {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
        else if(BlockID == org.bukkit.Material.WATER.getId() && !WaterFlows)
        {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
        else if(BlockID == org.bukkit.Material.FIRE.getId() && !FireFlows)
        {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }
    
    // Custom check placement / break code
    // Returns true if valid, false if not valid protection
    private boolean CheckProtection(Player player, BlockEvent event)
    {
        // Get the owners of this protection area
        String protectionName = plugin.protections.GetProtectionName(new Pair(event.getBlock().getX(), event.getBlock().getZ()));
        if(protectionName != null)
        {
            // Are we in the owner's list?
            LinkedList<String> protectionOwners = plugin.protections.GetProtectionOwners(protectionName);
            if(!protectionOwners.contains(player.getName()))
            {
                // Not in list, cancel
                player.sendMessage(ChatColor.RED + "You are not allowed to modify this protected land, named \"" + protectionName + "\"");
                return false;
            }
        }
        
        // Else, all good
        return true;
    }
}
