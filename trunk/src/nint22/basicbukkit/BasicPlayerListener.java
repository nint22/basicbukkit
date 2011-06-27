/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicPlayerListener.java
 Desc: The basic player listener for events such as join / quit
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.event.player.*;

public class BasicPlayerListener extends PlayerListener
{
    // Current working plugin
    private final BasicBukkit plugin;
    
    // Constructor saves plugin handle
    public BasicPlayerListener(BasicBukkit instance)
    {
        plugin = instance;
    }
    
    // Player joined game: run MOTD
    @Override
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Say where the player game from...
        System.out.println(event.getPlayer().getName() + " joined the server.");
        
        // Get the motd string
        String[] motd = plugin.configuration.getString("motd").split("\n");
        for(int i = 0; i < motd.length; i++)
        {
            // Colorize and print
            motd[i] = motd[i].replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
            event.getPlayer().sendMessage(motd[i]);
        }
    }
    
    // Player quit, announce globally
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        System.out.println(event.getPlayer().getName() + " left the server.");
    }
    
    // Player has said something...
    @Override
    public void onPlayerChat(PlayerChatEvent event) 
    {
        // What is the player's op status?
        
        // Set this player's title
        
        // Replace the "&#" 4-bit color scheme
        // Note the hex value is the signal byte for following colors
        event.setMessage(event.getMessage().replaceAll("&([0-9a-f])", (char)0xA7 + "$1"));
    }
}
