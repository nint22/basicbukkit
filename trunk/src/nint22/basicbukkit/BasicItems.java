/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicItems.java
 Desc: Loads the items list and contains a dictionary of all
 item names, item IDs and meta information.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import java.io.*;
import org.bukkit.util.config.Configuration;

public class BasicItems
{
    // Dictionary of [item name] [item ID, meta (defaults to 0)]
    private HashMap<String, String> ItemNames;
    
    // Banned items dictionary
    private LinkedList<String> BannedItems;
    
    // Standard constructor
    public BasicItems(File itemsFile, Configuration configuration)
    {
        // Allocate items and banned list
        ItemNames = new HashMap();
        BannedItems = new LinkedList();
        
        // Load file and parse items names
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(itemsFile));
            while(reader.ready())
            {
                // Get line, format: cobble,4,0
                String line = reader.readLine();
                String data[] = line.split(",");
                
                // Get key-value pairs
                String key = data[0].trim().toLowerCase();
                String dataID = data[1].trim();
                String meta = data[2].trim();
                
                // Put into dictionary
                ItemNames.put(key, dataID + ":" + meta);
            }
            reader.close();
        }
        catch(Exception e)
        {
            System.out.println("### BasicBukkit is unable to read the items.csv file: " + e.getMessage());
        }
        
        // Load the banned items list
        String banned = configuration.getString("banned");
        if(banned != null)
        {
            // Split
            String split[] = banned.split(",");
            for(String obj : split)
            {
                // Get the banned ID and make sure it is of form ID:Meta
                String bannedID = (String)obj;

                // If there is no colon, add ":0" at the end for consistency
                if(bannedID.indexOf(":") < 0)
                    bannedID += ":0";

                // Save in banned items
                BannedItems.add(bannedID);
            }
        }
        
        // Tell how much we loaded
        System.out.print("### BasicBukkit loaded " + ItemNames.size() + " item names and " + BannedItems.size() + " banned items.");
    }
    
    // Returns the itemID:meta string if the item is found by name
    // Returns null if not found
    public String FindItem(String itemName)
    {
        // To lower the string
        itemName = itemName.toLowerCase();
        
        // Is it in the dictionary?
        if(ItemNames.containsKey(itemName) == true)
            return ItemNames.get(itemName);
        else
            return null;
    }
    
    // Returns the item name based on the item ID (and meta id if given)
    // May return null if not found
    public String FindItem(int ItemID)
    {
        return FindItem(ItemID, 0);
    }
    
    // Find an item name based on the item ID and meta ID
    // May return null if not found
    public String FindItem(int ItemID, int MetaID)
    {
        // Form the ID:meta String
        String target = ItemID + ":" + MetaID;
        Iterator it = ItemNames.entrySet().iterator();
        
        // Do a linear search (not the best method..)
        while(it.hasNext())
        {
            // Get object
            Map.Entry pairs = (Map.Entry)it.next();
            if(pairs.getValue().equals(target))
            {
                return (String)pairs.getKey();
            }
        }
        
        // Else, never found
        return null;
    }
    
    // Returns true if the given item ID is banned
    public boolean IsBanned(int ItemID)
    {
        // Pass to parent function
        return IsBanned(ItemID, 0);
    }
    
    // Returns true if the given item ID and meta are banned
    public boolean IsBanned(int ItemID, int Meta)
    {
        if(BannedItems.contains(ItemID + ":" + Meta))
            return true;
        else
            return false;
    }
}
