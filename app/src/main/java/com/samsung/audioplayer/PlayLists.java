package com.samsung.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class PlayLists extends AppCompatActivity {
    final String toMusic ="/storage/self/primary/Music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_lists);
    }
}