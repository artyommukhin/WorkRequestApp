package com.example.tam.shadowtoast;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.Toast;
import android.database.sqlite.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorkRequestService extends IntentService {

    static boolean isConnection;

    public WorkRequestService() {
        super("dbUpdate");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
                }
            });

            AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "workrequestsdb").build();

            ArrayList<WorkRequest> workRequestsInAppDb = (ArrayList<WorkRequest>) db.getWorkRequestDao().getAllWorkRequests();

            ArrayList<WorkRequest> workRequestsToSend = new ArrayList<>();
            for (WorkRequest request : workRequestsInAppDb){
                if (request.isComplete() && !request.isSentToDb()){
                    workRequestsToSend.add(request);
                }
            }

            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

            if (isConnection){

                for (WorkRequest requestToSend : workRequestsToSend){
                    final int id = requestToSend.getId();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Заявка №" + id + " синхронизирована", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                alarmManager.cancel(pendingIntent);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Alarm cancelled from service", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Сервис не подключился к инету, попробует ещё раз через 20 сек", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"Сервис уничтожен", Toast.LENGTH_SHORT).show();
    }
}
