package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.view.View;

public class Main4Activity extends Activity {

    protected int getLayoutId(String str) {
        return ResourceUtil.getLayoutId(str);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.activity_main4);
        setContentView(getLayoutId("activity_main4"));
    }
}
