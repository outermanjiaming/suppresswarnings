package com.suppresswarnings.osgi.alone;

public class DescribeUtil {
	public static String describe(byte[] array) {
		if(array == null) return null;
		StringBuffer sb = new StringBuffer();
		sb.append("type:byte[]").append(" size:"+array.length);
		int sample = Math.min(8, array.length);
		sb.append(" sample(8):");
		for(int i=0;i<sample;i++) sb.append(array[i]).append(",");
		sb.append("...");
		int min = 256;
		int max = -256;
		for(int e : array) {
			if(e > max) max = e;
			if(e < min) min = e;
		}
		sb.append(" min:").append(min).append(" max:").append(max);
		return sb.toString();
	}
}
