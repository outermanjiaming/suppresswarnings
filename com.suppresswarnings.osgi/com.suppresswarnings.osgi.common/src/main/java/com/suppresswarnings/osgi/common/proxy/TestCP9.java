package com.suppresswarnings.osgi.common.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.suppresswarnings.osgi.common.proxy.CP9;
import com.suppresswarnings.osgi.common.proxy.SafeThirdProxy;
import com.suppresswarnings.osgi.common.proxy.ThirdParty;
import com.suppresswarnings.osgi.network.http.Parameter;

public class TestCP9 {

	public void testCp9(){
		ThirdParty cp = new CP9("invite");
		ThirdParty safe = SafeThirdProxy.newInstance(cp);
		String result = safe.call();
		System.out.println("result: "+result);
		System.out.println(cp);
	}
	
	public void testConfig(){
		AutowiredConfigFactory factory = new AutowiredConfigFactory();
		Map<String, List<String>> parameter = new HashMap<String, List<String>>();
		List<String> username = new ArrayList<String>();
		username.add("outermanjiaming");
		parameter.put("values", username);
		List<String> count = new ArrayList<String>();
		count.add("2");
		parameter.put("count", count);
		Parameter para = new Parameter(parameter);
		AnyInterface any = (AnyInterface) factory.create(para, AnyInterface.class); 
		System.out.println(Arrays.toString(any.values()));
		System.out.println(any.count());
	}
}
