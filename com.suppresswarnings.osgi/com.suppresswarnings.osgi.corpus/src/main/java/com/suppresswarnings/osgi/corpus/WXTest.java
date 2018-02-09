package com.suppresswarnings.osgi.corpus;

import java.nio.file.Files;
import java.nio.file.Paths;

public class WXTest {

	public static void main(String[] args) throws Exception {
		Content content = new Content();
		byte[] bytes = Files.readAllBytes(Paths.get("D:/tmp/nono.class"));
		content.set("nana", bytes);
		WXContext ctx = new WXContext(content, WXState.init);
		int i = 0;
		while(true) {
			boolean finish = ctx.test("inputs");
			System.out.println(finish + " = " + ctx.state());
			i ++;
			if(i > 6) {
				break;
			}
		}
	}
}
