package com.suppresswarnings.corpus.service.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttPublish implements Runnable {
	Logger logger = LoggerFactory.getLogger("SYSTEM");
	String url;
    String topic;
    String username;
    String password;
    String clientId;
    MqttClient mqttClient;
    String content;
    public MqttPublish(String msg, String url, String topic, String usernameProperty, String passwordProperty, String clientId) {
        this.content = msg;
    	this.url = url;
        this.topic = topic;
        this.clientId = clientId;
        this.username = System.getProperty(usernameProperty, usernameProperty);
        this.password = System.getProperty(passwordProperty, passwordProperty);
    }
    
	@Override
	public void run() {
		try {
			MemoryPersistence persistence = new MemoryPersistence();
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            mqttClient = new MqttClient(url, clientId, persistence);
            mqttClient.connect(connOpts);
			MqttMessage message = new MqttMessage(content.getBytes());
	        message.setQos(2);
	        mqttClient.publish(topic, message);
	        logger.info("Message published");
	        mqttClient.disconnect();
	        logger.info("Disconnected");
		} catch (Exception e) {
			logger.error("发送消息异常", e);
			e.printStackTrace();
		}
		
	}
    
    
}
