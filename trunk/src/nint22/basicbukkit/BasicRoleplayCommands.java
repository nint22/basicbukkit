/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicRoleplayCommands.java
 Desc: Implements all roleplay commands ranging from land control
 to money and bank management.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class BasicRoleplayCommands implements CommandExecutor
{
    // Plugin handle
    private final BasicBukkit plugin;
    
    // Default constructor
    public BasicRoleplayCommands(BasicBukkit plugin)
    {
        // Save plugin handler
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
        if(plugin.IsCommand(player, command, args, "buy"))
        {
            // Needs at least 1 arg
            if(args.length < 1)
                return false;
            
            // Are we close eno-ugh to any sales signs?
            if(!plugin.roleplay.CanTrade(player))
            {
                player.sendMessage(ChatColor.GRAY + "You cannot trade outside of your own kingdom and/or without being near a [store]");
                return true;
            }
            
            // Grab the item ID and ammount if possible
            int ItemID = -1;
            int Ammount = 1;
            
            try
            {
                ItemID = Integer.parseInt(args[0]);
                if(args.length > 1)
                    Ammount = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to parse Item ID and/or ammount");
                return true;
            }
            
            // Attempt the transaction
            plugin.economy.BuyItem(player, ItemID, Ammount);
        }
        else if(plugin.IsCommand(player, command, args, "sell"))
        {
            // Needs at least 1 arg
            if(args.length < 1)
                return false;
            
            // Are we close eno-ugh to any sales signs?
            if(!plugin.roleplay.CanTrade(player))
            {
                player.sendMessage(ChatColor.GRAY + "You cannot trade outside of your own kingdom and/or without being near a [store]");
                return true;
            }
            
            // Grab the item ID and ammount if possible
            int ItemID = -1;
            int Ammount = 1;
            
            try
            {
                ItemID = Integer.parseInt(args[0]);
                if(args.length > 1)
                    Ammount = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to parse Item ID and/or ammount");
                return true;
            }
            
            // Attempt the transaction
            plugin.economy.SellItem(player, ItemID, Ammount);
        }
        else if(plugin.IsCommand(player, command, args, "price"))
        {
            // Needs at least 1 arg
            if(args.length < 1)
                return false;
            
            // Grab the item ID and ammount if possible
            int ItemID = -1;
            int Ammount = 1;
            
            try
            {
                ItemID = Integer.parseInt(args[0]);
                if(args.length > 1)
                    Ammount = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                player.sendMessage(ChatColor.GRAY + "Unable to parse Item ID and/or ammount");
                return true;
            }
            
            // What is the price of the given Item ID?
            int Total = plugin.economy.GetPrice(ItemID) * Ammount;
            if(Total > 0)
                player.sendMessage(ChatColor.GRAY + "Total price of " + Ammount + " x item\"" + ItemID + "\" is: " + ChatColor.RED + Total + "Z");
            else
                player.sendMessage(ChatColor.GRAY + "Item \"" + ItemID + "\"is unknown");
        }
        else if(plugin.IsCommand(player, command, args, "money"))
        {
            // Tell the player what money they have
            player.sendMessage(ChatColor.GRAY + "Current money: " + ChatColor.RED + plugin.economy.GetMoney(player) + "Z");
        }
        else if(plugin.IsCommand(player, command, args, "level"))
        {
            // Get the player's experiance
            int Experiance = plugin.roleplay.GetExperiance(player);
            player.sendMessage(ChatColor.GRAY + "Your ammount of experiance points is: " + ChatColor.RED + String.format("%,d", Experiance));
        }
        else if(plugin.IsCommand(player, command, args, "kjoin"))
        {
            // Leave a given kingdom
            if(args.length < 1)
                return false;
            
            // Get the kingdom name and player name
            String Kingdom = args[0];
            if(!plugin.roleplay.IsKingdom(Kingdom))
            {
                player.sendMessage(ChatColor.GRAY + "The given kingdom does not currently exist");
                return true;
            }
            
            // Remove from kingdom
            plugin.users.SetTitle(player, Kingdom);
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + player.getName() + "\" has joined the kingdom \"" + Kingdom + ChatColor.GRAY + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "kleave"))
        {
            // Leave a given kingdom
            if(args.length > 0)
                return false;
            
            // Get the kingdom name and player name
            String Kingdom = plugin.users.GetSpecialTitle(player);
            
            // Remove from kingdom
            plugin.users.SetTitle(player, "");
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + player.getName() + "\" has left the kingdom \"" + Kingdom + ChatColor.GRAY + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "kkick"))
        {
            // Remove someone from a kingdom
            if(args.length < 2)
                return false;
            
            // Get the kingdom name and player name
            String Kingdom = args[0];
            if(!plugin.roleplay.IsKingdom(Kingdom))
            {
                player.sendMessage(ChatColor.GRAY + "The given kingdom does not currently exist");
                return true;
            }
            
            Player target = plugin.getServer().getPlayer(args[1]);
            if(target == null)
            {
                player.sendMessage(ChatColor.GRAY + "The given player does not currently exist");
                return true;
            }
            
            // Remove from kingdom
            plugin.users.SetTitle(target, "");
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + target.getName() + "\" was kicked from the kingdom \"" + Kingdom + ChatColor.GRAY + "\" by \"" + player.getName() + "\"");
        }
        else if(plugin.IsCommand(player, command, args, "kingdoms"))
        {
            // Form kingdoms list
            String allKingdoms = "";
            String[] Kingdoms = plugin.roleplay.GetKingdoms();
            for(int i = 0; i < Kingdoms.length; i++)
            {
                allKingdoms += Kingdoms[i];
                if(i != plugin.getServer().getOnlinePlayers().length - 1)
                    allKingdoms += ChatColor.GRAY + ", " + ChatColor.WHITE;
            }
            
            // Replace colors..
            allKingdoms = plugin.ColorString(allKingdoms);
            
            // Print all
            sender.sendMessage(ChatColor.GRAY + "All Kingdoms: (" + Kingdoms.length + ")");
            sender.sendMessage(allKingdoms);
        }
        
        // Done - parsed
        return true;
    }
}
