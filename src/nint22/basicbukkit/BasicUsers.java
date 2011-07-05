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
import java.util.Map.Entry;
import org.bukkit.util.config.Configuration;

public class BasicUsers
{
    // Internal config handler
    private Configuration users;
    
    // All users and groups, etc.. (In parallel)
    private LinkedList<String> OpNames;         // User name
    private LinkedList<Integer> OpGroup;        // User's group ID
    
    // All groups (in parallel)
    private LinkedList<Integer> GroupID;            // Group's unique ID
    private LinkedList<String> GroupName;           // Group's unique ID
    private LinkedList<String[]> GroupCommands;     // Valid commands in group (index based on groups)
    private LinkedList<String> GroupPreTitle;       // Pre-title for the given group
    private LinkedList<Boolean> GroupBannedItems;   // True if this group can access banned items
    
    // All kicked users time (unix time)
    // The unix epoch time when a user can join back
    private HashMap<String, Integer> KickedTimes;
    
    // All baned users
    private HashMap<String, String> BannedUsers;
    
    // God mode (if true, player recieves no damage); not saved to file!
    private HashMap<String, Boolean> GodMode;
    
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
        GroupName = new LinkedList();
        GroupCommands = new LinkedList();
        GroupPreTitle = new LinkedList();
        GroupBannedItems = new LinkedList();
        
        // Create new hash map
        KickedTimes = new HashMap();
        BannedUsers = new HashMap();
        GodMode = new HashMap();        // Note that this one is not saved
        
        // Parse config file that has all group info
        List<Object> ConfigGroups = config.getList("groups");
        
        // Save each group
        // Format; {id=2, pre=[Owner], commands=[help, item, ban, kick], banned_access=true}
        for(Object group : ConfigGroups)
        {
            // Get info
            LinkedHashMap GroupData = (LinkedHashMap)group;
            
            Integer ID = (Integer)GroupData.get("id");
            String Name = (String)GroupData.get("name");
            ArrayList<String> Commands = (ArrayList<String>)GroupData.get("commands");
            String PreTitle = (String)GroupData.get("pre");
            Boolean BannedItems = (Boolean)GroupData.get("banned_access");
            
            // Convert to string data (commands)
            String[] TempCommands = new String[Commands.size()];
            for(int i = 0; i < TempCommands.length; i++)
                TempCommands[i] = (String)Commands.get(i); 
            
            // Set info
            GroupID.add(ID);
            GroupName.add(Name);
            GroupCommands.add(TempCommands);
            GroupPreTitle.add(PreTitle);
            GroupBannedItems.add(BannedItems);
        }
        
        // Load all users
        Map<String, Object> UserData = users.getAll();
        Set<Entry<String, Object>> pairs = UserData.entrySet();
        for(Entry<String, Object> pair : pairs)
        {
            // If the string ends in ".group" this is the group ID key
            if(pair.getKey().endsWith(".group"))
            {
                OpNames.add(pair.getKey().substring(0, pair.getKey().length() - 6));
                OpGroup.add((Integer)pair.getValue());
            }
            
            // If the string ends ".kicktime" this is the 
            else if(pair.getKey().endsWith(".kicktime"))
            {
                // Save the time in which the player can get back via a kick
                KickedTimes.put(pair.getKey().substring(0, pair.getKey().length() - 9), (Integer)pair.getValue());
            }
            
            // If banned, read string
            else if(pair.getKey().endsWith(".banned"))
            {
                // Save as banned string
                BannedUsers.put(pair.getKey().substring(0, pair.getKey().length() - 7), (String)pair.getValue());
                
            }
        }
        
        // How many did we load?
        System.out.println("### BasicBukkit loaded " + GroupID.size() + " groups, " + OpNames.size() + " users (" + KickedTimes.size() + " kicked, " + BannedUsers.size() + " banned)");
    }
    
    // Write out if needed
    public void save()
    {
        // Get the freshest users file
        users.load();
        
        // For all users
        for(int i = 0; i < OpNames.size(); i++)
        {
            // Update user
            HashMap<String, Object> map = new HashMap();
            map.put("group", OpGroup.get(i));
            
            // Does this user have kick-time remaining?
            Integer kickTime = KickedTimes.get(OpNames.get(i));
            if(kickTime != null)
                map.put("kicktime", kickTime);
            
            // Was this user banned at some point?
            String banReason = BannedUsers.get(OpNames.get(i));
            if(banReason != null)
                map.put("banned", banReason);
            
            // Save this user to the map
            users.setProperty(OpNames.get(i), map);
        }
        
        // Save file
        users.save();
    }
    
    // Add a user to the target group
    // Returns false on failure, true on success
    public boolean SetUserGroup(String UserName, int UserGroupID)
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
        // Get the user's group ID
        int GroupIndex = GetGroupID(UserName);
        if(GroupIndex < 0)
            return "[Undefined Group]";
        
        return GroupPreTitle.get(GroupIndex);
    }
    
    // Return the user's group's ID index
    public int GetGroupID(String name)
    {
        // Does this user exist?
        int UserIndex = OpNames.indexOf(name);
        if(UserIndex < 0)
            return -1;
        
        // Get the group ID from the parallel index
        return OpGroup.get(UserIndex);
    }
    
    // Return the user's group's name
    public String GetGroupName(String name)
    {
        // Does this user exist?
        int GroupIndex = GetGroupID(name);
        if(GroupIndex < 0)
            return "Undefined Group";
        
        // Get the group ID from the parallel index
        return GroupName.get(GroupIndex);
    }
    
    // Does the given user have the ability (permission) to execute this command?
    // True if they can, else returns false
    public boolean CanExecute(String name, String command)
    {
        // Find the user's group (and fail out if does not exist)
        int GroupIndex = GetGroupID(name);
        if(GroupIndex < 0)
            return false;
        
        // Get the group's permissions
        String[] Available = GroupCommands.get(GroupIndex);
        for(int i = 0; i < Available.length; i++)
        {
            // Match found
            if(Available[i].compareToIgnoreCase(command) == 0)
                return true;
        }
        
        // Else, never found, just say no
        return false;
    }
    
    // Can the given user place / use banned blocks / items?
    public boolean CanUseBannedItems(String name)
    {
        // Find the user's group (and fail out if does not exist)
        int GroupIndex = GetGroupID(name);
        if(GroupIndex < 0)
            return false;
        
        // Get the group's permissions
        return GroupBannedItems.get(GroupIndex).booleanValue();
    }

    // Get a list of commands this user can use (based on his or her group)
    public String[] GetGroupCommands(String name)
    {
        // Find the user's group (and fail out if does not exist)
        int GroupIndex = GetGroupID(name);
        if(GroupIndex < 0)
            return null;
        
        // Get the group's permissions
        return GroupCommands.get(GroupIndex);
    }
    
    // Can a user join? i.e. is he or she not in the kicked list?
    public boolean UserCanJoin(String userName)
    {
        // Is it in the map?
        Integer time = KickedTimes.get(userName);
        
        // Not in list? Return true
        if(time == null)
            return true;
        
        // If the time is earlier than current time, then remove and join
        else if(time.intValue() <= (int)(System.currentTimeMillis()/1000))
        {
            // Remove from list
            KickedTimes.remove(userName);
            return true;
        }
        
        // Else, cannot yet join
        else
            return false;
    }
    
    // Insert new kick for length
    public void UserSetKickTime(String userName, int minutes)
    {
        // Create the time when the user can join back
        int KickEndTime = (int)(System.currentTimeMillis()/1000) + minutes * 60;
        
        // Insert into hash map
        KickedTimes.put(userName, new Integer(KickEndTime));
    }
    
    // Ban user
    public void UserBan(String userName, String reason)
    {
        BannedUsers.put(userName, reason);
    }
    
    // Pardon user
    public void UserUnban(String userName)
    {
        BannedUsers.remove(userName);
    }
    
    // Return a string if banned
    public String UserIsBanned(String userName)
    {
        return BannedUsers.get(userName);
    }
    
    // Gets god mode; initial (default) value is true
    boolean IsGod(String name)
    {
        // Does the user exist? If not, default to true
        Boolean state = GodMode.get(name);
        if(state == null)
            return true;
        else
            return state.booleanValue();
    }
    
    // Sets god mode for the given user
    public void SetGod(String name, boolean newState)
    {
        // Just save and overwrite
        GodMode.put(name, new Boolean(newState));
    }
}
