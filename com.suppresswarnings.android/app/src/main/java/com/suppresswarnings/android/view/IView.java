package com.suppresswarnings.android.view;

public interface IView {

    void loadWebview();

    void updateUI();

    void showProgressbar();

    void showDialog(String reason);

    void doCancel();

}
