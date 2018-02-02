package com.suppresswarnings.osgi.alone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class PersistUtil {
	public static void serialize(Serializable object, String serializeTo) {
		File file = new File(serializeTo);
		ObjectOutputStream oos = null;
		try {
			OutputStream stream = new FileOutputStream(file);
			oos = new ObjectOutputStream(stream);
			oos.writeObject(object);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Object deserialize(String serializeTo) {
		File file = new File(serializeTo);
		ObjectInputStream ois = null;
		try {
			InputStream stream = new FileInputStream(file);
			ois = new ObjectInputStream(stream);
			Object object = ois.readObject();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(ois != null) {
				try {
					ois.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
