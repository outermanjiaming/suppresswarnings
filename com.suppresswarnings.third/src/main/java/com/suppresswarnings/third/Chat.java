package com.suppresswarnings.third;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import com.tencentcloudapi.aai.v20180522.AaiClient;

import com.tencentcloudapi.aai.v20180522.models.ChatRequest;
import com.tencentcloudapi.aai.v20180522.models.ChatResponse;

public class Chat
{
    public static void main(String [] args) {
        try{

            Credential cred = new Credential("AKID72i8JGoReHemeHfmeGgqlRIuJUQAhwxi", "f5FAtNtGNGuJRKxOZUVGn7CetsbjcJlx");
            
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("aai.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);            
            
            AaiClient client = new AaiClient(cred, "ap-beijing", clientProfile);
            
            String params = "{\"Text\":\"吃饭了吗\",\"User\":\"{\\\"id\\\":\\\"10010\\\",\\\"gender\\\":\\\"0\\\"}\",\"ProjectId\":1255895122}";
            ChatRequest req = ChatRequest.fromJsonString(params, ChatRequest.class);
            
            ChatResponse resp = client.Chat(req);
            
            System.out.println(resp.getAnswer());
        } catch (TencentCloudSDKException e) {
                System.out.println(e.toString());
        }

    }
    
}

