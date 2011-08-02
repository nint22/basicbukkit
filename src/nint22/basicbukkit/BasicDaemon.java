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

import java.io.*;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class BasicDaemon extends Thread
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // Total number of minutes until we do a reload
    private int ReloadMinutes;
    
    // Thread loop control
    private boolean StopLooping;
    
    // Target permanent time
    private int TargetPermaTime;
    
    // Logging delay in minutes
    private int LogTime;
    
    // Stat log itself
    BufferedWriter Log = null;
    
    // Initialize messaging thread
    public BasicDaemon(BasicBukkit plugin, Configuration config)
    {
        // Save the plugin handler
        this.plugin = plugin;
        
        // Get the reload time..
        ReloadMinutes = config.getInt("reload", 15);
        
        // Read the permanent time if it was set, else the day-night cycle alone
        TargetPermaTime = plugin.configuration.getInt("permatime", -1);
        if(TargetPermaTime <= 0)
            TargetPermaTime = -1;
        
        // Read the log frequency (every n minutes, else we don't save)
        LogTime = plugin.configuration.getInt("logdelay", -1);
        if(LogTime <= 0)
            LogTime = -1;
        
        // Open a file to write + append to
        if(LogTime > 0)
        {
            try
            {
                Log = new BufferedWriter(new FileWriter("plugins/BasicBukkit/basicstats.csv", true));
            }
            catch(Exception e)
            {
                System.out.println("### BasicBukkit unable to append+write to log file: " + e.getMessage());
                System.exit(-1);
            }
        }
        
        // State what we are doing
        System.out.println("### BasicBukkit " + (ReloadMinutes > 0 ? ("saves every " + ReloadMinutes + " minutes") : "will not save at run-time") + " and " + (LogTime > 0 ? ("logs statistics every " + LogTime + " minutes") : "will not log statistics"));
        
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
        
        // Save log
        if(Log != null)
        {
            try
            {
                Log.close();
            }
            catch(Exception e)
            {
                System.out.println("### BasicBukkit unable to append+write to log file: " + e.getMessage());
                System.exit(-1);
            }
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
            if(TotalSeconds % (ReloadMinutes * 60) == 0)
            {
                // Tell the server we're going to lag a little
                plugin.BroadcastMessage(ChatColor.RED + "BasicBukkit: Saving plugin data...");
                
                // Attempt to do a full reload..
                plugin.getServer().savePlayers();
                plugin.protections.save();
                plugin.users.save();
                plugin.warps.save();
                
                // Tell the server we're done!
                plugin.BroadcastMessage(ChatColor.RED + "BasicBukkit: Server refresh done!");
            }
            
            // After 60 seconds (every minute), check if we should do an item clean
            if(TotalSeconds % 60 == 0)
            {
                // Do an iclean.. for each world..
                for(World world : plugin.getServer().getWorlds())
                    BasicAdminCommands.ICleanWorld(world);
            }
            
            // After 5 seconds, reset the world clock
            if(TotalSeconds % 5 == 0 && TargetPermaTime > 0)
            {
                for(World world : plugin.getServer().getWorlds())
                    world.setTime(TargetPermaTime);
            }
            
            // Every minute, log the server's status
            if(LogTime > 0 && TotalSeconds % LogTime == 0)
            {
                // Save some data...
                try
                {
                    String timeStamp = new Date().toString();
                    String playerList = "";
                    for(Player player : plugin.getServer().getOnlinePlayers())
                        playerList += player.getName() + " ";
                    Log.write(timeStamp + ",\t" + plugin.getServer().getOnlinePlayers().length + ",\t" + playerList + "\n");
                    Log.flush();
                }
                catch(Exception e)
                {
                    System.out.println("### BasicBukkit unable to append+write to log file: " + e.getMessage());
                    System.exit(-1);
                }
                
            }
            
            // Main thread loop
        }
    }
    
}
