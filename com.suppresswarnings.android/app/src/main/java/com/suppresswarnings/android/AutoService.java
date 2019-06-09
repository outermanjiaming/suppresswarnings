package com.suppresswarnings.android;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.media.Ringtone;
import android.media.RingtoneManager;
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

import com.suppresswarnings.android.model.Actions;
import com.suppresswarnings.android.model.HTTP;

import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AutoService extends AccessibilityService {
    public static final String TAG = "lijiaming";
    public AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());
    public AtomicBoolean running = new AtomicBoolean(false);
    public AtomicBoolean pause = new AtomicBoolean(false);
    public AtomicBoolean myself = new AtomicBoolean(true);
    public AtomicBoolean ready  = new AtomicBoolean(false);
    private AtomicBoolean lock = new AtomicBoolean(false);
    private AtomicInteger jump = new AtomicInteger(3);
    private AtomicInteger ignore = new AtomicInteger(-1);
    private AtomicInteger afterOpen = new AtomicInteger(-1);
    private AtomicInteger tooLong = new AtomicInteger(0);
    private AtomicInteger increment = new AtomicInteger(1000);
    private AtomicInteger otherCount = new AtomicInteger(0);
    private AtomicInteger checkTimes = new AtomicInteger(0);
    private AtomicReference<String> currActivity = new AtomicReference();
    private AtomicReference<String> current = new AtomicReference<String>();
    private AtomicReference<String[]> acommands = new AtomicReference<String[]>();
    private AtomicReference<Stack<Loop>> loop = new AtomicReference<>();
    private AtomicReference<StopRunnable> lastRunnable = new AtomicReference<StopRunnable>();
    private AtomicReference<AccessibilityNodeInfo> lastWindow = new AtomicReference();
    private String activity = null;
    private Handler handler;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private static AutoService INSTANCE =  null;
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
                testNotify("Exception", "loop error: " + e.getMessage());
                playRingtone();
                return next;
            }
        }

    }
    String[] commands = "素朴网联sleep素朴网联swipe0素朴网联left素朴网联right素朴网联sleep素朴网联sleep素朴网联info,这是一条通知。素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep素朴网联sleep".split("素朴网联");

    public Handler handler(){
        if(handler == null) {
            return new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    Log.w(TAG, "handler is null: " );
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
        Log.w(TAG, "stop the world");
        playRingtone();
        playRingtone();
        playRingtone();
        acommands.set(null);
        running.set(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf();
        }
        INSTANCE = null;
        this.stopSelf();
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
            wait1sec();
            Log.w(TAG, "performBackClick = " + current.get() + " : " + activity);
            ComponentName cn = new ComponentName(current.get(),activity) ;
            Intent intent = new Intent() ;
            intent.setComponent(cn) ;
            startActivity(intent);
            TimeUnit.SECONDS.sleep(1);
        } catch(Exception e) {
            Log.w(TAG, "打开APP异常：" + e.getMessage());
            testNotify("Exception", "try to openActivity(), performBackClick: "+ e.getMessage());
            playRingtone();
            try {
                Thread.sleep(500);
            } catch (Exception ex) {
                e.printStackTrace();
            }
            boolean result = performGlobalAction(GLOBAL_ACTION_BACK);
            Log.w(TAG, "GLOBAL_ACTION_BACK = " + result);
            if(!result){
                openActivity(current.get());
            }
        }
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
    public AtomicBoolean crazy = new AtomicBoolean(false);

    //TODO crazy
    public void crazyClick() {
        if(mad.get()) {

            return;
        }
        mad.set(true);
        try {
            AccessibilityNodeInfo window = lastWindow.get();
            if(window != null) {
                int count = window.getChildCount();
                if(count <=0) {
                    return;
                }
                for(int i = 0; i<count; i++) {
                    AccessibilityNodeInfo childNode = window.getChild(i);
                    Log.w(TAG, "first : " + i);
                    if(childNode == null) continue;
                    if(childNode.isClickable()) {
                        childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Log.w(TAG, "childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);");
                    }

                    int count_1 = childNode.getChildCount();
                    for(int j = 0; j<count_1; j++) {
                        AccessibilityNodeInfo childNode1 = childNode.getChild(j);
                        Log.w(TAG, "second : " + j);
                        if(childNode1 == null) continue;
                        if(childNode1.isClickable()) {
                            childNode1.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.w(TAG, "childNode1.performAction(AccessibilityNodeInfo.ACTION_CLICK);");
                        }

                        int count_2 = childNode1.getChildCount();
                        for(int k = 0; k<count_2; k++) {
                            AccessibilityNodeInfo childNode2 = childNode1.getChild(k);
                            Log.w(TAG, "third : " + k);
                            if(childNode2 == null) continue;
                            if(childNode2.isClickable()) {
                                childNode2.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Log.w(TAG, "childNode2.performAction(AccessibilityNodeInfo.ACTION_CLICK);");
                            }
                        }
                    }
                }
            }

            crazyClick1();
            mad.set(false);
        } catch (Exception e) {
            mad.set(false);
            crazy.set(false);
            Log.w(TAG, "crazy error");
            testNotify("Exception", "crazy error: " + e.getMessage());
            playRingtone();
        }
    }

    AtomicBoolean mad = new AtomicBoolean(false);
    public void crazyClick1() {
        if(mad.get()) return;
        try {
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            Random random = new Random();
            int range = 250;
            int x = 50;
            int y = 100;
            while(crazy.get()) {
                int rand = random.nextInt(range);
                mad.set(true);
                if(x >= width) {
                    x = 0;
                    y = y + rand;
                    Log.w(TAG, "crazyClick x >= width");
                }
                if(x >= width && y >= height) {
                    crazy.set(false);
                    Log.w(TAG, "crazyClick x >= width && y >= height");
                }

                if(!crazy.get()) {
                    Log.w(TAG, "crazyClick !crazy.get()");
                    break;
                }

                Path path = new Path();
                path.moveTo(x, y);
                x = x + rand;
                Log.w(TAG, "\n\t\t\tcrazyClick (" + x + "," + y + ")\n");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    GestureDescription.Builder builder = new GestureDescription.Builder();
                    GestureDescription gd = builder.addStroke(new GestureDescription.StrokeDescription(path, 10, 20)).build();
                    boolean result = dispatchGesture(gd, new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                            Log.w(TAG, "crazyClick onCompleted");
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            Log.w(TAG, "crazyClick onCancelled");
                        }
                    }, null);

                    Log.w(TAG, "dispatch result = " + result);
                } else {
                    Log.w(TAG, "VERSION.SDK_INT = " + android.os.Build.VERSION.SDK_INT);
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(888 + rand);
                    Log.w(TAG, "休息毫秒:");
                } catch (Exception e) {
                    Log.w(TAG, "失眠严重");
                }
            }
            try {
                TimeUnit.SECONDS.sleep(10);
                Log.w(TAG, "too crazy sleep 10s");
            } catch (Exception e) {
                Log.w(TAG, "can't fall asleep");
            }
            mad.set(false);
        } catch (Exception e) {
            mad.set(false);
            crazy.set(false);
            Log.w(TAG, "全屏幕疯狂点击失败，人生重来咯");
            testNotify("Exception", "crazy click error: " + e.getMessage());
            playRingtone();
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

    public void checkZombie(AtomicBoolean crazy, AtomicBoolean running, AtomicBoolean pause, AtomicBoolean ready, AtomicInteger count, AtomicLong lastTime) {
        if(!ready.get()) {
            crazy.set(false);
            Log.w(TAG, "calm: !ready.get()");
            return;
        }
        if(!running.get()) {
            crazy.set(false);
            Log.w(TAG, "calm: !running.get()");
            return;
        }
        if(running.get() && pause.get()) {
            crazy.set(false);
            Log.w(TAG, "calm: running.get() && pause.get()");
            return;
        }
        if(count.get() > 10 && System.currentTimeMillis() - lastTime.get() > TimeUnit.SECONDS.toMillis(10)) {
            count.set(0);
            crazy.set(true);
            Log.w(TAG, "crazy reason: count.get() > 10 && System.currentTimeMillis() - lastTime.get() > TimeUnit.SECONDS.toMillis(30)");
            return;
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        lastTime.set(System.currentTimeMillis());
        ready.set(true);
        myself.set(getPackageName().equals(event.getPackageName()));
        Log.d(TAG, "event:" + event.getPackageName() + ": " + event.getClassName());
        if(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()){
            String act = ""+event.getClassName();
            if(event.getPackageName().equals(current.get())){
                currActivity.set(act);
                testNotify(""+event.getPackageName(),  act);
            }
        }
        if(current.get() != null && current.get().equals(event.getPackageName())) {
            crazy.set(false);
            checkTimes.set(0);
        }

        if(running.get()) {
            if(current.get() != null && !current.get().equals(event.getPackageName())) {
                otherCount.incrementAndGet();
                if(otherCount.get() > 20) {
                    otherCount.set(0);
                    action();
                    Log.w(TAG, "程序为什么不工作？");
                }
            }
        }

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            lastWindow.set(getRootInActiveWindow());
        }

        Log.w(TAG, "\t\t\t" + event.toString());

        checkZombie(crazy, running, pause, ready, otherCount, lastTime);
        if(crazy.get()) {
            crazyClick();
        }
    }


    Runnable checkNoEvent = new Runnable() {
        @Override
        public void run() {
            Log.w(TAG, "checkNoEvent = " + checkTimes.incrementAndGet());
            if(checkTimes.get() > 10 && System.currentTimeMillis() - lastTime.get() > TimeUnit.SECONDS.toMillis(30)) {
                crazy.set(true);
                checkTimes.set(0);
                Log.w(TAG, "checkNoEvent reason: System.currentTimeMillis() - lastTime.get() > TimeUnit.SECONDS.toMillis(30) = " + checkTimes.incrementAndGet());
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        try {
            Log.w(TAG, "1/2. onServiceConnected = " + this);
            AutoService.INSTANCE = this;
            Log.w(TAG, "2/2. onServiceConnected = " + getInstance());
            ready.set(true);
            service.scheduleWithFixedDelay(checkNoEvent, 10,10, TimeUnit.SECONDS);
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
                            ignore.set(-1);
                            if (input != null && input.length() > 2) {
                                acommands.set(input.split("素朴网联"));
                            } else {
                                acommands.set(commands);
                            }
                            break;
                        case 100:
                            action();
                            break;
                        case 200:
                            finish();
                            break;
                        default:
                            break;
                    }

                    return false;
                }
            });
            Log.w(TAG, "schedule service connected");
            backHome("请打开任何一个APP");
        } catch (Exception e) {
            Log.w(TAG, "error");
            testNotify("Exception", "connected error: " + e.getMessage());
            playRingtone();
        }
    }

    public boolean openActivity(String who) {
        try {
            Log.w(TAG, "打开APP：" + who);
            wait1sec();
            Intent intent = getPackageManager().getLaunchIntentForPackage(who);
            startActivity(intent);
            current.set(who);
            TimeUnit.SECONDS.sleep(15);
            return true;
        } catch(Exception e) {
            Log.w(TAG, "打开APP异常：" + e.getMessage());
            testNotify("Exception", "openActivity error: " + e.getMessage());
            playRingtone();
            return false;
        }
    }

    public void action() {
        try{
            if(lock.get() && running.get()) {
                Log.w(TAG, "action() is locked");
                testNotify("lock.get()", "action() is locked");
                return;
            }
            lock.set(true);
            ready.set(true);
            running.set(true);
            pause.set(false);
            loop.set(null);
            jump.set(0);
            ignore.set(-1);
            activity = null;
            playRingtone();
            if(acommands.get() == null) {
                acommands.set(commands);
                Log.w(TAG, "采用默认命令");
            } else {
                Log.w(TAG, "采用新命令");
            }
            wait1sec();
            if (getInstance() != null && getInstance().running.get()) {
                StopRunnable runnable = new StopRunnable();
                StopRunnable lastOne = lastRunnable.get();
                if(lastOne != null) {
                    lastOne.stop();
                }
                lastRunnable.set(runnable);
                service.execute(runnable);
            } else {
                backHome("停止执行");
            }
            wait1sec();
        } catch (Exception e) {
            lock.set(false);
            Log.w(TAG, "fail to action()");
            testNotify("Exception", "fail to action()");
            playRingtone();
        }
    }

    class StopRunnable implements Runnable {
        AtomicBoolean quit = new AtomicBoolean(false);
        public StopRunnable() {
            this.quit.set(false);
        }
        public void stop(){
            this.quit.set(true);
        }

        @Override
        public void run() {
            String[] cmds = acommands.get();
            int index = 0;
            int last = 0;
            for (;index<cmds.length;) {
                if(quit.get()) {
                    Log.w(TAG, "I never quit");
                    lock.set(false);
                    return;
                }

                if(pause.get()) {
                    wait1sec();
                    index = last + 1;
                    continue;
                }


                if(index > cmds.length - 1) {
                    Log.w(TAG, "最后一条指令:" + index);
                    break;
                }

                checkZombie(crazy, running, pause, ready, otherCount, lastTime);
                if(crazy.get()) {
                    crazyClick();
                }

                final String cmd = cmds[index];

                last = index;
                Log.w(TAG, "执行命令：" + "\t\tindex: " + index + " = " + cmd);
                if(cmd == null || cmd.length() < 1) {
                    index = last + 1;
                    continue;
                }

                if (!getInstance().running.get()) {
                    Log.w(TAG, "stop = " + running.get());
                    break;
                }

                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    Log.w(TAG, "sleep 300ms error");
                }

                Actions action = HTTP.actions(cmd);
                if (action != null) {
                    try {
                        Message next = new Message();
                        next.what = action.getActionType().what();
                        next.obj = action.getInput();
                        index = doMessage(next, index);
                    } catch (Exception e) {
                        Log.w(TAG, "异常执行：" + cmd + " \t\tindex: " + index);
                        testNotify("Exception", "fail to doMessage: " + e.getMessage());
                        playRingtone();
                    }

                } else {
                    index = last + 1;
                    Log.w(TAG, "忽略未知命令：" + cmd);
                }
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.w(TAG, "action scheduler");
                    if(pause.get()) {
                        Log.w(TAG, "正在暂停");
                        return;
                    }
                    if (getInstance() != null && getInstance().running.get()) {
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

            lock.set(false);
        }
    }

    public void playRingtone(){
        try {
            Log.w(TAG, "RingtoneManager\t\t\tRingtoneManager\t\t\tRingtoneManager\t\t\tRingtoneManager");
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        r.stop();
                    } catch (Exception e) {
                        Log.w(TAG, "error r.stop();");
                    }
                }
            }, 2000);
        } catch (Exception e) {
            Log.w(TAG, "error r.play();");
        }
    }

    public void backHome(String reason){
        try {
            running.set(false);
            Log.w(TAG, "backHome reason = " + reason);
            Intent intent = new Intent(getApplicationContext() , MainActivity.class);
            startActivity(intent);
            wait1sec();
            wait1sec();
            wait1sec();
        } catch (Exception e) {
            testNotify("Exception", "fail to backHome: " + e.getMessage());
            playRingtone();
        }

    }

    public void backHome(String reason, String todo){
        try {
            Log.w(TAG, "backHome reason = " + reason);
            Intent intent = new Intent(getApplicationContext() , MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("todo", todo);
            intent.putExtras(bundle);
            startActivity(intent);
            pause.set(true);
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            testNotify("Exception", "fail to backHome: " + e.getMessage());
            playRingtone();
        }

    }

    public int doMessage(Message msg, int index) {
        if(pause.get()) {
            return index;
        }

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

                if(afterOpen.get() > 0 && index > afterOpen.get() && activity == null) {
                    activity = currActivity.get();
                }

                performSwipe0();
                break;
            case 503:

                if(afterOpen.get() > 0 && index > afterOpen.get() && activity == null) {
                    activity = currActivity.get();
                }

                performSwipe1();
                break;
            case 504:
                performScrollForward();
                break;
            case 505:
                performScrollBackward();
                break;
            case 506:

                if(afterOpen.get() > 0 && index > afterOpen.get() && activity == null) {
                    activity = currActivity.get();
                }

                String xy = (String) msg.obj;
                String[] args = xy.split("\\s+");
                String x = args[0];
                String y = args[1];
                performClick(x, y);
                break;
            case 507:
                String who = (String) msg.obj;
                activity = null;
                afterOpen.set(index);
                boolean open = openActivity(who);
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
            case 9999:
                if(ignore.get() == index) {
                    break;
                }
                running.set(false);
                String reason = (String) msg.obj;
                ignore.set(index);
                backHome("服务端通知", reason);
                break;
            default:
                Log.w(TAG, "无效命令");
                break;
        }
        return index + 1;
    }

    public void wait1sec() {
        try {
            if(!running.get()) {
                tooLong.incrementAndGet();
                if(tooLong.get() > 120) {
                    tooLong.set(0);
                    action();
                }
            }
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.w(TAG, "wait 1 sec end, running = " + running.get());
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
    public void onInterrupt() {
        Log.w(TAG, "onInterrupt");
        running.set(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public String toString() {
        return "AutoService{ running = " + running.get() + ", handler=" + handler + '}';
    }


    public void testNotify(String title, String message){
        try {
            Log.w(TAG, "testotify");
            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(title)
                            .setContentText(message);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(increment.incrementAndGet(), mBuilder.build());
        } catch (Exception e) {
            Log.w(TAG, "testotify Exception");
        }

    }
}
