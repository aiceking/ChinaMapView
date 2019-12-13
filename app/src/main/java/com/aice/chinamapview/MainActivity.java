package com.aice.chinamapview;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btn_normal)
    Button btnNormal;
    @BindView(R.id.btn_refresh)
    Button btnRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }
    @OnClick({R.id.btn_normal, R.id.btn_refresh})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_normal:
                startActivity(new Intent(this,NormalActivity.class));
                break;
            case R.id.btn_refresh:
                startActivity(new Intent(this,SwipRefreshAppbarActivity.class));

                break;
        }
    }
}
