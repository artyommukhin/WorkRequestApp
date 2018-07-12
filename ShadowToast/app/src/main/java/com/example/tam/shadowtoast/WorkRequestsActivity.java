package com.example.tam.shadowtoast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static com.example.tam.shadowtoast.WorkRequestsAdapter.alarmSet;

public class WorkRequestsActivity extends AppCompatActivity {

    public static ArrayList<WorkRequest> completedRequests = new ArrayList<>();

    WorkRequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_requests);

        final AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        Button startAlarmButton = findViewById(R.id.start_alarm_button);
        Button stopAlarmButton = findViewById(R.id.stop_alarm_button);

        final PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, WorkRequestService.class),0);

        startAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alarmSet){
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 10000, pendingIntent);
                    Toast.makeText(getApplicationContext(),"AlarmService запущен", Toast.LENGTH_SHORT).show();
                    alarmSet = true;
                }
            }
        });

        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarmSet) {
                    alarmManager.cancel(pendingIntent);
                    Toast.makeText(getApplicationContext(), "AlarmService остановлен", Toast.LENGTH_SHORT).show();
                    alarmSet = false;
                }
            }
        });

        ArrayList<WorkRequest> workRequests = new ArrayList<>();
        addWorkRequests(workRequests);

        ListView workRequestsLV = findViewById(R.id.work_requests_lv);

        adapter = new WorkRequestsAdapter(this, R.layout.work_requests_list_item, workRequests);
        workRequestsLV.setAdapter(adapter);

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WorkRequest workRequest = (WorkRequest) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), workRequest.getDescription(),Toast.LENGTH_SHORT).show();
            }
        };
        workRequestsLV.setOnItemClickListener(itemClickListener);
    }

    public void removeRequestsFromListView(ArrayList<WorkRequest> workRequests){
        for (WorkRequest request : workRequests) {

        }
    }

    public static boolean completedRequestsContain(WorkRequest request){
        for (WorkRequest cRequest : completedRequests) {
            if (request.getId()==cRequest.getId()){
                return true;
            }
        }
        return false;
    }

    public static void removeFromCompleted(WorkRequest request){
        Iterator<WorkRequest> iterator = completedRequests.iterator();
        while (iterator.hasNext()){
            WorkRequest cRequest = iterator.next();
            if (cRequest.getId()==request.getId()) {
                iterator.remove();
            }
        }
    }

    private void addWorkRequests(ArrayList<WorkRequest> list){
        list.add(new WorkRequest(
                1,
                "Забить гвоздь",
                "В гипсовую стену",
                10,
                dateParse("2018-01-01 00:00:00"),
                dateParse("2018-02-01 00:00:00"),
                false));
        list.add(new WorkRequest(
                2,
                "Сменить лампочку",
                "Лампочка за счёт заказчика",
                5,
                dateParse("2018-01-01 00:00:00"),
                dateParse("2018-02-01 00:00:00"),
                false));
        list.add(new WorkRequest(
                3,
                "Наклеить обои",
                "Обои за счёт заказчика",
                200,
                dateParse("2018-01-01 00:00:00"),
                dateParse("2018-02-01 00:00:00"),
                false));
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

}
