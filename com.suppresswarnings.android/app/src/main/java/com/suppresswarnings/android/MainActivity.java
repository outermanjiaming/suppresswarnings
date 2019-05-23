package com.suppresswarnings.android;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
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
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.suppresswarnings.android.model.Key;
import com.suppresswarnings.android.presenter.Presenter;
import com.suppresswarnings.android.view.FloatingView;
import com.suppresswarnings.android.view.IView;
import com.suppresswarnings.android.view.MyWebview;

import java.util.List;
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

    Presenter presenter;

    MainActivity context;
    static FloatingView floatBall;
    WindowManager windowManager;
    AtomicLong clicked = new AtomicLong(0);
    WindowManager.LayoutParams floatBallParams;
    AlertDialog alertDialog;

    public void showFloatBall() {
        if(!presenter.ok.get() || floatBall != null) {
            return;
        }

        floatBall = new FloatingView(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (floatBallParams == null) {
            floatBallParams = new WindowManager.LayoutParams();
            floatBallParams.width = floatBall.width;
            floatBallParams.height = floatBall.height;
            floatBallParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            Log.w("lijiaming", "Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                floatBallParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                floatBallParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatBallParams.format = PixelFormat.RGBA_8888;
        }

        windowManager.addView(floatBall, floatBallParams);

        floatBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(System.currentTimeMillis() - clicked.get() > 5000) {
                    clicked.set(System.currentTimeMillis());

                    AccessibilityManager mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
                    List<AccessibilityServiceInfo> accessibilityServices = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
                    Log.w("lijiaming", "accessibilityServices=" + accessibilityServices);
                    for (AccessibilityServiceInfo info : accessibilityServices) {
                        Log.w("lijiaming", "AccessibilityServiceInfo = " + info.getId());
                    }

                    if (AutoService.getInstance() == null) {
                        presenter.Log("请为「素朴网联」打开'无障碍'权限");
                        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        Log.w("lijiaming", "ACTION_ACCESSIBILITY_SETTINGS");
                    } else {
                        if (AutoService.getInstance().running.get()) {
                            Toast.makeText(context, "停止执行", Toast.LENGTH_SHORT).show();
                            AutoService.getInstance().running.set(false);
                            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                            vibrator.vibrate(400);
                        } else {
                            if(presenter.command.get() != null) {
                                String cmd = presenter.command.get();
                                Message message = new Message();
                                if(cmd.startsWith("UPDATE")) {
                                    message.obj = cmd;
                                    message.what = 1000;
                                } else {
                                    message.what = 1;
                                    message.obj = cmd;
                                }
                                Log.w("lijiaming", "set command");
                                AutoService.getInstance().handler().sendMessage(message);
                            }
                            Toast.makeText(context, "执行命令", Toast.LENGTH_SHORT).show();
                            Message message = new Message();
                            message.what = 100;
                            Log.w("lijiaming", "start command");
                            AutoService.getInstance().handler().sendMessage(message);
                            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                            vibrator.vibrate(200);
                        }
                    }
                } else {
                    Toast.makeText(context, "5秒之内请勿频繁点击，手机受不了！", Toast.LENGTH_SHORT).show();
                }
            }

        });

        floatBall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.w("lijiaming", "onLongClick");
                Toast.makeText(context, "关闭进程3秒之后消失", Toast.LENGTH_SHORT).show();
                if(AutoService.getInstance() != null) {
                    AutoService.getInstance().running.set(false);
                    Message message = new Message();
                    message.what = 200;
                    Log.w("lijiaming", "disable");
                    AutoService.getInstance().handler().sendMessage(message);
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(AutoService.getInstance() != null) {
                            AutoService.getInstance().finish();
                        }
                        windowManager.removeView(floatBall);
                        floatBall = null;
                    }
                }, 3000);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
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

        presenter = new Presenter(this, MainActivity.this, webview);
        loadWebview();
    }

    @Override
    public void loadWebview() {
        presenter.loadWebview();
    }

    @Override
    public void updateUI() {
        progressBar.setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        showFloatBall();
    }

    @Override
    public void showProgressbar() {
        warning.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDialog(String reason) {
        Log.w("lijiaming", "showDialog = "+ reason);
        if(presenter != null && presenter.ok.get()) {
            presenter.Log("检验合格");
            return;
        }

        if(alertDialog == null) {
            final EditText inputServer = new EditText(MainActivity.this);
            inputServer.setGravity(Gravity.CENTER);
            TextView textView = new TextView(MainActivity.this);
            textView.setText("(免责申明：「素朴网联」仅为内部人员提供自动化操作，请勿擅自传播并用于商业行为。)");

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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

            alertDialog = new AlertDialog.Builder(webview.getContext())
                .setTitle("请输入激活码")
                .setMessage("如何获取激活码？在微信公众号「素朴网联」输入：我要激活码")
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
        }

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
}
