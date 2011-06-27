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
import java.util.logging.*;

public class BasicBukkit extends JavaPlugin
{
    // Create a player listener object
    private final BasicPlayerListener playerListener = new BasicPlayerListener(this);
    
    // Global configuration
    public Configuration configuration = null;
    
    // Global logging system
    public Logger log = Logger.getLogger("Minecraft");
    
    // When mode is disabled
    @Override
    public void onDisable()
    {
        // Release plugin
        System.out.println("### BasicBukkit plugin disabled.");
    }
    
    // When mod is enabled
    @Override
    public void onEnable()
    {
        // Register all plugin events
        PluginManager pm = getServer().getPluginManager();
        
        // Attempt to load the "basicbukkit.yml" file
        File config = new File("plugins/basicbukkit.yml");
        if(!config.exists())
        {
            // Copy over basicbukkit.yml
            InputStream defaultFile = getClass().getClassLoader().getResourceAsStream("basicbukkit.yml");
            
            // Create file...
            try
            {
                // Print out we are creating a new config file...
                System.out.println("### BasicBukkit did not detect a config file: create new \"basicbukkit.yml\"");
                
                // Actually copy over as needed
                BufferedWriter out = new BufferedWriter(new FileWriter("plugins/basicbukkit.yml"));
                while(defaultFile.available() > 0)
                    out.write(defaultFile.read());
                out.close();
            }
            catch(Exception e)
            {
                // Just fail out writing the error message
                System.out.println("### BasicBukkit failed to initialize: " + e.toString());
                System.exit(0);
            }
            
            // Now re-open the file
            config = new File("plugins/basicbukkit.yml");
        }
        
        // Load config file
        configuration = new Configuration(config);
        configuration.load();
        
        // Join and leave game
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        // Intercept all chat messages so we can replace the color...
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Monitor, this);
        
        // Register all plugin commands
        getCommand("help").setExecutor(new BasicMiscCommands(this));
        getCommand("motd").setExecutor(new BasicMiscCommands(this));
        
        getCommand("op").setExecutor(new BasicAdminCommands(this));
        getCommand("kick").setExecutor(new BasicAdminCommands(this));
        getCommand("ban").setExecutor(new BasicAdminCommands(this));
        getCommand("who").setExecutor(new BasicAdminCommands(this));
        getCommand("time").setExecutor(new BasicAdminCommands(this));
        getCommand("weather").setExecutor(new BasicAdminCommands(this));
        getCommand("kill").setExecutor(new BasicAdminCommands(this));
        
        getCommand("kit").setExecutor(new BasicItemCommands(this));
        getCommand("item").setExecutor(new BasicItemCommands(this));
        getCommand("give").setExecutor(new BasicItemCommands(this));
        
        getCommand("tp").setExecutor(new BasicAdminCommands(this));
        getCommand("warp").setExecutor(new BasicAdminCommands(this));
        getCommand("setwarp").setExecutor(new BasicAdminCommands(this));
        getCommand("delwarp").setExecutor(new BasicAdminCommands(this));
        getCommand("home").setExecutor(new BasicAdminCommands(this));
        getCommand("sethome").setExecutor(new BasicAdminCommands(this));
        getCommand("spawn").setExecutor(new BasicAdminCommands(this));
        getCommand("setspawn").setExecutor(new BasicAdminCommands(this));
        getCommand("protect").setExecutor(new BasicAdminCommands(this));
        getCommand("p1").setExecutor(new BasicAdminCommands(this));
        getCommand("p2").setExecutor(new BasicAdminCommands(this));
        getCommand("protectadd").setExecutor(new BasicAdminCommands(this));
        getCommand("protectdel").setExecutor(new BasicAdminCommands(this));
        getCommand("top").setExecutor(new BasicAdminCommands(this));
        
        // Print out plugin initialization
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "### BasicBukkiet (v." + pdfFile.getVersion() + ") plugin enabled. ");
    }
}

