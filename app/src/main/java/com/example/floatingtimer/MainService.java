package com.example.floatingtimer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    private WindowManager mwindow;
    private LinearLayout layout;
    private TextView textView;
    int layoutParamsType;
    long sec = 0;
    long savedSec = 0;
    long lastTime;
    private Timer timer;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})

    @Override
    public void onDestroy(){

        Log.d("logg1", "destroyed");
        timer.cancel();
        mwindow.removeView(layout);
        stopSelf();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("channel01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
            Notification builder = new NotificationCompat.Builder(this, "channel01")
                    .setContentTitle("My Foreground Service")
                    .setContentText("Running in the foreground")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build();

            startForeground(1252, builder);


        return START_STICKY;

    }

    @Override
    public void onCreate (){
        super.onCreate();

        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        lastTime = System.currentTimeMillis() / 1000;
        layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.argb(0,255,255,255));
        layout.setGravity(Gravity.CENTER);

        textView = new TextView(this);
        layout.addView(textView);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        Handler handler = new Handler(Looper.getMainLooper());

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if(!myKM.inKeyguardRestrictedInputMode()) {
                    sec = savedSec + (System.currentTimeMillis() / 1000) - lastTime;

                }else{

                    savedSec = sec;
                    lastTime = (System.currentTimeMillis() / 1000);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        int hours = (int) (sec / 3600);
                        int minutes = (int) ((sec % 3600) / 60);
                        int seconds = (int) (sec % 60);

                        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        textView.setText(timeString);
                    }
                });
            }

        }, 0, 50);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParamsType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            layoutParamsType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mwindow = (WindowManager)getSystemService (WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams  (
                200,
                50,
                layoutParamsType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT
        );

        params.gravity = Gravity.END | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        mwindow.addView (layout,params);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

