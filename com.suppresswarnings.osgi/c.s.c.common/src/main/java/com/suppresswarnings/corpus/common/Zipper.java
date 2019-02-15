/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * zip file and store it in a customized format which include the byte length inside(not a standard form)
 * @author lijiaming
 *
 */
public class Zipper {
	public static byte[] compresser(byte[] input) {
		byte[] lijiaming = array2byte(string2byte(System.getProperty("zipper.password", "changeit")));
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
		xor(store, lijiaming);
		return store;
	}

	public static byte[] decompresser(byte[] output) {
		byte[] lijiaming = array2byte(string2byte(System.getProperty("zipper.password", "changeit")));
		// uncompress the bytes
		int compressedDataLength = byte2int(output);
		Inflater decompresser = new Inflater();
		try {
			xor(output, lijiaming);
			decompresser.setInput(output, 4, output.length - 4 - lijiaming.length);
			byte[] result = new byte[compressedDataLength];
			decompresser.inflate(result);
			decompresser.end();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public static byte[] int2byte(int res) {
		return new byte[] { (byte) (res & 0xff), (byte) ((res >> 8) & 0xff), (byte) ((res >> 16) & 0xff), (byte) (res >>> 24) };
	}
	public static byte[] array2byte(int[] array) {
		byte[] bytes = new byte[array.length];
		for(int i=0;i<array.length;i++) bytes[i] = (byte)array[i];
		return bytes;
	}
	public static void xor(byte[] origin, byte[] password) {
		for(int i=0,j=0;i<origin.length;i++,j++) {
			if(j==password.length) j=0;
			origin[i] ^= password[j];
		}
	}
	public static int[] string2byte(String password) {
		return password.chars().map(x->x-'a').toArray();
	}
	
	public static int byte2int(byte[] res) {
		return (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 16) & 0xff0000) | (res[3] << 24);
	}
}