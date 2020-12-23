package com.example.audiorecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    AudioManager am = null;
    AudioRecord record =null;
    AudioTrack track =null;
    static String TAG = "AUDIOTEST";
    boolean recordAndPlay_run_flag =true;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        verifyAudioPermissions(this);
        recordAndPlay_run_flag =true;
        init();
        t=new Thread() {
            @Override
            public void run() {
                recordAndPlay();
            }
        };
        t.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        recordAndPlay_run_flag =false;
        Log.d(TAG,"stop recordAndPlay");
        try {
            if(t != null)
                t.join();//注意这里
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        record.stop();
        track.stop();
        Log.d(TAG,"stoped recordAndPlay");
    }

    public void verifyAudioPermissions(Activity activity) {
        int permission = this.checkSelfPermission(Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"request permission");
            requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 1);
        }else{
            Log.d(TAG,"have got permission");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void init() {
        Log.d(TAG,"creat record...");
        int min = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);
        Log.d(TAG,"creat audiotrack...");
        int maxJitter = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 48000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
    }

    private void recordAndPlay() {
        short[] lin = new short[1024];
        int num = 0;
        int num2 = 0;
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        record.startRecording();
        track.play();
        while (recordAndPlay_run_flag ) {
            Log.d(TAG,"read...");
            num = record.read(lin, 0, 1024);
            Log.d(TAG,"read:"+ num);
            num2 = track.write(lin, 0, num);
            Log.d(TAG,"write:"+ num2);
        }
    }

    boolean isSpeaker = false;

    public void modeChange(View view) {
        Button modeBtn=(Button) findViewById(R.id.modeBtn);
        if (isSpeaker == true) {
            am.setSpeakerphoneOn(false);
            isSpeaker = false;
            modeBtn.setText("Call Mode");
        } else {
            am.setSpeakerphoneOn(true);
            isSpeaker = true;
            modeBtn.setText("Speaker Mode");
        }
    }

    boolean isPlaying=true;
    public void play(View view){
        Button playBtn=(Button) findViewById(R.id.playBtn);
        if(isPlaying){
            record.stop();
            track.pause();
            isPlaying=false;
            playBtn.setText("Play");
        }else{
            record.startRecording();
            track.play();
            isPlaying=true;
            playBtn.setText("Pause");
        }
    }
}