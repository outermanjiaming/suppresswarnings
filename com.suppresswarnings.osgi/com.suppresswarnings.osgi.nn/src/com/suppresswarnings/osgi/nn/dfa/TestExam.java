package com.suppresswarnings.osgi.nn.dfa;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestExam {

	public static void main(String[] args) throws Exception {
		ExamContext action = new ExamContext();
		Files.lines(Paths.get("/Users/lijiaming/company/exam001")).forEach(action);
		System.out.println(action);
	}
}
