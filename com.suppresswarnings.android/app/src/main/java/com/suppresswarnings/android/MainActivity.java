package com.suppresswarnings.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.suppresswarnings.android.presenter.Presenter;
import com.suppresswarnings.android.utils.HTTPUtil;
import com.suppresswarnings.android.view.IView;
import com.suppresswarnings.android.view.MyWebview;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements IView {

    @BindView(R.id.webview)
    MyWebview webview;
    @BindView(R.id.btn_control)
    Button control;
    @BindView(R.id.tv_warning)
    TextView warning;
    @BindView(R.id.background)
    LinearLayout background;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        presenter = new Presenter(this, MainActivity.this, webview);
    }

    @Override
    public void loadWebview() {
        presenter.loadWebview();
    }

    @Override
    public void updateUI() {
        control.setVisibility(View.VISIBLE);
        background.setVisibility(View.GONE);
    }

    @Override
    public void showProgressbar() {
        warning.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showDialog() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(webview.getContext());

        final EditText inputServer = new EditText(MainActivity.this);
        inputServer.setGravity(Gravity.CENTER);
        localBuilder.setMessage("请输入激活码")
                .setView(inputServer)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.doCancel();
                    }
                })
                .setPositiveButton("激活", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        final String code = inputServer.getText().toString();
//                                Log("激活码: " + code);
                        if (code == null || code.trim().length() < 1) {
                            presenter.Log("请关注微信公众号“素朴网联”获取激活码");
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String x = HTTPUtil.checkValid(presenter.getToken(), code);
                                    presenter.handleCase(2,x.equals("Paid") ? "恭喜，激活成功" : "未激活：请关注微信公众号“素朴网联”获取激活码，程序将自动退出");
                                } catch (Exception e) {
                                    presenter.handleCase(3,e.getMessage());
                                }
                            }
                        }).start();

                    }
                });
        localBuilder.setCancelable(false);
        localBuilder.create().show();
    }

    @Override
    public void doCancel(){
        presenter.Log("未激活，请关注微信公众号“素朴网联”获取激活码");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
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
        super.onDestroy();
    }

    @OnClick(R.id.btn_control)
    public void onButterKnifeBtnClick(View view) {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(webview.getContext());

        final EditText inputServer = new EditText(MainActivity.this);

        inputServer.setVerticalScrollBarEnabled(true);
        inputServer.setLines(10);
        inputServer.setGravity(Gravity.LEFT);
        localBuilder.setMessage("请输入命令")
                .setView(inputServer)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
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
}
