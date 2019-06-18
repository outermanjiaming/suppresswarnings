package com.xiaomi.ad.mimo.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Surface;
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

import com.miui.zeus.mimo.sdk.ad.AdWorkerFactory;
import com.miui.zeus.mimo.sdk.ad.IRewardVideoAdWorker;
import com.miui.zeus.mimo.sdk.listener.MimoRewardVideoListener;
import com.xiaomi.ad.common.pojo.AdType;
import com.xiaomi.ad.mimo.demo.model.HTTP;
import com.xiaomi.ad.mimo.demo.model.Key;
import com.xiaomi.ad.mimo.demo.presenter.Presenter;
import com.xiaomi.ad.mimo.demo.view.FloatingView;
import com.xiaomi.ad.mimo.demo.view.IView;
import com.xiaomi.ad.mimo.demo.view.MyWebview;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainActivity extends Activity implements IView {
    int REQUEST_MEDIA_PROJECTION = 12345678;
    AtomicBoolean stopped = new AtomicBoolean(false);
    MyWebview webview;
    TextView warning;
    LinearLayout background;
    ProgressBar progressBar;
    private ListView lv;
    private Adapter adapter;
    String TAG = "MainActivity";
    Presenter presenter;
    final static int LOOK = 0;
    final static int READ = 1;
    String[] template = {"素朴网联open,%s","素朴网联open,%s"};
    MainActivity context;
    MqttService mqttService;
    ServiceConnection serviceConnection;
    FloatingView floatBall;
    WindowManager windowManager;
    WindowManager.LayoutParams floatBallParams;
    AtomicLong clicked = new AtomicLong(0);
    List<Pair<String, String>> cached = new ArrayList<>();
    Set<String> selected = new HashSet<>();

    private int mScreenDensity;
    private int mWindowWidth;
    private int mWindowHeight;
    private VirtualDisplay mVirtualDisplay;
    private WindowManager mWindowManager;
    private int mResultCode;
    private Intent mResultData;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private String mVideoPath;
    private Surface mSurface;
    private MediaCodec mMediaCodec;
    private MediaMuxer mMuxer;

    private AtomicBoolean mIsQuit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private boolean mMuxerStarted = false;
    private int mVideoTrackIndex = -1;

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private IRewardVideoAdWorker mWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.webview);
        warning = findViewById(R.id.tv_warning);
        background = findViewById(R.id.background);
        progressBar = findViewById(R.id.progressbar);

        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }

        presenter = new Presenter(context, context, webview);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);
        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}     , 0);
        video();
        loadWebview();
        createEnvironment();
        mqtt();
        template[READ] = get("READ");
        template[LOOK] = get("LOOK");
    }

    public void video() {
        try {
            mWorker = AdWorkerFactory.getRewardVideoAdWorker(getApplicationContext(), Key.POSITION_ID, AdType.AD_REWARDED_VIDEO);
            mWorker.setListener(new RewardVideoListener(mWorker));
            mWorker.load();
            Log.w(TAG, "video ad is loading");
        } catch (Exception e) {
            Log.w(TAG, "fail to load video ad:" + e.getMessage());
        }

    }
    public void copy(String cmd) {
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

    public void mqtt() {
        Intent intent = new Intent(context, MqttService.class);
        Log.w(TAG, "to bind service");
        serviceConnection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.w(TAG, "onServiceConnected");
                mqttService = ((MqttService.CustomBinder)service).getService();
                mqttService.setConsumer(new BiConsumer<String, byte[]>() {

                    @Override
                    public void accept(String topic, byte[] bytes) {
                        mqttService.setService(serviceConnection);
                        String ss = mqttService.clientid();
                        if(topic.equals("corpus/android/" + ss + "/die")) {
                            String reason = new String(bytes);
                            if(md5(ss).equals(reason)) {
                                if(AutoService.getInstance() != null){
                                    AutoService.getInstance().uninstallApp(getPackageName());
                                } else {
                                    Toast.makeText(context, "请为「素朴网联」打开'无障碍'权限", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Log.w(TAG, "just ignore data: " + bytes.length);
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.w(TAG, "onServiceDisconnected");
            }
        };
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void loadWebview() {
        presenter.loadWebview();
    }

    public String md5(String content) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5")
                    //bug.digest(content).getBytes("UTF-8"));
                    .digest((content+content).getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10){
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e){
            Log.w(TAG, "no md5");
        }
        return content;
    }

    @Override
    public void updateUI() {
        progressBar.setVisibility(View.GONE);
        background.setVisibility(View.GONE);

        lv=findViewById(R.id.lv);
        adapter = new Adapter(context);
        Log.w(TAG, "adapter = " + adapter);

        adapter.addItem("第一步：悬浮球");
        adapter.addItem("第二步：无障碍");
        adapter.addItem("第三步：选APP");
        adapter.addItem("阅读模板");
        adapter.addItem("视频模板");
        adapter.addItem("查看命令");
        adapter.addItem("录制命令");
        adapter.addItem("查看广告");
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
                            presenter.Log("请关闭「素朴网联」'无障碍'权限");
                            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            Log.w(TAG, "ACTION_ACCESSIBILITY_SETTINGS");
                            aSwitch.setChecked(AutoService.getInstance() != null);
                            break;
                        case 6:
                            recordStop();
                            copy(mqttService.clientid());
                            adapter.update(mqttService.clientid(), 6);
                            adapter.notifyDataSetChanged();
                            break;
                        default:
                            break;
                    }
                } else {
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
                        case 3:
                            showEditView("修改阅读命令模板", template, READ, new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    set("READ", s);
                                    aSwitch.setChecked(false);
                                }
                            });
                            break;
                        case 4:
                            showEditView("修改视频命令模板", template, LOOK, new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    set("LOOK", s);
                                    aSwitch.setChecked(false);
                                }
                            });

                            break;
                        case 5:
                            showTextView("查看当前命令", new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    aSwitch.setChecked(false);
                                }
                            });
                            break;
                        case 6:
                            try {
                                Log.w(TAG, "startRecord");
                                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mWindowWidth, mWindowHeight);
                                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 6000000);
                                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
                                mMediaCodec = MediaCodec.createEncoderByType("video/avc");
                                mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                                mSurface = mMediaCodec.createInputSurface();
                                startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),REQUEST_MEDIA_PROJECTION);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case 7:
                            try {
                                mWorker.show();
                            } catch (Exception e) {
                                presenter.Log("video ad: " + e.getMessage());
                                Log.w(TAG, "fail to show video ad");
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

    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.i("yao", "SocketException");
        }
        return hostIp;
    }

    public void showFloatBall() {
        if (!Settings.canDrawOverlays(context)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1234);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                Log.w(TAG, "showFloatBall can't sleep");
            }
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
                                    AutoService.getInstance().stopRunning();
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
    public void showProgressbar() {
        warning.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public List<Pair<String,String>> reloadButtons() {
        if(!cached.isEmpty()) {
            return cached;
        }
        final PackageManager pm = context.getPackageManager();
        List<Pair<String, String>> pairs = new ArrayList<>();
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
        cached.clear();
        cached.addAll(pairs);
        return cached;
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

                if(selected.contains(info.second)) {
                    apps[i] = info.first + "(已选)";
                } else {
                    apps[i] = info.first;
                }
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
                selected.add(info.second);
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
                copy(old);
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
                copy(cmd);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(lp);
        layout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
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
                return;
            }
            if(AutoService.getInstance() == null) {
                Toast.makeText(context, "需要开通'无障碍'权限", Toast.LENGTH_SHORT).show();
                return;
            }
            final String reading = String.format(template[READ], p);
            final String looking = String.format(template[LOOK], p);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("确认APP: " + app + "(" + p+")\n阅读：" + reading + "\n\n视频：" + looking)
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
                                String cmd = reading + old;
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
                    .setPositiveButton("【视频】", new DialogInterface.OnClickListener() {

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
                                String cmd = looking + old;

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
        release();
        finish();
    }

    @Override
    protected void onDestroy() {
        stopped.set(true);
        //bug
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.d(TAG, "cancelled");
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "recording");
            mResultCode = resultCode;
            mResultData = data;
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("record_screen",
                    mWindowWidth, mWindowHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mSurface, null, null);

            Toast.makeText(this, "start reading ", Toast.LENGTH_SHORT).show();
           recordStart();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            Log.w(TAG, "mResultData" + mResultData);
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }
    private void createEnvironment() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowWidth = mWindowManager.getDefaultDisplay().getWidth();
        mWindowHeight = mWindowManager.getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenDensity = displayMetrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mVideoPath = Environment.getExternalStorageDirectory().getPath() + "/suppresswarnings/record/";
        Log.w(TAG, "createEnvironment " + mWindowWidth);
        Log.w(TAG, "createEnvironment " + mWindowHeight);
        Log.w(TAG, "createEnvironment " + mScreenDensity);
        Log.w(TAG, "createEnvironment " + mVideoPath);
    }

    private void recordStart() {
        Log.w(TAG, "recordStart");
        mMediaCodec.start();
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "start startRecord");
                startRecord();
            }
        }.start();
    }
    Random rand = new Random();
    AtomicInteger index = new AtomicInteger(  rand.nextInt() + 1);
    String fileName = null;
    private void startRecord() {
        try {
            File fileFolder = new File(mVideoPath);
            if (!fileFolder.exists())
                fileFolder.mkdirs();
            fileName = "reading" + index.getAndIncrement() + ".mp4";
            File file = new File(mVideoPath, fileName);
            if (!file.exists()) {
                Log.d(TAG, "file create success ");
                file.createNewFile();
            }

            Log.w(TAG, "======= ======= ======= ======== mMediaCodec start");
            mMuxer = new MediaMuxer(mVideoPath + fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            while (!mIsQuit.get()) {
                int index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {//后续输出格式变化
                    resetOutputFormat();
                    Log.w(TAG, "======= ======= ======= ======== mMuxer start");
                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {//请求超时
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "InterruptedException");
                    }
                } else if (index >= 0) {//有效输出
                    Log.d(TAG, "dequeue output buffer index=" + index);
                    if (!mMuxerStarted) {
                        throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                    }
                    encodeToVideoTrack(index);
                    mMediaCodec.releaseOutputBuffer(index, false);
                }
            }
            release();
        } catch (Exception e) {
            Log.w(TAG, e);
            release();
        }
    }
    private void resetOutputFormat() {
        if (mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        MediaFormat newFormat = mMediaCodec.getOutputFormat();
        Log.d(TAG, "output format changed.\n new format: " + newFormat.toString());
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        mMuxer.start();
        mMuxerStarted = true;
        Log.i(TAG, "started media muxer, videoIndex=" + mVideoTrackIndex);
    }
    private void encodeToVideoTrack(int index) {
        Log.e(TAG, "encodeToVideoTrack 1");
        ByteBuffer encodedData = mMediaCodec.getOutputBuffer(index);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {//是编码需要的特定数据，不是媒体数据
            Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            encodedData = null;
        } else {
            Log.d(TAG, "got buffer, info: size=" + mBufferInfo.size
                    + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                    + ", offset=" + mBufferInfo.offset);
        }
        if (encodedData != null) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);//写入
            Log.i(TAG, "sent " + mBufferInfo.size + " bytes to muxer...");
        }
    }

    private void recordStop() {
        mIsQuit.set(true);
    }

    private void release() {
        mIsQuit.set(false);
        mMuxerStarted = false;
        Log.i(TAG, " release() ");
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
            }catch (Exception e){
                Log.w(TAG, e);
            }
            mMuxer = null;
        }
        if(fileName == null) {
            return;
        } else {
            try {
                File file = new File(mVideoPath + fileName);
                byte[] bs = MqttService.fileToBytes(file);
                Log.w(TAG, "publish file: " + fileName);
                MqttService.publish("video", bs);
                file.delete();
            } catch (Exception e){
                Log.w(TAG, e);
            }
        }
    }

    public String get(String key) {
        SharedPreferences spf = context.getSharedPreferences(Key.cache, MODE_PRIVATE);
        String value = spf.getString(key, null);
        if (value == null) {
            return "素朴网联home素朴网联open,%s素朴网联info,命令设置失败请重试修改";
        }
        return value;
    }

    public String set(String key, String value) {
        SharedPreferences spf = context.getSharedPreferences(Key.cache, MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putString(key, value);
        boolean commited = editor.commit();
        return commited ? value : "设置失败，怎么回事？";
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




    private class RewardVideoListener implements MimoRewardVideoListener {
        private IRewardVideoAdWorker mAdWorker;

        public RewardVideoListener(IRewardVideoAdWorker adWorker) {
            mAdWorker = adWorker;
        }

        @Override
        public void onVideoStart() {
            Log.i(TAG, "onVideoStart");
            Toast.makeText(context, "onVideoStart status = " + mAdWorker.getVideoStatus(),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVideoPause() {
            Log.i(TAG, "onVideoPause");
            Toast.makeText(context, "onVideoPause status = " + mAdWorker.getVideoStatus(),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVideoComplete() {
            Log.i(TAG, "onVideoComplete");
            Toast.makeText(context, "onVideoComplete status = " + mAdWorker.getVideoStatus(),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAdPresent() {
            Log.i(TAG, "onAdPresent");
            Toast.makeText(context, "onAdPresent isReady = " + mAdWorker.isReady(),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAdClick() {
            Log.i(TAG, "onAdClick");
            Toast.makeText(context, "onAdClick", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAdDismissed() {
            Log.i(TAG, "onAdDismissed");
            Toast.makeText(context, "onAdDismissed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAdFailed(String message) {
            Log.e(TAG, "onAdFailed : " + message);
            Toast.makeText(context,
                    "onAdFailed isReady = " + mAdWorker.isReady() + " msg: " + message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAdLoaded(int size) {
            Log.i(TAG, "onAdLoaded : " + size);
            try {
                mWorker.show();
            } catch (Exception e) {
                presenter.Log("video ad: " + e.getMessage());
                Log.w(TAG, "fail to show video ad");
            }
            Toast.makeText(context,
                    "onAdLoaded isReady = " + mAdWorker.isReady() + " size: " + size, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStimulateSuccess() {
            Log.i(TAG, "onStimulateSuccess");
            Toast.makeText(context, "onStimulateSuccess", Toast.LENGTH_LONG).show();
        }
    }
}
