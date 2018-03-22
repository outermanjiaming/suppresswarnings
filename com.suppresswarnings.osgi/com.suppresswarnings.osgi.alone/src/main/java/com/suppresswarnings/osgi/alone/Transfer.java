/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Transfer {
	public static final String zip = ".zipped";
	public static final String unzip = ".unzip";
	public static void zip(String origin){
		String output = origin + zip;
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(origin));
			byte[] zip = Zipper.compresser(bytes);
			Files.write(Paths.get(output), zip, StandardOpenOption.CREATE_NEW);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void unzip(String zip){
		String output = zip + unzip;
		try {
			byte[] zipped = Files.readAllBytes(Paths.get(zip));
			byte[] origin = Zipper.decompresser(zipped);
			Files.write(Paths.get(output), origin, StandardOpenOption.CREATE_NEW);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		Transfer.zip("E:/osgi/origin-source.zip");
		Transfer.unzip("E:/osgi/origin-source.zip"+zip);
	}
}
