package com.suppresswarnings.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.suppresswarnings.android.model.Key;
import com.suppresswarnings.android.presenter.Presenter;
import com.suppresswarnings.android.model.HTTP;
import com.suppresswarnings.android.view.FloatingView;
import com.suppresswarnings.android.view.IView;
import com.suppresswarnings.android.view.MyWebview;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends Activity implements IView {
    AtomicBoolean stopped = new AtomicBoolean(false);
    MyWebview webview;
    TextView warning;
    LinearLayout background;
    ProgressBar progressBar;
    String TAG = "lijiaming";
    Presenter presenter;
    String[] template = {
            "素朴网联home素朴网联jump,30素朴网联open,%s素朴网联sleep素朴网联sleep素朴网联sleep素朴网联loop,200素朴网联swipe1素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联while素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep",
            "素朴网联home素朴网联jump,30素朴网联open,%s素朴网联sleep素朴网联sleep素朴网联sleep素朴网联right素朴网联left素朴网联sleep素朴网联loop,200素朴网联swipe0素朴网联sleep素朴网联click素朴网联sleep素朴网联sleep素朴网联sleep素朴网联loop,3素朴网联swipe0素朴网联sleep素朴网联swipe0素朴网联sleep素朴网联swipe1素朴网联sleep素朴网联swipe1素朴网联sleep素朴网联while素朴网联sleep素朴网联back素朴网联while素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep"
    };
    MainActivity context;
    FloatingView floatBall;
    WindowManager windowManager;
    WindowManager.LayoutParams floatBallParams;
    AtomicLong clicked = new AtomicLong(0);

    public void showFloatBall() {
        floatBall = new FloatingView(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        floatBallParams = new WindowManager.LayoutParams();
        floatBallParams.width = floatBall.width;
        floatBallParams.height = floatBall.height;
        floatBallParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        Log.w(TAG, "showFloatBall Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            floatBallParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            floatBallParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        floatBallParams.format = PixelFormat.RGBA_8888;
        windowManager.addView(floatBall, floatBallParams);
        floatBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis() - clicked.get() > 5000) {
                    clicked.set(System.currentTimeMillis());
                    if(AutoService.getInstance() == null) {
                        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        Log.w(TAG, "ACTION_ACCESSIBILITY_SETTINGS");
                    } else {
                        try {
                            if(presenter == null || !presenter.ok.get()) {
                                Toast.makeText(context, "需要激活", Toast.LENGTH_SHORT).show();
                            } else {
                                if(AutoService.getInstance().running.compareAndSet(true, false)) {
                                    Toast.makeText(context, "停止执行", Toast.LENGTH_SHORT).show();
                                } else if(AutoService.getInstance().myself.get()) {
                                    Toast.makeText(context, "执行命令", Toast.LENGTH_SHORT).show();
                                    if(presenter.command.get() != null) {
                                        String cmd = presenter.command.get();
                                        Message set = new Message();
                                        set.what = 1;
                                        set.obj = cmd;
                                        Log.w(TAG, "set command = " + cmd);
                                        AutoService.getInstance().handler().sendMessage(set);
                                    }
                                    Message message = new Message();
                                    message.what = 100;
                                    AutoService.getInstance().handler().sendMessage(message);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Log.w(TAG, "start command: " + presenter.command.get());
                    Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);
                } else {
                    Toast.makeText(context, "5秒之内请勿频繁点击，手机受不了！", Toast.LENGTH_SHORT).show();
                }
            }

        });

        floatBall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.w(TAG, "onLongClick");
                Toast.makeText(context, "关闭进程3秒之后消失", Toast.LENGTH_SHORT).show();
                try {
                    Message message = new Message();
                    message.what = 200;
                    AutoService.getInstance().handler().sendMessage(message);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                doCancel();

                Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                vibrator.vibrate(1500);
                return true;
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.webview);
        warning = findViewById(R.id.tv_warning);
        background = findViewById(R.id.background);
        progressBar = findViewById(R.id.progressbar);
        presenter = new Presenter(this, MainActivity.this, webview);
        presenter.Log("请为「素朴网联」打开'无障碍'权限");
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        Log.w(TAG, "ACTION_ACCESSIBILITY_SETTINGS");
        loadWebview();

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            presenter.Log("你的手机系统版本低于7.0不能自动控制，我们提供了另外一套方案但是需要ROOT。");
        }
    }

    @Override
    public void loadWebview() {
        presenter.loadWebview();
    }

    @Override
    public void updateUI() {
        progressBar.setVisibility(View.GONE);
        background.setVisibility(View.GONE);
    }

    @Override
    public void showProgressbar() {
        warning.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDialog(String reason) {
        Log.w(TAG, "showDialog = "+ reason);
        if(presenter != null && presenter.ok.get()) {
            return;
        }

        final EditText inputServer = new EditText(MainActivity.this);
        inputServer.setGravity(Gravity.CENTER);
        TextView textView = new TextView(MainActivity.this);
        textView.setText("免责申明：\n    我们仅提供自动化操作，不针对任何APP\n\n如何获取激活码？\n    在微信公众号「素朴网联」输入：我要激活码");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(lp);
        layout.setOrientation(LinearLayout.VERTICAL);

        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        inputServer.setLayoutParams(vlp);
        textView.setLayoutParams(vlp);

        layout.addView(inputServer);
        layout.addView(textView);

        AlertDialog alertDialog = new AlertDialog.Builder(webview.getContext())
                .setMessage("请输入激活码")
                .setView(layout)
                .setCancelable(false)
                .setNegativeButton("我要激活码", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            presenter.handleCase(0, "请到微信关注公众号「素朴网联」输入：我要激活码");
                            Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                            startActivity(intent);
                        } catch (Exception e) {
                            presenter.Log("手机没有安装微信？");
                        }

                    }
                })
                .setPositiveButton("同意并激活", new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, int which) {
                        final String code = inputServer.getText().toString();
                        if (code == null || code.trim().length() < 1) {
                            presenter.handleCase(0, Key.invalid);
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                try {
                                    presenter.checkValidAndSetCommand(code);
                                    if(presenter.ok.get()) {
                                        showFloatBall();
                                        presenter.handleCase(2, "恭喜，激活成功");
                                        dialog.dismiss();
                                    } else {
                                        presenter.handleCase(0, Key.invalid);
                                    }

                                } catch (Exception e) {
                                    presenter.ok.set(false);
                                    presenter.handleCase(3,e.getMessage());
                                }
                                Looper.loop();
                            }
                        }).start();

                    }
                }).create();

        alertDialog.show();
    }

    @Override
    public void doCancel(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
                stopped.set(true);
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        //释放资源
        if (presenter != null) {
            presenter.destroy();
            presenter = null;
        }
        stopped.set(true);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent (Intent intent){
        setIntent(intent);
        Log.w(TAG, "on new intent");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1234);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1198);
            }

            if (PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1199);
            }

            if (PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.BIND_ACCESSIBILITY_SERVICE)) {
                requestPermissions(new String[]{Manifest.permission.BIND_ACCESSIBILITY_SERVICE}, 1200);
            }
        }

        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            final String todo = bundle.getString("todo");
            Log.w(TAG, "todo = " + todo);
            if(todo != null) {
                final AtomicBoolean shua = new AtomicBoolean();
                if(todo.contains("你要刷这个APP吗")) {
                    shua.set(true);
                }
                Log.w(TAG, todo);
                if(presenter == null || !presenter.ok.get()) {
                    Toast.makeText(context, "需要激活", Toast.LENGTH_SHORT).show();
                }
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage(todo)
                        .setCancelable(true)
                        .setNegativeButton("【 A 】", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Log.w(TAG, "【 A 】" + shua.get());
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                HTTP.report(presenter.getToken(), "NO:"+todo);
                                            } catch (Exception ex) {
                                                presenter.handleCase(3, "不能连接服务器：" + ex.getMessage());
                                            }

                                        }
                                    }).start();
                                    if(shua.get()) {
                                        String old = presenter.command.get() == null ? "" : presenter.command.get();
                                        String cmd = String.format(template[1], AutoService.getInstance().packages.get()) + old;
                                        presenter.command.set(cmd);
                                        Message set = new Message();
                                        set.what = 1;
                                        set.obj = cmd;
                                        Log.w(TAG, "set command = " + cmd);
                                        AutoService.getInstance().handler().sendMessage(set);
                                        Message start = new Message();
                                        start.what = 100;
                                        Log.w(TAG, "start command: " + presenter.command.get());
                                        AutoService.getInstance().handler().sendMessage(start);
                                    } else {
                                        Intent home = new Intent(Intent.ACTION_MAIN);
                                        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        home.addCategory(Intent.CATEGORY_HOME);
                                        startActivity(home);
                                        presenter.Log("现在去打开任何一个APP");
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "【 A 】error: " + shua.get());
                                    presenter.Log(e.getMessage());
                                }
                            }
                        })
                        .setPositiveButton("【 B 】", new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, int which) {
                                try {
                                    Log.w(TAG, "【 B 】" + shua.get());
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                HTTP.report(presenter.getToken(), "YES:"+todo);
                                            } catch (Exception ex) {
                                                presenter.handleCase(3, "不能连接服务器：" + ex.getMessage());
                                            }

                                        }
                                    }).start();
                                    if(shua.get()) {
                                        String old = presenter.command.get() == null ? "" : presenter.command.get();
                                        String cmd = String.format(template[0], AutoService.getInstance().packages.get()) + old;

                                        presenter.command.set(cmd);
                                        Message set = new Message();
                                        set.what = 1;
                                        set.obj = cmd;
                                        Log.w(TAG, "set command = " + cmd);
                                        AutoService.getInstance().handler().sendMessage(set);
                                        Message start = new Message();
                                        start.what = 100;
                                        Log.w(TAG, "start command: " + presenter.command.get());
                                        AutoService.getInstance().handler().sendMessage(start);
                                    } else {
                                        Intent home = new Intent(Intent.ACTION_MAIN);
                                        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        home.addCategory(Intent.CATEGORY_HOME);
                                        startActivity(home);
                                        presenter.Log("打开任何一个APP");
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "【 B 】error: " + shua.get());
                                    presenter.Log(e.getMessage());
                                }

                            }
                        }).create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showFloatBall();
                        Log.w(TAG, "onDismiss setVisibility VISIBLE");
                    }
                });
                dialog.show();
            }
        }
    }
}
