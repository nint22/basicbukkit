/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicAdminCommands.java
 Desc: Lists all commands the player can execute as well as
 does some of the misc commands such as motd, etc..
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class BasicMiscCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Default constructor
    public BasicMiscCommands(BasicBukkit plugin)
    {
        this.plugin = plugin;
    }
    
    // Help was called
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // Get player
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        
        // Parse each specific command supported
        if(plugin.IsCommand(player, command, args, "help"))
        {
            // Get LinkedHashMap of the commands
            PluginDescriptionFile pdfFile = plugin.getDescription();
            LinkedHashMap Map = (LinkedHashMap)pdfFile.getCommands();
            
            // Get help list
            Collection collection = Map.values();
            
            // Remove all commands that the user does NOT have access to
            String[] ValidCommands = plugin.users.GetGroupCommands(player.getName());
            if(ValidCommands == null)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to retrieve commands for your group");
            }
            else
            {
                // Command names & descriptions
                LinkedList<String> CommandName = new LinkedList();
                LinkedList<String> CommandDescription = new LinkedList();
                
                // For each collection item
                for(Object cmd : collection)
                {
                    // If within valid commands, add to list
                    boolean IsValid = false;
                    for(int i = 0; i < ValidCommands.length; i++)
                    {
                        // Get this commands info
                        // My god look at the horrible casting...
                        String Command = ((String)((LinkedHashMap)cmd).get("usage")).split(" ")[0];
                        String Description = (String)((LinkedHashMap)cmd).get("description");
                        
                        // Remove the forward slash
                        Command = Command.substring(1, Command.length());
                        
                        // Get command string
                        if(Command.compareToIgnoreCase(ValidCommands[i]) == 0)
                        {
                            IsValid = true;
                            break;
                        }
                    }
                    
                    // If valid, add to list
                    if(IsValid)
                    {
                        CommandName.add((String)((LinkedHashMap)cmd).get("usage"));
                        CommandDescription.add((String)((LinkedHashMap)cmd).get("description"));
                    }
                }

                // How many pages of help do we have?
                int Count = CommandName.size();
                int Pages = Count / 5;
                int PageIndex = 0;

                // Convert arg to int
                try
                {
                    PageIndex = Integer.parseInt(args[0]) - 1;
                }
                catch(Exception e)
                {
                    // Just default back to 0
                    PageIndex = 0;
                }

                // To lower to the lowest page count..
                PageIndex = Math.min(Pages, PageIndex);

                // How page count and command count
                // Note the +1 offset so we are human friendly (i.e. we are 1..n rather than 0..n-1)
                player.sendMessage(ChatColor.WHITE + "Page " + ChatColor.RED + "[" + (PageIndex + 1) + "]" + ChatColor.WHITE + " of " + ChatColor.RED + "[" + (Pages+1) + "]" + ChatColor.WHITE +"; " + Count + " commands available of " + collection.size());

                // Print off 5 commands for this page
                for(int i = PageIndex * 5; i < Math.min(Count, PageIndex * 5 + 5); i++)
                {
                    // Print out info
                    String name = CommandName.get(i);
                    String description = CommandDescription.get(i);
                    player.sendMessage(ChatColor.GRAY + "#" + (i+1) + ": " + ChatColor.RED + name + ChatColor.GRAY + " - " + description);
                }
            }
        }
        else if(plugin.IsCommand(player, command, args, "motd"))
        {
            // Get the motd string
            String[] motd = plugin.GetMOTD();
            for(int i = 0; i < motd.length; i++)
                player.sendMessage(motd[i]);
        }
        else if(plugin.IsCommand(player, command, args, "clear"))
        {
            // Send enough empty lines to the client to
            // make sure we clear out the user's screen buffer
            for(int i = 0; i < 20; i++)
                player.sendMessage("");
        }
        else if(plugin.IsCommand(player, command, args, "where"))
        {
            // Cast to string and change precision
            String yaw = String.format("%.2f", player.getLocation().getYaw());
            String pitch = String.format("%.2f", player.getLocation().getPitch());
            
            // Print to the player where her or she is at and their facing
            player.sendMessage(ChatColor.GRAY + "Your location: <" + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + ">, facing: <" + pitch + ", " + yaw + ">");
            
            String protectionName = plugin.protections.GetProtectionName(player);
            if(protectionName != null)
                player.sendMessage(ChatColor.GRAY + "You are in the protected area named \"" + protectionName + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "afk"))
        {
            // Set self to afk
            plugin.users.SetAFK(player.getName(), true);
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + player.getName() + "\" is now AFK");
        }
        else if(plugin.IsCommand(player, command, args, "msg"))
        {
            // Do we have at least 2 args? (0: name, 1: message, message...)
            if(args.length >= 2)
            {
                // Get player
                Player targetPlayer = plugin.getServer().getPlayer(args[0]);
                if(targetPlayer == null)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to find player \"" + targetPlayer.getName() + "\" to message");
                    return true;
                }
                
                // Form message
                String message = ChatColor.GRAY + "Private message from \"" + player.getName() + "\":";
                targetPlayer.sendMessage(message);
                
                // For full message
                message = "&f";
                for(int i = 1; i < args.length; i++)
                    message += args[i] + " ";
                message = plugin.ColorString(message);
                targetPlayer.sendMessage(message);
                
                // Message sent
                player.sendMessage(ChatColor.GRAY + "Private message sent");
            }
            else
                return false;
        }
        
        // Done - parsed
        return true;
    }
}
