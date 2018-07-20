package com.example.tam.shadowtoast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class WorkRequestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_requests);

        Button startAlarmButton = findViewById(R.id.start_alarm_button);
        Button stopAlarmButton = findViewById(R.id.stop_alarm_button);
        ListView workRequestsLV = findViewById(R.id.work_requests_lv);

        ArrayList<WorkRequest> workRequestsFromDb = new ArrayList<>();
        try {
           workRequestsFromDb = new AddWorkRequestsToDb().execute().get();
        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
        final ArrayList<WorkRequest> workRequests = workRequestsFromDb;

        WorkRequestsAdapter adapter = new WorkRequestsAdapter(this, R.layout.work_requests_list_item, workRequests);
        workRequestsLV.setAdapter(adapter);

        final Intent intent = new Intent(this, WorkRequestService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        final AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        startAlarmButton.setOnClickListener(new View.OnClickListener() {
            boolean alarmSet = PendingIntent.getBroadcast(getApplicationContext(), 0 , intent, PendingIntent.FLAG_NO_CREATE) != null;
            @Override
            public void onClick(View v) {
                if (!alarmSet){
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 20 * 1000, pendingIntent);
                    Toast.makeText(getApplicationContext(),"AlarmService запущен", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmManager.cancel(pendingIntent);
                Toast.makeText(getApplicationContext(), "AlarmService остановлен", Toast.LENGTH_SHORT).show();
            }
        });

        workRequestsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WorkRequest workRequest = (WorkRequest) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), workRequest.getDescription(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    class AddWorkRequestsToDb extends AsyncTask<Void, Void, ArrayList<WorkRequest>>{

        AppDatabase db;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"workrequestsdb").build();
        }

        @Override
        protected ArrayList<WorkRequest> doInBackground(Void... voids) {
            WorkRequest wr1 = new WorkRequest(
                       1,
                       "Забить гвоздь",
                       "В гипсовую стену",
                       10,
                       dateParse("2018-01-01 00:00:00"),
                       dateParse("2018-02-01 00:00:00"),
                       false,
                       false);

               WorkRequest wr2 = new WorkRequest(
                       2,
                       "Сменить лампочку",
                       "Лампочка за счёт заказчика",
                       5,
                       dateParse("2018-01-01 00:00:00"),
                       dateParse("2018-02-01 00:00:00"),
                       false,
                       false);

               WorkRequest wr3 = new WorkRequest(
                       3,
                       "Наклеить обои",
                       "Обои за счёт заказчика",
                       300,
                       dateParse("2018-01-01 00:00:00"),
                       dateParse("2018-02-01 00:00:00"),
                       false,
                       false);

               WorkRequest[] list = {wr1, wr2, wr3};
               db.getWorkRequestDao().deleteAll();
               db.getWorkRequestDao().insertAll(list);

            return (ArrayList<WorkRequest>) db.getWorkRequestDao().getAllWorkRequests();
        }

        @Override
        protected void onPostExecute(ArrayList<WorkRequest> workRequests) {
            super.onPostExecute(workRequests);

        }
    }

    public Date dateParse(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date = new Date();
        try{
            date = format.parse(dateString);
        }catch (ParseException e){
            Toast.makeText(this,dateString + "did not parsed",Toast.LENGTH_LONG).show();
        }
        return date;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
