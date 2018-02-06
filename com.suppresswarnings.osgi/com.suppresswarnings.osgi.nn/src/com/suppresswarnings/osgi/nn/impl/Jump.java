package com.suppresswarnings.osgi.nn.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class Jump {
	public static String serializeTo = "nn.4.jump.ser";
	
	
	public static void main(String[] args) throws IOException {
		List<String> lines = Files.list(Paths.get("D:/tmp/jump/")).peek(path -> System.out.println(path.getFileName().toString())).filter(path -> path.getFileName().toString().startsWith("block-jump-duration-raw")).flatMap(path -> {
			try {
				return Files.lines(path);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
		System.out.println(lines.size());
		Set<String> xi = new HashSet<String>();
		Set<String> yi = new HashSet<String>();
		for(int i=0;i<lines.size();i++) {
			String[] vs = lines.get(i).split(",");
			xi.add(vs[0]);
			yi.add(vs[1]);
		}
		IntSummaryStatistics issx = xi.stream().map(x -> Integer.valueOf(x)).sorted().peek(x -> System.out.print(x +" ")).collect(Collectors.summarizingInt(x -> x));
		System.out.println();
		IntSummaryStatistics issy = yi.stream().map(x -> Integer.valueOf(x)).sorted().peek(x -> System.out.print(x +" ")).collect(Collectors.summarizingInt(x -> x));
		System.out.println();
		System.out.println(issx);
		System.out.println(issy);
	}
}
