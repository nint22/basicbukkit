/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicMessages.java
 Desc: Creates a single thread that prints out messages defined
 in the server configuration after every x number of seconds.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

public class BasicMessages extends Thread
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // List of all messages in parallel with times and the start countdown
    // All of these arrays are indexed in parallel
    private LinkedList<String> messages = null;
    private LinkedList<Integer> delays = null;
    private LinkedList<Integer> starts = null;
    
    // Main loop controlling boolean
    private boolean StopLooping = false;
    
    // Initialize messaging thread
    public BasicMessages(BasicBukkit plugin, Configuration config)
    {
        // Save the plugin handler
        this.plugin = plugin;
        
        // Allocate lists
        messages = new LinkedList();
        delays = new LinkedList();
        starts = new LinkedList();

        // Parse list of messages
        List<Object> MessageData = config.getList("messages");
        if(MessageData != null)
        {
            // Save each group
            // Format; {message=string, delay=integer}
            for(Object obj : MessageData)
            {
                // Convert to hash map
                LinkedHashMap map = (LinkedHashMap)obj;
                
                String Message = (String)map.get("message");
                Integer Delay = (Integer)map.get("delay");
                Integer Start = (Integer)map.get("start");
                
                // Save message
                messages.add(Message);
                delays.add(Delay);
                starts.add(Start);
            }
        }
        
        // Say how much we loaded
        System.out.println("### BasciBukkit loaded " + messages.size() + " server messages to broadcast");
        
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
            System.out.println("### BasicBukkit Unable to stop joining threads for BasicMessages");
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
            
            // For each message
            for(int i = 0; i < messages.size(); i++)
            {
                // Did we pass start?
                if(TotalSeconds >= starts.get(i))
                {
                    // Is it time to broadcast?
                    if((TotalSeconds - starts.get(i)) % delays.get(i) == 0)
                    {
                        String message = messages.get(i);
                        message = plugin.ColorString(message);
                        
                        plugin.BroadcastMessage(ChatColor.LIGHT_PURPLE + "Automated Broadcast:");
                        plugin.BroadcastMessage(ChatColor.LIGHT_PURPLE + message);
                    }
                }
            }
            
            // All done...
        }
    }
    
}
