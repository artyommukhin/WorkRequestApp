package com.example.tam.shadowtoast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button toWorkRequestsActivity = findViewById(R.id.goto_work_requests_activity_button);

        final Intent intentToWorkRequests = new Intent(MainActivity.this, WorkRequestsActivity.class);

        toWorkRequestsActivity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(intentToWorkRequests);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showToast(View v){
        Toast.makeText(getApplicationContext(),"I'm toast!", Toast.LENGTH_LONG).show();
    }


}
