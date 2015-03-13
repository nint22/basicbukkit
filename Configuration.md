# Introduction #

Installing BasicBukkit is as easy as copying BasicBukkit.jar into your plugins directory. Configuring a BasicBukkit server is also as easy: most configuration is done with the "config.yml" YAML-style file.

# Installation #

Installing BasicBukkit requires you to download the latest BasicBukkit.jar file, which can be found in the "[Downloads](http://code.google.com/p/basicbukkit/downloads/list)" section. Look for the file with the data nearest your current date. Alpha-tagged jar files are for developer-level testing only, while Beta-tagged is meant for general-purpose testing. Release-tagged files are meant as files that are considered stable and ready to use.

Once BasicBukkit.jar has been downloaded, place it into your plugins directory of your CraftBukkit server installation location.

Launch the server once, then halt it. This is to allow BasicBukkit to create all of the default configuration files it needs. You will even notice BasicBukkit says this on the server's terminal.

Now it is up to you to change configuration options before re-launching your server.

# config.yml #

This is the main configuration file for your server, split into roughly six (6) sections.

## MOTD (Message of the Day) ##

The MOTD tag is for you to write your own Messager of the Day. This string (or block of string) is sent to users upon logging in. This string MUST be a literal YAML block and can include colors (i.e. &0, &1, ... &f) of your choice. The default message is as follows:

```
motd:
    Welcome to a BasicBukkit server!
    Please type /help for a list of commands
```

## Groups ##

Groups in BasicBukkit are always listed in config.yml. A single group is defined in the following YAML block format: _Please note that this block is tabbed over twice because it is the child block of the "groups" heading._

```
    # Default group
    - id: 0
      name: Guest
      pre: "[&bGuest&f]"
      commands:
          - help
          - who
          - motd
      banned_access: false
      build: true
```

The first line of any group should be a unique integer, used for op assignment in-game. All groups follows simple hierarchy with this number, so that group 0 is less powerful than group 1, while group 1 is less powerful than group 2, etc.. Though this isn't strictly enforced in code, it is a good protocol to conform to for the sake of configuration readability.

The second line is the formal name of the group. A single string is required.

The third line is the "pre" title, meaning the title given to all of those in this group. This title shows as colored in the Minecraft client when a user of this group speaks.

The fourth line is an array of strings which represents commands users of this group may access. You must **explicitly** list all commands you want available to the user of this group. Higher-numbered group do not allow access to lower group number's commands. Again, note this block-array is tabbed since it is owned by the commands header.

Finally, the last two lines that are booleans that declares whether or not this group's users can access (i.e. generate and/or use) banned items and if they can build (i.e. place or break blocks).

## Banned Items ##

Baned items is a simple comma-delimited array of item IDs (blocks or items) that cannot be generated, used, placed, broken, or ignited unless the group of the placing user has the "banned\_access" header set to true.

The following is an example:

```
# Bans all forms of tnt, lava, flint & steel, obsidian, etc.. Good default values
banned: 0, 7, 8, 9, 10, 11, 46, 51, 52, 90, 325, 326, 327, 342, 343, 328, 333, 79
```

## Flows & TNT ##

If you would like to disable or enable lava, water, or fire to flow, simply change the following headers to true or false: _note that lava is also in this section_

```
# Allow flow of lava
lavaflows: false

# Allow flow of water
waterflows: false

# Allow spread of fire
fireflows: false

# Allow TNT explosions
allowTNT: false
```

## Timed Messages ##

If you would like for the server to send global messages after a certain number of seconds, you will have you will have to modify the "messages" sections. All messages are three-element blocks: message, delay, and start. The message is a string that allows color, but defaults to light purple, that is broadcast to all players. The delay is the number of seconds it takes between message broadcasts. The start is the number of seconds to wait before using the message's delay to start displaying. The start variable is a good way to time messages of equal-delay time so they don't all apear at the same time. _Note that only whole integers (i.e. whole seconds) are allowed; no floating-point values are allowed._

Sample message block:

```
messages:
    - message: Please support BasicBukkit, visit code.google.com/p/basicbukki
      delay: 60
      start: 10
      
    - message: This admin is awesome for using BasicBukkit
      delay: 100
      start: 0
```

## Kit ##

This block defines all the items, of the given quantity, for users to receive when calling the "/kit" command. Items must be a list of the item ID followed by the quantity. Quantities are required, and are thus not optional.

Unlike other servers, there is only one kit to be defined here - this may change in future BasicBukkit builds.

Sample kit configuration:

```
# Kit items
kit:
    - 277 1    # Diamond pick
    - 278 1    # Diamond shovel
    - 279 1    # Diamond axe
```

## World Size ##

Your server must have a maximum world size defined. By default, it is a small 256x256 block world (height is never limited). Note that the world size must be defined as while integers, and as a list (not comma-delimited).

Sample world size constraints:

```
# Max size of the world (width, length)
size:
    - 256
    - 256
```

# Other Files #

Other files are generated when BasicBukkit is installed: items.csv, protections.yml, users.yml, warps.yml

All of these are purely data files used for record keeping and saving. "users.yml" is a special file that defines which users are in which group - change this file to update a user's permissions out of game.

As a new administrator, you will want to make yourself a higher-level op. To do this, simply open users.yml and add your user name and preferred group ID in the following style to the bottom of the file:

```
user_name:
    group: 0
```