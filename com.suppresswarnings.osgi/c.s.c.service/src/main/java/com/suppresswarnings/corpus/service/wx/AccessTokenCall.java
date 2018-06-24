/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.wx;

import java.security.cert.CertificateException;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

public class AccessTokenCall implements Callable<String> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String urlFormat = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
	Object[] urlArgs = {"wx41b262e9b9d8885e", "e64302221a8a128fad1cbc723abc122d"};
	
	@Override
	public String call() throws Exception {
		logger.info("[AccessTokenCall] start call");
		SSLContext sc = SSLContext.getInstance("SSLv3");  
	    X509TrustManager trustManager = new X509TrustManager() {  
	        @Override  
	        public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
	            return null;  
	        }  
	    };  
	  
	    sc.init(null, new TrustManager[] { trustManager }, null);
//	    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//	    cm.setMaxTotal(100);
//	    cm.setDefaultMaxPerRoute(50);
	    RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .setRedirectsEnabled(true)
                .build();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLContext(sc)
				.setDefaultRequestConfig(requestConfig)
				.build();
        
        String url = String.format(urlFormat, urlArgs);
        HttpGet httpGet = new HttpGet(url);
        String srtResult = "";
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                srtResult = EntityUtils.toString(httpResponse.getEntity());
                logger.info("[AccessTokenCall] http result: " + srtResult);
            } else {
            	logger.error("[AccessTokenCall] fail to request " + url + ", response " + httpResponse.getStatusLine().toString());
            }
        } catch (Exception e) {
            logger.error("[AccessTokenCall] Exception when request", e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return srtResult;
	}

}
