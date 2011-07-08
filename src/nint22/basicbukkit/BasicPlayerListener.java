/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicPlayerListener.java
 Desc: The basic player listener for events such as join / quit
 
***************************************************************/

package nint22.basicbukkit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.Location;
import org.bukkit.ChatColor;

public class BasicPlayerListener extends PlayerListener
{
    // Current working plugin
    private final BasicBukkit plugin;
    
    // World sizes
    int WorldWidth, WorldLength;
    
    // Locations of players
    private HashMap<String, String> playerProtectionLocations;
    
    // Constructor saves plugin handle
    public BasicPlayerListener(BasicBukkit instance)
    {
        // Save plugin
        plugin = instance;
        
        // Save max world size...
        List<Integer> sizes = plugin.configuration.getIntList("size", null);
        WorldWidth = sizes.get(0).intValue();
        WorldLength = sizes.get(1).intValue();
        
        // Allocate player's current protection locations
        playerProtectionLocations = new HashMap();
    }
    
    // Player joined game: run MOTD
    @Override
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Get the player
        Player player = event.getPlayer();
        
        // Check for ban
        String BanReason = plugin.users.IsBanned(player.getName());
        if(BanReason != null)
        {
            player.kickPlayer("You are banned. Reason: \"" + BanReason + "\"");
            return;
        }
        
        // Is it past their kick time?
        if(!plugin.users.IsKicked(player.getName()))
        {
            player.kickPlayer("Your kick time is not yet up.");
            return;
        }
        
        // Say where the player game from...
        plugin.BroadcastMessage(ChatColor.GRAY + player.getName() + " joined the server.");
        
        // Show the MOTD string
        String[] motd = plugin.GetMOTD();
        for(int i = 0; i < motd.length; i++)
            player.sendMessage(motd[i]);
        
        // Has this player ever joined us before?
        if(plugin.users.GetGroupID(player.getName()) < 0)
        {
            System.out.println(player.getName() + " is a new user to your server.");
            
            // Register this first time user
            plugin.users.SetUserGroup(player.getName(), 0);
            
            // Warp to spawn
            if(plugin.warps.GetSpawn() != null)
                player.teleport(plugin.warps.GetSpawn());
        }
        else
            System.out.println(player.getName() + " is a known user.");
        
        // Set the player's title
        player.setDisplayName(plugin.users.GetUserTitle(player.getName()) + player.getName());
        
    }
    
    // Player quit, announce globally
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        plugin.BroadcastMessage(ChatColor.GRAY + event.getPlayer().getName() + " left the server.");
    }
    
    // Player moves...
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        // Player not longer afk
        if(plugin.users.GetAFK(event.getPlayer().getName()))
        {
            plugin.users.SetAFK(event.getPlayer().getName(), false);
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + event.getPlayer().getName() + "\" is no longer AFK");
        }
        
        // Get target position
        Location location = event.getTo();
        
        // Is this location within the bounds of the width and length?
        if(location.getX() > WorldWidth / 2 || location.getX() < -WorldWidth / 2 || location.getZ() > WorldLength / 2 || location.getZ() < -WorldLength / 2)
        {
            // Warp back player
            event.getPlayer().teleport(event.getFrom());
            event.getPlayer().sendMessage(ChatColor.RED + "You have hit the world bounds of (" + WorldWidth + ", " + WorldLength + ")");
        }
        
        // Get current zone
        String oldZone = playerProtectionLocations.get(event.getPlayer().getName());
        String newZone = plugin.protections.GetProtectionName(event.getPlayer());
        
        // Did we go from a non-zone to a new zone
        if(oldZone == null && newZone != null)
        {
            event.getPlayer().sendMessage(ChatColor.GRAY + "You have walked into the protected zone \"" + newZone + "\"");
            playerProtectionLocations.put(event.getPlayer().getName(), newZone);
        }
        // Did we get out of a zone to a non-zone
        else if(oldZone != null && newZone == null)
        {
            event.getPlayer().sendMessage(ChatColor.GRAY + "You have left the protected zone \"" + oldZone + "\"");
            playerProtectionLocations.remove(event.getPlayer().getName());
        }
        // Did we change zones?
        else if(oldZone != null && newZone != null && !newZone.equalsIgnoreCase(oldZone))
        {
            event.getPlayer().sendMessage(ChatColor.GRAY + "You have left the protected zone \"" + oldZone + "\" and are now in \"" + newZone + "\"");
            playerProtectionLocations.put(event.getPlayer().getName(), newZone);
        }
    }
    
    // Player has said something...
    @Override
    public void onPlayerChat(PlayerChatEvent event) 
    {
        // Player not longer afk
        if(plugin.users.GetAFK(event.getPlayer().getName()))
        {
            plugin.users.SetAFK(event.getPlayer().getName(), false);
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + event.getPlayer().getName() + "\" is no longer AFK");
        }
        
        // Get player
        Player player = event.getPlayer();
        
        // Note the hex value is the signal byte for following colors
        String message = event.getMessage();
        message = plugin.ColorString(message);
        event.setMessage(message);
        
        // Get title (formatted with color)
        String title = plugin.users.GetUserTitle(player.getName());
        title = plugin.ColorString(title);
        
        // Set the player's title
        player.setDisplayName(title + " " + player.getName());
    }
    
    // Player permissions check
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        /*** Ban Check ***/
        
        // Can the player place this block?
        Player player = event.getPlayer();
        int ItemID = event.getItemDrop().getItemStack().getTypeId();
        
        // If we cannot place banned items...
        if(!plugin.users.CanUseBannedItem(ItemID, player.getName()))
        {
            player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot drop banned items.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        // If we aren't in the area's owners list, we can't steal
        String protectionName = plugin.protections.GetProtectionName(event.getPlayer());
        if(protectionName == null)
        {
            // Nothing to wory about; no land
            return;
        }
        
        // Are we in a protected area? If so, get the owners list
        LinkedList<String> owners = plugin.protections.GetProtectionOwners(protectionName);
        if(!owners.contains(player.getName()))
        {
            player.sendMessage(ChatColor.RED + "You cannot drop items in this protected area.");
            event.setCancelled(true);
        }
        
        // Else, all good
    }
    
    // Player permissions check
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        /*** Ban Check ***/
        
        // Can the player place this block?
        Player player = event.getPlayer();
        int ItemID = event.getItem().getItemStack().getTypeId();
        
        // If we cannot place banned items...
        if(!plugin.users.CanUseBannedItem(ItemID, player.getName()))
        {
            player.sendMessage(ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot pickup banned items.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        // If we aren't in the area's owners list, we can't steal
        String protectionName = plugin.protections.GetProtectionName(event.getPlayer());
        if(protectionName == null)
        {
            // Nothing to wory about; no land
            return;
        }
        
        // Are we in a protected area? If so, get the owners list
        LinkedList<String> owners = plugin.protections.GetProtectionOwners(protectionName);
        if(!owners.contains(player.getName()))
        {
            player.sendMessage(ChatColor.RED + "You cannot pickup items in this protected area.");
            event.setCancelled(true);
        }
        
        // Else, all good
    }
}
