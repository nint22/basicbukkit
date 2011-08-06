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
import java.util.HashMap;
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
    private BufferedWriter Log = null;
    
    /*** Voting Systems ***/
    
    // Are we currently voting?
    private boolean isVoting;
    
    // True if we are kicking, false for banning; works in "isVoting"
    private boolean isKicking;
    
    // Minimum number of votes
    private int votingMinimum;
    
    // Number of seonds during a countdown
    private int votingCountdown;
    
    // Number of seconds to vote-kick
    private int votingKickTime;
    
    // Ban reason
    private String votingBanReason;
    
    // Ballots
    private HashMap<String, Boolean> ballots;
    
    // Who we are targeting
    private Player voteTarget;
    
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
                String timeStamp = new Date().toString();
                Log.write(timeStamp + ",\t" + 0 + ",\t \n");
                Log.flush();
            }
            catch(Exception e)
            {
                System.out.println("### BasicBukkit unable to append+write to log file: " + e.getMessage());
                System.exit(-1);
            }
        }
        
        // Default voting to false
        isVoting = false;
        votingMinimum = plugin.configuration.getInt("minvotes", 1);
        votingCountdown = 0;
        ballots = new HashMap();
        
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
                String timeStamp = new Date().toString();
                Log.write(timeStamp + ",\t" + 0 + ",\t \n");
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
                
                // Save all users and protection data
                plugin.users.save();
                plugin.protections.save();
                plugin.warps.save();
                plugin.locks.save();
                plugin.roleplay.save();
                
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
            if(TargetPermaTime > 0 && TotalSeconds % 5 == 0)
            {
                for(World world : plugin.getServer().getWorlds())
                    world.setTime(TargetPermaTime);
            }
            
            // Every minute, log the server's status
            if(LogTime > 0 && TotalSeconds % (LogTime * 60) == 0)
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
            
            // Every 5 seconds, update all clients with the latest hidden list
            if(TotalSeconds % 5 == 0)
            {
                for(Player player : plugin.getServer().getOnlinePlayers())
                    plugin.users.SetHidden(player, plugin.users.IsHidden(player));
            }
            
            // Update vote
            UpdateVote(TotalSeconds);
            
            // Main thread loop
        }
    }
    
    public boolean IsVoting()
    {
        return isVoting;
    }
    
    public void SetVote(Player player, String string)
    {
        // Parse to boolean
        Boolean newVote = false;
        if(string.toLowerCase().startsWith("y") || string.toLowerCase().startsWith("t"))
            newVote = true;
        else if(string.toLowerCase().startsWith("n") || string.toLowerCase().startsWith("f"))
            newVote = false;
        else
        {
            player.sendMessage(ChatColor.GRAY + "Unable to recognize vote command; either /vote yes or /vote no");
            return;
        }
        
        // Has the player already voted?
        Boolean previousVote = ballots.get(player.getName());
        if(previousVote == null)
        {
            ballots.put(player.getName(), newVote);
            player.sendMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "You have voted \"" + (newVote.booleanValue() ? "yes" : "no") + "\"");
        }
        else if(previousVote.booleanValue() != newVote.booleanValue())
        {
            ballots.put(player.getName(), newVote);
            player.sendMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "You have changed vote from \"" + (previousVote.booleanValue() ? "yes" : "no") + "\" to \"" + (newVote.booleanValue() ? "yes" : "no") + "\"");
        }
        else
            player.sendMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "Your previous vote already was \"" + (previousVote.booleanValue() ? "yes" : "no") + "\"");
    }
    
    public void StartVKick(Player player, Player target, int KickTime)
    {
        // Reset voting boolean and ballots
        isVoting = true;
        ballots.clear();
        votingCountdown = 60;
        votingKickTime = KickTime;
        isKicking = true;
        voteTarget = target;
        
        // Declare start
        plugin.BroadcastMessage(ChatColor.DARK_RED + "VoteKick: " + ChatColor.WHITE + "\"" + player.getName() + "\" wants to vote-kick \"" + voteTarget.getName() + "\" for " + KickTime + " minutes; please vote using /vote");
    }
    
    public void StartVBan(Player player, Player target, String banReason)
    {
        // Reset voting boolean and ballots
        isVoting = true;
        ballots.clear();
        votingCountdown = 60;
        votingBanReason = banReason;
        isKicking = false;
        voteTarget = target;
        
        // Declare start
        plugin.BroadcastMessage(ChatColor.DARK_RED + ">> VoteBan: " + ChatColor.WHITE + "\"" + player.getName() + ChatColor.WHITE + "\" wants to vote-ban \"" + voteTarget.getName() + "\" for reason \"" + votingBanReason + "\" please vote using /vote");
    }
    
    private void UpdateVote(int TotalSeconds)
    {
        // Update the player system
        if(!isVoting)
            return;
        
        // Decrease seconds
        votingCountdown--;
        
        // Warning at 45 seconds
        if(votingCountdown == 45)
            plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "45 seconds left to /vote ends...");

        // Warning at 30 seconds
        else if(votingCountdown == 30)
            plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "30 seconds left to /vote ends...");

        // Warning at 15 seconds
        else if(votingCountdown == 15)
            plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "15 seconds left to /vote ends...");

        // Warning at 10 seconds
        else if(votingCountdown == 10)
            plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "10 seconds left to /vote ends...");

        // Warning at 5 seconds
        else if(votingCountdown == 5)
            plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote: " + ChatColor.WHITE + "5 seconds left to /vote ends...");

        // Are we done voting?
        if(votingCountdown <= 0)
        {
            // No more voting
            isVoting = false;

            // Count the yes/no votes
            int yesVote = 0;
            int noVote = 0;

            for(Boolean vote : ballots.values())
            {
                if(vote.booleanValue())
                    yesVote++;
                else
                    noVote++;
            }
            
            // Did it fail?
            if(yesVote < votingMinimum)
                plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote failed: " + ChatColor.WHITE + "The minimum number of votes (" + votingMinimum + ") was not reached");
            // Did the majority say no?
            else if(yesVote < noVote)
                plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote failed: " + ChatColor.WHITE + "Not enough \"yes\" votes: " + yesVote + " yes vs. " + noVote + " no");
            // Did we get a stalemate?
            else if(yesVote == noVote)
                plugin.BroadcastMessage(ChatColor.DARK_RED + ">> Vote failed: " + ChatColor.WHITE + "Stalemate: " + yesVote + " yes vs. " + noVote + " no");
            // Else, it did succeed
            else
            {
                // Are we kicking the user?
                if(isKicking == true)
                {
                    plugin.BroadcastMessage(ChatColor.DARK_GREEN + ">> Vote passed! \"" + ChatColor.WHITE + voteTarget.getName() + "\" was vote-kicked by a majority: " + yesVote + " yes vs. " + noVote + " no");
                    plugin.users.SetKickTime(voteTarget.getName(), votingKickTime);
                    voteTarget.kickPlayer("Kicked from the server by vote for " + votingKickTime + " minutes");
                }
                
                // Just banning
                else
                {
                    plugin.BroadcastMessage(ChatColor.DARK_GREEN + ">> Vote passed! \"" + ChatColor.WHITE + voteTarget.getName() + "\" was vote-banned by a majority: " + yesVote + " yes vs. " + noVote + " no");
                    plugin.users.SetBan(voteTarget.getName(), votingBanReason);
                    voteTarget.kickPlayer(votingBanReason);
                }
            }
        }
    }
    
}
