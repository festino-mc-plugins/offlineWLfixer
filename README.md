# offlineWLfixer
Minecraft offline UUIDs differ from online, that is why "/whitelist" command doesn't work in offline mode while player is offline.
Plugin is fixing this issue. There are "/wl" command providing access to all the "/whitelist" options, but also working correctly on offline players.
While vanilla command needs "minecraft.command.whitelist" permissions for all the options, our command implements following permissions:
* "minecraft.command.whitelist.add" - /wl add
* "minecraft.command.whitelist.remove" - /wl remove
* "minecraft.command.whitelist.list" - /wl list
* "minecraft.command.whitelist.enable" - /wl on
* "minecraft.command.whitelist.disable" - /wl off
* "minecraft.command.whitelist.reload" - /wl reload
But also player must have vanilla permissions because player is the command executor for vanilla command.

To do:
* Provide command execution without "minecraft.command.whitelist" permission but with corresponding "minecraft.command.whitelist.<...>" permission.
* May be tab completing improvement using https://github.com/JorelAli/1.13-Command-API.
