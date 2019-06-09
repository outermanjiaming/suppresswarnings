package com.suppresswarnings.android;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.suppresswarnings.android.model.HTTP;
import com.suppresswarnings.android.model.Key;
import com.suppresswarnings.android.presenter.Presenter;
import com.suppresswarnings.android.view.FloatingView;
import com.suppresswarnings.android.view.IView;
import com.suppresswarnings.android.view.MyWebview;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MainActivity extends Activity implements IView {
    AtomicBoolean stopped = new AtomicBoolean(false);
    MyWebview webview;
    TextView warning;
    LinearLayout background;
    ProgressBar progressBar;
    private ListView lv;
    private Adapter adapter;
    String TAG = "lijiaming";
    Presenter presenter;
    String templateA = "素朴网联home素朴网联jump,30素朴网联open,%s素朴网联sleep素朴网联sleep素朴网联sleep素朴网联loop,200素朴网联swipe1素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联while素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep";
    String templateB = "素朴网联home素朴网联jump,30素朴网联open,%s素朴网联sleep素朴网联sleep素朴网联sleep素朴网联left素朴网联left素朴网联sleep素朴网联loop,200素朴网联swipe0素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联click素朴网联sleep素朴网联sleep素朴网联sleep素朴网联loop,2素朴网联swipe0素朴网联sleep素朴网联swipe0素朴网联sleep素朴网联swipe1素朴网联sleep素朴网联swipe0素朴网联sleep素朴网联while素朴网联sleep素朴网联back素朴网联while素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep";
    String[] template = {templateA,templateB};
    MainActivity context;
    FloatingView floatBall;
    WindowManager windowManager;
    WindowManager.LayoutParams floatBallParams;
    AtomicLong clicked = new AtomicLong(0);

    public void showFloatBall() {
        if (!Settings.canDrawOverlays(context)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1234);
        }
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
                            if(AutoService.getInstance().crazy.get()) {
                                Log.w(TAG, "不要crazy点击自己");
                                return;
                            }
                            if(presenter == null || !presenter.ok.get()) {
                                Toast.makeText(context, "需要激活", Toast.LENGTH_SHORT).show();
                                showDialog("未激活");
                            } else {
                                if(AutoService.getInstance().running.compareAndSet(true, false)) {
                                    Toast.makeText(context, "停止执行", Toast.LENGTH_SHORT).show();
                                } else if(AutoService.getInstance().ready.get()) {
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
                                } else {
                                    presenter.Log("请打开无障碍权限");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Log.w(TAG, presenter.toString());
                    Log.w(TAG, presenter.command + "");
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
                Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                vibrator.vibrate(1500);
                Toast.makeText(context, "关闭进程3秒之后消失", Toast.LENGTH_SHORT).show();
                if(AutoService.getInstance() != null) {
                    try {
                        Message message = new Message();
                        message.what = 200;
                        AutoService.getInstance().handler().sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                doCancel();
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

        lv=findViewById(R.id.lv);
        adapter = new Adapter(context);
        Log.w(TAG, "adapter = " + adapter);

        adapter.addItem("悬浮球");
        adapter.addItem("无障碍");
        adapter.addItem("修改A模板");
        adapter.addItem("修改B模板");
        adapter.addItem("查看命令");
        adapter.addItem("选择APP");
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.w(TAG, i + "," + l + " = " + view);
                if(presenter == null || !presenter.ok.get()) {
                    showDialog("未激活");
                    Log.w(TAG, "未激活");
                    return;
                }
                final Switch aSwitch = view.findViewById(R.id.aSwitch);
                if(aSwitch.isChecked()){
                    aSwitch.setChecked(false);
                    Log.w(TAG, "aSwitch.setChecked(      );");
                    //进行业务处理
                    switch (i) {
                        case 0:
                            Log.w(TAG, "remove floatball");
                            windowManager.removeView(floatBall);
                            break;
                        case 1:
                            if(AutoService.getInstance() != null) {
                                try {
                                    Message message = new Message();
                                    message.what = 200;
                                    AutoService.getInstance().handler().sendMessage(message);
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        default:
                            presenter.Log("新功能还没有实现");
                            break;
                    }
                }else {
                    aSwitch.setChecked(true);
                    //进行业务处理
                    Log.w(TAG, "aSwitch.setChecked(true);");
                    switch (i) {

                        case 0:
                            showFloatBall();
                            break;

                        case 1:

                            if(AutoService.getInstance() == null) {
                                presenter.Log("请为「素朴网联」打开'无障碍'权限");
                                context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                                Log.w(TAG, "ACTION_ACCESSIBILITY_SETTINGS");
                            } else {
                                presenter.Log("打开选择APP了吗");
                            }

                            break;
                        case 2:
                            showEditView("修改命令模板A", template, 1, new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    aSwitch.setChecked(false);
                                }
                            });

                            break;
                        case 3:
                            showEditView("修改命令模板B", template, 0, new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    aSwitch.setChecked(false);
                                }
                            });

                            break;
                        case 4:
                            showTextView("查看当前命令", new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    aSwitch.setChecked(false);
                                }
                            });
                            break;
                        case 5:
                            if(AutoService.getInstance() == null) {
                                presenter.Log("打开无障碍权限了吗");
                            } else {
                                showSingleAlertDialog(new Consumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        aSwitch.setChecked(false);
                                    }
                                });
                            }
                            break;
                        default:
                            presenter.Log("新功能还没有实现");
                            break;
                    }
                }
            }
        });

    }

    @Override
    public void showProgressbar() {
        warning.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public List<Pair<String,String>> reloadButtons() {
        final PackageManager pm = context.getPackageManager();
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(30, 0x0002);
        int numTasks = recentTasks.size();
        Log.w(TAG, "numTasks = "+numTasks);
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
            Log.w(TAG, "baseActivity = "+info.baseActivity);
            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }
            intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);

            if (resolveInfo != null) {
                final ActivityInfo activityInfo = resolveInfo.activityInfo;
                final String title = activityInfo.loadLabel(pm).toString();
                Pair<String, String> one = new Pair<>(title, activityInfo.packageName);
                pairs.add(one);
            }
        }

        List<ApplicationInfo>  list = pm.getInstalledApplications(0);
        list.sort(new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo o1, ApplicationInfo o2) {
                String s1 = (String)o1.loadLabel(pm);
                String s2 = (String)o2.loadLabel(pm);
                return firstPinYin(s2).toUpperCase().compareTo(firstPinYin(s1).toUpperCase());
            }
        });
        for(ApplicationInfo ai : list) {
            String title = (String)ai.loadLabel(pm);
            Pair<String, String> one = new Pair<>(firstPinYin(title) + "   " + title, ai.packageName);
            if(!pairs.contains(one)) {
                pairs.add(one);
            }
        }
        return pairs;
    }

    public String firstPinYin(String input) {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        char c = input.charAt(0);
        String[] pinyinArray = null;
        try {
            pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
        } catch (Exception e){

        }

        if (pinyinArray != null) {
            return "" + pinyinArray[0].charAt(0);
        }

        return "" + c;
    }

    public void showSingleAlertDialog(final Consumer<String> callback) {
        final List<Pair<String,String>> items = reloadButtons();

        final String[] apps = new String[items.size()];
        for(int i = 0;i<apps.length;i++) {
            try {
                Pair<String,String> info = items.get(i);
                apps[i] = info.first;
            } catch (Exception e) {
                Toast.makeText(context, "异常获取APP", Toast.LENGTH_SHORT).show();
            }
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择APP");
        final AtomicInteger chose = new AtomicInteger();
        alertBuilder.setSingleChoiceItems(apps, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(context, apps[i], Toast.LENGTH_SHORT).show();
                chose.set(i);
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Pair<String, String> info = items.get(chose.get());
                shua(info.first, info.second);
                callback.accept("ok");
            }
        });
        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.Log("未选择APP");
                callback.accept("no");
            }
        });
        alertBuilder.create().show();

    }

    public void showEditView(String message, final String[] ref, final int index, final Consumer<String> callback) {
        final EditText inputServer = new EditText(context);
        inputServer.setGravity(Gravity.CENTER);
        final String old = ref[index];
        TextView textView = new TextView(context);
        textView.setText(old);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(old);
                    Toast.makeText(context, "复制成功。", Toast.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromInputMethod(context.getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    Log.w(TAG, "不能复制");
                }

            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(lp);
        layout.setOrientation(LinearLayout.VERTICAL);

        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                400);

        inputServer.setHint("请输入新的命令模板（提示：%s代表某个APP的包名）");
        inputServer.setLayoutParams(vlp);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setLayoutParams(vlp);
        layout.addView(textView);
        layout.addView(inputServer);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setView(layout)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.w(TAG, "命令没修改");
                        callback.accept(old);
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, int which) {
                        String now = inputServer.getText().toString().trim();
                        ref[index] = now;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            callback.accept(now);
                        }
                    }
                }).create();

        alertDialog.show();
    }

    public void showTextView(String message, final Consumer<String> callback) {
        final EditText inputServer = new EditText(context);
        inputServer.setGravity(Gravity.CENTER);
        TextView textView = new TextView(context);
        final String cmd = presenter == null ? "" : presenter.command.get();
        textView.setText(cmd);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(cmd);
                    Toast.makeText(context, "复制成功，可以分享给朋友。", Toast.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromInputMethod(context.getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    Log.w(TAG, "不能复制");
                }

            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(lp);
        layout.setOrientation(LinearLayout.VERTICAL);

        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                400);
        inputServer.setHint("请输入新的命令");
        inputServer.setLayoutParams(vlp);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setLayoutParams(vlp);
        layout.addView(textView);
        layout.addView(inputServer);
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setView(layout)
                .setCancelable(false)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.w(TAG, "查看 取消");
                        callback.accept("cancel");
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, int which) {
                        Log.w(TAG, "查看 确定");
                        String now = inputServer.getText().toString().trim();
                        if(presenter != null) {
                            presenter.command.set(now);
                        }
                        callback.accept("ok");
                    }
                }).create();

        alertDialog.show();
    }

    public void shua(String app, final String p) {
            if(presenter == null || !presenter.ok.get()) {
                Toast.makeText(context, "需要激活", Toast.LENGTH_SHORT).show();
            }
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("确认刷这个APP吗？" + app + "(" + p+")")
                    .setCancelable(true)
                    .setNegativeButton("【阅读】", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            HTTP.report(presenter.getToken(), "NO:shua:" + p);
                                        } catch (Exception ex) {
                                            presenter.handleCase(3, "不能连接服务器：" + ex.getMessage());
                                        }

                                    }
                                }).start();

                                String old = presenter.command.get() == null ? "" : presenter.command.get();
                                String cmd = String.format(template[1], p) + old;
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
                            } catch (Exception e) {
                                presenter.Log(e.getMessage());
                            }
                        }
                    })
                    .setPositiveButton("【观看】", new DialogInterface.OnClickListener() {

                        public void onClick(final DialogInterface dialog, int which) {
                            try {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            HTTP.report(presenter.getToken(), "YES:shua:"+p);
                                        } catch (Exception ex) {
                                            presenter.handleCase(3, "不能连接服务器：" + ex.getMessage());
                                        }

                                    }
                                }).start();
                                String old = presenter.command.get() == null ? "" : presenter.command.get();
                                String cmd = String.format(template[0], p) + old;

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
                            } catch (Exception e) {
                                presenter.Log(e.getMessage());
                            }

                        }
                    }).create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.w(TAG, "onDismiss setVisibility VISIBLE");
                }
            });
            dialog.show();
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

        AlertDialog alertDialog = new AlertDialog.Builder(context)
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

        //释放资源
        if (presenter != null) {
            presenter.destroy();
            presenter = null;
        }

        windowManager.removeView(floatBall);
        floatBall = null;

        stopped.set(true);
        finish();

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                throw new RuntimeException("暴力停止");
//            }
//        }, 3000);
    }

    @Override
    protected void onDestroy() {
        stopped.set(true);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent){
        setIntent(intent);
        Log.w(TAG, "on new intent");
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            final String todo = bundle.getString("todo");
            Log.w(TAG, "todo = " + todo);
            if(todo != null) {
                presenter.Log(todo);
            } else {
                presenter.Log("欢迎回到「素朴网联」");
            }

        }
    }
}
