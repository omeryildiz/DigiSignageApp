package org.omeryildiz.dukatvapplication;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int INTERVAL = 3000; // poll every 3 secs
    private static String APP_PACKAGE_NAME;

    private static boolean stopTask;
    private PowerManager.WakeLock mWakeLock;


    private static final String VIDEO_SAMPLE =
            "https://developers.google.com/training/images/tacoma_narrows.mp4";

    private VideoView mVideoView;
    private ImageView tvBanner;
    private Toolbar toolbar;
    private static String videoUrl="/storage/emulated/0/Sync";
    private String constVideoUrl="/storage/emulated/0/";
    private static String fileVideoName = "playbackvideo.mp4";

    public static void setFileVideoName(String fileVideoName) {
        MainActivity.fileVideoName = fileVideoName;
    }

    public static String getFileVideoName() {
        return fileVideoName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = "";
        this.videoUrl = this.constVideoUrl + videoUrl;
    }

    public static String getAppPackageName() {
        return APP_PACKAGE_NAME;
    }

    public static void setAppPackageName(String appPackageName) {
        APP_PACKAGE_NAME = appPackageName;
    }

    // Current playback position (in milliseconds).
    private int mCurrentPosition = 0;

    // Tag for the instance state bundle.
    private static final String PLAYBACK_TIME = "play_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.VISIBLE);
        setAppPackageName(getApplicationContext().getPackageName());


        mVideoView = findViewById(R.id.videoView);
        //mBufferingTextView = findViewById(R.id.buffering_textview);
        //mBufferingTextView.setVisibility(VideoView.INVISIBLE);
        tvBanner = findViewById(R.id.imageView);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }



        /*
        FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(this);
        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                Toast toast=Toast. makeText(getApplicationContext(),path,Toast. LENGTH_SHORT);
                toast.setMargin(50,50);
                toast.show();
                setVideoUrl(path);
            }
        });
        fileListerDialog.setDefaultDir("/");
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.DIRECTORY_ONLY);
        fileListerDialog.show();

         */

        // Set up the media controller widget and attach it to the video view.
        setupSharedPreferences();
        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);
        checkApplicationStatus();
        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                checkBuffer(mVideoView);          // this method will contain your almost-finished HTTP calls
                handler.postDelayed(this, 5000);
            }
        }, 5000);

         */


        Toast toast=Toast. makeText(getApplicationContext(),getVideoUrl() + "/" + getFileVideoName(),Toast. LENGTH_LONG);
        //toast.setMargin(50,50);
        toast.show();

    }


    @SuppressLint("InvalidWakeLockTag")
    private void checkApplicationStatus() {
        stopTask = false;

        // Optional: Screen Always On Mode!
        // Screen will never switch off this way
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        // Start your (polling) task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // If you wish to stop the task/polling
                if (stopTask) {
                    this.cancel();
                }

                // The first in the list of RunningTasks is always the foreground task.
                ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                ActivityManager.RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);
                String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

                // Check foreground app: If it is not in the foreground... bring it!
                if (!foregroundTaskPackageName.equals(getAppPackageName())) {
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(getAppPackageName());
                    startActivity(LaunchIntent);
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, INTERVAL);
    }

    @Override
    protected void onDestroy() {
        stopTask = true;
        if (mWakeLock != null)
            mWakeLock.release();
        super.onDestroy();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setVideoUrl(sharedPreferences.getString("video_directory", "Sync"));
        setFileVideoName(sharedPreferences.getString("file_video_name", "playbackvideo.mp4"));
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Load the media each time onStart() is called.
        initializePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.VISIBLE);
        // In Android versions less than N (7.0, API 24), onPause() is the
        // end of the visual lifecycle of the app.  Pausing the video here
        // prevents the sound from continuing to play even after the app
        // disappears.
        //
        // This is not a problem for more recent versions of Android because
        // onStop() is now the end of the visual lifecycle, and that is where
        // most of the app teardown should take place.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Media playback takes a lot of resources, so everything should be
        // stopped and released at this time.
        releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current playback position (in milliseconds) to the
        // instance state bundle.
        outState.putInt(PLAYBACK_TIME, mVideoView.getCurrentPosition());
    }

    private void initializePlayer() {
        // Show the "Buffering..." message while the video loads.
        //mBufferingTextView.setVisibility(VideoView.VISIBLE);
        tvBanner.setVisibility(ImageView.VISIBLE);

        // Buffer and decode the video sample.
//        Uri videoUri = getMedia(VIDEO_SAMPLE);
        //Toast toast=Toast. makeText(getApplicationContext(),getVideoUrl() + "/" + getFileVideoName() + " testval",Toast. LENGTH_LONG);
        //toast.setMargin(50,50);
        //toast.show();
        //Uri videoUri = getMedia(getVideoUrl());

        //mVideoView.setVideoURI(videoUri);
        mVideoView.setVideoPath(getVideoUrl() + "/" + getFileVideoName());

        // Listener for onPrepared() event (runs after the media is prepared).
        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        //mediaPlayer.setLooping(true);

                        // Hide buffering message.
                        //mBufferingTextView.setVisibility(VideoView.INVISIBLE);
                        tvBanner.setVisibility(ImageView.INVISIBLE);
                        toolbar.setVisibility(View.INVISIBLE);


                        // Restore saved position, if available.
                        if (mCurrentPosition > 0) {
                            mVideoView.seekTo(mCurrentPosition);
                        } else {
                            // Skipping to 1 shows the first frame of the video.
                            mVideoView.seekTo(1);
                        }


                        // Start playing!
                        mVideoView.start();
                    }
                });



        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(getPackageName(), String.format("Error(%s%s)", what, extra));
                Handler handler = new Handler();
                handler.postDelayed((Runnable) () -> onStart(), 18000);
                return true;
            }
        });


        // Listener for onCompletion() event (runs after media has finished
        // playing).
        mVideoView.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        //Toast.makeText(MainActivity.this,
                        //        "Çalıştırılıyor...",
                        //        Toast.LENGTH_SHORT).show();

                        // Return the video position to the start.
                        mVideoView.seekTo(0);
                        onStart();
                    }
                });
    }

    private void checkBuffer(VideoView mediaPlayer) {
        Toast.makeText(MainActivity.this,
                "Buffer durumu yüzde olarak: " + mediaPlayer.getCurrentPosition() + " duration:  " + mediaPlayer.getDuration() ,
                Toast.LENGTH_SHORT).show();
        if (mediaPlayer.getCurrentPosition() + 500 >=  mediaPlayer.getDuration()) {
            releasePlayer();
            onStart();
        }
    }


    // Release all media-related resources. In a more complicated app this
    // might involve unregistering listeners or releasing audio focus.
    private void releasePlayer() {
        mVideoView.stopPlayback();

    }

    // Get a Uri for the media sample regardless of whether that sample is
    // embedded in the app resources or available on the internet.
    private Uri getMedia(String mediaName) {
        if (URLUtil.isValidUrl(mediaName)) {
            // Media name is an external URL.
            return Uri.parse(mediaName);
        } else {
            // Media name is a raw resource embedded in the app.
            return Uri.parse("android.resource://" + getPackageName() +
                    "/raw/" + mediaName);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Toast toast1=Toast. makeText(getApplicationContext(),key,Toast. LENGTH_SHORT);
        toast1.show();
        if(key.equals("video_directory")) {
            setVideoUrl(sharedPreferences.getString("video_directory", "Sync"));
        } else if (key.equals("file_video_name")) {
            setFileVideoName(sharedPreferences.getString("file_video_name", "playbackvideo.mp4"));
        }

    }

}