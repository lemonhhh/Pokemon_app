package com.example.pokedex;

import android.media.Image;
//Pokemon对象的容器，包含了Pokemon的name和url（这个属性是根据导入的API网站的数据来调整的）
public class Pokemon {
    private String name;
    private String url;

    Pokemon(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName(){
        return  name;
    }

    public String getUrl(){
        return url;
    }

}
