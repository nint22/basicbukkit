/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicAdminCommands.java
 Desc: Implements all major admin commands; these are commands called
 by special op groups and via the server's console interface.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

// Special inclusions to help with invisibility hack...
// Note that this code requires the original server lib as well
// as is based on a current server-side bug; may not
// be supported in future clients
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BasicAdminCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;

    // Random number generator
    private Random randomGenerator;
    
    // Default constructor
    public BasicAdminCommands(BasicBukkit plugin)
    {
        this.plugin = plugin;
        
        // Generate a random number gen.
        randomGenerator = new Random();
    }
    
    // General admin commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        /*** CONSOLE COMMANDS ***/
        if (sender instanceof ConsoleCommandSender)
        {
            // Say we are a console
            if(command.getName().equalsIgnoreCase("op"))
            {
                // Command format: /op <player> [op level, defaults to 0]
                // There can only be either 1 or 2 args
                if(args.length < 1 || args.length > 2)
                    return false;
                
                // Execute the op
                PlayerOp(null, args);
            }
            else if(command.getName().equalsIgnoreCase("kick"))
            {
                // Kick
                if(args.length < 1 || args.length > 2)
                    return false;
                
                // Kick with args
                PlayerKick(null, args);
            }
            else if(command.getName().equalsIgnoreCase("ban"))
            {
                // Must have a user name and reason
                if(args.length < 1)
                    return false;
                
                // Ban with args
                PlayerBan(null, args);
                
            }
            else if(command.getName().equalsIgnoreCase("unban") || command.getName().equalsIgnoreCase("unkick"))
            {
                // Must have a user name
                if(args.length < 1)
                    return false;
                
                // Get player name
                String playerName = plugin.users.SetUnban(args[0]);
                
                // Remove player if found
                if(playerName != null)
                    plugin.BroadcastMessage(ChatColor.RED + "Player \"" + playerName + "\" is now unbaned by \"" + "Server Console" + "\"");
                else
                    sender.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\" in banned logs");
            }
            else if(command.getName().equalsIgnoreCase("who"))
            {
                // Do we have an arg?
                if(args.length > 0)
                {
                    // For each arg
                    for(int i = 0; i < args.length; i++)
                    {
                        // Get player and get player
                        Player whoTarget = plugin.getServer().getPlayer(args[i]);
                        sender.sendMessage(ChatColor.GRAY + whoTarget.getName() + ": " + whoTarget.getAddress().getAddress().getHostAddress());
                    }
                }
                // Else, just list all players
                else
                {
                    // Form users list
                    String allPlayers = "";
                    for(int i = 0; i < plugin.getServer().getOnlinePlayers().length; i++)
                    {
                        String playerName = plugin.getServer().getOnlinePlayers()[i].getDisplayName();
                        allPlayers += playerName;
                        if(i != plugin.getServer().getOnlinePlayers().length - 1)
                            allPlayers += ", ";
                    }

                    // Replace colors..
                    allPlayers = plugin.ColorString(allPlayers);

                    // Print all
                    sender.sendMessage(ChatColor.GRAY + "Online Players: (" + plugin.getServer().getOnlinePlayers().length + ")");
                    sender.sendMessage(allPlayers);
                }
            }
            else if(command.getName().equalsIgnoreCase("say"))
            {
                // Send this message to all players
                if(args.length < 1)
                {
                    sender.sendMessage(ChatColor.GRAY + "You must say something!");
                }
                // Send to all
                else
                {
                    // Form total string
                    String message = " ";

                    for(int i = 0; i < args.length; i++)
                        message += " " + args[i];

                    // Send to all
                    message = plugin.ColorString(message);
                    plugin.BroadcastMessage("Server admin says:" + message);
                }
            }
            
            // Execute console command
            return true;
        }
        /*** PLAYER COMMANDS ***/
        else if (sender instanceof Player)
        {
            // Get player object
            Player player = (Player) sender;

            // Parse each specific command supported
            if(plugin.IsCommand(player, command, args, "op"))
            {
                // Command format: /op <player> [op level, defaults to 0]
                // There can only be either 1 or 2 args
                if(args.length < 1 || args.length > 2)
                    return false;
                
                // Execute the op
                PlayerOp(player, args);
            }
            else if(plugin.IsCommand(player, command, args, "kick"))
            {
                // Kick
                if(args.length < 1 || args.length > 2)
                    return false;
                
                // Kick with args
                PlayerKick(player, args);
            }
            else if(plugin.IsCommand(player, command, args, "ban"))
            {
                // Must have a user name and reason
                if(args.length < 1)
                    return false;
                
                // Ban with args
                PlayerBan(player, args);
            }
            else if(plugin.IsCommand(player, command, args, "unban") || plugin.IsCommand(player, command, args, "unkick"))
            {
                // Must have a user name
                if(args.length < 1)
                    return false;
                
                // Get player name
                String playerName = plugin.users.SetUnban(args[0]);
                
                // Remove player if found
                if(playerName != null)
                    plugin.BroadcastMessage(ChatColor.RED + "Player \"" + playerName + "\" is now unbaned by \"" + player.getName() + "\"");
                else
                    sender.sendMessage(ChatColor.GRAY + "Cannot find player \"" + args[0] + "\" in banned logs");
            }
            else if(plugin.IsCommand(player, command, args, "who"))
            {
                // Do we have an arg?
                if(args.length > 0)
                {
                    // For each arg
                    for(int i = 0; i < args.length; i++)
                    {
                        // Get player and get player
                        Player whoTarget = plugin.getServer().getPlayer(args[i]);
                        player.sendMessage(ChatColor.GRAY + whoTarget.getName() + ": " + whoTarget.getAddress().getAddress().getHostAddress());
                    }
                }
                // Else, just list all players
                else
                {
                    // Form users list
                    String allPlayers = "";
                    for(int i = 0; i < plugin.getServer().getOnlinePlayers().length; i++)
                    {
                        String playerName = plugin.getServer().getOnlinePlayers()[i].getDisplayName();
                        allPlayers += playerName;
                        if(i != plugin.getServer().getOnlinePlayers().length - 1)
                            allPlayers += ", ";
                    }

                    // Replace colors..
                    allPlayers = plugin.ColorString(allPlayers);

                    // Print all
                    player.sendMessage(ChatColor.GRAY + "Online Players: (" + plugin.getServer().getOnlinePlayers().length + ")");
                    player.sendMessage(allPlayers);
                }
            }
            else if(plugin.IsCommand(player, command, args, "time"))
            {
                // What are we setting to?
                String time = "";
                if(args.length > 0 && args[0] != null)
                    time = args[0];

                // Get worlds list
                List<World> worlds = plugin.getServer().getWorlds();

                // Get arg if exists
                for(World world : worlds)
                {
                    if(time.compareToIgnoreCase("dawn") == 0)
                        world.setTime(0);
                    else if(time.compareToIgnoreCase("day") == 0)
                        world.setTime(6000);
                    else if(time.compareToIgnoreCase("dusk") == 0)
                        world.setTime(12000);
                    else if(time.compareToIgnoreCase("night") == 0)
                        world.setTime(37700);
                    else
                        return false; // Failed
                }

                // Say we changed the weather
                plugin.BroadcastMessage(ChatColor.GRAY + "Time set to " + time.toLowerCase() + " by \"" + player.getName() + "\"");
            }
            else if(plugin.IsCommand(player, command, args, "weather"))
            {
                // What are we setting to?
                String weatherType = "";
                if(args.length > 0 && args[0] != null)
                    weatherType = args[0];

                // Get worlds list
                List<World> worlds = plugin.getServer().getWorlds();

                // Get arg if exists
                for(World world : worlds)
                {
                    if(weatherType.compareToIgnoreCase("dry") == 0)
                        world.setStorm(false);
                    else if(weatherType.compareToIgnoreCase("wet") == 0)
                        world.setStorm(true);
                    else
                        return false; // Failed
                }

                // Say we changed the weather
                plugin.BroadcastMessage(ChatColor.GRAY + "Weather set to " + weatherType.toLowerCase() + " by \"" + player.getName() + "\"");
            }
            else if(plugin.IsCommand(player, command, args, "kill"))
            {
                // Do we have an arg?
                if(args.length > 0)
                {
                    // Find the target player
                    Player target = plugin.getServer().getPlayer(args[0]);
                    if(target != null)
                    {
                        target.setHealth(0);
                        target.sendMessage(ChatColor.GRAY + "You have been killed by " + player.getName());
                    }
                    else
                        player.sendMessage("Unable to find player \"" + args[0] + "\"");
                }
                // Else, kill self
                else
                    player.setHealth(0);
            }
            else if(plugin.IsCommand(player, command, args, "say"))
            {
                // Send this message to all players
                if(args.length < 1)
                {
                    player.sendMessage(ChatColor.GRAY + "You must say something!");
                }
                // Send to all
                else
                {
                    // Form total string
                    String message = " ";

                    for(int i = 0; i < args.length; i++)
                        message += " " + args[i];

                    // Send to all
                    message = plugin.ColorString(message);
                    plugin.BroadcastMessage(ChatColor.RED + player.getName() + " says:" + message);
                }
            }
            else if(plugin.IsCommand(player, command, args, "god") || plugin.IsCommand(player, command, args, "pvp"))
            {
                // Get current god state
                boolean IsGod = plugin.users.IsGod(player.getName());

                // Invert
                if(IsGod == true)
                    IsGod = false;
                else
                    IsGod = true;

                // Save god mode
                plugin.users.SetGod(player.getName(), IsGod);

                // Tell the player if it is on or off
                //player.getWorld().strikeLightningEffect(player.getLocation());
                player.sendMessage(ChatColor.GRAY + "God mode has been turned " + (IsGod ? "on" : "off"));
            }
            else if(plugin.IsCommand(player, command, args, "iclean"))
            {
                // Get the current world
                World world = player.getWorld();
                int TotalRemoved = ICleanWorld(world);
                
                // How many did we remove?
                player.sendMessage(ChatColor.GRAY + "Removed a total of " + TotalRemoved + " items in this world");
            }
            else if(plugin.IsCommand(player, command, args, "mclean"))
            {
                // Get the current world
                World world = player.getWorld();
                int TotalRemoved = 0;
                
                // For all non-player entities, remove
                for(Entity entity : world.getEntities())
                {
                    if(!(entity instanceof Player))
                    {
                        entity.remove();
                        TotalRemoved++;
                    }
                }
                
                // How many did we remove?
                player.sendMessage(ChatColor.GRAY + "Removed a total of " + TotalRemoved + " mobs in this world");
            }
            else if(plugin.IsCommand(player, command, args, "scout"))
            {
                // Warp this player silently and slighty away from any other
                // random player
                if(plugin.getServer().getOnlinePlayers().length <= 1)
                    player.sendMessage(ChatColor.GRAY + "You need more online players to scout");
                else
                {
                    // Get the list of online players exclusing self
                    LinkedList<Player> onlinePlayers = new LinkedList();
                    for(int i = 0; i < plugin.getServer().getOnlinePlayers().length; i++)
                    {
                        if(plugin.getServer().getOnlinePlayers()[i] != player)
                            onlinePlayers.add(plugin.getServer().getOnlinePlayers()[i]);
                    }
                    
                    // Randomly choose a player to warp to
                    Player target = onlinePlayers.get(randomGenerator.nextInt(onlinePlayers.size()));
                    
                    // Warp to that person
                    player.sendMessage(ChatColor.GRAY + "You have been silently warped to near player \"" + target.getDisplayName() + "\"");
                    
                    // Warp to the target player but moved a little further
                    Location targetLocation = target.getLocation();
                    targetLocation = targetLocation.add(5, 0, 5);
                    
                    int y = BasicWorldCommands.GetHighestBlock(targetLocation);
                    targetLocation.setY((double)y);
                    
                    player.teleport(targetLocation);
                }
            }
            else if(plugin.IsCommand(player, command, args, "hide"))
            {
                // Toggles hidden state...
                HidePlayer(player, !plugin.users.IsHidden(player));
            }
            
            // Done - parsed
            return true;
        }
        
        // Unknown sender...
        return true;
    }
    
    // Set a player to a given op group
    // Player may be null if it is the console calling this function
    private void PlayerOp(Player player, String args[])
    {
        // Target group
        int GroupID = 0;

        // Change group ID
        if(args.length == 2)
        {
            try
            {
                GroupID = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                // Failed to parse as int, attempt to get by group name
                GroupID = plugin.users.GetGroupIDByGroup(args[1]);
                
                // Just bug out if not valid
                if(GroupID < 0)
                {
                    if(player != null)
                        player.sendMessage(ChatColor.GRAY + "Unable to op: Invalid group ID or name");
                    return;
                }
            }
        }
        
        // Get the target player's data
        Player targetPlayer = plugin.getServer().getPlayer(args[0]);
        if(targetPlayer == null)
        {
            if(player != null)
                player.sendMessage(ChatColor.GRAY + "Unable to find player named \"" + args[0] + "\"");
            return;
        }
        
        // Attempt to change group now
        else
        {
            // Check permissions (Only works if both players are online)
            if(player != null && targetPlayer != null)
            {
                int sourceGID = plugin.users.GetGroupID(player.getName());
                int targetGID = plugin.users.GetGroupID(targetPlayer.getName());
                if(targetGID >= sourceGID)
                {
                    // Cannot ban GIDs higher or equal
                    player.sendMessage(ChatColor.GRAY + "Cannot op-change users with an equal or higher group ID (theirs: " + targetGID + ", yours: " + sourceGID + ")");
                    return;
                }
            }
            
            // Make actual change
            if(plugin.users.SetUserGroup(targetPlayer.getName(), GroupID))
            {
                String playerName = player == null ? "Server Console" : player.getName();
                String groupName = plugin.users.GetGroupName(targetPlayer.getName());
                plugin.BroadcastMessage(ChatColor.RED + "\"" + playerName + "\" has set player \"" + targetPlayer.getName() + "\" to group \"" + groupName + "\" (GID " + GroupID + ")");
            }
            else if(player != null)
                player.sendMessage(ChatColor.GRAY + "Unable to assign \"" + targetPlayer.getName() + "\" to group ID " + GroupID);
        }
    }
    
    // Ban the given player name; reason included is optional
    // Checks permissions...
    private void PlayerBan(Player player, String args[])
    {
        // Target player and message
        String targetName = "";
        String banReason = "";
        
        // Add the default message
        if(args.length == 1)
        {
            targetName = args[0];
            banReason = "No defined ban reason";
        }
        else
        {
            targetName = args[0];
            for(int i = 1; i < args.length; i++)
            {
                banReason += args[i];
                if(i != args.length - 1)
                    banReason += " ";
            }
        }
        
        // Find the player
        Player banPlayer = plugin.getServer().getPlayer(targetName);
        
        // Check permissions (Only works if both players are online)
        if(player != null && banPlayer != null)
        {
            int sourceGID = plugin.users.GetGroupID(player.getName());
            int targetGID = plugin.users.GetGroupID(banPlayer.getName());
            if(targetGID >= sourceGID)
            {
                // Cannot ban GIDs higher or equal
                player.sendMessage(ChatColor.GRAY + "Cannot ban users with an equal or higher group ID (theirs: " + targetGID + ", yours: " + sourceGID + ")");
                return;
            }
        }
        
        // If found...
        if(banPlayer != null)
        {
            targetName = banPlayer.getName();
            plugin.BroadcastMessage(ChatColor.RED + "User \"" + targetName + "\" banned and kicked by \"" + (player == null ? "Server Console" : player.getName()) + "\"");
            banPlayer.kickPlayer(banReason);
        }
        // Else, player not found...
        else
            plugin.BroadcastMessage(ChatColor.RED + "User \"" + targetName + "\" banned but not kicked (user not online) by \"" + (player == null ? "Server Console" : player.getName()) + "\"");
        
        // Declare the reason
        plugin.BroadcastMessage(ChatColor.RED + "Reason: " + banReason);
        
        // Save the ban...
        plugin.users.SetBan(targetName, banReason);
    }
    
    // Kick a player based on args by the player (which may be null if console)
    private void PlayerKick(Player player, String[] args)
    {
        // Get the target player
        Player target = plugin.getServer().getPlayer(args[0]);
        if(target == null && player != null)
        {
            player.sendMessage(ChatColor.GRAY + "Player \"" + args[0] + "\" is not online");
            return;
        }
        
        // Do we have a declared time?
        int KickTime = -1;
        if(args.length == 2)
        {
            try
            {
                KickTime = Integer.parseInt(args[1]);
                if(KickTime < 0)
                {
                    if(player != null)
                        player.sendMessage(ChatColor.GRAY + "Unable to kick; you cannot assign negative minutes");
                    return;
                }
                else if(KickTime > 24 * 60)
                {
                    if(player != null)
                        player.sendMessage(ChatColor.GRAY + "Unable to kick; you cannot assign greater than 24 hours");
                    return;
                }
            }
            catch(Exception e)
            {
                if(player != null)
                    player.sendMessage(ChatColor.GRAY + "Unable to kick; unable parse time argument");
                return;
            }
        }
        
        // Check permissions (Only works if both players are online)
        if(player != null && target != null)
        {
            int sourceGID = plugin.users.GetGroupID(player.getName());
            int targetGID = plugin.users.GetGroupID(target.getName());
            if(targetGID >= sourceGID)
            {
                // Cannot ban GIDs higher or equal
                player.sendMessage(ChatColor.GRAY + "Cannot kick users with an equal or higher group ID (theirs: " + targetGID + ", yours: " + sourceGID + ")");
                return;
            }
        }
        
        // If player found, kick
        if(target != null)
        {
            // Get kicker's name
            String kickerName = player == null ? "Server Console" : player.getName();
            
            // With time?
            if(KickTime > 0)
            {
                target.kickPlayer("Kicked from the server by \"" + kickerName + "\" for " + KickTime + " minute(s).");
                plugin.users.SetKickTime(target.getName(), KickTime);
                plugin.BroadcastMessage(ChatColor.RED + "\"" + kickerName + "\" has kicked player \"" + target.getName() + "\" for " + KickTime + " minute(s)");
            }
            else
            {
                target.kickPlayer("Kicked from the server by \"" + kickerName + "\"");
                plugin.BroadcastMessage(ChatColor.RED + "\"" + kickerName + "\" has kicked player \"" + target.getName() + "\"");
            }
        }
        // Else if player not found, send message iff its a player
        else if(player != null)
        {
            player.sendMessage(ChatColor.GRAY + "Unable to kick player: player not found");
        }
        
        // All done with kick
    }
    
    // Cleans all items, vehicle, etc.. in a given world (i.e. nether, etc..)
    // Returns the number of items removed
    public static int ICleanWorld(World world)
    {
        int TotalRemoved = 0;
        List<Entity> worldEntities = world.getEntities();
        
        for(Entity entity : worldEntities)
        {
            // If craft item, remove
            // Though this is not a good method, it prevents the
            // need to include a new library (i.e. org.craftbukkit)
            if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftItem"))
            {
                entity.remove();
                TotalRemoved++;
            }
            // Remove vehicles
            else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftBoat"))
            {
                entity.remove();
                TotalRemoved++;
            }
            else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftMinecart"))
            {
                entity.remove();
                TotalRemoved++;
            }
            else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftPoweredMinecart"))
            {
                entity.remove();
                TotalRemoved++;
            }
            else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftStorageMinecart"))
            {
                entity.remove();
                TotalRemoved++;
            }
            else if(entity.getClass().getName().equalsIgnoreCase("org.bukkit.craftbukkit.entity.CraftArrow"))
            {
                entity.remove();
                TotalRemoved++;
            }
        }
        
        return TotalRemoved;
    }
    
    // Hide a given player
    public void HidePlayer(Player target, boolean Hide)
    {
        // Save the new hidden state
        plugin.users.SetHidden(target, Hide);
        
        // Tell them the new state they are in
        if(Hide)
            target.sendMessage(ChatColor.GRAY + "You are now hidden");
        else
            target.sendMessage(ChatColor.GRAY + "You are now visible");
        
        // For each online player, send a "gone" packet
        for(Player other : plugin.getServer().getOnlinePlayers())
        {
            // Ignore self
            if(other == target)
                continue;
            
            // Are we hiding the player?
            if(Hide)
            {
                // Cast to get access to send custom packet
                CraftPlayer targetCraftPlayer = (CraftPlayer)target;
                Packet hideTarget = new Packet29DestroyEntity(targetCraftPlayer.getEntityId());
                targetCraftPlayer.getHandle().netServerHandler.sendPacket(hideTarget);
            }
            else
            {
                // Cast to get access to send custom packet
                CraftPlayer targetCraftPlayer = (CraftPlayer)target;
                Packet unhideTarget = new Packet20NamedEntitySpawn(targetCraftPlayer.getHandle());
                targetCraftPlayer.getHandle().netServerHandler.sendPacket(unhideTarget);
            }
        }
    }
}
