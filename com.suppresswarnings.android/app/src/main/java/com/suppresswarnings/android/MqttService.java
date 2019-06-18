package com.xiaomi.ad.mimo.demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class MqttService extends Service {
    public static final String TAG = "MqttService";
    /**
     * 以下为MQTT
     */
    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private String host = "tcp://iot.eclipse.org:1883";
    private String userName = "iot.eclipse.org";
    private String passWord = "iot.eclipse.org";
    private static String clientId = null;
    private ScheduledFuture future = null;
    AtomicBoolean stop = new AtomicBoolean(false);
    BiConsumer<String, byte[]> consumer;
    ServiceConnection service;
    public void setConsumer(BiConsumer<String, byte[]> consumer) {
        this.consumer = consumer;
        Log.w(TAG,"consumer");
    }
    //bug
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            init();
        } catch (Exception e) {
            Log.w(TAG, "Exception when init:" + e.getMessage());
        }
    }
    public static void hearbeat(String msg){
        publish("heartbeat",msg.getBytes());
    }

    public static void publish(String topic, byte[] bytes) {

        Integer qos = 0;
        Boolean retained = false;
        try {
            if (client != null){
                String realTopic = "corpus/android/" + topic + "/" + clientId;
                Log.w(TAG, "client.publish(" + realTopic+ ")");
                client.publish(realTopic, bytes, qos.intValue(), retained.booleanValue());
            }
        } catch (Exception e) {
            Log.w(TAG, "Exception publish " + e.getMessage());
        }
    }
    public static byte[] longToBytes(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    public static long bytesToLong(byte[] buffer) {
        long  values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8; values|= (buffer[i] & 0xff);
        }
        return values;
    }
    public static int byteToInt2(byte[] b) {

        int mask=0xff;
        int temp=0;
        int n=0;
        for(int i=0;i<b.length;i++){
            n<<=8;
            temp=b[i]&mask;
            n|=temp;
        }
        return n;
    }

    public static byte[] intToBytes2(int n){
        byte[] b = new byte[4];

        for(int i = 0;i < 4;i++)
        {
            b[i]=(byte)(n>>(24-i*8));

        }
        return b;
    }
    public static void bytesToFile(byte[] buffer, File file){
        OutputStream output = null;
        BufferedOutputStream bufferedOutput = null;
        try {
            output = new FileOutputStream(file);
            bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(buffer);
        } catch (Exception e) {
        } finally{
            if(null!=bufferedOutput){
                try {
                    bufferedOutput.close();
                } catch (Exception e) {
                }
            }

            if(null != output){
                try {
                    output.close();
                } catch (Exception e) {
                }
            }
        }


    }
    public static byte[] fileToBytes(File file) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];

            int n;

            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }

            buffer = bos.toByteArray();
        } catch (Exception ex) {
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ex) {
            }
        }

        return buffer;
    }

    public static ByteBuffer encodeValue(byte[] value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(value.length);
        byteBuffer.clear();
        byteBuffer.get(value, 0, value.length);
        return byteBuffer;
    }


    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 "+clientId + ", client=" + client);
            try {
                String topic = "corpus/android/" + clientId + "/#";
                client.subscribe(topic,1);
            } catch (Exception e) {
                Log.i(TAG, "Exception:"+ e.getMessage());
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            Log.i(TAG, "onFailure ");
        }
    };
    public String createOpenid() {
        try {
            int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            String m_szDevIDShort = "35" +
                    Build.BOARD.length()%10 +
                    Build.BRAND.length()%10 +
                    Build.CPU_ABI.length()%10 +
                    Build.DEVICE.length()%10 +
                    Build.DISPLAY.length()%10 +
                    Build.HOST.length()%10 +
                    Build.ID.length()%10 +
                    Build.MANUFACTURER.length()%10 +
                    Build.MODEL.length()%10 +
                    Build.PRODUCT.length()%10 +
                    Build.TAGS.length()%10 +
                    Build.TYPE.length()%10 +
                    Build.USER.length()%10 ;
            return version + "A" + m_szDevIDShort;
        } catch (Exception e) {
            return "Exception:" + e.getMessage();
        }

    }
    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }
    private void init() {
        Log.w(TAG, "init service");
        String uri = host;
        clientId = createOpenid();
        client = new MqttAndroidClient(this, uri, clientId);
        conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setAutomaticReconnect(true);
        conOpt.setConnectionTimeout(10);
        conOpt.setKeepAliveInterval(20);
        conOpt.setUserName(userName);
        conOpt.setPassword(passWord.toCharArray());
        doClientConnection();
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        final long start = System.currentTimeMillis();
        future = service.scheduleWithFixedDelay(new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if(stop.get()) {
                    Log.w(TAG, "stop the heart");
                    future.cancel(true);
                }
                long duration = System.currentTimeMillis() - start;
                index ++;
                hearbeat(start + "T" + duration + "T" + index);
            }
        }, 3, 40, TimeUnit.SECONDS);
    }


    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNormal()) {
            try {
                client.setCallback(new MqttCallback() {

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        byte[] data = message.getPayload();
                        String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
                        Log.i(TAG, "messageArrived:" + data.length);
                        Log.i(TAG, str2);
                        if(consumer != null) {
                            consumer.accept(topic, data);
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken arg0) {
                        Log.w(TAG, "delivery mqtt");
                    }

                    @Override
                    public void connectionLost(Throwable arg0) {
                        Log.w(TAG, "connectionLost -> doClientConnection");
                        doClientConnection();
                    }
                });
                client.connect(conOpt, null, iMqttActionListener);
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured when doClientConnection", e);
            }
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        stop.set(true);
        Log.w(TAG, "mqtt onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * 以上为MQTT
     */

    @Override
    public IBinder onBind(Intent intent) {
        return new CustomBinder();
    }

    public String clientid() {
        return clientId;
    }

    public void setService(ServiceConnection serviceConnection) {
        this.service = serviceConnection;
    }


    public class CustomBinder extends Binder {
        public MqttService getService(){
            Log.w(TAG, "getService MqttService");
            return MqttService.this;
        }
    }
}
