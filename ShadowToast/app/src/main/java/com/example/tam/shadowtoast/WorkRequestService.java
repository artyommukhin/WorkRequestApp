package com.example.tam.shadowtoast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import static com.example.tam.shadowtoast.WorkRequestsActivity.completedRequests;

public class WorkRequestService extends Service {

    public WorkRequestService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        for (WorkRequest request: completedRequests) {
            request.setCompleted(true);
        }
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, WorkRequestService.class),0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (completedRequests.size()==0) {

            alarmManager.cancel(pendingIntent);
            WorkRequestsAdapter.alarmSet = false;
            Toast.makeText(this,"Service cancelled from service", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this,"Count of unsent completed requests: " + completedRequests.size(), Toast.LENGTH_SHORT).show();

        this.stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
