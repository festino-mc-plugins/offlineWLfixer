package com.wl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.bukkit.Bukkit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class VersionGetter {
	private static Integer version_number = null;
	private static final Integer MAX_VERSION = 10000;
	private static final String ADDRESS = "127.0.0.1";
	private static final int PORT = Bukkit.getPort();
	
	private static boolean calculating = false;
	
	public static int getVersionNumber()
	{
		if (version_number != null) {
			return version_number;
		}
		
		if (calculating) {
			return 0;
		}
		
		calculating = true;

		new Thread(new Runnable() {
		    public void run() {
		    	int res = calculate();
				calculating = false;
		    }
		}).start();
		
		return 0;
	}
	
	private static int calculate()
	{
		InetSocketAddress host;
		Socket socket;
		
		DataOutputStream output;
	    DataInputStream input;
	    
	    try {
		    host = new InetSocketAddress(ADDRESS, PORT);
		    socket = new Socket();
			socket.connect(host, 3001);
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
	    } catch (Exception e) {
	    	return -1;
		}

		if (socket.getPort() == 0) {
			try {
				socket.close();
			} catch (IOException e) { }
			return -1;
		}
	    
		int num = 0;
	    while (version_number == null) {
	    	Bukkit.getLogger().info("Test protocol version " + num);
	    	
			ByteArrayDataOutput buf = ByteStreams.newDataOutput();
			try {
		        PacketUtils.writeVarInt(buf, 0);
		        PacketUtils.writeVarInt(buf, num);
		        PacketUtils.writeString(buf, host.getAddress().getHostAddress());
		        buf.writeShort(host.getPort());
		        PacketUtils.writeVarInt(buf, 2);
		        PacketUtils.sendPacket(buf, output);
		        
		        buf = ByteStreams.newDataOutput();
				
				PacketUtils.writeVarInt(buf, 0);
				PacketUtils.writeString(buf, "p" + num);
				PacketUtils.sendPacket(buf, output);
	        	output.flush();
	        	
	    	    int size = PacketUtils.readVarInt(input);
	    	    int packetId = PacketUtils.readVarInt(input);

	    	    if (packetId == -1) {
	    	        throw new IOException("Premature end of stream.");
	    	    }
System.out.println("5");
	    	    if (packetId != 0x01) {
	    	    	if(packetId == 0x00) {
	    	    		int length = PacketUtils.readVarInt(input);
	    	    		byte[] in = new byte[length];
	    	    	    input.readFully(in);  //read json string
	    	    	    String json = new String(in);
	    	    	    System.out.println("json: " + json);
	    	    	    if (json.contains("Outdated client! Please use")) {
	    	    	    	num++;
	    	    	    	continue;
	    	    	    }
	    	    	}
	    	    }
	        	
	        	version_number = num;
			} catch (Exception e) { }
			
			num++;
			if (num >= MAX_VERSION) {
				try {
					socket.close();
				} catch (IOException e) { }
				return -1;
			}
			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
			}
		}
	    
	    Bukkit.getLogger().info("Current protocol version: " + version_number);
	    
	    try {
			socket.close();
		} catch (IOException e) {
		}
	    
	    return version_number;
	}
}
