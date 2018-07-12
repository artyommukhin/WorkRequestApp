package com.example.tam.shadowtoast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;
import static com.example.tam.shadowtoast.WorkRequestsActivity.completedRequests;
import static com.example.tam.shadowtoast.WorkRequestsActivity.completedRequestsContain;
import static com.example.tam.shadowtoast.WorkRequestsActivity.removeFromCompleted;

public class WorkRequestsAdapter extends ArrayAdapter<WorkRequest> {

    public static boolean alarmSet;

    private Context context;
    private ArrayList<WorkRequest> notCompletedRequests;
    private LayoutInflater inflater;
    private int layout;


    public WorkRequestsAdapter(Context context, int layout, ArrayList<WorkRequest> workRequests){
        super(context, layout, workRequests);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
        notCompletedRequests = getNotCompletedRequests(workRequests);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final WorkRequest workRequest = notCompletedRequests.get(position);

        viewHolder.title.setText(workRequest.getTitle());
        viewHolder.payment.setText(workRequest.getPayment() + " руб.");


        viewHolder.completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button button = viewHolder.completed;

                if (!completedRequestsContain(workRequest)){
                    completedRequests.add(workRequest);
                    button.setText("Выполнено");

                    if(!alarmSet){

                        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(context, WorkRequestService.class),0);
                        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 10000, pendingIntent);

                        alarmSet = true;
                        Toast.makeText(context,"Service started from adapter", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    removeFromCompleted(workRequest);
                    button.setText("Не выполнено");
                }
            }
        });

        return convertView;
    }

    private class ViewHolder{
        TextView title, payment;
        Button completed;
        ViewHolder(View v){
            title = v.findViewById(R.id.list_item_title);
            payment = v.findViewById(R.id.list_item_payment);
            completed = v.findViewById(R.id.list_item_completed_button);
        }
    }

    public ArrayList<WorkRequest> getNotCompletedRequests(ArrayList<WorkRequest> allRequests){
        ArrayList<WorkRequest> notCompletedRequests = new ArrayList<>();
        for (WorkRequest request : allRequests){
            if (!request.isCompleted()){
                notCompletedRequests.add(request);
            }
        }
        return notCompletedRequests;
    }

    @Override
    public int getCount() {
        return notCompletedRequests.size();
    }
}
