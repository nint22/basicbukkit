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
        if(BlockID != 0 && !plugin.users.CanUseItem(BlockID, player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot place banned blocks.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        event.setCancelled(!CheckProtection(event.getPlayer(), event.getBlock().getLocation(), event));
        
        /*** Kingdom Check ***/
        
        // Is this event within a kingdom?
        String Kingdom = plugin.roleplay.GetKingdom(event.getBlock().getLocation());
        if(Kingdom != null)
        {
            // Only members of this kingdom can break blocks
            String PlayerTitle = plugin.users.GetSpecialTitle(event.getPlayer());
            if(!PlayerTitle.equalsIgnoreCase(Kingdom))
            {
                event.getPlayer().sendMessage(ChatColor.GRAY + "You cannot place a block that is not within your kingdom!");
                event.setCancelled(true);
            }
        }
        
        // Add experiance regardless
        plugin.roleplay.AddExperiance(event.getPlayer(), 1);
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
        
        /*** Kingdom Check ***/
        
        // Is this event within a kingdom?
        String Kingdom = plugin.roleplay.GetKingdom(event.getBlock().getLocation());
        if(Kingdom != null)
        {
            // Only members of this kingdom can break blocks
            String PlayerTitle = plugin.users.GetSpecialTitle(event.getPlayer());
            if(!PlayerTitle.equalsIgnoreCase(Kingdom))
            {
                event.getPlayer().sendMessage(ChatColor.GRAY + "You cannot break a block that is not within your kingdom!");
                event.setCancelled(true);
            }
        }
        
        // Add experiance regardless
        plugin.roleplay.AddExperiance(event.getPlayer(), 1);
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
    
    // Sign has had text placed
    @Override
    public void onSignChange(SignChangeEvent event)
    {
        // Get the first line
        String top = event.getLine(0);
        if(top == null || top.length() <= 0)
            return;
        
        // Get the owner
        Player player = event.getPlayer();
        if(player == null)
            return;
        
        // New kingdom?
        // Can only be placed by the highest rank on the server
        if(top.equalsIgnoreCase("[kingdom]") || top.equalsIgnoreCase("[store]") || top.equalsIgnoreCase("[temple]"))
        {
            // Did we fail?
            boolean Valid = true;
            
            // Do we have an arg? (Only needed for "kingdom" sign)
            if(event.getLines()[1] != null && event.getLines()[1].length() > 0)
                Valid = plugin.roleplay.AddSign(player, new BasicSignType("[kingdom]", event.getLines()[1], event.getBlock().getLocation()));
            // No args needed for store or church
            else
                Valid = plugin.roleplay.AddSign(player, new BasicSignType(top.toLowerCase(), null, event.getBlock().getLocation()));
            
            // Cancel if failed
            if(Valid == false)
                event.setCancelled(true);
        }
    }
}
