package com.suppresswarnings.osgi.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SafeThirdProxy implements InvocationHandler {
	ThirdParty safe;
	private SafeThirdProxy(){}
	private SafeThirdProxy(ThirdParty checked){
		this.safe = checked;
	}
	public static ThirdParty newInstance(ThirdParty unSafe) throws SecurityException, NullPointerException {
		if(unSafe != null && unSafe.token() != null) {
			String token = unSafe.token();
			//Security Check.
			if("lijiaming".equals(token)){
				ClassLoader loader = unSafe.getClass().getClassLoader();
				Class<?>[] interfaces = unSafe.getClass().getInterfaces();
				SafeThirdProxy proxy = new SafeThirdProxy(unSafe);
				return (ThirdParty) Proxy.newProxyInstance(loader, interfaces, proxy);
			} else {
				throw new SecurityException("token not safe");
			}
		} else {
			throw new NullPointerException(unSafe == null ? "null ThirdParty" : "token required");
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("Going to invoke: " + method.getName());
		Object result = null;
		try {
			result = method.invoke(safe, args);
		} catch (Exception e) {
			System.out.println("Exception occur when invoke method: " + method.getName());
		}
		System.out.println("method has been invoked and result is going to return");
		return result;
	}
}
