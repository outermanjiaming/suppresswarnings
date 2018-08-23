/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WXPayConfigImpl extends WXPayConfig {
	Logger logger = LoggerFactory.getLogger("SYSTEM");
	IWXPayDomain domain = new IWXPayDomain() {
		
		@Override
		public void report(String domain, long elapsedTimeMillis, Exception ex) {
			logger.info("[wxpay report] domain:" + domain + ", elapsedTimeMillis:" + elapsedTimeMillis + ", ex:" + ex.getMessage());
		}
		
		@Override
		public DomainInfo getDomain(WXPayConfig config) {
			return new DomainInfo("api.mch.weixin.qq.com", true);
		}
	};
	@Override
	String getAppID() {
		return System.getProperty("wx.appid");
	}

	@Override
	String getMchID() {
		return System.getProperty("wx.mchid");
	}

	@Override
	String getKey() {
		return System.getProperty("wx.key");
	}

	@Override
	InputStream getCertStream() {
		File cert = new File("/etc/nginx/apiclient_cert.pem");
		try {
			InputStream inputStream = new FileInputStream(cert);
			return inputStream;
		} catch (Exception e) {
			logger.error("[wxpay config] cert not found", e);
			return null;
		}
	}

	@Override
	IWXPayDomain getWXPayDomain() {
		return domain;
	}

}
