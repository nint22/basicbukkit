/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicItemCommands.java
 Desc: All item commands: /item /give, etc..
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BasicItemCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Default constructor
    public BasicItemCommands(BasicBukkit plugin)
    {
        this.plugin = plugin;
    }
    
    // General admin commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // Get player
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        
        // Parse each specific command supported
        if(command.getName().compareToIgnoreCase("item") == 0)
        {
            // Set op...
        }
        // Else, unknown
        else
            return false;
        
        // Done - parsed
        return true;
    }
}
