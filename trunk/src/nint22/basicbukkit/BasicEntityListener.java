/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicEntityListener.java
 Desc: The basic entity listener listens exclusively for explo-
 sions, preventing them all from happening. Very simple class.
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class BasicEntityListener extends EntityListener
{
    // Current working plugin
    private final BasicBukkit plugin;
    
    // Allows TNT to explore
    boolean AllowTNT;
    
    // Constructor saves plugin handle
    public BasicEntityListener(BasicBukkit instance)
    {
        // Save plugin
        plugin = instance;
        
        // Do we allow TNT on this server?
        AllowTNT = plugin.configuration.getBoolean("allowTNT", false);
    }
    
    // Disable priming if needed
    @Override
    public void onExplosionPrime(ExplosionPrimeEvent event)
    {
        // Only supress TNT
        if(!(event.getEntity() instanceof Creeper))
            event.setCancelled(!AllowTNT);
    }
    
    // Disable explosion if needed
    @Override
    public void onEntityExplode(EntityExplodeEvent event)
    {
        // Only supress TNT
        if(!(event.getEntity() instanceof Creeper))
            event.setCancelled(!AllowTNT);
    }
    
    // If a player gets damage, only apply it if god mode is off
    @Override
    public void onEntityDamage(EntityDamageEvent event)
    {
        // If not player, ignore
        if(!(event.getEntity() instanceof Player))
            return;
        
        // Get player recieving damage
        Player player = (Player)event.getEntity();
        
        // Apply god mode first
        if(plugin.users.IsGod(player.getName()))
            event.setCancelled(true);
        
        // Ignore damage if in a protected zone AND is flagged non-pvp AND is another entity cause it
        String protectionName = plugin.protections.GetProtectionName(player.getLocation());
        if((protectionName != null && plugin.protections.GetPVP(protectionName)) && (event.getCause() == DamageCause.ENTITY_ATTACK))
            event.setCancelled(false);
    }
    
    // When the player dies, make a global message
    @Override
    public void onEntityDeath(EntityDeathEvent event)
    {
        // Only do player events
        if(event.getEntity() instanceof Player)
        {
            // Who died?
            Player player = (Player)event.getEntity();
            plugin.BroadcastMessage(ChatColor.GRAY + "Player \"" + player.getDisplayName() + ChatColor.GRAY + "\" died...");
        }
    }
}
