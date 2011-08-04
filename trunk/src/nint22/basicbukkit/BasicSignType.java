/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicSignType.java
 Desc: Helper class with signs.
 
***************************************************************/

package nint22.basicbukkit;

// Helper class with signs

import org.bukkit.Location;

public class BasicSignType
{
    // Create a sign type
    BasicSignType(String SignType, String SignArg, Location SignLoc)
    {
        this.SignType = SignType;
        this.SignArg = SignArg;
        this.SignLoc = SignLoc;
    }
    
    // Sign type
    public String SignType;
    
    // Name / argument (i.e. [City]/nDerp)
    public String SignArg;
    
    // Location of sign
    Location SignLoc;
}