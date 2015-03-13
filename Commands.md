# Introduction #

The following is a list of commands available to certain users. Based on the server's basicbukkit.yml configuration file, certains users may or may not have access to any of the following commands.

Total commands: **~35**

# Commands #
## General Commands ##

  * help:
    * description: Prints all commands
    * usage: `/help [page]`
  * motd:
    * description: Prints prints the Mesage of the Day
    * usage: `/motd`
  * clear:
    * description: Clears the output buffer
    * usage: `/clear`
  * where:
    * description: Prints your location
    * usage: `/where`
  * msg:
    * description: Sends a private message to the given player
    * usage: `/msg <player> <message>`
  * afk:
    * description: Declares yourself as away-from-keyboard; any movement or typing will de-afk you
    * usage: `/afk`

## Time & Weather Management ##

  * time:
    * description: Sets the current time to either day or night
    * usage: `/time <dawn|day|dusk|night>`
  * weather:
    * description: Sets the current weather
    * usage: `/weather <dry|wet>`

## Item Management ##

  * kit:
    * description: Gives the player the decalred kit
    * usage: `/kit`
  * item:
    * description: Gives the player the target item
    * usage: `/item <item ID|keyword> [number, defaults to 64]`
  * i:
    * description: Same as /item
    * usage: `/i <item ID|keyword> [number, defaults to 64]`
  * give:
    * description: Gives the target player the target item
    * usage: `/give <player> <item ID|keyword> [number, defaults to 64]`
  * clean:
    * description: Removes all of the user's inventory except the active inventory
    * usage: `/clean`
  * iclean:
    * description: Removes all items and vehicles in the world
    * usage: /iclean

## User Managament ##

  * kick:
    * description: Kicks a given player off the server
    * usage: `/kick <player> [duration in minutes]`
  * ban:
    * description: Bans the given player from the server
    * usage: `/ban <player> [reason]`
  * unban:
    * description: Unbans the given player from the server
    * usage: `/unban`
  * who:
    * description: List all current players
    * usage: `/who`
  * op:
    * description: Assigns the target player to the given op rank
    * usage: `/op <player> [group ID or name]`
  * god:
    * description: Toggles invincibility; on by default
    * usage: /god
  * pvp:
    * description: Toggles invincibility; on by default
    * usage: /pvp
  * kill:
    * description: Kills self or the target player
    * usage: `/kill [player]`
  * say:
    * description: Make a server-wide announcement
    * usage: `/say <message>`

## Movement & Location Management ##

  * tp:
    * description: Teleports self to player 1or target player 1 to player 2
    * usage: `/tp <player1> [player2]`
  * warp:
    * description: Warps self to given warp or lists warp locations
    * usage: `/warp [location]`
  * list:
    * description: As as /warp sans arguments
    * usage: `/list`
  * setwarp:
    * description: Sets the current location as a warp location
    * usage: `/setwarp <warp name>`
  * delwarp:
    * description: Deletes the given warp name from the warp locations
    * usage: `/delwarp <warp name>`
  * home:
    * description: Warps user to self home location
    * usage: `/home`
  * sethome:
    * description: Saves the current location for self's home location
    * usage: `/sethome`
  * spawn:
    * description: Warps user to spawn
    * usage: `/spawn`
  * setspawn:
    * description: Saves the current location as default spawn location
    * usage: `/setspawn`
  * top:
    * description: Warps player to the highest block in the player's column
    * usage: `/top`
  * jump:
    * description: Warps player to the player's targeted location
    * usage: `/jump`

## Protection Management ##

  * p1:
    * description: Sets the first corner location of a protection volume
    * usage: `/p1`
  * p2:
    * description: Sets the second corner location of a protection volume
    * usage: `/p2`
  * protect:
    * description: Protects the given area agains any modifications by non-owners
    * usage: `/protect <area name>`
  * protectadd:
    * description: Adds player to protected area owners list
    * usage: `/protectadd <area name> <player>`
  * protectrem:
    * description: Removes player from protected area owners list
    * usage: `/protectrem <area name> <player>`
    * protectdel:
      * description: Delete the protected area
      * usage: `/protectdel <area name>`