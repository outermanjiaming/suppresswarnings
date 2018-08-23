/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.http;

import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

public class CallableGet implements Callable<String> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String urlFormat;
	Object[] urlArgs;
	public CallableGet(String urlFormat, Object...objects) {
		this.urlFormat = urlFormat;
		this.urlArgs = objects;
	}

	@Override
	public String call() throws Exception {
		String url = String.format(urlFormat, urlArgs);
		CloseableHttpClient httpClient = HttpClientHolder.getInstance().newHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String srtResult = "";
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                srtResult = EntityUtils.toString(httpResponse.getEntity());
                logger.info("[Get] http result: " + srtResult);
            } else {
            	logger.error("[Get] fail to request " + url + ", response " + httpResponse.getStatusLine().toString());
            	throw new Exception("Fail to request get");
            }
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
            }
        }
		return srtResult;
	}

}
