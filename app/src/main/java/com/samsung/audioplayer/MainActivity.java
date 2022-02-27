package com.samsung.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    final String toMusic ="/storage/self/primary/Music";
    TextView position, lastPosition;
    ImageView previous, play, pause, next, playList;
    SeekBar seekBar;
    MediaPlayer player;
    MyThread myThread;
    ArrayList<String> arrayList;
    public ArrayList<File> files;
    public ListView listView;
    final static int MY_PERMISSION_REQUEST = 1;
    Uri songUri;
    int songPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
        }


        pause = findViewById(R.id.pause);
        pause.setOnClickListener(this);
        play = findViewById(R.id.play);
        play.setOnClickListener(this);
        previous = findViewById(R.id.previous);
        previous.setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        playList = findViewById(R.id.playList);
        playList.setOnClickListener(this);


        player = MediaPlayer.create(this, R.raw.slipknotvermilionpt1);
        lastPosition = findViewById(R.id.lastPosition);
        lastPosition.setText(timeFormatter(player.getDuration()));
        position = findViewById(R.id.position);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(player.getDuration());
        myThread = new MyThread();
        myThread.start();
        File directory = new File(String.valueOf(toMusic));
        File[] filesRaw = directory.listFiles();
        files = new ArrayList<>();
        for (int i = 0; i < filesRaw.length; i++) {
            if(filesRaw[i].isFile()){
                files.add(filesRaw[i]);
            }
        }
        arrayList = new ArrayList<>();
        listView = findViewById(R.id.listView);
        ArrayAdapter<File> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        listView.setAdapter(adapter);
//        getMusic();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                songUri = Uri.parse(String.valueOf(files.get(position)));
                songPos = position;
                try {
                    player.stop();
                    player = new MediaPlayer();
                    player = MediaPlayer.create(MainActivity.this, songUri);
                    playSong();
                    lastPosition.setText(timeFormatter(player.getDuration()));
                    seekBar.setMax(player.getDuration());
                    seekBar.setOnSeekBarChangeListener(MainActivity.this);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"song", Toast.LENGTH_LONG).show();

                }
            }
        });


    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:{
                playSong();
                break;
            }
            case R.id.pause:{
                pauseSong();
                break;
            }
            case R.id.previous:{
                try {
                    songUri = Uri.parse(String.valueOf(files.get(songPos-1)));
                    songPos --;
                }catch (Exception e){
                    songUri = Uri.parse(String.valueOf(files.get(files.size()-1)));
                    songPos = files.size()-1;
                }finally {
                    newSongStart();
                }
                break;
            }
            case R.id.next:{
                try {
                    songUri = Uri.parse(String.valueOf(files.get(songPos+1)));
                    songPos ++;
                }catch (Exception e){
                    songUri = Uri.parse(String.valueOf(files.get(0)));
                    songPos = 0;
                }finally {
                    newSongStart();
                }
                break;
            }
            case R.id.playList:{
                Intent intent = new Intent(this, PlayLists.class);
                startActivity(intent);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }


    public void playSong(){
        play.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
        player.start();
    }
    public void pauseSong(){
        play.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        player.pause();
    }
    public void newSongStart(){
        player.stop();
        player = new MediaPlayer();
        player = MediaPlayer.create(MainActivity.this, songUri);
        playSong();
        lastPosition.setText(timeFormatter(player.getDuration()));
        seekBar.setMax(player.getDuration());
        seekBar.setOnSeekBarChangeListener(MainActivity.this);
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

//    public void getMusic() {
//        ContentResolver contentResolver = getContentResolver();
//        songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
//        Log.d("no Tag", "1");
//        if (songCursor != null && songCursor.moveToFirst()) {
//            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
//            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
//            do {
//                String currentTitle = songCursor.getString(songTitle);
//                String currentArtist = songCursor.getString(songArtist);
//                arrayList.add(currentTitle + " " + currentArtist);
//                Log.d("My Tag", currentTitle + " " + currentArtist);
//            } while (songCursor.moveToNext());
//        }
//        Log.d("My Tag", String.valueOf(arrayList.size()));
//    }

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
                try {
                    if (String.valueOf(position.getText()).equals(String.valueOf(lastPosition.getText()))){
                        try {
                            songUri = Uri.parse(String.valueOf(files.get(songPos+1)));
                            songPos ++;
                        }catch (Exception e){
                            songUri = Uri.parse(String.valueOf(files.get(0)));
                            songPos = 0;
                        }finally {
                            newSongStart();
                        }
                    }
                }catch (Exception ignored){

                }
            }
        }
    }
}