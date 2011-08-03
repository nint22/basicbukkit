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
import org.bukkit.Material;
import org.bukkit.event.player.PlayerPreLoginEvent.*;

public class BasicPlayerListener extends PlayerListener
{
    // Current working plugin
    private final BasicBukkit plugin;
    
    // World sizes
    private int WorldWidth, WorldLength;
    
    // Locations of players (used to prevent unnecessary updates)
    private HashMap<String, String> PlayerProtectionLocations;
    
    // Is chat local-based?
    private boolean LocalChat;
    
    // If chat is local-based, what is the distance
    private int LocalChatRadius;
    
    // Constructor saves plugin handle
    public BasicPlayerListener(BasicBukkit instance)
    {
        // Save plugin
        plugin = instance;
        
        // Save max world size...
        List<Integer> sizes = plugin.configuration.getIntList("size", null);
        WorldWidth = sizes.get(0).intValue();
        WorldLength = sizes.get(1).intValue();
        
        // Get local chat information
        LocalChat = plugin.configuration.getBoolean("localchat", false);
        LocalChatRadius = plugin.configuration.getInt("chatradius", 128);
        
        // Declare we are using local chat
        if(LocalChat)
            System.out.println("### BasicBukkit enabled the local-chat system with a chat radius of " + LocalChatRadius);
        
        // Allocate player's current protection locations
        PlayerProtectionLocations = new HashMap();
    }
    
    // Do  permissions check before the player joins
    @Override
    public void onPlayerPreLogin(PlayerPreLoginEvent event)
    {
        // Check for ban
        String BanReason = plugin.users.IsBanned(event.getName());
        if(BanReason != null)
            event.disallow(Result.KICK_BANNED, "You are banned. Reason: \"" + BanReason + "\"");
        
        // Is it past their kick time?
        else if(!plugin.users.IsKicked(event.getName()))
            event.disallow(Result.KICK_OTHER, "Your kick time is not yet up.");
        
        // Else, all good!
    }
    
    // Player joined game: run MOTD
    @Override
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Get player from event
        Player player = event.getPlayer();
        
        // Show the MOTD string
        String[] motd = plugin.GetMOTD();
        for(int i = 0; i < motd.length; i++)
            player.sendMessage(motd[i]);
        
        // Has this player ever joined us before?
        if(plugin.users.GetGroupID(player.getName()) < 0)
        {
            plugin.BroadcastMessage(ChatColor.GRAY + player.getName() + " is a new user to the server.");
            
            // Register this first time user
            plugin.users.SetUserGroup(player.getName(), 0);
            
            // Warp to spawn
            if(plugin.warps.GetSpawn() != null)
                player.teleport(plugin.warps.GetSpawn());
        }
        else
            plugin.BroadcastMessage(ChatColor.GRAY + player.getName() + " is a known user to the server.");
        
        // Set the player's god mode status
        plugin.users.SetGod(player.getName(), plugin.configuration.getBoolean("defaultgod", true));
        
        // Set the player's title
        player.setDisplayName(plugin.users.GetUserTitle(player.getName()) + player.getName());
    }
    
    // Player quit, announce globally
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        //plugin.BroadcastMessage(ChatColor.GRAY + event.getPlayer().getName() + " left the server.");
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
            plugin.SendMessage(event.getPlayer(), ChatColor.RED + "You have hit the world bounds of (" + WorldWidth + ", " + WorldLength + ")");
        }
        
        // Get current zone
        String oldZone = PlayerProtectionLocations.get(event.getPlayer().getName());
        String newZone = plugin.protections.GetProtectionName(event.getPlayer().getLocation());
        
        // Did we go from a non-zone to a new zone
        if(oldZone == null && newZone != null)
        {
            event.getPlayer().sendMessage(ChatColor.GRAY + "You have walked into the protected zone \"" + newZone + "\", PVP is " + (plugin.protections.GetPVP(newZone) ? ChatColor.RED + "enabled" : ChatColor.GREEN + "disabled"));
            PlayerProtectionLocations.put(event.getPlayer().getName(), newZone);
        }
        // Did we get out of a zone to a non-zone
        else if(oldZone != null && newZone == null)
        {
            event.getPlayer().sendMessage(ChatColor.GRAY + "You have left the protected zone \"" + oldZone + "\"");
            PlayerProtectionLocations.remove(event.getPlayer().getName());
        }
        // Did we change zones?
        else if(oldZone != null && newZone != null && !newZone.equalsIgnoreCase(oldZone))
        {
            event.getPlayer().sendMessage(ChatColor.GRAY + "You have left the protected zone \"" + oldZone + "\" and are now in \"" + newZone + "\", PVP is " + (plugin.protections.GetPVP(newZone) ? ChatColor.RED + "enabled" : ChatColor.GREEN + "disabled"));
            PlayerProtectionLocations.put(event.getPlayer().getName(), newZone);
        }
    }
    
    // Player has said something...
    @Override
    public void onPlayerChat(PlayerChatEvent event) 
    {
        // Get player
        Player player = event.getPlayer();
        
        // Player not longer afk
        if(plugin.users.GetAFK(player.getName()))
        {
            plugin.users.SetAFK(player.getName(), false);
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + event.getPlayer().getName() + "\" is no longer AFK");
        }
        
        // Is this player muted?
        if(plugin.users.IsMute(player))
        {
            player.sendMessage(ChatColor.GRAY + "You are currently muted.");
            event.setCancelled(true);
            return;
        }
        
        // Is this player spamming?
        if(!plugin.CanSendChat(player, event.getMessage()))
        {
            player.sendMessage(ChatColor.GRAY + "You are chatting too fast; please wait.");
            event.setCancelled(true);
            return;
        }
        
        // Update for colors
        String message = event.getMessage();
        message = plugin.ColorString(message);
        event.setMessage(message);
        
        // Get title (formatted with color)
        String title = plugin.users.GetUserTitle(player.getName());
        title = plugin.ColorString(title);
        
        // Set the player's title
        player.setDisplayName(title + " " + player.getName());

        // If we are using local chat, customize the messaging event
        if(LocalChat)
        {
            // Cancel the event, we will manually send out player info
            event.setCancelled(true);
            
            // For each player...
            for(Player target : plugin.getServer().getOnlinePlayers())
            {
                // Is the distance short enough to send?
                if(target.getLocation().distance(player.getLocation()) <= LocalChatRadius)
                    player.sendMessage(player.getDisplayName() + " " + event.getMessage());
            }
            
            // Done sending local
        }
    }
    
    // Player permissions check
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        /*** Ban Check ***/
        
        // Can the player place this block?
        Player player = event.getPlayer();
        int ItemID = event.getItemDrop().getItemStack().getTypeId();
        
        // If the player is not allowed to build, then they are not allowed to
        // throw / drop off items
        if(!plugin.users.CanBuild(player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot pickup items.");
            event.setCancelled(true);
            return;
        }
        
        // If we cannot place banned items...
        if(!plugin.users.CanUseItem(ItemID, player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot drop banned items.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        // If we aren't in the area's owners list, we can't steal
        String protectionName = plugin.protections.GetProtectionName(event.getItemDrop().getLocation());
        if(protectionName == null)
        {
            // Nothing to wory about; no land
            return;
        }
        
        // Are we in a protected area? If so, get the owners list
        LinkedList<String> owners = plugin.protections.GetProtectionOwners(protectionName);
        if(!owners.contains(player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "You cannot drop items in this protected area.");
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
        
        // If the player is not allowed to build, then they are not allowed to
        // pickup items
        if(!plugin.users.CanBuild(player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot pickup items.");
            event.setCancelled(true);
            return;
        }
        
        // If we cannot place banned items...
        if(!plugin.users.CanUseItem(ItemID, player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "Your group (GID " + plugin.users.GetGroupID(player.getName()) + ", " + plugin.users.GetGroupName(player.getName()) + ") cannot pickup banned items.");
            event.setCancelled(true);
            return;
        }
        
        /*** Protection Check ***/
        
        // If we aren't in the area's owners list, we can't steal
        String protectionName = plugin.protections.GetProtectionName(event.getPlayer().getLocation());
        if(protectionName == null)
        {
            // Nothing to wory about; no land
            return;
        }
        
        // Are we in a protected area? If so, get the owners list
        LinkedList<String> owners = plugin.protections.GetProtectionOwners(protectionName);
        if(!owners.contains(player.getName()))
        {
            plugin.SendMessage(player, ChatColor.RED + "You cannot pickup items in this protected area.");
            event.setCancelled(true);
        }
        
        // Else, all good
    }
    
    // If the player attempts to interact / use items
    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        // If this is a door of sorts, do a check of ownership
        Material targetBlock = event.getClickedBlock() == null ? null : event.getClickedBlock().getType();
        if(targetBlock != null && (targetBlock == Material.CHEST || targetBlock == Material.WOODEN_DOOR || targetBlock == Material.IRON_DOOR_BLOCK || targetBlock == Material.TRAP_DOOR))
        {
            // Who is the owner of the block
            String owner = plugin.locks.LockOwner(event.getClickedBlock().getLocation());
            if(owner != null)
            {
                // If not the owner, cancel event
                if(!owner.equals(event.getPlayer().getName()))
                {
                    event.setCancelled(true);
                    plugin.SendMessage(event.getPlayer(), ChatColor.GRAY + "You cannot open or use this item; the owner is \"" + owner + "\"");
                }
                else
                    plugin.SendMessage(event.getPlayer(), ChatColor.GRAY + "You have opened or used a locked item of yours");
            }
        }
        
        // Do we allow jumping?
        if(plugin.configuration.getBoolean("compassjump", false))
        {
            // Is it a compass?
            if(event.getMaterial() == Material.COMPASS)
            {
                // Attempt a jump
                BasicWorldCommands.JumpAtTarget(event.getPlayer());
            }
        }
    }
}
