package com.aice.chinamapview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.aice.chinamapview.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.btnNormal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NormalActivity.class)));
        mBinding.btnRefresh.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SwipRefreshAppbarActivity.class)));
    }

}
