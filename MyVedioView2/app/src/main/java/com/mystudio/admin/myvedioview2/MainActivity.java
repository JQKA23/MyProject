package com.mystudio.admin.myvedioview2;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl{
    private TextView tvName;
    private VideoView vv;

    private String path= "http://121.40.57.194/web/img/19e680c6a9edb6c5fbcc57e08c9feb0a.mp4";//要播放的视频路径
//    private String path="/DCIM/Camera/VID_20170121_172735.mp4";//要播放的视频路径

    private void assignViews() {
        tvName = (TextView) findViewById(R.id.tv_name);
        vv = (VideoView) findViewById(R.id.vv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assignViews();

        Log.i("-----------------------", Environment.getExternalStorageDirectory() + path);
//        File file=new File(Environment.getExternalStorageDirectory() + path);
//        Log.i("-----------------------", file.exists()+"");

        Uri uri= Uri.parse(path);
//        vv.setVideoPath(Environment.getExternalStorageDirectory() + path);//设置视频路径
        vv.setVideoURI(uri);//设置视频路径
        //设置视频控制器,
        vv.setMediaController(new MediaController(this));
        //开始播放
        vv.start();
        //设置当前播放器窗口为焦点
        vv.requestFocus();
        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
