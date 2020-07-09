package com.example.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class NoteActivity extends AppCompatActivity {
    private EditText editText;
    private int id;

    //Note活动的初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //通过intent传递活动的上下文来初始化各个参数
        editText = findViewById(R.id.note_edit_text);
        String contents = getIntent().getStringExtra("contents");
        //将contents内容输出到视图中
        id = getIntent().getIntExtra("id",0);
        editText.setText(contents);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //将新建立的Note存储在数据库中
        PokemonActivity.database.noteDao().save(editText.getText().toString(), id);
    }
}
