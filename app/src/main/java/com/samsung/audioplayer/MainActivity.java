package com.samsung.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    TextView position, lastPosition;
    ImageView previous, play, pause, next;
    SeekBar seekBar;
    MediaPlayer player;
    MyThread myThread;
    ArrayList<String> arrayList;
    final static int MY_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        }
        Log.d("no Tag", "1");
        pause = findViewById(R.id.pause);
        pause.setOnClickListener(this);
        play = findViewById(R.id.play);
        play.setOnClickListener(this);
        previous = findViewById(R.id.previous);
        previous.setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        player = MediaPlayer.create(this, R.raw.slipknotvermilionpt1);
        lastPosition = findViewById(R.id.lastPosition);
        lastPosition.setText(timeFormatter(player.getDuration()));
        position = findViewById(R.id.position);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(player.getDuration());
        myThread = new MyThread();
        myThread.start();
        arrayList = new ArrayList<>();
        getMusic();
        Log.d("no Tag", "1");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:{
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                player.start();
                break;
            }
            case R.id.pause:{
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
                player.pause();
                break;
            }
            case R.id.previous:{

                break;
            }
            case R.id.next:{

                break;
            }
        }
    }

    public String timeFormatter(int time){
        time /= 1000;
        int second = time % 60;
        int minute = time / 60;
        String results = "";
        if (minute % 100 / 10 == 0) {
            results += "0";
        }
        results += minute;
        results += ":";
        if (second % 100 / 10 == 0) {
            results += "0";
        }
        return results + second;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        position.setText(timeFormatter(player.getCurrentPosition()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.seekTo(seekBar.getProgress());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:{
                if (grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "permission failed", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
        Log.d("no Tag", "1");
        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                arrayList.add(currentTitle + " " + currentArtist);
                Log.d("My Tag", currentTitle + " " + currentArtist);
            } while (songCursor.moveToNext());
        }
        Log.d("My Tag", String.valueOf(arrayList.size()));
    }

    class MyThread extends Thread{
        @Override
        public void run() {
            while (true) {
                seekBar.setProgress(player.getCurrentPosition());
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}