package com.example.pokedex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.os.Bundle;
//打开app的最初界面，该部分主要用于初始化RecyclerView界面
public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {        //使用适配器将View与数据相连
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);        //准备设置主界面的View
        adapter = new PokedexAdapter(getApplicationContext());  //主界面的recyclerView的内容
        layoutManager = new LinearLayoutManager(this); //主界面的布局设置
        //将内容和布局设置set到recyclerView
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

    }
}
