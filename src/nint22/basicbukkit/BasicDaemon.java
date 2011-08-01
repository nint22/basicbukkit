/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicDaemon.java
 Desc: Creates a single thread that prints out messages defined
 in the server configuration after every x number of seconds.
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;

public class BasicDaemon extends Thread
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // Total number of minutes until we do a reload
    private int reloadMinutes;
    
    // Thread loop control
    private boolean StopLooping;
    
    // Initialize messaging thread
    public BasicDaemon(BasicBukkit plugin, Configuration config)
    {
        // Save the plugin handler
        this.plugin = plugin;
        
        // Get the reload time..
        reloadMinutes = config.getInt("reload", 15);
        
        // Start threading
        StopLooping = false;
        this.start();
    }
    
    // Stop the main thread (May take a second or two..)
    // Note to self: this code looks like some old legacy design code
    // with Java's threading standard; may want to change it...
    public void stop(boolean setStop)
    {
        StopLooping = setStop;
        try
        {
            this.join();
        }
        catch(Exception e)
        {
            System.out.println("### BasicBukkit Unable to stop joining threads for BasicDaemon");
        }
    }
    
    // Main thread loop
    @Override
    public void run()
    {
        // Create a seconds time
        Integer TotalSeconds = new Integer(0);
        
        // Keep looping
        while(!StopLooping)
        {
            // Sleep off a second
            try
            {
                // 1000 milliseconds is 1 second
                BasicMessages.sleep(1000);
            }
            catch (Exception e)
            {
                // Do nothing..
                System.out.println("### BasciBukkit BasicMessages failed to sleep");
            }
            
            // Increment the timer
            TotalSeconds++;
            
            // Is it time to reload?
            // 60 seconds in a minute, hence the multiplication
            if(TotalSeconds % reloadMinutes * 60 == 0)
            {
                // Tell the server we're going to lag a little
                plugin.BroadcastMessage(ChatColor.RED + "Server Warning: Saving plugin data...");
                
                // Attempt to do a full reload..
                plugin.getServer().reload();
                
                // Tell the server we're done!
                plugin.BroadcastMessage(ChatColor.RED + "Server refresh done!");
            }
            // After 60 seconds (every minute), check if we should do an item clean
            else if(TotalSeconds % 60 == 0)
            {
                // Do an iclean.. for each world..
                for(World world : plugin.getServer().getWorlds())
                    BasicAdminCommands.ICleanWorld(world);
            }
            
            // Main thread loop
        }
    }
    
}
