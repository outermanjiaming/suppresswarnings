package com.suppresswarnings.osgi.common;

import com.suppresswarnings.osgi.common.proxy.CP9;
import com.suppresswarnings.osgi.common.proxy.SafeThirdProxy;
import com.suppresswarnings.osgi.common.proxy.ThirdParty;

import junit.framework.TestCase;

public class TestCP9 extends TestCase {

	public void testCp9(){
		ThirdParty cp = new CP9("this is what you want.");
		ThirdParty safe = SafeThirdProxy.newInstance(cp);
		String result = safe.call();
		System.out.println(result);
		System.out.println(safe);
		System.out.println(cp);
	}
}
