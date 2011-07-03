/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicPlayerListener.java
 Desc: The basic player listener for events such as join / quit
 
***************************************************************/

package nint22.basicbukkit;

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
    
    // Constructor saves plugin handle
    public BasicPlayerListener(BasicBukkit instance)
    {
        // Save plugin
        plugin = instance;
        
        // Save max world size...
        List<Integer> sizes = plugin.configuration.getIntList("size", null);
        WorldWidth = sizes.get(0).intValue();
        WorldLength = sizes.get(1).intValue();
    }
    
    // Player joined game: run MOTD
    @Override
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Get the player
        Player player = event.getPlayer();
        
        // Say where the player game from...
        System.out.println(player.getName() + " joined the server.");
        
        // Get the motd string
        String[] motd = plugin.configuration.getString("motd").split("\n");
        for(int i = 0; i < motd.length; i++)
        {
            // Colorize and print
            motd[i] = motd[i].replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
            player.sendMessage(motd[i]);
        }
        
        // Set the player's title
        System.out.println("Title: " + plugin.users.GetUserTitle(player.getName()));
        player.setDisplayName(plugin.users.GetUserTitle(player.getName()) + player.getName());
    }
    
    // Player quit, announce globally
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        System.out.println(event.getPlayer().getName() + " left the server.");
    }
    
    // Player moves...
    public void onPlayerMove(PlayerMoveEvent event)
    {
        // Get target position
        Location location = event.getTo();
        
        // Is this location within the bounds of the width and length?
        if(location.getX() > WorldWidth / 2 || location.getX() < -WorldWidth / 2 || location.getZ() > WorldLength / 2 || location.getZ() < -WorldLength / 2)
        {
            // Warp back player
            event.getPlayer().teleport(event.getFrom());
            event.getPlayer().sendMessage(ChatColor.RED + "You have hit the world bounds of (" + WorldWidth + ", " + WorldLength + ")");
        }
    }
    
    // Player has said something...
    @Override
    public void onPlayerChat(PlayerChatEvent event) 
    {
        // Get player
        Player player = event.getPlayer();
        
        // Note the hex value is the signal byte for following colors
        event.setMessage(event.getMessage().replaceAll("&([0-9a-f])", (char)0xA7 + "$1"));
        
        // Get title (formatted with color)
        String Title = plugin.users.GetUserTitle(player.getName()).replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
        
        // Set the player's title
        player.setDisplayName(Title + " " + player.getName());
    }
}
