package com.suppresswarnings.osgi.common.proxy;

import java.util.Properties;

import com.suppresswarnings.osgi.common.proxy.CP9;
import com.suppresswarnings.osgi.common.proxy.SafeThirdProxy;
import com.suppresswarnings.osgi.common.proxy.ThirdParty;

import junit.framework.TestCase;

public class TestCP9 extends TestCase {

	public void testCp9(){
		ThirdParty cp = new CP9("invite");
		ThirdParty safe = SafeThirdProxy.newInstance(cp);
		String result = safe.call();
		System.out.println("result: "+result);
		System.out.println(cp);
	}
	
	public void testConfig(){
		AutowiredConfigFactory factory = new AutowiredConfigFactory();
		Properties para = new Properties();
		para.setProperty("name", "lijiaming");
		para.setProperty("count", "23");
		AnyInterface any = (AnyInterface) factory.create(para, AnyInterface.class); 
		System.out.println(any.name());
		System.out.println(any.count());
	}
}
