package com.wl;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	static final String COMMAND_WLF = "wl";

	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new Client(), this);

		WLCommandExecutor ce = new WLCommandExecutor(this);
		WLTabCompleter tc = new WLTabCompleter(getServer());
		getCommand(COMMAND_WLF).setExecutor(ce);
		getCommand(COMMAND_WLF).setTabCompleter(tc);
		
		VersionGetter.getVersionNumber();
	}
}
