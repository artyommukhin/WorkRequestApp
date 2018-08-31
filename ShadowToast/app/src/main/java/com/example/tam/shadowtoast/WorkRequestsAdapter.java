package com.example.tam.shadowtoast;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class WorkRequestsAdapter extends ArrayAdapter<WorkRequest> {

    private Context context;
    private ArrayList<WorkRequest> workRequests;
    private LayoutInflater inflater;
    private int layout;



    public WorkRequestsAdapter(Context context, int layout, ArrayList<WorkRequest> workRequests){
        super(context, layout);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
        this.workRequests = workRequests;
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

        final WorkRequest workRequest = workRequests.get(position);

        viewHolder.title.setText(workRequest.getTitle());
        viewHolder.payment.setText(String.format("%d руб.", workRequest.getPayment()));

        if (workRequest.isComplete()){
            viewHolder.completed.setText("Готово");
            viewHolder.completed.setTextColor(context.getResources().getColor(R.color.colorWorkComplete));
        }
        else{
            viewHolder.completed.setText("Не готово");
            viewHolder.completed.setTextColor(context.getResources().getColor(R.color.colorWorkIncomplete));
        }

        viewHolder.completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!workRequest.isComplete()) {

                    workRequest.setComplete(true);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "workrequestsdb").build();
                            db.getWorkRequestDao().update(workRequest);
                        }
                    });

                    Toast.makeText(context, "Заявка №" + workRequest.getId() + " отправлена в БД", Toast.LENGTH_SHORT).show();
                }

                Button button = viewHolder.completed;
                button.setText("Готово");
                button.setTextColor(context.getResources().getColor(R.color.colorWorkComplete));
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

    @Override
    public int getCount() {
        return workRequests.size();
    }

    @Nullable
    @Override
    public WorkRequest getItem(int position) {
        return workRequests.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
