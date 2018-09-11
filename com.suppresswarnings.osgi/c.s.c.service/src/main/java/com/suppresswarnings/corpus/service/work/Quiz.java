/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.work;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.suppresswarnings.corpus.common.CheckUtil;
import com.suppresswarnings.corpus.common.KeyValue;

public class Quiz {
	KeyValue quiz;
	List<KeyValue> reply = new ArrayList<>();
	List<KeyValue> similar = new ArrayList<>();
	public Quiz(String key, String value){
		this.quiz = new KeyValue(key, value);
	}
	
	public KeyValue getQuiz() {
		return quiz;
	}

	public void setQuiz(KeyValue quiz) {
		this.quiz = quiz;
	}

	public List<KeyValue> getReply() {
		return reply;
	}

	public void setReply(List<KeyValue> reply) {
		this.reply = reply;
	}

	public List<KeyValue> getSimilar() {
		return similar;
	}

	public void setSimilar(List<KeyValue> similar) {
		this.similar = similar;
	}
	public void similar(String key, String value) {
		this.similar.add(new KeyValue(key, value));
	}
	public void reply(String key, String value) {
		this.reply.add(new KeyValue(key, value));
	}
	
	public boolean assimilate(Quiz other) {
		String me = CheckUtil.cleanStr(quiz.value());
		String yo = CheckUtil.cleanStr(other.quiz.value());
		
		HashSet<String> mine = new HashSet<>();
		mine.add(me);
		for(KeyValue same : similar) {
			mine.add(CheckUtil.cleanStr(same.value()));
		}
		if(mine.contains(yo)) {
			takein(other);
			return true;
		}
		for(KeyValue same : other.similar) {
			String check = CheckUtil.cleanStr(same.value());
			if(mine.contains(check)) {
				takein(other);
				return true;
			}
		}
		return false;
	}
	
	public void takein(Quiz other) {
		reply.addAll(other.reply);
		similar.addAll(other.similar);
		//TODO important
		similar.add(other.quiz);
	}

	@Override
	public String toString() {
		return "Quiz [quiz=" + quiz + ", \n\treply=" + reply + ", \n\tsimilar=" + similar + "]";
	}
}
