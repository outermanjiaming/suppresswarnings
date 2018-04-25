/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * convert Object A to B
 * @author lijiaming
 *
 */
public class ConvertEntityUtil {

	@SuppressWarnings("unchecked")
	public static void convert(Object src, Object dest) {
		Map<String, Field> fieldsMap = new HashMap<String, Field>();
		Field[] destFields = dest.getClass().getDeclaredFields();
		for (Field field : destFields) {
			field.setAccessible(true);
			Set setter = field.getAnnotation(Set.class);
			if(setter == null) {
				continue;
			}
			fieldsMap.put(setter.value(), field);
		}
		
		Field[] srcFields = src.getClass().getDeclaredFields();
		for (Field field : srcFields) {
			field.setAccessible(true);
			Field target = fieldsMap.get(field.getName());
			//it means there is a Set on the target field of the destination Object
			if(target != null) {
				Set setter = target.getAnnotation(Set.class);
				if(setter.type() == 0) {
					try {
						Object value = field.get(src);
						target.set(dest, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (setter.type() == 1) {
					try {
						Object value = field.get(src);
						target.set(dest, String.valueOf(value));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (setter.type() == 2) {
					try {
						Object value = field.get(src);
						Object primitive = string2primitive(target.getType(), String.valueOf(value));
						target.set(dest, primitive);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (setter.type() == 3) {
					try {
						Object value = field.get(src);
						if(value == null) {
							continue;
						}
						
						@SuppressWarnings("rawtypes")
						List list = (List) value;
						
						@SuppressWarnings("rawtypes")
						Class clazz = setter.clazz();
						
						@SuppressWarnings("rawtypes")
						List todo = new ArrayList();
						
						for(int i=0;i<list.size();i++) {
							Object listobj = list.get(i);
							Object objInList = clazz.newInstance();
							convert(listobj, objInList);
							todo.add(objInList);
						}
						target.set(dest, todo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}//==
		fieldsMap.clear();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T string2primitive(Class<T> clazz, String value) {
		if(clazz == int.class || clazz == Integer.class) {
			return (T) Integer.valueOf(value);
		}
		
		if(clazz == long.class || clazz == Long.class) {
			return (T) Long.valueOf(value);
		}
		
		if(clazz == float.class || clazz == Float.class) {
			return (T) Float.valueOf(value);
		}
		
		if(clazz == double.class || clazz == Double.class) {
			return (T) Double.valueOf(value);
		}

		if(clazz == boolean.class || clazz == Boolean.class) {
			return (T) Boolean.valueOf(value);
		}
		
		if(clazz == char.class || clazz == Character.class) {
			return (T) Character.valueOf(value.charAt(0));
		}
		
		return (T) Float.valueOf(value);
	}
	
	
	public static void main(String[] args) {
		String x = "13";
		long y = string2primitive(Integer.class, x);
		System.out.println(y);
	}
}
