package com.example.pokedex;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> {
    public static class PokedexViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout containerView;
        public TextView textView;

        PokedexViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.pokedex_row);       //MainActivity列的总体视图布局
            textView = view.findViewById(R.id.pokedex_row_text_view);  //MainActivity列的textView视图设置
            textView.getPaint().setFakeBoldText(true);                 //黑体加粗
            //为MainActivity中的内容设置鼠标事件
            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    //Intent存储上下文，记录从MainActivity跳转到PokemonActivity
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());
                    //intent.putExtra("number", current.getUr1());
                    //imageView.setImageResource(R.drawable.bulbassaur);
                    v.getContext().startActivity(intent);   //启动活动
                }
            });
        }
    }

    private List<Pokemon> pokemon = new ArrayList<>();     //创建一个空的Pokemon对象的列表
    private RequestQueue requestQueue;                     //请求队列

    //传递活动到适配器
    PokedexAdapter(Context context){
        requestQueue = Volley.newRequestQueue(context);
        loadPokemon();
    }

    public void loadPokemon(){                 //加载API的Pokemon数据
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";

        //用来处理JSON的响应
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //获取results的数据，并将其存储在results数组中
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length();i++){
                        JSONObject result = results.getJSONObject(i);
                        String name = result.getString("name");
                        pokemon.add(new Pokemon(             //向列表中顺序添加API导入的元素，每一个列表元素都直接通过构造函数生成
                                  String.format("%d. ", i + 1)+ name.substring(0,1).toUpperCase() + name.substring(1),
                                result.getString("url")
                        ));
                    }
                    notifyDataSetChanged();       //更新数据
                } catch (JSONException e) {
                    Log.e("Ingram14", "Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Ingram14","Pokemon list error");
            }
        });
        requestQueue.add(request);
    }
    //将布局转化为视图（XML转视图）
    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);  //与返回值对应
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position){ //修改视图的持有者
        Pokemon current = pokemon.get(position);            //获取数据的容器
        holder.textView.setText(current.getName());
        holder.containerView.setTag(current);               //与该容器联系，以便点击时跳转到相应界面
    }

    @Override
    public int getItemCount() {
        return pokemon.size();
    }
}
