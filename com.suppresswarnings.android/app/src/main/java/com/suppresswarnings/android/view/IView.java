package com.xiaomi.ad.mimo.demo.view;

public interface IView {

    void loadWebview();

    void updateUI();

    void showProgressbar();

    void showDialog(String reason);

    void doCancel();

}
