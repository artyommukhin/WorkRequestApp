package com.example.tam.shadowtoast;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;

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
                    Toast.makeText(getApplicationContext(), "Сервис запущен", Toast.LENGTH_SHORT).show();
                }
            });

            AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "workrequestsdb").build();

            ArrayList<WorkRequest> workRequestsInAppDb = (ArrayList<WorkRequest>) db.getWorkRequestDao().getAllWorkRequests();

            ArrayList<WorkRequest> workRequestsToSend = new ArrayList<>();
            for (WorkRequest request : workRequestsInAppDb){
                if (request.isComplete()){
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
                        Toast.makeText(getApplicationContext(),"Alarm отменён сервисом", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Нет интернета, повтор через 1 минуту", Toast.LENGTH_SHORT).show();
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
