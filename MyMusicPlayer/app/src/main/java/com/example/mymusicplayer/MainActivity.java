package com.example.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.mymusicplayer.PlayNewAudio";
    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<SongInfo>audioList;
    RecyclerView recyclerView;
    ImageView playBtn;
    ImageView previousBtn;
    ImageView nextBtn;
    SeekBar seekBar;
    Handler mHandler;
    TextView pastText;
    TextView remText;

    boolean isPause;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler(getMainLooper());
        isPause=false;
        player=null;
        playBtn = findViewById(R.id.playBtn);
        previousBtn = findViewById(R.id.prevBtn);
        nextBtn = findViewById(R.id.nextBtn);
        seekBar = findViewById(R.id.seekBar);
        pastText = findViewById(R.id.pastTextView);
        remText = findViewById(R.id.remTextView);
        playBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        audioList = new ArrayList<>();
        permisssion();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                   int curpos = seekBar.getProgress()*1000;
                   if(player!=null)
                       player.goToPos(curpos);
            }
        });
    }

    public void initRecyclerView()
    {
        recyclerView = findViewById(R.id.recyclerView);
        SongAdapter adapter  = new SongAdapter(this,audioList);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

//            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void playAudio(int audioIndex) {
        //Check is service is active
        playBtn.setImageResource(R.drawable.pause);
        if (!serviceBound) {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
//            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            SeekBarSetter thread = new SeekBarSetter(mHandler);
            thread.run();
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
            SeekBarSetter thread = new SeekBarSetter(mHandler);
            thread.run();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    public boolean permisssion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            return false;
        } else
        {
            loadAudio();
            initRecyclerView();
            return true;
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                audioList.add(new SongInfo(data, title,artist));
            }
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            loadAudio();
            initRecyclerView();
        }
        else
            Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id)
        {
            case R.id.playBtn: if(player==null)
                playAudio(0);
                else if(isPause==false) {
                player.pauseMedia();
                isPause = true;
                playBtn.setImageResource(R.drawable.play);
                }
                else{
                    player.resumeMedia();
                    isPause=false;
                    playBtn.setImageResource(R.drawable.pause);
                 }
            break;
            case R.id.prevBtn:
                player.skipToPrevious();
                break;
            case R.id.nextBtn:
                player.skipToNext();

        }
    }

    public class SeekBarSetter implements Runnable
    {
        Handler mHandler;
        public SeekBarSetter(Handler mHandler)
        {
            this.mHandler = mHandler;
        }
        @Override
        public void run() {
            if(player!=null)
            {
                int maxDuration = player.getDuration();
                int curpos = player.getCurrentPos();
                int rem = maxDuration-curpos;
                int minpast = curpos/60000;
                int secpast = (curpos-minpast*60000)/1000;
                int minrem = rem/60000;
                int secrem = (rem - minrem*60000)/1000;
                String past,topass;
                 if(secpast<=9)
                    past = ""+minpast+":0"+secpast;
                 else
                     past = ""+minpast+":"+secpast;
                 if(secrem<=9)
                     topass = "-"+minrem+":0"+secrem;
                 else
                     topass = "-"+minrem+":"+secrem;
                seekBar.setMax(maxDuration/1000);
                seekBar.setProgress(curpos/1000);
                pastText.setText(past);
                remText.setText(topass);
            }
            mHandler.postDelayed(this,1000);
        }
    }
}