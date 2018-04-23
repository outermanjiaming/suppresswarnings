package com.suppresswarnings.osgi.alone;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Check {
	//检测类型，是字符串还是数字
	ValidationType type();
	//检测不通过时的提示信息
	String name();
	//如果是String类型，默认最大长度5000
	int maxLength() default 5000;
	//如果是Integer类型，默认最大值为整数最大值
	long max() default Integer.MAX_VALUE;
	int minLength() default 0;
	long min() default Integer.MIN_VALUE;
	//任何类型默认不为null：比如公用model的类型，有的时候不用传某个属性，而有时候需要传。那么就设置为false。
	boolean nonNull() default true;
	//限制不能输入< >
	boolean xss() default true;
	
}
