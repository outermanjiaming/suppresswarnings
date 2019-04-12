package com.suppresswarnings.corpus.service.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.work.Quiz;

public class Ally {
	String openid;
	String qrScene;
	String ownerid;
	WaitingRoom room;
	Iterator<Quiz> quiz;
	Quiz current;
	List<KeyValue> qa = new ArrayList<>();
	volatile int state = -1;
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getQrScene() {
		return qrScene;
	}

	public void setQrScene(String qrScene) {
		this.qrScene = qrScene;
	}

	public String getOwnerid() {
		return ownerid;
	}

	public void setOwnerid(String ownerid) {
		this.ownerid = ownerid;
	}

	public WaitingRoom getRoom() {
		return room;
	}

	public void setRoom(WaitingRoom room) {
		this.room = room;
		this.quiz = room.quiz.iterator();
	}
	
	public void start(Context<CorpusService> context) {
		if(quiz.hasNext()) {
			state = 0;
			current = quiz.next();
			context.output(current.getQuiz().value());
		} else {
			context.output("你很幸运，题库空虚，无需作答");
		}
	}
	
	public void reply(String reply, Context<CorpusService> context) {
		qa.add(new KeyValue(current.getQuiz().value(), reply));
		String keyReply = String.join(Const.delimiter, current.getQuiz().key(), "Reply", openid, context.time(), context.random());
		context.content().data().put(keyReply, reply);
		if(quiz.hasNext()) {
			current = quiz.next();
			context.output(current.getQuiz().value());
		} else {
			state = 1;
			context.output("你已完成答题，等待对方完成。你有一次机会可以和对手留言（双方都完成之后的3分钟之内）");
		}
	}
	
	public void chat(String t, Context<CorpusService> context) {
		room.chat(openid, t);
		context.output("你刚才的留言会在对方完成答题之后送达");
	}
	
	public void complete(CorpusService service, List<KeyValue> qa) {
		StringBuffer info = new StringBuffer();
		for(int i=0;i<qa.size();i++) {
			try {
				int index = i + 1;
				KeyValue e1 = this.qa.get(i);
				KeyValue e2 = qa.get(i);
				info.append("问： " + index+ ". " + e1.key()).append("\n");
				info.append("甲： " + e1.value()).append("\n");
				info.append("乙： " + e2.value()).append("\n");
			} catch (Exception e) {
				info.append("异常：云端检测结果时异常").append("\n");
			}
		}
		service.atUser(openid, info.toString());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((openid == null) ? 0 : openid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ally other = (Ally) obj;
		if (openid == null) {
			if (other.openid != null)
				return false;
		} else if (!openid.equals(other.openid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Ally [openid=" + openid + ", qrScene=" + qrScene + ", ownerid=" + ownerid
				+", room.key="+ room.key() + "]";
	}

	public boolean finish() {
		return state == 1;
	}

	public void clear() {
		qa.clear();
		room = null;
	}
}
