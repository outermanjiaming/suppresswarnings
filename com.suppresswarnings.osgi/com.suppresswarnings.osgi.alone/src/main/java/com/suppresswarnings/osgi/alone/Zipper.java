package com.suppresswarnings.osgi.alone;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * zip file and store it in a customized format which include the byte length inside(not a standard form)
 * @author lijiaming
 *
 */
public class Zipper {
	public static byte[] lijiaming = {11,8,9,8,0,12,8,13,6};
	public static byte[] compresser(byte[] input) {
		// Compress the bytes
		byte[] output = new byte[input.length];
		Deflater compresser = new Deflater();
		compresser.setInput(input);
		compresser.finish();
		int compressedDataLength = compresser.deflate(output);
		compresser.end();
		byte[] store = new byte[compressedDataLength + 4 + lijiaming.length];
		byte[] head = int2byte(input.length);
		System.arraycopy(head, 0, store, 0, 4);
		System.arraycopy(output, 0, store, 4, compressedDataLength);
		System.arraycopy(lijiaming, 0, store, 4 + compressedDataLength, lijiaming.length);
		return store;
	}

	public static byte[] decompresser(byte[] output) {
		// uncompress the bytes
		int compressedDataLength = byte2int(output);
		Inflater decompresser = new Inflater();
		decompresser.setInput(output, 4, output.length - 4 - lijiaming.length);
		byte[] result = new byte[compressedDataLength];
		try {
			decompresser.inflate(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		decompresser.end();
		return result;
	}

	public static byte[] int2byte(int res) {
		return new byte[] { (byte) (res & 0xff), (byte) ((res >> 8) & 0xff), (byte) ((res >> 16) & 0xff), (byte) (res >>> 24) };
	}

	public static int byte2int(byte[] res) {
		return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 16) & 0xff0000) | (res[3] << 24);
	}
}