/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicBukkit.java
 Desc: Main application entry point for a bukkit plugin...
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;
import java.io.*;

public class BasicBukkit extends JavaPlugin
{
    // Create a player listener object
    private BasicPlayerListener playerListener = null;
    
    // Global configuration
    public Configuration configuration = null;
    
    // Global groups (ops)
    public BasicUsers users = null;
    
    // Global permissions (land blocks)
    public Configuration protections = null;
    
    // Global warps, homes, and spawn location
    public BasicWarps warps = null;
    
    // Global item list
    public ItemNames itemNames = null;
    
    // Create a file as needed, copying the source from the default package data
    private File loadFile(String fileName)
    {
        // Check plugin directory existance
        File BasicDirectory = new File("plugins/BasicBukkit/");
        if(!BasicDirectory.exists())
        {
            // Create dir
            BasicDirectory.mkdir();
            System.out.println("### BasicBukkut has created the BasicBukkit plugin directory");
        }
        
        // Attempt to load the "basicbukkit.yml" file
        File config = new File("plugins/BasicBukkit/" + fileName);
        if(!config.exists())
        {
            // Copy over basicbukkit.yml
            InputStream defaultFile = getClass().getClassLoader().getResourceAsStream(fileName);
            
            // Create file...
            try
            {
                // Print out we are creating a new config file...
                System.out.println("### BasicBukkit did not detect a config file: createed new file \"" + fileName + "\"");
                
                // Actually copy over as needed
                BufferedWriter out = new BufferedWriter(new FileWriter("plugins/BasicBukkit/" + fileName));
                while(defaultFile.available() > 0)
                    out.write(defaultFile.read());
                out.close();
            }
            catch(Exception e)
            {
                // Just fail out writing the error message
                System.out.println("### BasicBukkit warning: " + e.toString());
            }
            
            // Now re-open the file
            config = new File("plugins/BasicBukkit/" + fileName);
        }
        
        // Return the file
        return config;
    }
    
    // When mode is disabled
    @Override
    public void onDisable()
    {
        // Save all users and protection data
        users.save();
        protections.save();
        warps.save();
        
        // Release plugin
        System.out.println("### BasicBukkit plugin disabled.");
    }
    
    // When mod is enabled
    @Override
    public void onEnable()
    {
        // Register all plugin events
        PluginManager pm = getServer().getPluginManager();
        
        // Load the items file
        itemNames = new ItemNames(loadFile("items.csv"));
        
        // Load config file
        configuration = new Configuration(loadFile("config.yml"));
        configuration.load();
        
        // Load users file
        users = new BasicUsers(new Configuration(loadFile("users.yml")), configuration);
        
        // Load protected areas file
        protections = new Configuration(loadFile("protections.yml"));
        protections.load();
        
        // Load the warps
        warps = new BasicWarps(this, new Configuration(loadFile("warps.yml")));
        
        // Create a new basic listener
        playerListener = new BasicPlayerListener(this);
        
        // Join and leave game
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        // Player movement limitation
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        
        // Intercept all chat messages so we can replace the color...
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Monitor, this);
        
        // Register all plugin commands
        getCommand("help").setExecutor(new BasicMiscCommands(this));            // Done
        getCommand("motd").setExecutor(new BasicMiscCommands(this));            // Done
        getCommand("clear").setExecutor(new BasicMiscCommands(this));           // Done
        getCommand("where").setExecutor(new BasicMiscCommands(this));           // Done
        
        getCommand("op").setExecutor(new BasicAdminCommands(this));             // Done
        getCommand("kick").setExecutor(new BasicAdminCommands(this));           // Done
        getCommand("ban").setExecutor(new BasicAdminCommands(this));            // Done
        getCommand("who").setExecutor(new BasicAdminCommands(this));            // Done
        getCommand("time").setExecutor(new BasicAdminCommands(this));           // Done
        getCommand("weather").setExecutor(new BasicAdminCommands(this));        // Done
        getCommand("kill").setExecutor(new BasicAdminCommands(this));           // Done
        getCommand("say").setExecutor(new BasicAdminCommands(this));            // Done
        
        getCommand("kit").setExecutor(new BasicItemCommands(this));             // Done
        getCommand("item").setExecutor(new BasicItemCommands(this));            // Done
        getCommand("give").setExecutor(new BasicItemCommands(this));            // Done
        getCommand("clean").setExecutor(new BasicItemCommands(this));           // Done
        
        getCommand("tp").setExecutor(new BasicWorldCommands(this));             // Done
        getCommand("warp").setExecutor(new BasicWorldCommands(this));           // Done
        getCommand("setwarp").setExecutor(new BasicWorldCommands(this));        // Done
        getCommand("delwarp").setExecutor(new BasicWorldCommands(this));        // Done
        getCommand("home").setExecutor(new BasicWorldCommands(this));           // Done
        getCommand("sethome").setExecutor(new BasicWorldCommands(this));        // Done
        getCommand("spawn").setExecutor(new BasicWorldCommands(this));          // Done
        getCommand("setspawn").setExecutor(new BasicWorldCommands(this));       // Done
        getCommand("top").setExecutor(new BasicWorldCommands(this));            // Done
        
        getCommand("protect").setExecutor(new BasicAdminCommands(this));        
        getCommand("p1").setExecutor(new BasicAdminCommands(this));             
        getCommand("p2").setExecutor(new BasicAdminCommands(this));             
        getCommand("protectadd").setExecutor(new BasicAdminCommands(this));     
        getCommand("protectdel").setExecutor(new BasicAdminCommands(this));     
        
        // Print out plugin initialization
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "### BasicBukkiet (v." + pdfFile.getVersion() + ") plugin enabled. ");
    }
}

