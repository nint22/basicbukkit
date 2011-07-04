/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicProtection.java
 Desc: World protection class - loads and saves areas (2d columns)
 into the protections.yml file and prohibits non-owners and sub-ranks
 from modifying the area...
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

// Helper pair-value (tuple) class
class Pair
{
    public int x;
    public int y;
    public Pair(int newx, int newy)
    {
        x = newx;
        y = newy;
    }
}

public class BasicProtection
{
    // Protections configuration file handle
    private Configuration protections;
    
    // List of all protections (arrays run in parallel)
    private LinkedList<String> names;
    private LinkedList<LinkedList<String>> owners = null;   // List of owners for each protection
    private LinkedList<Pair> corner1 = null;
    private LinkedList<Pair> corner2 = null;
    
    // Initialize protections
    public BasicProtection(Configuration protections)
    {
        // Load the users file (just in case it hasn't yet)
        this.protections = protections;
        this.protections.load();
        
        // Allocate as needed
        names = new LinkedList();
        owners = new LinkedList();
        corner1 = new LinkedList();
        corner2 = new LinkedList();
        
        Map<String, ConfigurationNode> protectionData = protections.getNodes("protections");
        
        if(protectionData != null)
        {
            for(ConfigurationNode obj : protectionData.values())
            {
                // Is a hash map of string data...
                Map<String, Object> map = obj.getAll();
                
                // Get owner and pair info
                String Name = (String)map.get("name");
                String Owner = (String)map.get("owners");
                String Geometry = (String)map.get("geometry");
                
                // Add name
                names.add(Name); 
                
                // Parse all owners
                String[] splitString = Owner.split(",");
                LinkedList<String> allUsers = new LinkedList();
                for(int i = 0; i < splitString.length; i++)
                    allUsers.add(splitString[i].trim());
                owners.add(allUsers);
                
                // Parse all geometry
                splitString = Geometry.split(",");
                if(splitString.length != 4)
                {
                    System.out.println("### BasciBukkit unable to parse the geometry string: \"" + Geometry + "\"");
                    System.exit(0);
                }
                try
                {
                    // Try getting all four integers
                    int x1 = Integer.parseInt(splitString[0].trim());
                    int y1 = Integer.parseInt(splitString[1].trim());
                    int x2 = Integer.parseInt(splitString[2].trim());
                    int y2 = Integer.parseInt(splitString[3].trim());
                    
                    // Add to list
                    corner1.add(new Pair(x1, y1));
                    corner2.add(new Pair(x2, y2));
                }
                catch(Exception e)
                {
                    // Force crash
                    System.out.println("### BasicBukkit unable to parse the geometry string: " + e.toString());
                }
            }
        }
        
        // How many did we load?
        System.out.println("### BasicBukkit loaded " + owners.size() + " protected areas");
    }
    
    // Write out if needed
    public void save()
    {
        // For each protected piece of land..
        HashMap<String, HashMap> allProtections = new HashMap();
        for(int i = 0; i < names.size(); i++)
        {
            // Create a hash map to push out as a property
            HashMap<String, String> map = new HashMap();
            
            // Put name
            map.put("name", names.get(i));
            
            // Put owners (generate comma-delimited list)
            String allOwners = "";
            for(int j = 0; j < owners.get(i).size(); j++)
            {
                // Comma-seperate unless at end
                allOwners += owners.get(i).get(j);
                if(j != owners.get(i).size() - 1)
                    allOwners += ", ";
            }
            map.put("owners", allOwners);
            
            // Save geometry
            map.put("geometry", corner1.get(i).x + ", " + corner1.get(i).y + ", " + corner2.get(i).x + ", " + corner2.get(i).y );
            
            // Save this as all warps
            allProtections.put("protection_" + i, map);
        }
        
        // Save all warps
        protections.setProperty("protections", allProtections);
        protections.save();
    }
    
    // Get the number of protections
    public int GetProtectionCount()
    {
        return owners.size();
    }
    
    // Get the owner of the said protection
    public LinkedList<String> GetProtectionOwners(String name)
    {
        // Get the index name
        int index = names.indexOf(name);
        if(index < 0)
            return null;
        else
            return owners.get(index);
    }
    
    // Get the name of the area based on a position
    public String GetProtectionName(Pair location)
    {
        // For all protections..
        for(int i = 0; i < names.size(); i++)
        {
            if(location.x >= corner1.get(i).x && location.y >= corner1.get(i).y &&
               location.x <= corner2.get(i).x && location.y <= corner2.get(i).y)
            {
                // Found, return index
                return names.get(i);
            }
        }
        
        // Else, not found, return  null
        return null;
    }
    
    // Get the first point of the given protection index
    public Pair GetProtectionP1(int index)
    {
        return corner1.get(index);
    }
    
    // Get the second point of the given protection index
    public Pair GetProtectionP2(int index)
    {
        return corner2.get(index);
    }
    
    // Add a new area
    // Returns true on success, false if there is an intersection
    public boolean AddProtection(String owner, String name, Pair p1, Pair p2)
    {
        // Make sure the the first point is always the smallest one
        if(p1.x > p2.x)
        {
            int temp = p1.x;
            p1.x = p2.x;
            p2.x = temp;
        }
        if(p1.y > p2.y)
        {
            int temp = p1.y;
            p1.y = p2.y;
            p2.y = temp;
        }
        
        // Max distance can only be 64 in both directions
        if(p2.x - p1.x > 64)
            return false;
        if(p2.y - p1.y > 64)
            return false;
        
        // As of now, we do NOT do collision checks
        // if(collision)
        //     return false;
        
        // Add to list of points
        names.add(name);
        LinkedList<String> newOwners = new LinkedList();
        newOwners.add(owner);
        owners.add(newOwners);
        corner1.add(p1);
        corner2.add(p2);
        
        // All done
        return true;
    }
    
    // Remove this entire protection
    public void RemoveProtection(String name)
    {
        int index = names.indexOf(name);
        if(index >= 0)
        {
            names.remove(index);
            owners.remove(index);
            corner1.remove(index);
            corner2.remove(index);
        }
    }
}
