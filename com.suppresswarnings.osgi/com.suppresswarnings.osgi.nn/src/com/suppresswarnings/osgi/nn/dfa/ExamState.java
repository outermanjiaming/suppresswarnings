package com.suppresswarnings.osgi.nn.dfa;

public enum ExamState {
	Init {
		@Override
		public void handle(String line, ExamContext context) {
			throw new RuntimeException("init state won't handle nothing");
		}

		@Override
		public ExamState transmit(String action) {
			if(ExamAction.exam.equals(action)) return Exam;
			return Final;
		}
	},Exam {
		@Override
		public void handle(String line, ExamContext context) {
			if(ExamAction.exam.equals(line)) context.newExam();
			else {
				String[] kv = line.split("=");
				context.exam.set(kv[0], kv[1]);
			}
		}

		@Override
		public ExamState transmit(String action) {
			if(ExamAction.page.equals(action)) return Page;
			return this;
		}
	},Page {
		@Override
		public void handle(String line, ExamContext context) {
			if(ExamAction.page.equals(line)) context.newPage();
			else {
				String[] kv = line.split("=");
				context.page.set(kv[0], kv[1]);
			}
		}

		@Override
		public ExamState transmit(String action) {
			if(ExamAction.quiz.equals(action)) return Quiz;
			return this;
		}
	},Quiz {
		@Override
		public void handle(String line, ExamContext context) {
			if(ExamAction.quiz.equals(line)) context.newQuiz();
			else {
				String[] kv = line.split("=");
				context.quiz.set(kv[0], kv[1]);
			}
			
		}

		@Override
		public ExamState transmit(String action) {
			if(ExamAction.quiz.equals(action)) return Quiz;
			if(ExamAction.page.equals(action)) return Page;
			return this;
		}
	}, Final {
		@Override
		public void handle(String line, ExamContext context) {
			throw new RuntimeException("final state won't do nothing");
		}

		@Override
		public ExamState transmit(String action) {
			return this;
		}
	};
	public abstract void handle(String line, ExamContext context);
	public abstract ExamState transmit(String action);
}
