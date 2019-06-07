package com.suppresswarnings.android.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.suppresswarnings.android.model.Key;
import com.suppresswarnings.android.model.HTTP;
import com.suppresswarnings.android.view.IView;
import com.suppresswarnings.android.view.MyWebview;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static android.content.Context.MODE_PRIVATE;

public class Presenter {

    private static final String url = "http://www.suppresswarnings.com/app.html";
    private static final String internal = "素朴网联sleep素朴网联swipe0素朴网联left素朴网联right素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep";
    private IView iView;
    private int version;
    private Context mContext;
    private MyWebview mWebview;
    private String openid;
    private String token;
    private Handler mHandler;
    private ScheduledExecutorService service;
    public AtomicReference<String> command = new AtomicReference<>();
    public AtomicBoolean ok = new AtomicBoolean(false);

    public Presenter(IView iView, Context context, MyWebview webview) {
        this.iView = iView;
        this.mContext = context;
        this.mWebview = webview;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.service = Executors.newSingleThreadScheduledExecutor();
        init();
    }

    public String url() {
        return url + "?token=" + getToken();
    }

    /**
    * 初始化
    * */
    private void init() {
        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log("NameNotFoundException");
        }

        SharedPreferences spf = mContext.getSharedPreferences(Key.cache, MODE_PRIVATE);
        this.openid = spf.getString(Key.openid, null);
        if (this.openid == null) {
            String temp = createOpenid();
            SharedPreferences.Editor editor = spf.edit();
            editor.putString(Key.openid, temp);
            boolean commited = editor.commit();
            if (commited) this.openid = temp;
        }

        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean x =checkValidAndSetCommand("");
                    Log.w("lijiaming", "check validate and set command = " + x);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (!ok.get()) {
                                handleCase(0, Key.invalid);
                            } else {
                                handleCase(1, "");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handleCase(3, e.getMessage());
                }
            }
        }, 4, TimeUnit.HOURS.toSeconds(4), TimeUnit.SECONDS);

    }

    public boolean checkValidAndSetCommand(String code) throws Exception {
        String x = HTTP.checkValid(getToken(), code);
        ok.set(HTTP.valid(x));
        if(ok.get() && x.split("~").length > 1) {
            command.set(x.split("~")[1]);
        } else {
            command.set(internal);
        }
        return ok.get();
    }

    /**
    * @param type 事件类型 0：未激活 1：已激活 2：激活中，等待结果 3：出现异常
     * @param msg 消息通知
    * */
    public void handleCase(final int type, final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (type) {
                        case 0:
                            Log(msg);
                            if(!ok.get() && iView != null) {
                                iView.showDialog("case 0");
                            }
                            break;
                        case 1:
                            iView.updateUI();
                            break;
                        case 2:
                            if (!ok.get() && !msg.equals("恭喜，激活成功")) {
                                iView.showDialog("case 2");
                            } else {
                                Log(msg);
                                iView.updateUI();
                            }
                            break;
                        case 3:
                            Log(msg);
                            break;
                    }
                } catch (Exception e) {
                    Log.w("lijiaming", "error while handle Case: " + e.getMessage());
                }
            }
        });
    }


    /**
    * 加载webview
    * */
    public void loadWebview() {

        mWebview.addJavascriptInterface(this, "android");
        //添加js监听 这样html就能调用客户端
        WebSettings settings = mWebview.getSettings();

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //设置js可以直接打开窗口，如window.open()，默认为false
        settings.setJavaScriptEnabled(true);
        //是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
        settings.setSupportZoom(true);
        //是否可以缩放，默认true
        settings.setBuiltInZoomControls(true);
        //是否显示缩放按钮，默认false
        settings.setUseWideViewPort(true);
        //设置此属性，可任意比例缩放。大视图模式
        settings.setLoadWithOverviewMode(true);
        //和setUseWideViewPort(true)一起解决网页自适应问题
        settings.setAppCacheEnabled(true);
        //是否使用缓存
        settings.setDomStorageEnabled(true);
        //DOM Storage
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //不使用缓存，只从网络获取数据.
        mWebview.loadUrl(url());
        mWebview.setWebViewClient(webViewClient);
    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            //页面加载完成
            iView.updateUI();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //页面开始加载
            iView.showProgressbar();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen", "拦截url:" + url);
            if (url.equals("http://www.google.com/")) {
                Log("国内不能访问google,拦截该url");
                return true;
                //表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    };

    /**
     * JS调用android的方法
     *
     * @param str
     * @return
     */
    @JavascriptInterface //仍然必不可少
    public void getClient(String str) {
        Log("js调用android: " + str);
    }

    public void Log(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    public String getToken() {
        token = openid;
        return token;
    }

    /**
     * 释放引用，防止内存泄露
     */
    public void destroy() {
        mWebview.destroy();
        mWebview = null;
        iView = null;
    }

    public String createOpenid() {
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
    }

    public void doCancel() {
        iView.doCancel();
        service.shutdown();
    }
}
