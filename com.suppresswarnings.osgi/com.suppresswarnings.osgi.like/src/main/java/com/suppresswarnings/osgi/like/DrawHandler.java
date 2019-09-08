package com.suppresswarnings.osgi.like;

import java.util.List;

import com.suppresswarnings.osgi.like.model.Quiz;

public interface DrawHandler {
	String insert(String userid, String category, String chapter, String question, String type, String optionsa, String optionsb, String optionsc, String optionsd, String right, String explain);
	List<String> list(String userid, String category, String chapter);
	Quiz select(String userid, String category, String chapter, String id);
}
