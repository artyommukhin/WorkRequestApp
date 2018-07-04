package com.example.tam.shadowtoast;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startServiceButton = findViewById(R.id.start_service_button);
        Button stopServiceButton = findViewById(R.id.stop_service_button);

        startServiceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startService(new Intent(MainActivity.this, UnsentRequestCheckService.class));

            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               // stopService(new Intent(MainActivity.this, UnsentRequestCheckService.class));
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
