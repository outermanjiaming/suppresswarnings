package com.suppresswarnings.osgi.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

public class AutowiredConfigFactory implements InvocationHandler {
	Properties properties;
	
	public Object create(Properties para, Class<?> t){
		this.properties = para;
		return Proxy.newProxyInstance(t.getClassLoader(), new Class[]{t}, this);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String property = properties.getProperty(method.getName());
		if (property == null)
			return (null);

		final Class<?> returns = method.getReturnType();
		if (returns.isPrimitive()) {
			if (returns.equals(int.class))
				return (Integer.valueOf(property));
			else if (returns.equals(long.class))
				return (Long.valueOf(property));
			else if (returns.equals(double.class))
				return (Double.valueOf(property));
			else if (returns.equals(float.class))
				return (Float.valueOf(property));
			else if (returns.equals(boolean.class))
				return (Boolean.valueOf(property));
		}
		return property;
	}

}
