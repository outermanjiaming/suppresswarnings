package com.suppresswarnings.android.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.suppresswarnings.android.utils.HTTPUtil;
import com.suppresswarnings.android.model.Key;
import com.suppresswarnings.android.view.MyWebview;
import com.suppresswarnings.android.view.IView;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.MODE_PRIVATE;

public class Presenter {

    private static final String url = "http://www.suppresswarnings.com/app.html";
//    private static final String url = "http://www.suppresswarnings.com?token=" + getToken();

    private IView iView;
    private int version;
    private Context mContext;
    private MyWebview mWebview;
    private String openid;
    private String token;
    Handler mHandler;

    AtomicBoolean ok = new AtomicBoolean(false);

    public Presenter(IView iView, Context context, MyWebview webview) {
        this.iView = iView;
        this.mContext = context;
        this.mWebview = webview;
        mHandler = new Handler(Looper.getMainLooper());
        init();
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String x = HTTPUtil.checkValid(getToken(), "");
                    ok.set("Paid".equals(x));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!ok.get()) {
                                handleCase(0, "未激活：请关注微信公众号“素朴网联”获取激活码");
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
        }).start();

    }

    /**
    * @param type 事件类型 0：未激活 1：已激活 2：激活中，等待结果 3：出现异常
     * @param msg 消息通知
    * */
    public void handleCase(final int type, final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case 0:
                        Log(msg);
                        iView.showDialog();
                        break;
                    case 1:
                        iView.loadWebview();
                        break;
                    case 2:
                        if (!msg.equals("恭喜，激活成功")) {
                            iView.doCancel();
                        } else {
                            Log(msg);
                            iView.loadWebview();
                        }
                        break;
                    case 3:
                        Log(msg);
                        break;
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
        mWebview.loadUrl(url);
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
        return version + "A" + new Random().nextInt(1000);
    }

    public void doCancel() {
        iView.doCancel();
    }
}
