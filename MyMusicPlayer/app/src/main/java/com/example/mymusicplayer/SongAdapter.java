package com.example.mymusicplayer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

   MainActivity context;
   ArrayList<SongInfo>_songinfo= new ArrayList<>();
   int colorpos=-1;
   ViewHolder prev;

   public SongAdapter(MainActivity context,ArrayList<SongInfo>songs)
   {
       this.context =context;
       this._songinfo = songs;
       prev = null;
   }

    public interface PlayOption
    {
        public void playsong(int index);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item,parent,false);
       ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.singerText.setText(_songinfo.get(position).getSinger());
        holder.songText.setText(_songinfo.get(position).getName());
        holder.art.setImageResource(R.drawable.image);
        if(colorpos!=position)
            holder.parent_layout.setBackgroundColor(Color.WHITE);
        else
            holder.parent_layout.setBackgroundColor(Color.MAGENTA);
        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.playAudio(position);
                if(prev!=null)
                    prev.parent_layout.setBackgroundColor(Color.WHITE);
                holder.parent_layout.setBackgroundColor(Color.MAGENTA);
                colorpos=position;
                prev = holder;
            }
        });
    }

    @Override
    public int getItemCount() {
        return _songinfo.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder
   {
         TextView songText;
         TextView singerText;
         ImageView art;
         RelativeLayout parent_layout;
         public ViewHolder(@NonNull View itemView) {
           super(itemView);
           art = itemView.findViewById(R.id.thumbImage);
           songText = itemView.findViewById(R.id.songText);
           singerText = itemView.findViewById(R.id.singerText);
           parent_layout = itemView.findViewById(R.id.parent_layout);
       }
   }
}
