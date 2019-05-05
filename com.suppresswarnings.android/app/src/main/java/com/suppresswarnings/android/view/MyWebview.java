package com.suppresswarnings.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MyWebview extends WebView {

    Context mContext;

    public MyWebview(Context context) {
        super(context);
        mContext = context;
        setWebViewClient();
    }

    public MyWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setWebViewClient();
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    boolean setWebViewClient() {

        setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus(View.FOCUS_DOWN);
        WebSettings webSettings = getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);

        setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }

        });

        return true;
    }

}
