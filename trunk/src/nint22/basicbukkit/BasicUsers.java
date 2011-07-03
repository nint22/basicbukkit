/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicUsers.java
 Desc: Manage all users, and user groups.
 
***************************************************************/

package nint22.basicbukkit;

import java.util.*;
import org.bukkit.util.config.Configuration;

public class BasicUsers
{
    // Internal config handler
    private Configuration users;
    
    // All users and groups, etc.. (In parallel)
    private LinkedList<String> OpNames;         // User name
    private LinkedList<Integer> OpGroup;        // User's group ID
    
    // All groups (in parallel
    private LinkedList<Integer> GroupID;        // Group's unique ID
    private LinkedList<String[]> GroupCommands; // Valid commands in group (index based on groups)
    private LinkedList<String> GroupPreTitle;   // Pre-title for the given group
    private LinkedList<Boolean> GroupBannedItems;   // True if this group can access banned items
    
    // Initialize users
    public BasicUsers(Configuration users, Configuration config)
    {
        // Load the users file (just in case it hasn't yet)
        this.users = users;
        this.users.load();
        
        // Allocate users and info
        OpNames = new LinkedList();
        OpGroup = new LinkedList();
        
        // Allocate all lists as needed
        GroupID = new LinkedList();
        GroupCommands = new LinkedList();
        GroupPreTitle = new LinkedList();
        GroupBannedItems = new LinkedList();
        
        // Parse config file that has all group info
        List<Object> ConfigGroups = config.getList("groups");
        
        // Save each group
        // Format; {id=2, pre=[Owner], commands=[help, item, ban, kick], banned_access=true}
        for(Object group : ConfigGroups)
        {
            // Get info
            LinkedHashMap GroupData = (LinkedHashMap)group;
            
            Integer ID = (Integer)GroupData.get("id");
            ArrayList<String> Commands = (ArrayList<String>)GroupData.get("commands");
            String PreTitle = (String)GroupData.get("pre");
            Boolean BannedItems = (Boolean)GroupData.get("banned_access");
            
            // Convert to string data (commands)
            String[] TempCommands = new String[Commands.size()];
            for(int i = 0; i < TempCommands.length; i++)
                TempCommands[i] = (String)Commands.get(i); 
            
            // Set info
            GroupID.add(ID);
            GroupCommands.add(TempCommands);
            GroupPreTitle.add(PreTitle);
            GroupBannedItems.add(BannedItems);
        }
        
        // How many did we load?
        System.out.println("### BasicBukkit loaded " + GroupID.size() + " groups");
    }
    
    // Write out if needed
    public void save()
    {
        // For all users
        for(int i = 0; i < OpNames.size(); i++)
        {
            // Update user
            HashMap<String,Integer> map = new HashMap();
            map.put("group", OpGroup.get(i));
            users.setProperty(OpNames.get(i), map);
        }
        
        // Save file
        users.save();
    }
    
    // Add a user to the target group
    // Returns false on failure, true on success
    public boolean SetUser(String UserName, int UserGroupID)
    {
        // Does the group ID exist?
        if(OpGroup.contains(new Integer(UserGroupID)))
        {
            // Group exists, add user to group ID
            // Does the user already exist? If so, just change at the same index
            if(OpNames.contains(UserName))
            {
                // Just change that index in OpGroup
                int UserIndex = OpNames.indexOf(UserName);
                OpGroup.set(UserIndex, UserGroupID);
            }
            // New user
            else
            {
                OpNames.add(UserName);
                OpGroup.add(UserGroupID);
            }
            
            // All done
            return true;
        }
        // Else, error
        else
            return false;
    }
    
    // Returns the player's title
    public String GetUserTitle(String UserName)
    {
        // Does this player exist yet?
        if(OpNames.contains(UserName))
        {
            // Does exist, just return the title
            return GroupPreTitle.get( OpNames.indexOf(UserName) );
        }
        // Else, just create the user and try again
        else
        {
            // Add user
            OpNames.add(UserName);
            OpGroup.add(new Integer(0));
            
            // Now return the default title
            return GroupPreTitle.get(0);
        }
    }
    
    // Return the user's 
    public int GetGroupID(String name)
    {
        // Does this user exist?
        int UserIndex = OpNames.indexOf(name);
        if(UserIndex < 0)
            return -1;
        
        // Get the group ID from the parallel index
        return OpGroup.get(UserIndex);
    }
}
