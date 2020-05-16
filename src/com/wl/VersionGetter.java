package com.wl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.Bukkit;

public class VersionGetter {
	private static final long RECONNECT_SECONDS = 1 * 60 * 60;
	private static Integer version_number = null;
	
	public static int getVersionNumber()
	{
		if (version_number != null) {
			return version_number;
		}

		new Thread(new Runnable() {
			public void run() {
				requestVersion();
				while (version_number == null) {
					try {
						Thread.sleep(RECONNECT_SECONDS * 1000);
						requestVersion();
					} catch (InterruptedException e) {}
				}
			}
		}).start();
		
		return 0;
	}
	
	private static void requestVersion()
	{
		String version = Bukkit.getBukkitVersion().split("-")[0];
		try {
			URL url = new URL("https://wiki.vg/Protocol_version_numbers");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			String contentType = con.getHeaderField("Content-Type");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			
			int status = con.getResponseCode();
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(con.getInputStream()));
			String inputLine;
			boolean next = false;
			String int_prefix = "<td>";
			while ((inputLine = in.readLine()) != null) {
				if (next && inputLine.startsWith(int_prefix)) {
					version_number = Integer.parseInt(inputLine.substring(int_prefix.length()));
					Bukkit.getLogger().info("Got Protocol version " + version_number + ".");
					break;
				}
				// <td> <b><span ...><a ...>1.13.2</a></span></b>
				inputLine = inputLine.split("</a>")[0];
				String[] parts = inputLine.split(">");
				inputLine = parts[parts.length - 1];
				
				if (inputLine.contains(version)) {
					next = true;
				}
			}
			if (version_number == null) {
				Bukkit.getLogger().warning("Could not get Protocol version.");
			}
			in.close();
			con.disconnect();
		} catch (Exception e) {
			Bukkit.getLogger().warning("Could not get Protocol version, exception:");
			e.printStackTrace();
		}
	}
}
