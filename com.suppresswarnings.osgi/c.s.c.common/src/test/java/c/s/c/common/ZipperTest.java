package c.s.c.common;

import java.util.Arrays;

import org.junit.Test;

public class ZipperTest {
	
	@Test
	public void testZip() {
		String lijiaming = "lijiaming";
		int[] bytes = lijiaming.chars().map(x->x-'a').toArray();
		System.out.println(Arrays.toString(bytes));
		for(int x : bytes) System.out.println(x^2);
	}

}
