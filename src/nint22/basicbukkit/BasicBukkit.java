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
import java.util.List;

public class BasicBukkit extends JavaPlugin
{
    // Create the main player listener object
    private BasicPlayerListener playerListener = null;
    
    // Create the main block listener
    private BasicBlockListener blockListener = null;
    
    // Create the special case event listener
    private BasicEntityListener entityListener = null;
    
    // Global configuration
    public Configuration configuration = null;
    
    // Global groups (ops)
    public BasicUsers users = null;
    
    // Global permissions (land blocks)
    public BasicProtection protections = null;
    
    // Global warps, homes, and spawn location
    public BasicWarps warps = null;
    
    // Global messaging system, prints messages after certain time...
    public BasicMessages messages = null;
    
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
        
        // Stop messages (may take a second)
        messages.stop(true);
        
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
        protections = new BasicProtection(new Configuration(loadFile("protections.yml")));
        
        // Load the warps
        warps = new BasicWarps(this, new Configuration(loadFile("warps.yml")));
        
        // Load the messaging system
        messages = new BasicMessages(this, configuration);
        
        /*** Player Events ***/
        playerListener = new BasicPlayerListener(this);
        
        // Join and leave game
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        // Player movement limitation
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        
        // Intercept all chat messages so we can replace the color...
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        
        /*** Block Place / Usage Events ***/
        blockListener = new BasicBlockListener(this);
        
        // Check all block placement and breaks
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        
        // Spreading fire, lava, water, etc..
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        
        /*** Entity Events ***/
        entityListener = new BasicEntityListener(this);
        
        // Register TNT ignition and explosion
        pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        
        // Prevent player damage if needed
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        
        /*** Player Commands ***/
        
        // Register all plugin commands
        BasicMiscCommands MiscCommands = new BasicMiscCommands(this);
        getCommand("help").setExecutor(MiscCommands);                           // Done
        getCommand("motd").setExecutor(MiscCommands);                           // Done
        getCommand("clear").setExecutor(MiscCommands);                          // Done
        getCommand("where").setExecutor(MiscCommands);                          // Done
        
        BasicAdminCommands AdminCommands = new BasicAdminCommands(this);
        getCommand("op").setExecutor(AdminCommands);                            // Done
        getCommand("kick").setExecutor(AdminCommands);                          // Done
        getCommand("ban").setExecutor(AdminCommands);                           // Done
        getCommand("unban").setExecutor(AdminCommands);                         // Done
        getCommand("who").setExecutor(AdminCommands);                           // Done
        getCommand("time").setExecutor(AdminCommands);                          // Done
        getCommand("weather").setExecutor(AdminCommands);                       // Done
        getCommand("kill").setExecutor(AdminCommands);                          // Done
        getCommand("say").setExecutor(AdminCommands);                           // Done
        getCommand("god").setExecutor(AdminCommands);                           // Done
        
        BasicItemCommands ItemCommands = new BasicItemCommands(this);
        getCommand("kit").setExecutor(ItemCommands);                            // Done
        getCommand("item").setExecutor(ItemCommands);                           // Done
        getCommand("i").setExecutor(ItemCommands);                              // Done
        getCommand("give").setExecutor(ItemCommands);                           // Done
        getCommand("clean").setExecutor(ItemCommands);                          // Done
        
        BasicWorldCommands WorldCommands = new BasicWorldCommands(this);
        getCommand("tp").setExecutor(WorldCommands);                            // Done
        getCommand("warp").setExecutor(WorldCommands);                          // Done
        getCommand("list").setExecutor(WorldCommands);                          // Done
        getCommand("setwarp").setExecutor(WorldCommands);                       // Done
        getCommand("delwarp").setExecutor(WorldCommands);                       // Done
        getCommand("home").setExecutor(WorldCommands);                          // Done
        getCommand("sethome").setExecutor(WorldCommands);                       // Done
        getCommand("spawn").setExecutor(WorldCommands);                         // Done
        getCommand("setspawn").setExecutor(WorldCommands);                      // Done
        getCommand("top").setExecutor(WorldCommands);                           // Done
        getCommand("jump").setExecutor(WorldCommands);                          // Done
        
        BasicProtectionCommands Protection = new BasicProtectionCommands(this);
        getCommand("p1").setExecutor(Protection);                               // Done
        getCommand("p2").setExecutor(Protection);                               // Done
        getCommand("protect").setExecutor(Protection);                          // Done
        getCommand("protectadd").setExecutor(Protection);                       // Done
        getCommand("protectrem").setExecutor(Protection);                       // Done
        getCommand("protectdel").setExecutor(Protection);                       // Done
        
        // Turn off spawn protection
        getServer().setSpawnRadius(0);
        
        // Print out plugin initialization
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "### BasicBukkiet (v." + pdfFile.getVersion() + ") plugin enabled. ");
    }
    
    // Get a list of strings that are the MOTD
    public String[] GetMOTD()
    {
        // Get all motd messages
        List<Object> sourceMOTD = configuration.getList("motd");
        
        String[] motd = new String[sourceMOTD.size()];
        for(int i = 0; i < sourceMOTD.size(); i++)
            motd[i] = (String)sourceMOTD.get(i);
        
        // Fix colors for each string
        for(int i = 0; i < motd.length; i++)
            motd[i] = motd[i].replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
        
        // Return motd
        return motd;
    }
}

