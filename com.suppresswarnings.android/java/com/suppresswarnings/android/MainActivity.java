package com.suppresswarnings.android;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
/**
 * SuppressWarnings.android
 * @author lijiaming
 *
 */
public class MainActivity extends Activity {
	interface Key {
		String cache = "suppresswarnings";
		String openid = "onetime.openid";
	}
	private WebView webView;
    private ProgressBar progressBar;
    private Button btn_control;
    private String openid;
    private String token;
    private int version;
    AtomicBoolean ok = new AtomicBoolean(false);

    //用于处理和发送消息的Hander
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            //如果返现msg.what=SHOW_RESPONSE，则进行制定操作，如想进行其他操作，则在子线程里将SHOW_RESPONSE改变
            Log((String)msg.obj);
        }
    };
    
    private String createOpenid() {
    	return version + "A" + new Random().nextInt(1000);
    }
    
    public void Log(String msg) {
    	Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }
    
    public String getToken() {
    	token = openid;
    	return token;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
			version = this.getPackageManager().getPackageInfo(this.getApplication().getPackageName(), 0).versionCode;
		} catch (Exception e) {
			Log("NameNotFoundException");
		}
        
        SharedPreferences spf = this.getSharedPreferences(Key.cache, MODE_PRIVATE);
        this.openid = spf.getString(Key.openid, null);
        if(this.openid == null) {
        	String temp = createOpenid();
        	Editor editor = spf.edit();
        	editor.putString(Key.openid, temp);
        	boolean commited = editor.commit();
        	if(commited) this.openid = temp;
        }
        
        new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String x = HTTPUtil.checkValid(getToken(), "");
					ok.set("Paid".equals(x));
					if(!ok.get()) {
						Message message=new Message();
						message.what=0;
						message.obj="未激活：请到公众号素朴网联获取激活码";
						handler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Message message=new Message();
					message.what=1;
					message.obj="Exception: " + e.getMessage();
					handler.sendMessage(message);
				}
			}
        }).start();
        
        progressBar= (ProgressBar)findViewById(R.id.progressbar);//进度条

        webView = (WebView) findViewById(R.id.webview);
        btn_control = (Button) findViewById(R.id.btn_control);
        String url = "http://www.suppresswarnings.com?token=" + getToken();
        webView.addJavascriptInterface(this, "android");
        //添加js监听 这样html就能调用客户端
        WebSettings settings= webView.getSettings();
        
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
        webView.loadUrl(url);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);

		btn_control.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());

				final EditText inputServer = new EditText(MainActivity.this);

				inputServer.setVerticalScrollBarEnabled(true);
				inputServer.setLines(10);
				inputServer.setGravity(Gravity.LEFT);
				localBuilder.setMessage("请输入命令")
						.setView(inputServer)
						.setNegativeButton("取消", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						})
						.setPositiveButton("输入命令", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(MainActivity.this, CMDService.class);
								intent.putExtra("commands", inputServer.getText().toString());
								startService(intent);
							}
						});
				localBuilder.setCancelable(false);
				localBuilder.create().show();
			}
		});
    }
    
  //WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient=new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {
        	//页面加载完成
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	//页面开始加载
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen","拦截url:"+url);
            if(url.equals("http://www.google.com/")){
                Log("国内不能访问google,拦截该url");
                return true;
                //表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

    };
    
    public boolean openActivity(String who, String where) {
    	try {
    		Intent intent = new Intent(Intent.ACTION_MAIN);
			ComponentName cmp = new ComponentName(who, where);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(cmp);
			startActivity(intent);
			return true;
    	} catch(Exception e) {
    		Log("Fail to open " + where + " of " + who);
    		return false;
    	}
    }
    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient= new WebChromeClient(){
        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
    	 
        @Override
        public boolean onJsAlert(WebView webView, String url, final String message, JsResult result) {
        	
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            
            final EditText inputServer = new EditText(MainActivity.this);
           
            if("输入激活码".equals(message)) {
            	if(ok.get()) {
             		result.confirm();
             		return true;
             	}
            	inputServer.setGravity(Gravity.CENTER);
            	localBuilder.setMessage(message)
                .setView(inputServer)
                .setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
		        })
                .setPositiveButton("激活", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						final String code = inputServer.getText().toString();
						Log("激活码: " + code);
						if(code == null || code.trim().length() < 1) {
							Log("请到素朴网联公众号获取激活码");
							return;
						}
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									String x = HTTPUtil.checkValid(getToken(), code);
									ok.set("Paid".equals(x));
									Message message=new Message();
									message.what=0;
									message.obj=ok.get()?"恭喜，激活成功":"未激活：请到公众号素朴网联获取激活码";
									handler.sendMessage(message);
								} catch (Exception e) {
									e.printStackTrace();
									Message message=new Message();
									message.what=1;
									message.obj="Exception: " + e.getMessage();
									handler.sendMessage(message);
								}
							}
				        }).start();
                    	
                    }
                });
                localBuilder.setCancelable(false);
                localBuilder.create().show();
            } else if("输入命令".equals(message)) {
            	inputServer.setVerticalScrollBarEnabled(true);
            	inputServer.setLines(10);
            	inputServer.setGravity(Gravity.LEFT);
            	localBuilder.setMessage(message)
		        .setView(inputServer)
		        .setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
		        })
		        .setPositiveButton("输入命令", new DialogInterface.OnClickListener() {
		
		            public void onClick(DialogInterface dialog, int which) {
		               Intent intent = new Intent(MainActivity.this, CMDService.class);
		               intent.putExtra("commands", inputServer.getText().toString());
		               startService(intent);
		             }
		        });
		        localBuilder.setCancelable(false);
		        localBuilder.create().show();
            } else if("直接运行".equals(message)) {
            	localBuilder.setMessage(message)
		        .setPositiveButton("直接运行", new OnClickListener() {
		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(MainActivity.this, CMDService.class);
						startService(intent);
					}
		        	
		        });
		        localBuilder.setCancelable(false);
		        localBuilder.create().show();
            } else {
            	localBuilder.setMessage(message)
		        .setPositiveButton("确定", new OnClickListener() {
		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						System.out.println("收到消息:" + message);
					}
		        	
		        });
		        localBuilder.setCancelable(false);
		        localBuilder.create().show();
            }

            //注意:
            //必须要这一句代码:result.confirm()表示:
            //处理结果为确定状态同时唤醒WebCore线程
            //否则不能继续点击按钮
            result.confirm();
            return true;
        }

        //获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        //加载进度回调
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){
        	//点击返回按钮的时候判断有没有上一页
            webView.goBack(); 
            //goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * JS调用android的方法
     * @param str
     * @return
     */
    @JavascriptInterface //仍然必不可少
    public void getClient(String str){
        Log("js调用android: "+str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        webView.destroy();
        webView=null;
    }
}
