package com.wl;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class WLCommandExecutor implements CommandExecutor {
	
	public static class WLOption {
		public final String label;
		public final String permission;
		
		public WLOption(String lbl, String perm) {
			this.label = lbl;
			this.permission = perm;
		}
	}
	
	JavaPlugin plugin;

	public static final String PERM_VANILLA = "minecraft.command.whitelist";
	public static final String PERM_PREFIX = "minecraft.command.whitelist";
	public static final WLOption[] OPTIONS = { new WLOption("add", PERM_PREFIX + ".add"),
												new WLOption("remove", PERM_PREFIX + ".remove"),
												new WLOption("list", PERM_PREFIX + ".list"),
												new WLOption("on", PERM_PREFIX + ".enable"),
												new WLOption("off", PERM_PREFIX + ".disable"),
												new WLOption("reload", PERM_PREFIX + ".reload") };

	public static final String MSG_DENY_PERM = ChatColor.RED + "You do not have permission for \"/%s\".",
							   MSG_ADD_DENY_ALREADY = ChatColor.RED + "Player is already whitelisted.",
							   MSG_ADD_OK = ChatColor.GREEN + "Added %s to the whitelist.",
							   MSG_HELP_ALL = ChatColor.GRAY + "Usage: /" + Main.COMMAND_WLF + " <%s>",
							   MSG_STATE = ChatColor.GRAY + "The whitelist is now %s.",
							   MSG_CANT_HELP = ChatColor.RED + "No whitelist options available. =(";

	public WLCommandExecutor(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!cmd.getName().equalsIgnoreCase(Main.COMMAND_WLF)) {
			return false;
		}

		String full_command = "whitelist "
								+ (args.length > 0 ? args[0] : "" )
								+ (args.length > 1 ? (" " + args[1]) : "");

		// INFO on/off
		if (args.length == 0) {
			String state = Bukkit.hasWhitelist() ? "ON" : "OFF";
			sender.sendMessage(String.format(MSG_STATE, state));
			return true;
		}
		
		// HELP
		if (args.length == 1 && args[0].contains("?")) {
			List<String> options = WLTabCompleter.getOptions(sender);
			if (options.size() == 0) {
				sender.sendMessage(MSG_CANT_HELP);
				return false;
			}
			
			String options_label = options.get(0);
			options.remove(0);
			for (String op : options) {
				options_label += "/" + op;
			}
			sender.sendMessage(String.format(MSG_HELP_ALL, options_label));
			return true;
		}
		
		boolean found = false, permitted = false;
		for (WLOption op : OPTIONS) {
			if (op.label.equalsIgnoreCase(args[0])) {
				found = true;
				if (sender.hasPermission(op.permission)) {
					permitted = true;
				}
				break;
			}
		}
		
		if (!found) {
			return Bukkit.getServer().dispatchCommand(sender, full_command);
		} else if (!permitted) {
			sender.sendMessage(String.format(MSG_DENY_PERM, full_command));
			return false;
		}
		
		if (args[0].equalsIgnoreCase("add")) {
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
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), full_command);
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
									boolean ok = cl.connect();
									if (ok) {
										sender.sendMessage(String.format(MSG_ADD_OK, args[1]));
									} else {
										
									}
									// bad code location
									Bukkit.setWhitelist(cl.wl);
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
		} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("on")
				|| args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("reload")) {
			return dispatchCommand(sender, full_command, PERM_VANILLA);
		}
		
		return true;
	}
	
	public boolean dispatchCommand(CommandSender sender, String full_command, String perm) {
		PermissionAttachment attachment = sender.addAttachment(plugin, perm, true);
		boolean res = Bukkit.getServer().dispatchCommand(sender, full_command);
		sender.removeAttachment(attachment);
		return res;
	}
}
