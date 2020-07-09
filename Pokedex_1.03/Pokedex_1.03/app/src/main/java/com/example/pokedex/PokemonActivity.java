package com.example.pokedex;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.KuwaharaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.PixelationFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SwirlFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;

public class PokemonActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private ImageView imageView;                //处理从用户手机中选取的图片，对象化处理
    private Bitmap image;                       //加载类，将FileDescriptor转换成Image
    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    public static NotesDatabase database;
    public ImageView imageView_pokemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        imageView = findViewById(R.id.image_view);   //对象的初始化

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");             //获取Adapter中的上下文intent，并获取其中的url内容
        nameTextView = findViewById(R.id.pokemon_name);             //PokemonActivity布局nameText
        nameTextView.getPaint().setFakeBoldText(true);              //加粗
        numberTextView = findViewById(R.id.pokemon_number);         //PokemonActivity布局numberText
        type1TextView = findViewById(R.id.pokemon_type1);           //PokemonActivity布局Type1Text
        type2TextView = findViewById(R.id.pokemon_type2);           //PokemonActivity布局Type2Text
        imageView_pokemon = findViewById(R.id.Pokemon_picture);

        load();

        //询问用户是否有访问图库的权利
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        //初始化Note的库
        database = Room.databaseBuilder(getApplicationContext(), NotesDatabase.class, "notes")
                .allowMainThreadQueries()
                .build();
        //NoteActivity中布局recyclerView，同时为其增加指向下一个Activity的Button
        recyclerView = findViewById(R.id.recycler_view_1);
        layoutManager = new LinearLayoutManager(this);
        adapter = new NotesAdapter();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //设置增加Note的点击事件
        FloatingActionButton button = findViewById(R.id.add_note_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                database.noteDao().create();
                adapter.reload();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.reload();
    }
    //原本想设置存储图片，结果失败了，这个函数其实没什么用
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void choosePhoto(View v){                               //从用户的手机中选择图片
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);   //实现了从安卓手机中打开文件（活动）
        intent.setType("image/*");                                 //限定文件类型为所有图片
        startActivityForResult(intent, 1);           //激活，1表示从文件中返回
    }
    //加载Adaptor中传递过来的内容
    public void load(){
        type1TextView.setText("");
        type2TextView.setText("");
        //加载API中url的内容
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //安排name和number放置到视图中
                    nameTextView.setText(response.getString("name").toUpperCase());
                    numberTextView.setText(String.format("#%03d",response.getInt("id")));

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        //读取url中的slot关键字的值
                        int slot = typeEntry.getInt("slot");

                        //读取url中的宝可梦属性，并set到text中
                        String type = typeEntry.getJSONObject("type").getString("name");
                        if (slot == 1) {
                            String new_type = "Main Type:  " + type;
                            type1TextView.setText(new_type);
                        }
                        else if (slot == 2){
                            String extra_type = "Extra Type:  " + type;
                            type2TextView.setText(extra_type);
                        }
                        //由于API网站上并没有图片，因此，如果想导入图片，只能通过手动添加
                        //（或许有其他方法，但是限于我们小组的能力有限，因此我们采用了笨办法，将151个Pokemon手动逐一导入）
                        if (response.getInt("id") == 1)
                            imageView_pokemon.setImageResource(R.drawable.i001bulbasaur);
                        else if (response.getInt("id") == 2)
                            imageView_pokemon.setImageResource(R.drawable.i002ivysaur);
                        else if (response.getInt("id") == 3)
                            imageView_pokemon.setImageResource(R.drawable.i003venusaur);
                        else if (response.getInt("id") == 4)
                            imageView_pokemon.setImageResource(R.drawable.i004charmander);
                        else if (response.getInt("id") == 5)
                            imageView_pokemon.setImageResource(R.drawable.i005charmeleon);
                        else if (response.getInt("id") == 6)
                            imageView_pokemon.setImageResource(R.drawable.i006charizard);
                        else if (response.getInt("id") == 7)
                            imageView_pokemon.setImageResource(R.drawable.i007squirtle);
                        else if (response.getInt("id") == 8)
                            imageView_pokemon.setImageResource(R.drawable.i008wartortle);
                        else if (response.getInt("id") == 9)
                            imageView_pokemon.setImageResource(R.drawable.i009blastoise);
                        else if (response.getInt("id") == 10)
                            imageView_pokemon.setImageResource(R.drawable.i010caterpie);
                        else if (response.getInt("id") == 11)
                            imageView_pokemon.setImageResource(R.drawable.i011metapod);
                        else if (response.getInt("id") == 12)
                            imageView_pokemon.setImageResource(R.drawable.i012butterfree);
                        else if (response.getInt("id") == 13)
                            imageView_pokemon.setImageResource(R.drawable.i013weedle);
                        else if (response.getInt("id") == 14)
                            imageView_pokemon.setImageResource(R.drawable.i014kakuna);
                        else if (response.getInt("id") == 15)
                            imageView_pokemon.setImageResource(R.drawable.i015beedrill);
                        else if (response.getInt("id") == 16)
                            imageView_pokemon.setImageResource(R.drawable.i016pidgey);
                        else if (response.getInt("id") == 17)
                            imageView_pokemon.setImageResource(R.drawable.i017pidgeotto);
                        else if (response.getInt("id") == 18)
                            imageView_pokemon.setImageResource(R.drawable.i018pidgeot);
                        else if (response.getInt("id") == 19)
                            imageView_pokemon.setImageResource(R.drawable.i019rattata);
                        else if (response.getInt("id") == 20)
                            imageView_pokemon.setImageResource(R.drawable.i020raticate);
                        else if (response.getInt("id") == 21)
                            imageView_pokemon.setImageResource(R.drawable.i021spearow);
                        else if (response.getInt("id") == 22)
                            imageView_pokemon.setImageResource(R.drawable.i022fearow);
                        else if (response.getInt("id") == 23)
                            imageView_pokemon.setImageResource(R.drawable.i023ekans);
                        else if (response.getInt("id") == 24)
                            imageView_pokemon.setImageResource(R.drawable.i024arbok);
                        else if (response.getInt("id") == 25)
                            imageView_pokemon.setImageResource(R.drawable.i025pikachu);
                        else if (response.getInt("id") == 26)
                            imageView_pokemon.setImageResource(R.drawable.i026raichu);
                        else if (response.getInt("id") == 27)
                            imageView_pokemon.setImageResource(R.drawable.i027sandshrew);
                        else if (response.getInt("id") == 28)
                            imageView_pokemon.setImageResource(R.drawable.i028sandslash);
                        else if (response.getInt("id") == 29)
                            imageView_pokemon.setImageResource(R.drawable.i029nidoran);
                        else if (response.getInt("id") == 30)
                            imageView_pokemon.setImageResource(R.drawable.i030nidorina);
                        else if (response.getInt("id") == 31)
                            imageView_pokemon.setImageResource(R.drawable.i031nidoqueen);
                        else if (response.getInt("id") == 32)
                            imageView_pokemon.setImageResource(R.drawable.i032nidoran);
                        else if (response.getInt("id") == 33)
                            imageView_pokemon.setImageResource(R.drawable.i033nidorino);
                        else if (response.getInt("id") == 34)
                            imageView_pokemon.setImageResource(R.drawable.i034nidoking);
                        else if (response.getInt("id") == 35)
                            imageView_pokemon.setImageResource(R.drawable.i035clefairy);
                        else if (response.getInt("id") == 36)
                            imageView_pokemon.setImageResource(R.drawable.i036clefable);
                        else if (response.getInt("id") == 37)
                            imageView_pokemon.setImageResource(R.drawable.i037vulpix);
                        else if (response.getInt("id") == 38)
                            imageView_pokemon.setImageResource(R.drawable.i038ninetales);
                        else if (response.getInt("id") == 39)
                            imageView_pokemon.setImageResource(R.drawable.i039jigglypuff);
                        else if (response.getInt("id") == 40)
                            imageView_pokemon.setImageResource(R.drawable.i040wigglytuff);
                        else if (response.getInt("id") == 41)
                            imageView_pokemon.setImageResource(R.drawable.i041zubat);
                        else if (response.getInt("id") == 42)
                            imageView_pokemon.setImageResource(R.drawable.i042golbat);
                        else if (response.getInt("id") == 43)
                            imageView_pokemon.setImageResource(R.drawable.i043oddish);
                        else if (response.getInt("id") == 44)
                            imageView_pokemon.setImageResource(R.drawable.i044gloom);
                        else if (response.getInt("id") == 45)
                            imageView_pokemon.setImageResource(R.drawable.i045vileplume);
                        else if (response.getInt("id") == 46)
                            imageView_pokemon.setImageResource(R.drawable.i046paras);
                        else if (response.getInt("id") == 47)
                            imageView_pokemon.setImageResource(R.drawable.i047parasect);
                        else if (response.getInt("id") == 48)
                            imageView_pokemon.setImageResource(R.drawable.i048venonat);
                        else if (response.getInt("id") == 49)
                            imageView_pokemon.setImageResource(R.drawable.i049venomoth);
                        else if (response.getInt("id") == 50)
                            imageView_pokemon.setImageResource(R.drawable.i050diglett);
                        else if (response.getInt("id") == 51)
                            imageView_pokemon.setImageResource(R.drawable.i051dugtrio);
                        else if (response.getInt("id") == 52)
                            imageView_pokemon.setImageResource(R.drawable.i051meowth);
                        else if (response.getInt("id") == 53)
                            imageView_pokemon.setImageResource(R.drawable.i052persian);
                        else if (response.getInt("id") == 54)
                            imageView_pokemon.setImageResource(R.drawable.i053psyduck);
                        else if (response.getInt("id") == 55)
                            imageView_pokemon.setImageResource(R.drawable.i054golduck);
                        else if (response.getInt("id") == 56)
                            imageView_pokemon.setImageResource(R.drawable.i055mankey);
                        else if (response.getInt("id") == 57)
                            imageView_pokemon.setImageResource(R.drawable.i056primeape);
                        else if (response.getInt("id") == 58)
                            imageView_pokemon.setImageResource(R.drawable.i057growlithe);
                        else if (response.getInt("id") == 59)
                            imageView_pokemon.setImageResource(R.drawable.i058arcanine);
                        else if (response.getInt("id") == 60)
                            imageView_pokemon.setImageResource(R.drawable.i059poliwag);
                        else if (response.getInt("id") == 61)
                            imageView_pokemon.setImageResource(R.drawable.i060poliwhirl);
                        else if (response.getInt("id") == 62)
                            imageView_pokemon.setImageResource(R.drawable.i061poliwrath);
                        else if (response.getInt("id") == 63)
                            imageView_pokemon.setImageResource(R.drawable.i062abra);
                        else if (response.getInt("id") == 64)
                            imageView_pokemon.setImageResource(R.drawable.i063kadabra);
                        else if (response.getInt("id") == 65)
                            imageView_pokemon.setImageResource(R.drawable.i064alakazam);
                        else if (response.getInt("id") == 66)
                            imageView_pokemon.setImageResource(R.drawable.i065machop);
                        else if (response.getInt("id") == 67)
                            imageView_pokemon.setImageResource(R.drawable.i066machoke);
                        else if (response.getInt("id") == 68)
                            imageView_pokemon.setImageResource(R.drawable.i067machamp);
                        else if (response.getInt("id") == 69)
                            imageView_pokemon.setImageResource(R.drawable.i068bellsprout);
                        else if (response.getInt("id") == 70)
                            imageView_pokemon.setImageResource(R.drawable.i069weepinbell);
                        else if (response.getInt("id") == 71)
                            imageView_pokemon.setImageResource(R.drawable.i070victreebel);
                        else if (response.getInt("id") == 72)
                            imageView_pokemon.setImageResource(R.drawable.i071tentacool);
                        else if (response.getInt("id") == 73)
                            imageView_pokemon.setImageResource(R.drawable.i072tentacruel);
                        else if (response.getInt("id") == 74)
                            imageView_pokemon.setImageResource(R.drawable.i073geodude);
                        else if (response.getInt("id") == 75)
                            imageView_pokemon.setImageResource(R.drawable.i074graveler);
                        else if (response.getInt("id") == 76)
                            imageView_pokemon.setImageResource(R.drawable.i075golem);
                        else if (response.getInt("id") == 77)
                            imageView_pokemon.setImageResource(R.drawable.i076ponyta);
                        else if (response.getInt("id") == 78)
                            imageView_pokemon.setImageResource(R.drawable.i077rapidash);
                        else if (response.getInt("id") == 79)
                            imageView_pokemon.setImageResource(R.drawable.i078slowpoke);
                        else if (response.getInt("id") == 80)
                            imageView_pokemon.setImageResource(R.drawable.i079slowbro);
                        else if (response.getInt("id") == 81)
                            imageView_pokemon.setImageResource(R.drawable.i080magnemite);
                        else if (response.getInt("id") == 82)
                            imageView_pokemon.setImageResource(R.drawable.i081magneton);
                        else if (response.getInt("id") == 83)
                            imageView_pokemon.setImageResource(R.drawable.i082farfetchd);
                        else if (response.getInt("id") == 84)
                            imageView_pokemon.setImageResource(R.drawable.i083doduo);
                        else if (response.getInt("id") == 85)
                            imageView_pokemon.setImageResource(R.drawable.i084dodrio);
                        else if (response.getInt("id") == 86)
                            imageView_pokemon.setImageResource(R.drawable.i085seel);
                        else if (response.getInt("id") == 87)
                            imageView_pokemon.setImageResource(R.drawable.i086dewgong);
                        else if (response.getInt("id") == 88)
                            imageView_pokemon.setImageResource(R.drawable.i087grimer);
                        else if (response.getInt("id") == 89)
                            imageView_pokemon.setImageResource(R.drawable.i088muk);
                        else if (response.getInt("id") == 90)
                            imageView_pokemon.setImageResource(R.drawable.i088muk);
                        else if (response.getInt("id") == 91)
                            imageView_pokemon.setImageResource(R.drawable.i090cloyster);
                        else if (response.getInt("id") == 92)
                            imageView_pokemon.setImageResource(R.drawable.i091gastly);
                        else if (response.getInt("id") == 93)
                            imageView_pokemon.setImageResource(R.drawable.i092haunter);
                        else if (response.getInt("id") == 94)
                            imageView_pokemon.setImageResource(R.drawable.i093gengar);
                        else if (response.getInt("id") == 95)
                            imageView_pokemon.setImageResource(R.drawable.i094onix);
                        else if (response.getInt("id") == 96)
                            imageView_pokemon.setImageResource(R.drawable.i095drowzee);
                        else if (response.getInt("id") == 97)
                            imageView_pokemon.setImageResource(R.drawable.i096hypno);
                        else if (response.getInt("id") == 98)
                            imageView_pokemon.setImageResource(R.drawable.i097krabby);
                        else if (response.getInt("id") == 99)
                            imageView_pokemon.setImageResource(R.drawable.i098kingler);
                        else if (response.getInt("id") == 100)
                            imageView_pokemon.setImageResource(R.drawable.i099voltorb);
                        else if (response.getInt("id") == 101)
                            imageView_pokemon.setImageResource(R.drawable.i100electrode);
                        else if (response.getInt("id") == 102)
                            imageView_pokemon.setImageResource(R.drawable.i102exeggcute);
                        else if (response.getInt("id") == 103)
                            imageView_pokemon.setImageResource(R.drawable.i103exeggutor);
                        else if (response.getInt("id") == 104)
                            imageView_pokemon.setImageResource(R.drawable.i104cubone);
                        else if (response.getInt("id") == 105)
                            imageView_pokemon.setImageResource(R.drawable.i105marowak);
                        else if (response.getInt("id") == 106)
                            imageView_pokemon.setImageResource(R.drawable.i106hitmonlee);
                        else if (response.getInt("id") == 107)
                            imageView_pokemon.setImageResource(R.drawable.i107hitmonchan);
                        else if (response.getInt("id") == 108)
                            imageView_pokemon.setImageResource(R.drawable.i108lickitung);
                        else if (response.getInt("id") == 109)
                            imageView_pokemon.setImageResource(R.drawable.i109koffing);
                        else if (response.getInt("id") == 110)
                            imageView_pokemon.setImageResource(R.drawable.i110weezing);
                        else if (response.getInt("id") == 111)
                            imageView_pokemon.setImageResource(R.drawable.i111rhyhorn);
                        else if (response.getInt("id") == 112)
                            imageView_pokemon.setImageResource(R.drawable.i112rhydon);
                        else if (response.getInt("id") == 113)
                            imageView_pokemon.setImageResource(R.drawable.i113chansey);
                        else if (response.getInt("id") == 114)
                            imageView_pokemon.setImageResource(R.drawable.i114tangela);
                        else if (response.getInt("id") == 115)
                            imageView_pokemon.setImageResource(R.drawable.i115kangaskhan);
                        else if (response.getInt("id") == 116)
                            imageView_pokemon.setImageResource(R.drawable.i116horsea);
                        else if (response.getInt("id") == 117)
                            imageView_pokemon.setImageResource(R.drawable.i117seadra);
                        else if (response.getInt("id") == 118)
                            imageView_pokemon.setImageResource(R.drawable.i118goldeen);
                        else if (response.getInt("id") == 119)
                            imageView_pokemon.setImageResource(R.drawable.i119seaking);
                        else if (response.getInt("id") == 120)
                            imageView_pokemon.setImageResource(R.drawable.i120staryu);
                        else if (response.getInt("id") == 121)
                            imageView_pokemon.setImageResource(R.drawable.i121starmie);
                        else if (response.getInt("id") == 122)
                            imageView_pokemon.setImageResource(R.drawable.i122mrmime);
                        else if (response.getInt("id") == 123)
                            imageView_pokemon.setImageResource(R.drawable.i123scyther);
                        else if (response.getInt("id") == 124)
                            imageView_pokemon.setImageResource(R.drawable.i124jynx);
                        else if (response.getInt("id") == 125)
                            imageView_pokemon.setImageResource(R.drawable.i125electabuzz);
                        else if (response.getInt("id") == 126)
                            imageView_pokemon.setImageResource(R.drawable.i126magmar);
                        else if (response.getInt("id") == 127)
                            imageView_pokemon.setImageResource(R.drawable.i127pinsir);
                        else if (response.getInt("id") == 128)
                            imageView_pokemon.setImageResource(R.drawable.i128tauros);
                        else if (response.getInt("id") == 129)
                            imageView_pokemon.setImageResource(R.drawable.i129magikarp);
                        else if (response.getInt("id") == 130)
                            imageView_pokemon.setImageResource(R.drawable.i130gyarados);
                        else if (response.getInt("id") == 131)
                            imageView_pokemon.setImageResource(R.drawable.i131lapras);
                        else if (response.getInt("id") == 132)
                            imageView_pokemon.setImageResource(R.drawable.i132ditto);
                        else if (response.getInt("id") == 133)
                            imageView_pokemon.setImageResource(R.drawable.i133eevee);
                        else if (response.getInt("id") == 134)
                            imageView_pokemon.setImageResource(R.drawable.i134vaporeon);
                        else if (response.getInt("id") == 135)
                            imageView_pokemon.setImageResource(R.drawable.i135jolteon);
                        else if (response.getInt("id") == 136)
                            imageView_pokemon.setImageResource(R.drawable.i136flareon);
                        else if (response.getInt("id") == 137)
                            imageView_pokemon.setImageResource(R.drawable.i137porygon);
                        else if (response.getInt("id") == 138)
                            imageView_pokemon.setImageResource(R.drawable.i138omanyte);
                        else if (response.getInt("id") == 139)
                            imageView_pokemon.setImageResource(R.drawable.i139omastar);
                        else if (response.getInt("id") == 140)
                            imageView_pokemon.setImageResource(R.drawable.i140kabuto);
                        else if (response.getInt("id") == 141)
                            imageView_pokemon.setImageResource(R.drawable.i141kabutops);
                        else if (response.getInt("id") == 142)
                            imageView_pokemon.setImageResource(R.drawable.i142aerodactyl);
                        else if (response.getInt("id") == 143)
                            imageView_pokemon.setImageResource(R.drawable.i143snorlax);
                        else if (response.getInt("id") == 144)
                            imageView_pokemon.setImageResource(R.drawable.i144articuno);
                        else if (response.getInt("id") == 145)
                            imageView_pokemon.setImageResource(R.drawable.i145zapdos);
                        else if (response.getInt("id") == 146)
                            imageView_pokemon.setImageResource(R.drawable.i146moltres);
                        else if (response.getInt("id") == 147)
                            imageView_pokemon.setImageResource(R.drawable.i147dratini);
                        else if (response.getInt("id") == 148)
                            imageView_pokemon.setImageResource(R.drawable.i148dragonair);
                        else if (response.getInt("id") == 149)
                            imageView_pokemon.setImageResource(R.drawable.i149dragonite);
                        else if (response.getInt("id") == 150)
                            imageView_pokemon.setImageResource(R.drawable.i150mewtwo);
                        else if (response.getInt("id") == 151)
                            imageView_pokemon.setImageResource(R.drawable.i151mew);
                    }
                } catch (JSONException e) {
                    Log.e("Ingram14", "Pokemon Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Ingram14","Pokemon details error");
            }
        });
        requestQueue.add(request);
    }
    //用来给图片添加滤镜，使用了第三方库glide-transformations。由于每一个滤镜都采用了同一个照片处理途径，因此写了该函数用于精简代码
    public void apply(Transformation<Bitmap> filter){

        Glide                                                  //Glide库提供的静态类
                .with(this)                           //返回来一个请求管理器
                .load(image)                                   //加载方法，用来加载图像
                .apply(RequestOptions.bitmapTransform(filter)) //应用转换
                .into(imageView);                              //使用图像视图
    }
    //增加不同的滤镜，会在layout的button中调用
    public void applySepia(View v){
        apply(new SepiaFilterTransformation());
    }
    public void applyToon(View v){
        apply(new ToonFilterTransformation());
    }
    public void applySketch(View v){
        apply(new SketchFilterTransformation());
    }
    public void applyContrast(View v){ apply(new ContrastFilterTransformation()); }
    public void applyInvert(View v){
        apply(new InvertFilterTransformation());
    }
    public void applyPixelation(View v){
        apply(new PixelationFilterTransformation());
    }
    public void applySwirl(View v){
        apply(new SwirlFilterTransformation());
    }
    public void applyBrightness(View v){
        apply(new BrightnessFilterTransformation());
    }
    public void applyKuwahara(View v){
        apply(new KuwaharaFilterTransformation());
    }
    public void applyVignette(View v){
        apply(new VignetteFilterTransformation());
    }
    public void applyGrayscale(View v) { apply(new GrayscaleTransformation());}
    public void applyBlur(View v) { apply(new BlurTransformation());}

    //一个活动结束后，系统自动调用的函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        //确保参数返回值没问题并且数据不为空
        if (resultCode == Activity.RESULT_OK && data != null){
            try{
                Uri uri = data.getData();             //得到指向活动来源的指针
                //通过以下两个操作，经过指针的层层指向，得到我们想要的活动来源的数据
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(uri,"r");  //相当于指向文件的指针
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                image = BitmapFactory.decodeFileDescriptor(fileDescriptor); //将上一步产生的FileDescriptor转换成image类的中间步骤
                parcelFileDescriptor.close();                 //关闭
                imageView.setImageBitmap(image);              //将Bitmap类转化成图片对象。
            }
            catch (IOException e){
                Log.e("Ingram14","Image not found", e);
            }
        }
    }

}
