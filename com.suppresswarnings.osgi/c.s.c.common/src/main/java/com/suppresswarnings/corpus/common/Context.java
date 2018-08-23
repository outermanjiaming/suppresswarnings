/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.util.Random;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

/**
 * Context with state, holds a content to make decision
 * @author lijiaming
 *
 * @param <T>
 */
public abstract class Context<T> implements Predicate<String> {
	public org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	T content;
	protected State<Context<T>> state;
	StringBuffer output = new StringBuffer();
	String time;
	String rand;
	public Context(T ctx) {
		this.content = ctx;
		this.time = "" + System.currentTimeMillis();
		this.rand = "" + new Random().nextInt(1000);
	}
	public T content() {
		return content;
	}
	public String time(){
		return time;
	}
	public String random() {
		return rand;
	}
	public String output() {
		String out = this.output.toString();
		this.output.setLength(0);
		return out;
	}
	public void output(String string) {
		this.output.setLength(0);
		this.output.append(string);
	}
	public Context<T> appendLine(String line) {
		if(this.output.length() > 0) {
			this.output.append("\n");
		}
		this.output.append(line);
		return this;
	}
	public State<Context<T>> state(){
		return state;
	}
	public abstract State<Context<T>> exit(); 
	public boolean yes(String input, String expect) {
		return confirm(input, expect, "yes y ok okay alright 好 好的 可以 嗯 是 是的 没错 当然 好啊 是啊 可以的 对 确定 确认 ");
	}
	
	public boolean exit(String input, String expect) {
		return confirm(input, expect, "我要退出 退出 quit exit exit() 不玩了 不想玩了 不做了 不要了 不了 算了 不用了 返回 ");
	}
	
	public boolean confirm(String input, String expect, String common) {
		if(expect == input) return true;
		if(input == null) return false;
		if(expect != null && expect.equals(input)) return true;
		//TODO ner check yes
		String yes = CheckUtil.cleanStr(input.trim()) + " ";
		if(common.contains(yes.toLowerCase())) return true;
		return false;
	}
	
	public void update(){
		this.time = "" + System.currentTimeMillis();
		this.rand = "" + new Random().nextInt(1000);
	}
	
	@Override
	public boolean test(String t) {
		
		try {
			logger.info(random() + "<-state:" + state);
			if(exit(t, "exit()")) {
				state = exit();
			} else {
				state = state.apply(t, this);
			}
			logger.info(random() + "->state:" + state);
			state.accept(t, this);
		} catch (Exception e) {
			this.appendLine("没事，处理数据出了点问题: " + e.getMessage());
		}
		
		return state.finish();
	}
	
	@Override
	public String toString() {
		return "Context [content=" + content + ", state=" + state + ", output=" + output + ", time=" + time + ", rand=" + rand + "]";
	}
	
}
