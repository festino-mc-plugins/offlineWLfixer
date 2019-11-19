package com.wl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class mainListener extends JavaPlugin {
	static final String PERM_PREFIX = "minecraft.command.whitelist",
						PERM_ADD = PERM_PREFIX + ".add",
						PERM_REMOVE = PERM_PREFIX + ".remove",
						PERM_ON = PERM_PREFIX + ".enable",
						PERM_OFF = PERM_PREFIX + ".disable",
						PERM_LIST = PERM_PREFIX + ".list",
						PERM_RELOAD = PERM_PREFIX + ".reload";

	static final String MSG_DENY_PERM = ChatColor.RED + "You do not have permission for \"/%s\".",
						MSG_ADD_DENY_ALREADY = ChatColor.RED + "Player is already whitelisted.",
						MSG_ADD_OK = ChatColor.GREEN + "Added %s to the whitelist.",
						MSG_HELP_ALL = ChatColor.GRAY + "Usage: /wlf <list/add/remove/on/off/reload>";

	public void onEnable()
	{
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new Client(), this);
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		String full_command = "whitelist "
								+ (args.length > 0 ? args[0] : "" )
								+ (args.length > 1 ? (" " + args[1]) : "");
		if (cmd.getName().equalsIgnoreCase("wlf"))
		{
			if (args.length == 0) {
				sender.sendMessage(MSG_HELP_ALL);
				return true;
			}
			
			if (args[0].equalsIgnoreCase("add")) {
				if (!sender.hasPermission(PERM_ADD)) {
					sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
					return false;
				}
				
				if (args.length == 2) {
					boolean player_exists = false;
					for (OfflinePlayer op : Bukkit.getServer().getWhitelistedPlayers())
						if (op.getName().equalsIgnoreCase(args[1])) {
							player_exists = true;
							sender.sendMessage(MSG_ADD_DENY_ALREADY);
							break;
						}

					if (!player_exists)
						for (Player p : Bukkit.getServer().getOnlinePlayers())
							if (p.getName().equalsIgnoreCase(args[1])) {
								Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), full_command);
								sender.sendMessage(String.format(MSG_ADD_OK, args[1]));
								player_exists = true;
								break;
							}
					
					if (!player_exists) {
						new Thread(new Runnable() {
						    public void run() {
						     	try {
									Client cl = new Client(sender, "127.0.0.1", Bukkit.getPort(), args[1]);
									try {
										cl.connect();
										sender.sendMessage(String.format(MSG_ADD_OK, args[1]));
									} catch (Exception e) {
										sender.sendMessage(ChatColor.RED + "There was some error with adding offline player to white list. Please contact the server administrator." +e);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
						    }
						}).start();
					}
					return true;
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!sender.hasPermission(PERM_REMOVE)) {
					sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
					return false;
				}
				boolean res = Bukkit.getServer().dispatchCommand(sender, full_command);
				//msg
				return res;
			} else if (args[0].equalsIgnoreCase("list")) {
				if (!sender.hasPermission(PERM_LIST)) {
					sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
					return false;
				}
				return Bukkit.getServer().dispatchCommand(sender, full_command);
			} else if (args[0].equalsIgnoreCase("on")) {
				if (!sender.hasPermission(PERM_ON)) {
					sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
					return false;
				}
				return Bukkit.getServer().dispatchCommand(sender, full_command);
			} else if (args[0].equalsIgnoreCase("off")) {
				if (!sender.hasPermission(PERM_OFF)) {
					sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
					return false;
				}
				return Bukkit.getServer().dispatchCommand(sender, full_command);
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission(PERM_RELOAD)) {
					sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
					return false;
				}
				return Bukkit.getServer().dispatchCommand(sender, full_command);
			}
			
			Bukkit.getServer().dispatchCommand(sender, full_command);
			return true;
		}
		return false;
	}
}
