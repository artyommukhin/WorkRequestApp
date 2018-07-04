package com.example.tam.shadowtoast;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

public class UnsentRequestCheckService extends IntentService {
    public UnsentRequestCheckService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    TimerTask check = new TimerTask() {
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Toast.makeText(getApplicationContext(),"Check",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this,"Service started", Toast.LENGTH_SHORT).show();
        new Timer(true).schedule(check, 0, 10 * 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        check.cancel();
        Toast.makeText(this,"Service destroyed", Toast.LENGTH_LONG).show();
    }


}
