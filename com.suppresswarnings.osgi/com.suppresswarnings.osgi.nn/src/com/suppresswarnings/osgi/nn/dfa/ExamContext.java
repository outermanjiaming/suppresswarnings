package com.suppresswarnings.osgi.nn.dfa;

import java.util.function.Consumer;

public class ExamContext implements Consumer<String> {
	ExamState state = ExamState.Init;
	Exam exam;
	Page<Quiz> page;
	Quiz quiz;
	public void newExam() {
		exam = new Exam();
	}
	public void newPage() {
		page = new Page<Quiz>();
		exam.addPage(page);
	}
	public void newQuiz() {
		quiz = new Quiz();
		page.add(quiz);
	}
	
	@Override
	public void accept(String t) {
		state = state.transmit(t);
		state.handle(t, this);
	}
	
	@Override
	public String toString() {
		return "ExamContext [state=" + state + ", exam=" + exam + "]";
	}
	
}
