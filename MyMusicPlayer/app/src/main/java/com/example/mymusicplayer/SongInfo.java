package com.example.mymusicplayer;

import java.io.Serializable;

public class SongInfo implements Serializable {
    String data;
    String name;
    String singer;
//    String image;
    public SongInfo(String data,String name,String singer)
    {
        this.data = data;
        this.name = name;
        this.singer = singer;
//        this.image = image;
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String getSinger() {
        return singer;
    }

//    public String getImage() {
//        return image;
//    }
}
