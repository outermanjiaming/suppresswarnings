package com.xiaomi.ad.mimo.demo;

import android.app.Application;
import android.widget.Toast;

import com.miui.zeus.mimo.sdk.MimoSdk;
import com.miui.zeus.mimo.sdk.api.IMimoSdkListener;
import com.xiaomi.ad.mimo.demo.model.Key;

public class SuppressWarnings extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 如果担心sdk自升级会影响开发者自身app的稳定性可以关闭，
        // 但是这也意味着您必须得重新发版才能使用最新版本的sdk, 建议开启自升级
        // MimoSdk.setEnableUpdate(false);

        // 正式上线时候务必关闭stage和debug
        MimoSdk.setDebugOn();
        MimoSdk.setStageOn();

        // 如需要在本地预置插件,请在assets目录下添加mimo_asset.apk;
        MimoSdk.init(this, Key.APP_ID, Key.APP_KEY, Key.APP_TOKEN, new IMimoSdkListener() {
            @Override
            public void onSdkInitSuccess() {
                Toast.makeText(SuppressWarnings.this, "ready go", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSdkInitFailed() {
                Toast.makeText(SuppressWarnings.this, "wait a minute", Toast.LENGTH_LONG).show();
            }
        });
    }
}
