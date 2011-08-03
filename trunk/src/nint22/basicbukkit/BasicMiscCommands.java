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
            // Only accepts 0 to 1 args
            if(args.length < 0 || args.length > 1)
                return false;
            
            // Get LinkedHashMap of the commands
            PluginDescriptionFile pdfFile = plugin.getDescription();
            LinkedHashMap Map = (LinkedHashMap)pdfFile.getCommands();
            
            // Get help list
            Collection collection = Map.values();
            
            // Remove all commands that the user does NOT have access to
            String[] ValidCommands = plugin.users.GetGroupCommands(player.getName());
            if(ValidCommands == null)
                player.sendMessage(ChatColor.GRAY + "Unable to retrieve commands for your group");
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
                
                // Convert arg to int OR if it is a command, just print the command
                try
                {
                    // To lower to the lowest page count..
                    if(args.length == 1)
                        PageIndex = Integer.parseInt(args[0]) - 1;
                    else
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
                catch(Exception e)
                {
                    // Is it a known command?
                    String query = args[0];
                    if(Map.containsKey(query.toLowerCase()))
                    {
                        LinkedHashMap cmd = (LinkedHashMap)Map.get(query.toLowerCase());
                        String usage = (String)((LinkedHashMap)cmd).get("usage");
                        String description = (String)((LinkedHashMap)cmd).get("description");
                        player.sendMessage(ChatColor.GRAY + "Command usage: " + ChatColor.RED + usage);
                        player.sendMessage(ChatColor.GRAY + "Description: " + description);
                    }
                    else
                        player.sendMessage(ChatColor.GRAY + "Unknown or inaccessible command \"" + args[0] + "\"");
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
            
            String protectionName = plugin.protections.GetProtectionName(player.getLocation());
            if(protectionName != null)
                player.sendMessage(ChatColor.GRAY + "You are in the protected area named \"" + protectionName + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "afk"))
        {
            // Set self to afk
            plugin.users.SetAFK(player.getName(), true);
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + player.getName() + "\" is now AFK");
        }
        else if(plugin.IsCommand(player, command, args, "msg") || plugin.IsCommand(player, command, args, "pm"))
        {
            // Do we have at least 2 args? (0: name, 1: message, message...)
            if(args.length >= 2)
            {
                // Get player
                Player targetPlayer = plugin.getServer().getPlayer(args[0]);
                if(targetPlayer == null)
                {
                    player.sendMessage(ChatColor.GRAY + "Unable to find player \"" + args[0] + "\" to message");
                    return true;
                }
                
                // Form message
                String message = ChatColor.GREEN + "Private message from \"" + player.getName() + "\":";
                targetPlayer.sendMessage(message);
                
                // For full message
                message = "&f";
                for(int i = 1; i < args.length; i++)
                    message += args[i] + " ";
                message = plugin.ColorString(message);
                targetPlayer.sendMessage(message);
                
                // Message sent
                player.sendMessage(ChatColor.GREEN + "Private message sent to \"" + targetPlayer.getName() + "\":");
                player.sendMessage(message);
            }
            else
                return false;
        }
        else if(plugin.IsCommand(player, command, args, "mute"))
        {
            // Mutes the given player name
            if(args.length < 1)
                return false;
            
            // Get player
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to find player \"" + args[0] + "\"");
                return true;
            }
            
            // Can only mute ranks lower
            int SourceGID = plugin.users.GetGroupID(player.getName());
            int TargetGID = plugin.users.GetGroupID(target.getName());
            if(TargetGID >= SourceGID)
            {
                // Cannot mute GIDs higher or equal
                player.sendMessage(ChatColor.GRAY + "Cannot mute users with an equal or higher group ID (theirs: " + TargetGID + ", yours: " + SourceGID + ")");
                return true;
            }
            
            // Get the current status for this target player by this player
            boolean IsMuted = plugin.users.IsMute(target);
            if(IsMuted)
                IsMuted = false;
            else
                IsMuted = true;
            plugin.users.SetMute(target, IsMuted);
            
            // Message both players
            if(IsMuted)
            {
                player.sendMessage(ChatColor.GRAY + "You have muted player \"" + target.getName() + "\"");
                target.sendMessage(ChatColor.GRAY + "Player \"" + player.getName() + "\" can no longer hear you (muted)");
            }
            else
            {
                player.sendMessage(ChatColor.GRAY + "You have unmuted player \"" + target.getName() + "\"");
                target.sendMessage(ChatColor.GRAY + "Player \"" + player.getName() + "\" can now longer hear you (unmuted)");
            }
        }
        else if(plugin.IsCommand(player, command, args, "title"))
        {
            // We must have least 2 argument
            if(args.length < 1)
                return false;
            
            // Find the player
            Player target = plugin.getServer().getPlayer(args[0]);
            if(target == null)
            {
                player.sendMessage(ChatColor.GRAY + "Player \"" + args[0] + "\" not found");
                return true;
            }
            
            // Do we have a title? (Defaults to blank)
            String NewTitle = "";
            if(args.length > 1)
            {
                for(int i = 1; i < args.length; i++)
                {
                    NewTitle += args[i];
                    if(i != args.length - 1)
                        NewTitle += " ";
                }
            }
            
            // Set the user's new title and make the announcement
            plugin.users.SetTitle(target, NewTitle);
            if(NewTitle.length() > 0)
                plugin.BroadcastMessage(ChatColor.RED + "Player \"" + target.getName() + "\" has a new title \"" + NewTitle + ChatColor.RED + "\" given by " + player.getName() + "\"");
            else
                plugin.BroadcastMessage(ChatColor.RED + "Player \"" + target.getName() + "\" has had their title removed by \"" + player.getName() + "\"");
        }
        
        // Done - parsed
        return true;
    }
}
