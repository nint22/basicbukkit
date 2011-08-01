/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicVehicleListener.java
 Desc: A vehicle placement /destruction handler; does the same
 security check as does the item and entity listeners work.
 
***************************************************************/

package nint22.basicbukkit;

import org.bukkit.event.vehicle.*;

public class BasicVehicleListener extends VehicleListener
{
    // Current working plugin
    private final BasicBukkit plugin;

    // Constructor saves given plugin
    public BasicVehicleListener(BasicBukkit instance)
    {
        plugin = instance;
    }
    
    // Catch all vehicle creation...
    @Override
    public void onVehicleCreate(VehicleCreateEvent event)
    {
        // Who created the vehicle?
        //event.getVehicle().
    }
}
