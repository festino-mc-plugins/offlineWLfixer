package com.wl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PacketUtils {

	/** writes a varint to the buffer */
	public static void writeVarInt(ByteArrayDataOutput outs, int paramInt) throws IOException
	{
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				outs.writeByte((byte) paramInt);
				return;
			}
			
			outs.writeByte((byte) (paramInt & 0x7F | 0x80));
			paramInt >>>= 7;
		}
	}

	public static int readVarInt(DataInputStream in) throws IOException
	{
		int i = 0;
		int j = 0;
		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}
		return i;
	}

	/** writes a string to the bytebuffer */
	public static void writeString(ByteArrayDataOutput out, String s) throws IOException
	{
		PacketUtils.writeVarInt(out, s.length());
		out.write(s.getBytes("UTF-8"));
	}
	
	public static void sendPacket(ByteArrayDataOutput buf, DataOutputStream out) throws Exception
	{
		ByteArrayDataOutput send1 = ByteStreams.newDataOutput();
		writeVarInt(send1, buf.toByteArray().length);
		send1.write(buf.toByteArray());
		out.write(send1.toByteArray());
		out.flush();
	}
}
