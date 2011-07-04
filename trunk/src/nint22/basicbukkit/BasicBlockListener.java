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
    
    // List of banned items
    LinkedList<Integer> banned = null;
    
    // Constructor saves plugin handle
    public BasicBlockListener(BasicBukkit instance)
    {
        // Save plugin
        plugin = instance;
        
        // Save the three types of flow
        LavaFlows = plugin.configuration.getBoolean("lavaflows", false);
        WaterFlows = plugin.configuration.getBoolean("waterflows", false);
        FireFlows = plugin.configuration.getBoolean("fireflows", false);
        
        // Get list of banned items
        banned = new LinkedList();
        
        String bannedString = plugin.configuration.getString("banned");
        String[] splitString = bannedString.split(",");
        try
        {
            for(int i = 0; i < splitString.length; i++)
            {
                // Note we clean the string...
                int ItemID = Integer.parseInt(splitString[i].trim());
                banned.add(new Integer(ItemID));
            }
        }
        catch(Exception e)
        {
            // Force crash
            System.out.println("### BasicBukkit unable to parse the banned items array: " + e.toString());
        }
        
        // How may did we ban?
        System.out.println("### BasicBukkit loaded " + banned.size() + " banned items");
    }
    
    // When a block is placed
    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        /*** Ban Check ***/
        
        // Can the player place this block?
        Player player = event.getPlayer();
        int BlockID = event.getBlock().getTypeId();
        
        // Can the player use this banned item?
        if(banned.contains(new Integer(BlockID)))
        {
            // If we cannot place banned items...
            if(!plugin.users.CanUseBannedItems(player.getName()))
            {
                player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot place banned blocks.");
                event.setCancelled(true);
                return;
            }
        }
        
        /*** Protection Check ***/
        
        System.out.println("Can place here: "+ CheckProtection(event.getPlayer(), event));
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
