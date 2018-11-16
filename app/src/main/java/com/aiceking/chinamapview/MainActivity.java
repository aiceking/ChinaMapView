package com.aiceking.chinamapview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aiceking.chinamap.view.ChinaMapView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.map)
    ChinaMapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}
