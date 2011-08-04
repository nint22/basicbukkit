/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicBukkit.java
 Desc: Main application entry point for a bukkit plugin...
 
***************************************************************/

package nint22.basicbukkit;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;
import com.sk89q.bukkit.migration.PermissionsProvider;

public class BasicBukkit extends JavaPlugin implements PermissionsProvider
{
    // Create the main player listener object
    private BasicPlayerListener playerListener = null;
    
    // Create the main block listener
    private BasicBlockListener blockListener = null;
    
    // Create the special case event listener
    private BasicEntityListener entityListener = null;
    
    // Create the vehicle placement listener
    private BasicVehicleListener vehicleListener = null;
    
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
    
    // Global daemon system
    public BasicDaemon daemon = null;
    
    // Global item list
    public BasicItems itemNames = null;
    
    // Global locks list
    public BasicLocks locks = null;
    
    // Economy and roleplay interface
    public BasicEconomy economy = null;
    public BasicRoleplay roleplay = null;
    
    // A hashmap that contains an outgoing message of the form
    // playername_message as the key and the unix epoch time stamp as the
    // data; we only send messages after we make sure a solid 5 seconds has
    // passed since the last message was sent
    private HashMap<String, Long> MessageTime;
    
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
                System.out.println("### BasicBukkit warning: " + e.getMessage());
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
        // Stop messages (may take a second)
        messages.stop(true);
        daemon.stop(true);
        
        // Save all users and protection data
        users.save();
        protections.save();
        warps.save();
        locks.save();
        economy.save();
        roleplay.save();
        
        // Release plugin
        System.out.println("### BasicBukkit plugin disabled.");
    }
    
    // When mod is enabled
    @Override
    public void onEnable()
    {
        // Register all plugin events
        PluginManager pm = getServer().getPluginManager();
        
        // Load config file
        configuration = new Configuration(loadFile("config.yml"));
        configuration.load();
        
        // Load the items file
        itemNames = new BasicItems(loadFile("items.csv"), configuration);
        
        // Load users file
        users = new BasicUsers(this, new Configuration(loadFile("users.yml")), configuration);
        
        // Load protected areas file
        protections = new BasicProtection(new Configuration(loadFile("protections.yml")));
        
        // Load the warps
        warps = new BasicWarps(this, new Configuration(loadFile("warps.yml")));
        
        // Load the messaging system and the daemon system
        messages = new BasicMessages(this, configuration);
        daemon = new BasicDaemon(this, configuration);
        
        // Load the locks system
        locks = new BasicLocks(this, new Configuration(loadFile("locks.yml")));
        
        // Allocate economy system
        economy = new BasicEconomy(this, loadFile("prices.csv"), new Configuration(loadFile("bank.yml")));
        
        // Allocate roleplay system
        roleplay = new BasicRoleplay(this, new Configuration(loadFile("signs.yml")), new Configuration(loadFile("experiance.yml")));
        
        // Allocate the spam message check
        MessageTime = new HashMap();
        
        /*** Player Events ***/
        playerListener = new BasicPlayerListener(this);
        
        // Join and leave game
        pm.registerEvent(Event.Type.PLAYER_PRELOGIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        // Player movement limitation
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        
        // Intercept all chat messages so we can replace the color...
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        
        // Item drop / steal
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
        
        // Item and mechanism usage
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        
        /*** Block Place / Usage Events ***/
        blockListener = new BasicBlockListener(this);
        
        // Check all block placement and breaks
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        
        // Spreading fire, lava, water, etc..
        pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Normal, this);
        
        // Sign catching
        pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        
        /*** Entity Events ***/
        entityListener = new BasicEntityListener(this);
        
        // Register TNT ignition and explosion
        pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        
        // Prevent player and death damage if needed
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        
        /*** Vehicle Events ***/
        vehicleListener = new BasicVehicleListener(this);
        
        // Prevent vehicle placement
        pm.registerEvent(Event.Type.VEHICLE_CREATE, vehicleListener, Priority.Normal, this);
        
        /*** Player Commands ***/
        
        // Register all plugin commands
        BasicMiscCommands MiscCommands = new BasicMiscCommands(this);
        getCommand("help").setExecutor(MiscCommands);                           // Done
        getCommand("motd").setExecutor(MiscCommands);                           // Done
        getCommand("clear").setExecutor(MiscCommands);                          // Done
        getCommand("where").setExecutor(MiscCommands);                          // Done
        getCommand("afk").setExecutor(MiscCommands);                            // Done
        getCommand("msg").setExecutor(MiscCommands);                            // Done
        getCommand("pm").setExecutor(MiscCommands);                             // Done
        getCommand("mute").setExecutor(MiscCommands);                           // Done
        getCommand("title").setExecutor(MiscCommands);                          // Done
        
        BasicAdminCommands AdminCommands = new BasicAdminCommands(this);
        getCommand("op").setExecutor(AdminCommands);                            // Done
        getCommand("vote").setExecutor(AdminCommands);                          // Done
        getCommand("vkick").setExecutor(AdminCommands);                         // Done
        getCommand("vban").setExecutor(AdminCommands);                          // Done
        getCommand("kick").setExecutor(AdminCommands);                          // Done
        getCommand("ban").setExecutor(AdminCommands);                           // Done
        getCommand("unban").setExecutor(AdminCommands);                         // Done
        getCommand("unkick").setExecutor(AdminCommands);                        // Done
        getCommand("who").setExecutor(AdminCommands);                           // Done
        getCommand("time").setExecutor(AdminCommands);                          // Done
        getCommand("weather").setExecutor(AdminCommands);                       // Done
        getCommand("kill").setExecutor(AdminCommands);                          // Done
        getCommand("say").setExecutor(AdminCommands);                           // Done
        getCommand("god").setExecutor(AdminCommands);                           // Done
        getCommand("pvp").setExecutor(AdminCommands);                           // Done
        getCommand("iclean").setExecutor(AdminCommands);                        // Done
        getCommand("mclean").setExecutor(AdminCommands);                        // Done
        getCommand("scout").setExecutor(AdminCommands);                         // Done
        getCommand("hide").setExecutor(AdminCommands);                          // Done
        
        BasicItemCommands ItemCommands = new BasicItemCommands(this);
        getCommand("kit").setExecutor(ItemCommands);                            // Done
        getCommand("item").setExecutor(ItemCommands);                           // Done
        getCommand("i").setExecutor(ItemCommands);                              // Done
        getCommand("give").setExecutor(ItemCommands);                           // Done
        getCommand("clean").setExecutor(ItemCommands);                          // Done
        getCommand("cleanall").setExecutor(ItemCommands);                       // Done
        
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
        getCommand("mob").setExecutor(WorldCommands);                           // Done
        
        BasicProtectionCommands Protection = new BasicProtectionCommands(this);
        getCommand("p1").setExecutor(Protection);                               // Done
        getCommand("p2").setExecutor(Protection);                               // Done
        getCommand("protect").setExecutor(Protection);                          // Done
        getCommand("protectadd").setExecutor(Protection);                       // Done
        getCommand("protectrem").setExecutor(Protection);                       // Done
        getCommand("protectdel").setExecutor(Protection);                       // Done
        getCommand("protectpvp").setExecutor(Protection);                       // Done
        getCommand("protectinfo").setExecutor(Protection);                      // Done
        getCommand("lock").setExecutor(Protection);                             // Done
        getCommand("unlock").setExecutor(Protection);                           // Done
        
        // Only enable of RPG mode is on
        if(configuration.getBoolean("roleplay", false) == true)
        {
            BasicRoleplayCommands Roleplay = new BasicRoleplayCommands(this);
            getCommand("buy").setExecutor(Roleplay);                                // Done
            getCommand("sell").setExecutor(Roleplay);                               // Done
            getCommand("money").setExecutor(Roleplay);                              // Done
            getCommand("level").setExecutor(Roleplay);                              // Done
            getCommand("kjoin").setExecutor(Roleplay);                              // Done
            getCommand("kleave").setExecutor(Roleplay);                             // Done
            getCommand("kkick").setExecutor(Roleplay);                              // Done
            getCommand("kingoms").setExecutor(Roleplay);                            // Done
        }
        
        // Turn off spawn protection
        getServer().setSpawnRadius(0);
        
        // Print out plugin initialization
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( "### BasicBukkiet (v." + pdfFile.getVersion() + ") plugin enabled. ");
    }
    
    /*** Global Helper Functions ***/
    
    // Get a list of strings that are the MOTD
    public String[] GetMOTD()
    {
        // Get all motd messages
        List<Object> sourceMOTD = configuration.getList("motd");
        
        // Allocate string with +1 for extra extra server info
        String[] motd = new String[sourceMOTD.size() + 1];
        for(int i = 0; i < sourceMOTD.size(); i++)
            motd[i] = (String)sourceMOTD.get(i);
        
        // Fix colors for each string
        for(int i = 0; i < sourceMOTD.size(); i++)
            motd[i] = ColorString(motd[i]);
        
        // Append the user count
        int PlayerCount = this.getServer().getOnlinePlayers().length;
        if(PlayerCount <= 1)
            motd[motd.length - 1] = ChatColor.GRAY + "There is currently " + ChatColor.RED + PlayerCount + ChatColor.GRAY + " player online";
        else
            motd[motd.length - 1] = ChatColor.GRAY + "There are currently " + ChatColor.RED + PlayerCount + ChatColor.GRAY + " players online";
        
        // Return motd
        return motd;
    }
    
    // True if we can execute it, false otherwise
    public boolean IsCommand(Player player, Command command, String[] args, String commandName)
    {
        // Is this a match?
        if(command.getName().compareToIgnoreCase(commandName) == 0)
        {
            // Print the command we are using...
            String argsList = "[";
            for(int i = 0; i < args.length; i++)
            {
                argsList += args[i];
                if(i != args.length - 1)
                    argsList += ", ";
            }
            argsList += "]";
            System.out.println(player.getName() + ": /" + commandName + " " + argsList);
            
            // Security check (i.e. can this player execute this command?
            if(!users.CanExecute(player.getName(), commandName))
            {
                System.out.println(player.getName() + ": Group \"" + users.GetGroupName(player.getName()) + "\" (GID " + users.GetGroupID(player.getName()) + ") cannot use this command.");
                player.sendMessage(ChatColor.RED + "Your group \"" + users.GetGroupName(player.getName()) + "\" (GID " + users.GetGroupID(player.getName()) + ") cannot use this command.");
                return false;
            }
            
            // All good
            return true;
        }
        
        // Failed to match
        return false;
    }
    
    // Colorize a string (i.e. replace all &0, &1, ..., &f) with the associated color
    public String ColorString(String message)
    {
        return message.replaceAll("&([0-9a-f])", (char)0xA7 + "$1");
    }
    
    // Make a global broadcast
    public void BroadcastMessage(String message)
    {
        // Replace color and send over
        message = ColorString(message);
        System.out.println("Server log: " + message);
        getServer().broadcastMessage(message);
    }
    
    // Special function to verify outgoing text - only sends it
    // if there has been a good 5 seconds elapsed since the last message
    public void SendMessage(Player player, String message)
    {
        // Form the key
        String key = player.getName() + "_" + message;
        
        // Get current epoch time
        long epochNow = System.currentTimeMillis() / 1000;
        
        // Can we send the message now?
        boolean canSend = false;
        
        // Does it exist, if so, check the time
        if(MessageTime.containsKey(key))
        {
            // Check the time - are we up yet?
            Long epochTime = (Long)MessageTime.get(key);
            if(epochTime.longValue() <= epochNow)
            {
                // Delete this key
                MessageTime.remove(key);
                canSend = true;
            }
        }
        else
        {
            canSend = true;
            MessageTime.put(key, new Long(epochNow + 3));
        }
        
        // If we can send it, send it, AND save it as a new message event
        if(canSend)
        {
            player.sendMessage(message);
        }
    }
    
    // Special function to verify outgoing chat messages:
    // a player can only send one message per second. Returns
    // true if the buffer allows it, else, player must wait
    public boolean CanSendChat(Player player, String message)
    {
        // Form the key
        String key = "chat_" + player.getName() + "_" + message;
        
        // Get current epoch time
        long epochNow = System.currentTimeMillis() / 1000;
        
        // Can we send the message now?
        boolean canSend = false;
        
        // Does it exist, if so, check the time
        if(MessageTime.containsKey(key))
        {
            // Check the time - are we up yet?
            Long epochTime = (Long)MessageTime.get(key);
            if(epochTime.longValue() <= epochNow)
            {
                // Delete this key
                MessageTime.remove(key);
                canSend = true;
            }
        }
        else
        {
            canSend = true;
            MessageTime.put(key, new Long(epochNow + 1));
        }
        
        // Check if any good?
        return canSend;
    }
    
    /*** WorldEdit permissions system ***/
    
    @Override
    public boolean hasPermission(String string, String string1)
    {
        return users.CanWorldEdit(string);
    }
    
    @Override
    public boolean inGroup(String string, String string1)
    {
        return true;
    }
    
    @Override
    public String[] getGroups(String string)
    {
        return null;
    }
}
