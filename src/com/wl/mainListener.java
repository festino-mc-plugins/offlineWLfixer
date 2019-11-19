package com.wl;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class mainListener extends JavaPlugin {

	public void onEnable()
	{
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new Client(), this);
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("wlf"))
		{
			if(args.length == 2)
			{
				if(args[0].equalsIgnoreCase("add")) {
					boolean player_exists = false;
					for(OfflinePlayer op : Bukkit.getServer().getWhitelistedPlayers())
						if(op.getName().equalsIgnoreCase(args[1]))
							player_exists = true;
					if(!player_exists) {
						new Thread(new Runnable() {
						    public void run() {
						     	try {
									Client cl = new Client(sender, /*Bukkit.getIp()*/"127.0.0.1", Bukkit.getPort(), args[1]);
									try {
										cl.connect();
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
			}
			Bukkit.getServer().dispatchCommand(sender, "whitelist "
														+ (args.length > 0 ? args[0] : "" ) + " "
														+ (args.length > 1 ? (" "+args[1]) : "") );
			return true;
		}
		return false;
	}
}
