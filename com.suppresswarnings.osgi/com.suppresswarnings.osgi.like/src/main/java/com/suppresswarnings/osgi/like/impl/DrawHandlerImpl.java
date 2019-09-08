package com.suppresswarnings.osgi.like.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.osgi.like.DrawHandler;
import com.suppresswarnings.osgi.like.LikeService;
import com.suppresswarnings.osgi.like.model.Quiz;

public class DrawHandlerImpl implements DrawHandler {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	LikeService service;
	AtomicInteger index = new AtomicInteger(0);
	public DrawHandlerImpl(LikeService service) {
		this.service = service;
	}

	@Override
	public String insert(String userid, String category, String chapter, String question, String type, String optionsa, String optionsb,
			String optionsc, String optionsd, String right, String explain) {
		String now = "" + System.currentTimeMillis();
		String id = String.join(Const.delimiter, category, chapter, now, ""+index.getAndIncrement());
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", "Quizid", id), id);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, userid, "Draw", "Quiz", "Quizid", id), id);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Userid"), userid);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Category"), category);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Question"), question);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Type"), type);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsa"), optionsa);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsb"), optionsb);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsc"), optionsc);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsd"), optionsd);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Right"), right);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Createtime"), now);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Explain"), explain);
		logger.info("quiz saved " + id);
		return id;
	}

	@Override
	public List<String> list(String userid, String category, String chapter) {
		List<String> list = new ArrayList<>();
		String head = String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", "Quizid", category, chapter);
		service.data().page(head, head, null, Integer.MAX_VALUE, (k,v)->{
			list.add(v);
		});
		return list;
	}

	@Override
	public Quiz select(String userid, String category, String chapter, String id) {
		Quiz quiz = new Quiz();
		//TODO select json cache instead each get
		quiz.setId(id);
		quiz.setUserid(userid);
		quiz.setCategory(category);
		quiz.setCreateTime(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Createtime")));
		quiz.setExplain(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Explain")));
		quiz.setOptionsA(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsa")));
		quiz.setOptionsB(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsb")));
		quiz.setOptionsC(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsc")));
		quiz.setOptionsD(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Optionsd")));
		quiz.setQuestion(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Question")));
		quiz.setRight(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Right")));
		quiz.setType(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Type")));
		quiz.setUpdateTime(service.data().get(String.join(Const.delimiter, Const.Version.V1, "Draw", "Quiz", id, "Updatetime")));
		return quiz;
	}

	public static void main(String[] args) {
		System.out.println(String.join(".", "11111",""));
	}
}
