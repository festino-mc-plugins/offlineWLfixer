package com.wl;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;


public class Client implements Listener {
	final int VERSION_NUMBER = VersionGetter.getVersionNumber();
	
	//String address;
	//int port;
	CommandSender sender;
	static List<String> nicks = new ArrayList<>();
	static List<CommandSender> senders = new ArrayList<>();
	String nick;
	
	InetSocketAddress host;
	Socket socket;
	
	DataOutputStream output;
    DataInputStream input;

	static boolean wl = Bukkit.hasWhitelist();
	
	@EventHandler
	public static void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
		//System.out.println("Testing login of "+event.getName()+" while activator is "+name_activator+" ("+(event.getName().equals(name_activator))+")");
		if(nicks.contains(event.getName())) {
			wl = Bukkit.hasWhitelist();
			System.out.println("Whitelist: " + (wl ? "ON" : "OFF"));
			Bukkit.setWhitelist(false);
		}
		
	}
	
	@EventHandler
	public static void onPlayerLogin(PlayerJoinEvent event) {
		//System.out.println("Testing login of "+event.getName()+" while activator is "+name_activator+" ("+(event.getName().equals(name_activator))+")");
		if(nicks.contains(event.getPlayer().getName())) {
			//disconnect
			
			event.getPlayer().kickPlayer("ti eto vidish?");
	        
			String nick = event.getPlayer().getName();
			CommandSender sender = senders.get(nicks.indexOf(nick));
    		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "whitelist add " + nick);
			sender.sendMessage(String.format(WLCommandExecutor.MSG_ADD_OK, nick));
			Bukkit.setWhitelist(wl);

    		senders.remove(sender);
    		nicks.remove(nick);
		}
		
	}

	public Client() {
	
	}
	
	public Client(CommandSender sender, String address, int port, String nick) {
	    try {
	    	this.sender = sender;
	    	this.nick = nick;
	    	senders.add(sender);
	    	nicks.add(nick);
	    	host = new InetSocketAddress(address, port);
	    	socket = new Socket();
			socket.connect(host, 3000);
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Something went wrong while creating "+this.getClass().getName()+".");
			//e.printStackTrace();
		}
	}
	
	public boolean connect() throws Exception {
		if (socket.getPort() == 0)
			throw new Exception("Socket wasn't connected!");
	    /*----------------------------
	    Client connects to server
		C→S: Handshake State=2
		C→S: Login Start
		S→C: Encryption Request
		Client auth
		C→S: Encryption Response
		Server auth, both enable encryption
		S→C: Login Success
		S→C: Join Game
		S→C: Plugin Message: MC|Brand with the server's brand (Optional)
		S→C: Server Difficulty (Optional)
		S→C: Spawn Position (“home” spawn, not where the client will spawn on login)
		S→C: Player Abilities
		C→S: Plugin Message: MC|Brand with the client's brand (Optional)
		C→S: Client Settings
		S→C: Player Position And Look (Required, tells the client they're ready to spawn)
		C→S: Teleport Confirm
		C→S: Player Position And Look (to confirm the spawn position)
		C→S: Client Status (sent either before or while receiving chunks, further testing needed, server handles correctly if not sent)
		S→C: inventory, Chunk Data, entities, etc
	    ----------------------------*/

		StageHandshake();
		StageLoginStart();
		//StageEncryptionResponse();
		
		//Bukkit.getServer().getWhitelistedPlayers().add()
		/*Timer pollTimer = new Timer();
	    long delayMs = 1000;
	    pollTimer.schedule(
	            new TimerTask() {
	                @Override
	                public void run() {
	                }
		        }, delayMs);*/
	    
	    
	    
	    return true;
	}
	
	public String StageHandshake() throws Exception {

	    //System.out.println("Attempting handshake... "+host.getAddress().toString());
	    
	    /*----------------------------
	    C->S : Handshake State=1
	    C->S : Request
	    S->C : Response
	    C->S : Ping
	    S->C : Pong
	    ----------------------------*/

		// C->S : Handshake State=1
	    // send packet length and packet
	    /*byte [] handshakeMessage = createHandshakeMessage(host.getAddress().getHostAddress(), host.getPort());
	    PacketUtils.writeVarInt(output, handshakeMessage.length);
	    output.write(handshakeMessage);
	    output.flush();*/
	    ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        PacketUtils.writeVarInt(buf, 0);
        PacketUtils.writeVarInt(buf, VERSION_NUMBER);
        PacketUtils.writeString(buf, host.getAddress().getHostAddress());
        buf.writeShort(host.getPort());
        PacketUtils.writeVarInt(buf, 2);

        PacketUtils.sendPacket(buf, output);
	    
	    //System.out.println("Done handshake!");
		return null;
	}
	
	public String StageLoginStart() throws Exception {
		/*byte [] loginstartMessage = createLoginStart(nick);
	    PacketUtils.writeVarInt(output, loginstartMessage.length);
	    output.write(loginstartMessage);
	    output.flush();*/
		ByteArrayDataOutput buf = ByteStreams.newDataOutput();
		PacketUtils.writeVarInt(buf, 0);
		PacketUtils.writeString(buf, nick);

        PacketUtils.sendPacket(buf, output);

        output.flush();
	    /*
	    // C->S : Request
	    output.writeByte(0x01); //size is only 1
	    output.writeByte(0x00); //packet id for ping
	     */
	    // S->C : Response
	    int size = PacketUtils.readVarInt(input);
	    int packetId = PacketUtils.readVarInt(input);

	    if (packetId == -1) {
	        throw new IOException("Premature end of stream.");
	    }

	    if (packetId != 0x01) {
	    	if(packetId == 0x00) {
	    		int length = PacketUtils.readVarInt(input);
	    		byte[] in = new byte[length];
	    	    input.readFully(in);  //read json string
	    	    String json = new String(in);
	    	    System.out.println("Recieved packet 0x00, 0x01 was expected! : "+json);
	    	    return null;
	    	}
	        //throw new IOException("Invalid packetID");
	    }
	    int length = PacketUtils.readVarInt(input); //length of json string

	    if (length == -1) {
	        throw new IOException("Premature end of stream.");
	    }

	    if (length == 0) {
	        throw new IOException("Invalid string length.");
	    }

	    byte[] in = new byte[length];
	    input.readFully(in);  //read json string
	    String json = new String(in);
	    //System.out.println(json);
	    
		return null;
	}
	
	public String infoRequest() throws IOException {
		/*----------------------------
	    C->S : Handshake State=1
	    C->S : Request
	    S->C : Response
	    C->S : Ping
	    S->C : Pong
	    ----------------------------*/

	    byte [] handshakeMessage = createHandshakeMessage(host.getAddress().getHostAddress(), host.getPort());
		// C->S : Handshake State=1
	    // send packet length and packet
	    writeVarInt(output, handshakeMessage.length);
	    output.write(handshakeMessage);
	    
		// C->S : Request
	    output.writeByte(0x01); //size is only 1
	    output.writeByte(0x00); //packet id for ping


	    // S->C : Response
	    int size = PacketUtils.readVarInt(input);
	    int packetId = PacketUtils.readVarInt(input);

	    if (packetId == -1) {
	        throw new IOException("Premature end of stream.");
	    }

	    if (packetId != 0x00) { //we want a status response
	        throw new IOException("Invalid packetID");
	    }
	    int length = PacketUtils.readVarInt(input); //length of json string

	    if (length == -1) {
	        throw new IOException("Premature end of stream.");
	    }

	    if (length == 0) {
	        throw new IOException("Invalid string length.");
	    }

	    byte[] in = new byte[length];
	    input.readFully(in);  //read json string
	    String json = new String(in);

	    // C->S : Ping
	    long now = System.currentTimeMillis();
	    output.writeByte(0x09); //size of packet
	    output.writeByte(0x01); //0x01 for ping
	    output.writeLong(now); //time!?

	    // S->C : Pong
	    PacketUtils.readVarInt(input);
	    packetId = PacketUtils.readVarInt(input);
	    if (packetId == -1) {
	        throw new IOException("Premature end of stream.");
	    }

	    if (packetId != 0x01) {
	        throw new IOException("Invalid packetID");
	    }
	    long pingtime = input.readLong(); //read response
	     
	    

	    // print out server info
	    //System.out.println(json);
	    //System.out.println("Done handshake!");
		return json;
	}

	@Deprecated
	public static byte [] createHandshakeMessage(String host, int port) throws IOException {
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	    DataOutputStream handshake = new DataOutputStream(buffer);
	    handshake.writeByte(0x00); //packet id for handshake
	    writeVarInt(handshake, 4); //protocol version
	    writeString(handshake, host, StandardCharsets.UTF_8);
	    handshake.writeShort(port); //port
	    writeVarInt(handshake, 2); //state (1 for handshake)

	    return buffer.toByteArray();
	}

	@Deprecated
	public static byte [] createLoginStart(String nick) throws IOException {
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	    DataOutputStream loginstart = new DataOutputStream(buffer);
	    loginstart.writeByte(0x00);
	    writeString(loginstart, nick, StandardCharsets.UTF_8);

	    return buffer.toByteArray();
	}

	@Deprecated
	public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
	    byte [] bytes = string.getBytes(charset);
	    writeVarInt(out, bytes.length);
	    out.write(bytes);
	}

	@Deprecated
	public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
	    while (true) {
	        if ((paramInt & 0xFFFFFF80) == 0) {
	          out.writeByte(paramInt);
	          return;
	        }

	        out.writeByte(paramInt & 0x7F | 0x80);
	        paramInt >>>= 7;
	    }
	}
}
