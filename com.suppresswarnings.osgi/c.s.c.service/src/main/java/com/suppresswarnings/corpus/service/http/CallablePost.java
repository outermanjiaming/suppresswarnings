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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

public class CallablePost implements Callable<String> {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	String url;
	String json;
	public CallablePost(String url, String json) {
		this.url = url;
		this.json = json;
	}

	@Override
	public String call() throws Exception {
		CloseableHttpClient httpClient = HttpClientHolder.getInstance().newHttpClient();
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(json,"utf-8");//解决中文乱码问题    
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");    
        post.setEntity(entity);
        String srtResult = "";
        try {
            HttpResponse httpResponse = httpClient.execute(post);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                srtResult = EntityUtils.toString(httpResponse.getEntity());
                logger.info("[AccessTokenCall] http result: " + srtResult);
            } else {
            	logger.error("[AccessTokenCall] fail to request " + url + ", response " + httpResponse.getStatusLine().toString());
            	throw new Exception("Fail to request post");
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
