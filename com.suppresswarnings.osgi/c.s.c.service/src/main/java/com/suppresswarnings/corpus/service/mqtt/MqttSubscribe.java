package com.suppresswarnings.corpus.service.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttSubscribe implements Runnable {
	Logger logger = LoggerFactory.getLogger("SYSTEM");
    String url;
    String topic;
    String username;
    String password;
    String clientId;
    MqttClient mqttClient;
    IMqttMessageListener listener;

    public MqttSubscribe(IMqttMessageListener listener,String url, String topic, String usernameProperty, String passwordProperty, String clientId) {
        this.listener = listener;
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
            connOpts.setCleanSession(false);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            mqttClient = new MqttClient(url, clientId, persistence);
            mqttClient.connect(connOpts);
            mqttClient.subscribe(topic, new IMqttMessageListener(){

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    listener.messageArrived(s, mqttMessage);
                    logger.info(Thread.currentThread() + "\t-\t" + clientId + "\t-\t" + s + " " + mqttMessage.getId() + " " + mqttMessage.toString());
                }
            });
            
            logger.info("subscribe all set");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
    	try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
    }
}
