package com.example.pokedex;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
//从PokemonActivity跳转到NoteActivity的生成器
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        LinearLayout containerView;
        TextView textView;

        NoteViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.note_row);  //PokemonActivity中的Note区域的container视图
            textView = view.findViewById(R.id.note_row_text);  //PokemonActivity中的Note区域每一行的视图设置
            //为Note区域增加切换的事件（点击Note区域的一个Note即切换到该Note的编辑）
            containerView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Note current = (Note) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), NoteActivity.class);  //联系上下文
                    intent.putExtra("id",current.id);
                    intent.putExtra("contents",current.contents);

                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private List<Note> notes = new ArrayList<>();

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){ //创建视图，视图的初始化函数
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_row, parent, false);
        return new NoteViewHolder(view);
    }
    //将视图的持有者绑定当前视图
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note current = notes.get(position);
        holder.textView.setText(current.contents);
        holder.containerView.setTag(current);
    }
    //返回Note的个数
    @Override
    public int getItemCount(){
        return notes.size();
    }
    //新建了一个Note，需要重新加载数据库
    public void reload(){
        notes = PokemonActivity.database.noteDao().getAllNotes();
        notifyDataSetChanged();
    }
}
