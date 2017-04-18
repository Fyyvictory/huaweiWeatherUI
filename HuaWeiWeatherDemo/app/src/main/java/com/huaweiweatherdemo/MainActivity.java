package com.huaweiweatherdemo;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    protected WeatherView weather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        weather = (WeatherView) findViewById(R.id.weather);
        weather.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.waterday));
    }
}
