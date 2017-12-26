package com.suppresswarnings.osgi.common.proxy;

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
}
