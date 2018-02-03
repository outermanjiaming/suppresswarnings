package com.suppresswarnings.osgi.data;

/**
 * 
 * @author lijiaming
 *
 */
public class DefaultTextData {
	public static final String lijiaming = "lijiaming";

	public static final TextData _SetThePaper = new TextData()
			.value("请创建问题:")
			.byWhom(lijiaming)
			.createTime("1517643579408")
			.type(Const.TextDataType.collection)
			.describe("创建一个新的问题，用于收集不同的回复。")
			.uuid("default000001")
			.appendExtra("适用范围:出题人")
			.appendExtra("有效期:永久")
			.appendExtra("创建时间:2018年2月3日15点39分39秒");
	
	public static final TextData _DemoQuesion = new TextData()
			.value("请回复和电影相关的语句:")
			.byWhom(lijiaming)
			.createTime("1517644361720")
			.type(Const.TextDataType.question)
			.describe("收集和电影相关的语料，收集上来的语料数据还需要进一步分类和标记。")
			.uuid("default000002")
			.appendExtra("适用范围:所有人")
			.appendExtra("有效期:永久")
			.appendExtra("创建时间:2018年02月03日 15时52分41秒");
	
	
	public static void main(String[] args) {
		System.out.println(_SetThePaper.createTime());
		System.out.println(_DemoQuesion.createTime());
		System.out.println(_SetThePaper.createTime().compareTo(_DemoQuesion.createTime()));
		System.out.println(Long.parseLong("1615a9b4010", 16));
	}
}
