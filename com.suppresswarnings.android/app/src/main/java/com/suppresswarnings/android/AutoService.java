package com.suppresswarnings.android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.suppresswarnings.android.model.Actions;
import com.suppresswarnings.android.model.Key;
import com.suppresswarnings.android.utils.ExeCommand;
import com.suppresswarnings.android.utils.HTTPUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AutoService extends AccessibilityService {
    public static final String TAG = "lijiaming";
    public AtomicBoolean running = new AtomicBoolean(false);
    private AtomicInteger jump = new AtomicInteger(3);
    private AtomicReference<String[]> acommands = new AtomicReference<String[]>();
    private AtomicReference<Stack<Loop>> loop = new AtomicReference<>();
    private Handler handler;
    private static AutoService INSTANCE =  null;
    private boolean open = false;

    class Loop {
        int times;
        int index;
        Loop(int times, int index) {
            this.times = times;
            this.index = index;
        }

        public int loop(int next) {
            try {
                this.times --;
                if(this.times > 0) {
                    return index;
                } else {
                    Stack<Loop> push = loop.get();
                    if(push == null) {
                        Log.w(TAG, "最后一个堆栈: " + next);
                    } else {
                        if(!push.empty()) {
                            push.pop();
                        }
                    }
                    return next;
                }
            } catch (Exception e) {
                Log.w(TAG, "loop error : " + e.getMessage());
                return next;
            }
        }

    }
    String[] commands = "素朴网联sleep素朴网联swipe0素朴网联left素朴网联right素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep".split("素朴网联");

    public Handler handler(){
        if(handler == null) {
            return new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    Log.w(TAG, "handler is null: " );
                    Toast.makeText(getApplicationContext(), "还未开启无障碍权限", Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }
        return handler;
    }

    public static AutoService getInstance() {
        return INSTANCE;
    }

    public void finish() {
        acommands.set(null);
        running.set(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf();
        }
        INSTANCE = null;
    }

    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    private boolean checkAccessibilityEnabled(String serviceName) {
        AccessibilityManager mAccessibilityManager = (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前往开启辅助服务界面
     */
    public void goAccess() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = performGlobalAction(GLOBAL_ACTION_BACK);
        Log.w(TAG, "GLOBAL_ACTION_BACK = " + result);
        wait1sec();
    }

    public void performHomeClick() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean result = performGlobalAction(GLOBAL_ACTION_HOME);
        Log.w(TAG, "GLOBAL_ACTION_HOME = " + result);
        wait1sec();
    }

    /**
     * 模拟下滑操作
     */
    public void performScrollBackward() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int start = 600;
        int stop = 400;
        int x = width / 2;
        path.moveTo(x, start);
        pathS(path, start, stop, x);
        Log.w(TAG, "performScrollBackward");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 800)).build();
            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performScrollBackward onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performScrollBackward onCancelled");
                }
            }, null);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }

    public void pathS(Path path, int start, int stop, int x) {
        int length = stop - start;
        int steps = 5;
        int step = length / steps;
        Random random = new Random();
        for(int i=0;i<steps; i++) {
            int r = x + 10 + random.nextInt(50);
            start = start + step;
            path.lineTo(r, start);
        }
    }

    public void pathN(Path path, int start, int stop, int y) {
        int length = stop - start;
        int steps = 5;
        int step = length / steps;
        Random random = new Random();
        for(int i=0;i<steps; i++) {
            int r = y + 10 + random.nextInt(50);
            start = start + step;
            path.lineTo(start, r);
        }
    }

    /**
     * 模拟上滑操作
     */
    public void performScrollForward() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int start = 400;
        int stop = 600;
        int x = width / 2;
        path.moveTo(x, start);
        pathS(path, start, stop, x);
        Log.w(TAG, "performScrollForward");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 800)).build();
            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performScrollForward onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performScrollForward onCancelled");
                }
            }, null);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }

    /**
     * 查找对应文本的View
     *
     * @param text text
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        return findViewByText(text, false);
    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @param clickable 该View是否可以点击
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AccessibilityNodeInfo findViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    public void clickTextViewByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void clickTextViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    public void performClick(String x, String y) {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int xx = Math.min(Integer.parseInt(x), width);
        int yy = Math.min(Integer.parseInt(y), height);
        path.moveTo(xx, yy);
        Log.w(TAG, "performClick");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 20)).build();
            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performClick onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performClick onCancelled");
                }
            }, null);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }

    public void performSwipe0() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int start = height - 400;
        int stop = 400;
        int x = width / 2;
        path.moveTo(x, start);
        pathS(path, start, stop, x);
        Log.w(TAG, "performSwipe0");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 800)).build();
            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performSwipe0 onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performSwipe0 onCancelled");
                }
            }, null);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }


    public void performSwipe1() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int stop = height - 400;
        int start = 400;
        int x = width / 2;
        path.moveTo(x, start);
        pathS(path, start, stop, x);
        Log.w(TAG, "performSwipe1");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 800)).build();

            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performSwipe1 onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performSwipe1 onCancelled");
                }
            }, handler);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }


    public void performLeft() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int start = width - 100;
        int stop = 10;
        int y = height / 2;
        path.moveTo(start, y);
        pathN(path, start, stop, y);
        Log.w(TAG, "performLeft");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 800)).build();
            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performLeft onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performLeft onCancelled");
                }
            }, null);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }

    public void shareAppShop(String packageName) {
        try {
            Uri uri = Uri.parse("market://details?id="+ packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "你自己去应用市场下载对应的APP");
        }
    }

    public void performRight() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Path path = new Path();
        int stop = width - 10;
        int start = 100;
        int y = height / 2;
        path.moveTo(start, y);
        pathN(path, start, stop, y);
        Log.w(TAG, "performRight");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 100, 800)).build();
            boolean result = dispatchGesture(gd, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.w(TAG, "performRight onCompleted");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.w(TAG, "performRight onCancelled");
                }
            }, null);

            Log.w(TAG, "dispatch result = " + result);
        } else {
            Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
        }
        wait1sec();
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "onInterrupt");
        running.set(false);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.w(TAG, "1/2. onServiceConnected = " + this);
        AutoService.INSTANCE = this;
        Log.w(TAG, "2/2. onServiceConnected = " + getInstance());
        Toast.makeText(getApplicationContext(), "准备好了，可以点击图标执行命令了", Toast.LENGTH_LONG).show();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.w(TAG, "doMessage = " + msg.toString());
                switch (msg.what) {
                    case 0:
                        wait1sec();
                        break;
                    case 1:
                        String input = (String) msg.obj;
                        Log.w(TAG,  "收到命令 input = " + input);
                        if (input != null && input.length() > 2) {
                            acommands.set(input.split("素朴网联"));
                        } else {
                            acommands.set(commands);
                        }
                        break;
                    case 100:
                        running.set(true);
                        action();
                        break;
                    case 200:
                        running.set(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            disableSelf();
                        }
                        break;
                    case 1000:
                    case 1001:
                        doMessage(msg, 0);
                        break;
                    default:
                        break;
                }

                return false;
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(getInstance() != null) {
                    backHome("服务连接");
                }
            }
        }, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean openActivity(String who, String where) {
        try {
            Log.w(TAG, "打开APP：" + who);
            wait1sec();
            Intent intent = getPackageManager().getLaunchIntentForPackage(who);
            startActivity(intent);
            wait1sec();
            wait1sec();
            wait1sec();
            return true;
        } catch(Exception e) {
            Log.w(TAG, "打开APP异常：" + e.getMessage());
//            shareAppShop(who);
//            for(int i=70;i>0;i--) {
//                wait1sec();
//            }
            return false;
        }
    }


    public void action() {
        if(acommands.get() == null) {
            acommands.set(commands);
            Log.w(TAG, "采用默认命令");
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (getInstance().running.get()) {
                        String[] cmds = acommands.get();
                        if(!open) {
                            List<String> opens = new ArrayList<>();
                            for(String c : cmds) {
                                if(c.toLowerCase().contains("open")) {
                                    opens.add(c);
                                }
                            }

                            open = true;
                            for (String open : opens) {
                                Log.w(TAG, "打开APP：" + open);
                                Actions action = HTTPUtil.actions(open);
                                if (action != null) {
                                    try {
                                        Log.w(TAG, "执行命令：" + open);
                                        Message next = new Message();
                                        next.what = action.getActionType().what();
                                        next.obj = action.getInput();
                                        doMessage(next, 0);
                                    } catch (Exception e) {
                                        Log.w(TAG, "异常执行：" + open);
                                    }

                                } else {
                                    Log.w(TAG, "忽略未知命令：" + open);
                                }
                            }
                        }


                        int index = 0;
                        int last = 0;
                        for (;index<cmds.length;) {
                            if(last == index) index = last + 1;
                            if(index > cmds.length - 1) {
                                Log.w(TAG, "最后一条指令:" + index);
                                break;
                            }
                            final String cmd = cmds[index];
                            last = index;
                            Log.w(TAG, "执行命令：" + cmd + " index: " + index);
                            if(cmd == null || cmd.length() < 1) {
                                continue;
                            }

                            if (!getInstance().running.get()) {
                                Log.w(TAG, "stop = " + running.get());
                                break;
                            }

                            try {
                                Thread.sleep(300);
                            } catch (Exception e) {

                            }

                            Actions action = HTTPUtil.actions(cmd);
                            if (action != null) {
                                try {
                                    Message next = new Message();
                                    next.what = action.getActionType().what();
                                    next.obj = action.getInput();
                                    index = doMessage(next, index);
                                    Log.w(TAG, "执行命令：" + cmd + " 下一条: " + index);
                                } catch (Exception e) {
                                    Log.w(TAG, "异常执行：" + cmd + " index: " + index);
                                }

                            } else {
                                Log.w(TAG, "忽略未知命令：" + cmd);
                            }
                        }

                        Log.w(TAG, getInstance().running.get()?"在3秒之内你可以点击图标停止执行":Key.stopped);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (getInstance().running.get()) {
                                    Log.w(TAG, "「素朴网联」循环执行");
                                    Message next = new Message();
                                    next.setAsynchronous(false);
                                    next.what = 100;
                                    handler().sendMessage(next);
                                } else {
                                    backHome("已经停止，回到「素朴网联」" + this);
                                }
                            }
                        }, 3000);


                    } else {
                        backHome("停止执行");
                    }
                } catch (Exception e) {
                    backHome("异常执行，回到「素朴网联」" + e.getMessage());
                }
            }
        },800);
    }

    public  void backHome(String reason){
        Log.w(TAG, "backHome reason = " + reason);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        wait1sec();
        wait1sec();
        wait1sec();
    }
    public int doMessage(Message msg, int index) {

        switch (msg.what) {
            case 0:
                wait1sec();
                break;
            case 404:
                Log.w(TAG, "404 Not Found");
                break;
            case 500:
                performHomeClick();
                break;
            case 501:
                performBackClick();
                break;
            case 502:
                performSwipe0();
                break;
            case 503:
                performSwipe1();
                break;
            case 504:
                performScrollForward();
                break;
            case 505:
                performScrollBackward();
                break;
            case 506:
                String xy = (String) msg.obj;
                String[] args = xy.split("\\s+");
                String x = args[0];
                String y = args[1];
                performClick(x, y);
                break;
            case 507:
                String ww = (String) msg.obj;
                args = ww.split("/");
                String who = args[0];
                String where = args[1];
                boolean open = openActivity(who, where);
                if(!open) {
                    return index + jump.get();
                }
                wait1sec();
                wait1sec();
                wait1sec();
                break;
            case 508:
                performLeft();
                break;
            case 509:
                performRight();
                break;

            case 601:
                String times = (String) msg.obj;
                times = times.replace("}", "");
                int t = Integer.parseInt(times);
                Stack<Loop> push = loop.get();
                if(push == null) {
                    push = new Stack<>();
                    loop.set(push);
                }
                push.push(new Loop(t, index + 1));
                Log.w(TAG, "got Loop cmd and set loop at: " + index);
                break;
            case 602:
                Stack<Loop> lop = loop.get();
                if(lop != null) {
                    Loop peek = lop.peek();
                    if(peek == null) {
                        Log.w(TAG, "got WHILE cmd (peek == null) set loop to: " + (index + 1));
                        return index + 1;
                    }
                    int next = peek.loop(index + 1);
                    Log.w(TAG, "got WHILE cmd set loop to: " + next);
                    return next;
                } else {
                    Log.w(TAG, "got WHILE cmd but loop was not set");
                    break;
                }
            case 1000:
                String cmd = (String) msg.obj;
                update(cmd);
                break;
            case 1001:
                String ver = (String) msg.obj;
                remove(ver);
                break;
            case 1002:
                String jmp = (String) msg.obj;
                int var = Integer.parseInt(jmp);
                jump.set(var);
                break;
            case 1003:
                String text = (String) msg.obj;
                clickTextViewByText(text);
                break;
            case 1004:
                String paste = (String) msg.obj;
                String[] params = paste.split("/");
                if(params.length > 1) {
                    inputText(findViewByID(params[0]), params[1]);
                }
                break;
            default:
                break;
        }
        return index + 1;
    }

    public void wait1sec() {
        if(!running.get()) {
            Log.w(TAG, "「素朴网联」停止执行");
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.w(TAG, "wait 1 sec end");
    }

    public void update(String cmd) {
        if(cmd.contains("UPDATE") ) {
            cmd = cmd.substring("UPDATE".length());
        }
        Log.w(TAG, "cmd: " + cmd);
        String str3 = new ExeCommand(false).run(cmd, 0).getResult();
        Log.w(TAG, "str3: " + str3);
    }

    public void remove(String ver) {
        int version = Integer.parseInt(ver);
        try {
            int now = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            if(version == now) {
                return;
            }

            uninstallApp(getPackageName());
        } catch (Exception e) {
            Log.w(TAG, "NameNotFoundException");
        }
    }

    public void uninstallApp(String pageName){
        Intent uninstallIntent = new Intent();
        uninstallIntent.setAction(Intent.ACTION_DELETE);
        uninstallIntent.setData(Uri.parse("package:"+pageName));
        startActivity(uninstallIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy");
        running.set(false);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.w(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public String toString() {
        return "AutoService{ running = " + running.get() + ", handler=" + handler + '}';
    }
}
