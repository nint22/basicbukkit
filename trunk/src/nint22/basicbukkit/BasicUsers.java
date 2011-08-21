/***************************************************************
 
 BasicBukkit - Basic commands for any new MineCraft server
 Copyright 2011 Core S2 - See License.txt for info
 
 This source file is developed and maintained by:
 + Jeremy Bridon jbridon@cores2.com
 
 File: BasicUsers.java
 Desc: Manage all users, and user groups.
 
***************************************************************/

package nint22.basicbukkit;

import java.io.*;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

// Special inclusions to help with invisibility hack...
// Note that this code requires the original server lib as well
// as is based on a current server-side bug; may not
// be supported in future clients
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BasicUsers
{
    // Main plugin handler
    private BasicBukkit plugin;
    
    // Internal config handler
    private Configuration users;
    
    // All users and groups, etc.. (In parallel)
    private LinkedList<String> OpNames;         // User name
    private LinkedList<Integer> OpGroup;        // User's group ID
    private LinkedList<String> OpTitle;         // Special title regardless of rank
    
    // All groups (in parallel)
    private LinkedList<Integer> GroupID;            // Group's unique ID
    private LinkedList<String> GroupName;           // Group's unique ID
    private LinkedList<String[]> GroupCommands;     // Valid commands in group (index based on groups)
    private LinkedList<String> GroupPreTitle;       // Pre-title for the given group
    private LinkedList<Boolean> GroupBannedItems;   // True if this group can access banned items
    private LinkedList<Boolean> GroupCanBuild;      // Can this group build? (i.e. break and place?)
    private LinkedList<Boolean> GroupCanWorldEdit;  // Group can use the world edit plugin
    private LinkedList<Integer> GroupExp;           // The minimum number of experiance for this group
    
    // All kicked users time (unix time)
    // The unix epoch time when a user can join back
    private HashMap<String, Integer> KickedTimes;
    
    // All baned users
    private HashMap<String, String> BannedUsers;
    
    // God mode (if true, player recieves no damage); not saved to file!
    private HashMap<String, Boolean> GodMode;
    
    // AFK users (if true, user is afk)
    private HashMap<String, Boolean> AFKMode;
    
    // Muted users: key is users name and boolean is true/false
    private HashMap<String, Boolean> MuteMode;
    
    // Hidden users: key is users name and boolean is true/false
    private HashMap<String, Boolean> HiddenMode;
    
    // Initialize users
    public BasicUsers(BasicBukkit plugin, Configuration users, Configuration config)
    {
        // Save plugin handle
        this.plugin = plugin;
        
        // Load the users file (just in case it hasn't yet)
        this.users = users;
        this.users.load();
        
        // Allocate users and info
        OpNames = new LinkedList();
        OpGroup = new LinkedList();
        OpTitle = new LinkedList();
        
        // Allocate all lists as needed
        GroupID = new LinkedList();
        GroupName = new LinkedList();
        GroupCommands = new LinkedList();
        GroupPreTitle = new LinkedList();
        GroupBannedItems = new LinkedList();
        GroupCanBuild = new LinkedList();
        GroupCanWorldEdit = new LinkedList();
        GroupExp = new LinkedList();
        
        // Create new hash map
        KickedTimes = new HashMap();
        BannedUsers = new HashMap();
        GodMode = new HashMap();        // Note that this one is not saved
        AFKMode = new HashMap();        // Not saved
        MuteMode = new HashMap();       // Not saved
        HiddenMode = new HashMap();     // Not saved
        
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
            Boolean CanBuild = (Boolean)GroupData.get("build");
            Boolean CanWorldEdit = (Boolean)GroupData.get("worldedit"); 
            
            Integer MinExperiance = (Integer)GroupData.get("exp");
            if(MinExperiance == null)
                MinExperiance = new Integer(-1);
            
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
            GroupCanBuild.add(CanBuild);
            GroupCanWorldEdit.add(CanWorldEdit);
            GroupExp.add(MinExperiance);
        }
        
        // For each user?
        for(String key : users.getKeys())
        {
            // Player name is key
            OpNames.add(key);
            
            // Get the optional title
            OpTitle.add(users.getString(key + ".title", ""));
            
            // Get the group ID
            OpGroup.add(users.getInt(key + ".group", 0));
            
            // Does this user have a kick time?
            int KickTime = users.getInt(key + ".kicktime", 0);
            if(KickTime > 0)
                KickedTimes.put(key, KickTime);
            
            // Banned string
            String BanReason = users.getString(key + ".banned", "");
            if(BanReason.length() > 0)
                BannedUsers.put(key, BanReason);
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
            map.put("title", OpTitle.get(i));
            
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
        if(GroupID.contains(new Integer(UserGroupID)))
        {
            // Group exists, add user to group ID
            // Does the user already exist? If so, just change at the same index
            if(OpNames.contains(UserName))
            {
                // Just change that index in GroupID
                int UserIndex = OpNames.indexOf(UserName);
                OpGroup.set(UserIndex, UserGroupID);
            }
            // New user
            else
            {
                OpNames.add(UserName);
                OpTitle.add("");
                OpGroup.add(UserGroupID);
            }
            
            // Set the experiance back to this level
            if(plugin.configuration.getBoolean("roleplay", false))
            {
                String groupName = plugin.users.GetGroupName(UserName);
                int minExp = plugin.users.GetGroupExp(groupName);
                plugin.roleplay.SetExperiance(UserName, minExp);
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
        String GroupTitle = "Undefined Group";
        int GroupIndex = GetGroupID(UserName);
        if(GroupIndex >= 0)
            GroupTitle = ChatColor.WHITE + "[" + GroupPreTitle.get(GroupIndex) + ChatColor.WHITE + "]";
        
        // Get the user's special title
        String UserTitle = "";
        int UserIndex = OpNames.indexOf(UserName);
        if(UserIndex >= 0 && OpTitle.get(UserIndex).length() > 0)
            UserTitle = ChatColor.WHITE + "[" + OpTitle.get(UserIndex) + ChatColor.WHITE + "]";
        
        // Error check user title
        if(UserTitle == null)
            UserTitle = "";
        
        // If the user is afk, add it
        String AFK = "";
        if(GetAFK(UserName))
            AFK =  ChatColor.WHITE + "[" + ChatColor.RED + "afk" + ChatColor.WHITE + "]";
        
        return AFK + GroupTitle + UserTitle;
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
    public boolean CanUseItem(int ItemID, String name)
    {
        return CanUseItem(ItemID, 0, name);
    }
    
    // Can the given user place / use banned blocks / items?
    public boolean CanUseItem(int ItemID, int MetaID, String name)
    {
        // Find the user's group (and fail out if does not exist)
        int GroupIndex = GetGroupID(name);
        if(GroupIndex < 0)
            return false;
        
        // Can we use banned items?
        boolean CanUseBanned = GroupBannedItems.get(GroupIndex).booleanValue();
        
        // Is it a banned item?
        boolean IsBanned = plugin.itemNames.IsBanned(ItemID, MetaID); 
        
        // If it is a banned item and we cannot use it, return false
        if(IsBanned)
        {
            if(CanUseBanned)
                return true;
            else
                return false;
        }
        // Not-banned, just use
        else
            return true;
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
    public boolean IsKicked(String userName)
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
    public void SetKickTime(String userName, int minutes)
    {
        // Create the time when the user can join back
        int KickEndTime = (int)(System.currentTimeMillis()/1000) + minutes * 60;
        
        // Insert into hash map
        KickedTimes.put(userName, new Integer(KickEndTime));
    }
    
    // Ban user
    public void SetBan(String userName, String reason)
    {
        // Ban / kick if online
        // Put to self-ban list
        BannedUsers.put(userName, reason);
        
        // Add to official banned list
        // Open the ban files
        try
        {
            // Append (note the true boolean)
            BufferedWriter writer = new BufferedWriter(new FileWriter("banned-players.txt", true));
            writer.write("\n" + userName);
            writer.close();
        }
        catch(Exception e)
        {
            System.out.println("### BasicBukkit Unable to update ban list: " + e.getMessage());
        }
    }
    
    // Pardon user
    // Return the user's name if found, else returns null
    public String SetUnban(String userName)
    {
        // Look at all banned users - can we find them in our own banned list
        // We will be doing a "smart search"
        boolean BasicBukkitFound = false;
        for(String name : BannedUsers.keySet())
        {
            // Do we have a partial match?
            if(name.toLowerCase().startsWith(userName.toLowerCase()))
            {
                BasicBukkitFound = true;
                userName = name;
                break;
            }
        }
        
        // Check the kicked logs..
        if(BasicBukkitFound == false)
        {
            for(String name : KickedTimes.keySet())
            {
                // Do we have a partial match?
                if(name.toLowerCase().startsWith(userName.toLowerCase()))
                {
                    BasicBukkitFound = true;
                    userName = name;
                    break;
                }
            }
        }
        
        // Remove from BasicBukkit ban and system
        if(BasicBukkitFound)
        {
            KickedTimes.remove(userName);
            BannedUsers.remove(userName);
        }
        
        // Now remove from the official list
        boolean FileFound = false;
        try
        {
            // Read banned and write to new banned list
            BufferedReader reader = new BufferedReader(new FileReader("banned-players.txt"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt"));

            // Source to out line
            String source = "";

            // Keep reading line-by-line
            while((source = reader.readLine()) != null)
            {
                // If it doesn't match, write out
                if(!source.toLowerCase().startsWith(userName.toLowerCase()))
                    writer.write("\n" + source);
                // Else found, so don't write name...
                else
                {
                    userName = source;
                    FileFound = true;
                }
            }

            // All done
            reader.close();
            writer.close();

            // Change file
            File temp = new File("temp.txt");
            temp.renameTo(new File("banned-players.txt"));
        }
        catch(Exception e)
        {
            System.out.println("### BasicBukkit Unable to update ban list: " + e.getMessage());
        }
        
        // Remove from BasicBukkit ban and system
        if(BasicBukkitFound)
        {
            KickedTimes.remove(userName);
            BannedUsers.remove(userName);
        }
        
        // Was the player removed from either of the list?
        if(BasicBukkitFound || FileFound)
            return userName;
        else
            return null;
    }
    
    // Return a string if banned
    public String IsBanned(String userName)
    {
        // Did we ban using BasicBukkit?
        String basicBanned = BannedUsers.get(userName);
        
        // Is banned via basicbukkit?
        if(basicBanned != null)
            return basicBanned;
        // Is banned via official ban-list?
        else
        {
            try
            {
                // Read banned and write to new banned list
                BufferedReader reader = new BufferedReader(new FileReader("banned-players.txt"));
                
                // Source to out line
                String source = "";
                
                // Keep reading line-by-line
                while((source = reader.readLine()) != null)
                {
                    // If matched, it is a banned user
                    if(source.equalsIgnoreCase(userName))
                    {
                        basicBanned = "No defined ban reason";
                        break;
                    }
                }
                
                // All done
                reader.close();
            }
            catch(Exception e)
            {
                System.out.println("### BasicBukkit Unable to read ban list: " + e.getMessage());
            }
        }
        
        // Final ban check
        if(basicBanned != null)
            return basicBanned;
        else
            return null;
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
    
    // Get AFK mode
    public boolean GetAFK(String name)
    {
        Boolean isAFK = AFKMode.get(name);
        if(isAFK == null || (isAFK.booleanValue() == false))
        {
            AFKMode.remove(name);
            return false;
        }
        else
            return true;
    }
    
    // Set AFK mode
    public void SetAFK(String name, boolean isAFK)
    {
        // Just save and overwrite
        AFKMode.put(name, new Boolean(isAFK));
        
        // Update title
        Player player = plugin.getServer().getPlayer(name);
        if(player != null)
            player.setDisplayName( plugin.users.GetUserTitle(name)  + player.getName() );
    }
    
    // Return a group ID based on groupName; returns -1 on error
    public int GetGroupIDByGroup(String groupName)
    {
        // Go through manually so we can do comparisons; ignore case
        for(int i = 0; i < GroupName.size(); i++)
        {
            // Does match (ignore case)
            if(GroupName.get(i).toLowerCase().startsWith(groupName.toLowerCase()))
                return GroupID.get(i);
        }
        
        // Not found, return -1
        return -1;
    }
    
    // Get all group IDs
    public LinkedList<Integer> GetGroupIDs()
    {
        return GroupID;
    }
    
    // get all group names based on list index, not group ID
    public LinkedList<String> GetGroupNames()
    {
        return GroupName;
    }
    
    // Can build
    public boolean CanBuild(String userName)
    {
        // Get user's group
        int GroupID = GetGroupID(userName);
        if(GroupID < 0)
            return false;
        
        // Get group's build status
        return GroupCanBuild.get(GroupID).booleanValue();
    }

    // Can use world edit
    public boolean CanWorldEdit(String userName)
    {
        // Get user's group
        int GroupID = GetGroupID(userName);
        if(GroupID < 0)
            return false;
        
        // Get group's build status
        return GroupCanWorldEdit.get(GroupID).booleanValue();
    }
    
    public Integer GetGroupExp(String groupName)
    {
        // Get group index
        int index = GroupName.indexOf(groupName);
        if(index < 0)
            return -1;
        else
            return GroupExp.get(index);
    }
    
    public boolean IsMute(Player target)
    {
        // Find key
        Boolean isMuted = MuteMode.get(target.getName());
        if(isMuted == null)
            return false;
        else
            return isMuted.booleanValue();
    }
    
    public void SetMute(Player target, boolean IsMuted)
    {
        // Save new hash
        if(IsMuted == false)
            MuteMode.remove(target.getName());
        else
            MuteMode.put(target.getName(), IsMuted);
    }

    public void SetTitle(Player target, String NewTitle)
    {
        // Is the player in the current groups?
        int UserIndex = OpNames.indexOf(target.getName());
        if(UserIndex >= 0)
            OpTitle.set(UserIndex, NewTitle);
    }
    
    public String GetSpecialTitle(Player target)
    {
        // Is the player in the current groups?
        int UserIndex = OpNames.indexOf(target.getName());
        if(UserIndex >= 0)
            return OpTitle.get(UserIndex);
        else
            return "";
    }
    
    public boolean IsHidden(Player target)
    {
        Boolean isHidden = HiddenMode.get(target.getName());
        if(isHidden == null)
            return false;
        else
            return isHidden.booleanValue();
    }
    
    public void SetHidden(Player target, boolean IsHidden)
    {
        // Save new hash
        if(IsHidden == false)
            HiddenMode.remove(target.getName());
        else
            HiddenMode.put(target.getName(), IsHidden);
        
        // For each online player, send a "gone" packet
        for(Player other : plugin.getServer().getOnlinePlayers())
        {
            // Ignore self
            if(other != target)
            {
                // Are we hiding the player?
                if(IsHidden)
                {
                    // Cast to get access to send custom packet
                    // Target hides from others
                    CraftPlayer targetCraftPlayer = (CraftPlayer)target;
                    CraftPlayer otherCraftPlayer = (CraftPlayer)other;
                    
                    Packet hideTarget = new Packet29DestroyEntity(targetCraftPlayer.getEntityId());
                    otherCraftPlayer.getHandle().netServerHandler.sendPacket(hideTarget);
                }
                else
                {
                    // Cast to get access to send custom packet
                    CraftPlayer targetCraftPlayer = (CraftPlayer)target;
                    CraftPlayer otherCraftPlayer = (CraftPlayer)other;
                    
                    Packet unhideTarget = new Packet20NamedEntitySpawn(targetCraftPlayer.getHandle());
                    otherCraftPlayer.getHandle().netServerHandler.sendPacket(unhideTarget);
                }
            }
        }
        
        // Done with function
    }
}
