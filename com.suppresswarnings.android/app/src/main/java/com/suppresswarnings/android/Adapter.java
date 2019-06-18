package com.xiaomi.ad.mimo.demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends BaseAdapter {

    private List<String> list_title;
    private Context mContext;

    public Adapter(Context context) {
        mContext=context;
        list_title=new ArrayList<>();
    }

    public void addItem(String title){
        list_title.add(title);
    }

    public void update(String title, int i) {
        list_title.set(i, title);
        Log.w("Adapter","update(" + i + " => " + title + ")");
    }
    @Override
    public int getCount() {
        return list_title.size();
    }

    @Override
    public Object getItem(int i) {
        return list_title.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.item,null);
        TextView tv = view.findViewById(R.id.tv);
        String title = list_title.get(i);
        tv.setText(title);
        return view;
    }
}