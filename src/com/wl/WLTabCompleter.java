package com.wl;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.wl.WLCommandExecutor.WLOption;

public class WLTabCompleter implements TabCompleter {
	Server server;
	
	public WLTabCompleter(Server server) {
		this.server = server;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		List<String> all_options = getOptions(sender);
		if (args.length == 0) {
			return all_options;
		}
		
		if (args.length == 1) {
			List<String> options = new ArrayList<>();
			for (String op : all_options) {
				if (op.startsWith(args[0])) {
					options.add(op);
				}
			}
			return options;
		}
		
		if (args.length == 2) {
			String option = null;
			for (String op : all_options) {
				if (op.equalsIgnoreCase(args[0])) {
					option = op;
					break;
				}
			}
			
			if (option == "remove") {
				ArrayList<String> options = new ArrayList<String>();
				String arg = args[1].toLowerCase();
				for (OfflinePlayer player : server.getWhitelistedPlayers()) {
					String name = player.getName();
					if (name.toLowerCase().startsWith(arg)) {
						options.add(name);
					}
				}
				return options;
			}
			
			if (option == "on" || option == "off" || option == "reload" || option == "list") {
				return new ArrayList<String>();
			}
			
			// vanilla - online players
			if (option == "add") {
				return null;
			}
		}
		
		return null;
	}

	public static List<String> getOptions(CommandSender sender)
	{
		List<String> options = new ArrayList<>();
		for (WLOption op : WLCommandExecutor.OPTIONS) {
			if (sender.hasPermission(op.permission)) {
				options.add(op.label);
			}
		}
		return options;
	}
}
