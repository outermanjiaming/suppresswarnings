/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.robot;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientHolder {
	Logger logger = LoggerFactory.getLogger("SYSTEM");
	public static HttpClientHolder getInstance(){
		return INSTANCE;
	}
	private static final HttpClientHolder INSTANCE = new HttpClientHolder();
	private SSLContext sc;
	private RequestConfig requestConfig;
	private HttpClientHolder() {
		try {
			sc = SSLContext.getInstance("SSLv3");
			X509TrustManager trustManager = new X509TrustManager() {  
		        @Override  
		        public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
		        	logger.info("[HttpClientHolder] checkClientTrusted: doing nothing");
		        }  
		  
		        @Override  
		        public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
		        	logger.info("[HttpClientHolder] checkServerTrusted: doing nothing");
		        }  
		  
		        @Override  
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		        	logger.info("[HttpClientHolder] getAcceptedIssuers: doing nothing");
		            return null;
		        }  
		    };
		    sc.init(null, new TrustManager[] { trustManager }, null);
		    requestConfig = RequestConfig.custom()
	                .setConnectTimeout(5000)
	                .setConnectionRequestTimeout(5000)
	                .setSocketTimeout(5000)
	                .setRedirectsEnabled(true)
	                .build();
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}
	public SSLContext sslContext() {
		return sc;
	}
	public CloseableHttpClient newHttpClient() {
		return HttpClients.custom().setSSLContext(sc).setDefaultRequestConfig(requestConfig).build();
	}
}
