package com.example.tam.shadowtoast;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class WorkRequestsActivity extends AppCompatActivity implements LocationListener {

    final int FINE_LOCATION_PERMISSION_RESULT = 1;
    final static int MIN_UPDATE_TIME = 10 * 1000;
    final static int MIN_UPDATE_DISTANCE = 100;

    TextView longitude, latitude;
    LocationManager locationManager;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_requests);

        Button startAlarmButton = findViewById(R.id.start_alarm_button);
        Button stopAlarmButton = findViewById(R.id.stop_alarm_button);
        ListView workRequestsLV = findViewById(R.id.work_requests_lv);

        Button startLocationUpdate = findViewById(R.id.start_location_upd_button);
        Button stopLocationUpdate = findViewById(R.id.stop_location_upd_button);

        longitude = findViewById(R.id.longitude_tv);
        latitude = findViewById(R.id.latitude_tv);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final Intent intent = new Intent(this, WorkRequestService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        startAlarmButton.setOnClickListener(new View.OnClickListener() {
            boolean alarmSet = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null;

            @Override
            public void onClick(View v) {
                if (!alarmSet) {
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 5 * 1000, 60 * 1000, pendingIntent);
                    Toast.makeText(getApplicationContext(), "AlarmService запущен", Toast.LENGTH_SHORT).show();
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

        ArrayList<WorkRequest> workRequestsFromDb = new ArrayList<>();
        try {
            workRequestsFromDb = new AddWorkRequestsToDb().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        final ArrayList<WorkRequest> workRequests = workRequestsFromDb;

        WorkRequestsAdapter adapter = new WorkRequestsAdapter(this, R.layout.work_requests_list_item, workRequests);
        workRequestsLV.setAdapter(adapter);

        workRequestsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WorkRequest workRequest = (WorkRequest) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), workRequest.getDescription(), Toast.LENGTH_SHORT).show();
            }
        });

        startLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        stopLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.removeUpdates(WorkRequestsActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_RESULT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        new AlertDialog.Builder(this)
                                .setMessage("Необходимо включить геолокацию для определения точного местоположения")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_RESULT);
                                    }
                                })
                                .setNegativeButton("Отмена", null)
                                .show();
                    } else{
                        Toast.makeText(this, "Отсутсвует разрешение, координаты недоступны", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Геолокация не включена")
                .setMessage("Перейти в настройки?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            switch (requestCode) {
                case 1:
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        showSettingsAlert();
                    }else {
                        getLocation();
                    }
                    break;
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // from GPS
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_UPDATE_TIME,
                        MIN_UPDATE_DISTANCE, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // from Network Provider
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_UPDATE_TIME,
                        MIN_UPDATE_DISTANCE, this);
            } else{
                showSettingsAlert();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_RESULT);
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            changeCurrentLocationIfBetter(networkLocation);
            changeCurrentLocationIfBetter(gpsLocation);

            updateUI(currentLocation);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_RESULT);
        }

    }

    private void updateUI(Location loc) {
        if(loc!=null) {
            latitude.setText(String.format("Широта: %.6f", loc.getLatitude()));
            longitude.setText(String.format("Долгота: %.6f", loc.getLongitude()));
        }
    }


    //LocationListener
    @Override
    public void onLocationChanged(Location location) {
        changeCurrentLocationIfBetter(location);
        updateUI(currentLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, provider + " on", Toast.LENGTH_SHORT).show();
        getLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, provider + " off", Toast.LENGTH_SHORT).show();
    }

    void changeCurrentLocationIfBetter(Location location) {
        if (location != null) {
            if (currentLocation == null) {
                // A new location is always better than no location
                currentLocation = location;
                return;
            }

            // Check whether the new location fix is newer or older

            long timeDelta = location.getTime() - currentLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > 60 * 1000;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                currentLocation = location;
                // If the new location is more than two minutes older, it must be worse
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(), currentLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                currentLocation = location;
            } else if (isNewer && !isLessAccurate) {
                currentLocation = location;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                currentLocation = location;
            }
        }
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    //SQLite
    class AddWorkRequestsToDb extends AsyncTask<Void, Void, ArrayList<WorkRequest>> {

        AppDatabase db;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "workrequestsdb").build();
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
                    false);

            WorkRequest wr2 = new WorkRequest(
                    2,
                    "Сменить лампочку",
                    "Лампочка за счёт заказчика",
                    5,
                    dateParse("2018-01-01 00:00:00"),
                    dateParse("2018-02-01 00:00:00"),
                    false);

            WorkRequest wr3 = new WorkRequest(
                    3,
                    "Наклеить обои",
                    "Обои за счёт заказчика",
                    300,
                    dateParse("2018-01-01 00:00:00"),
                    dateParse("2018-02-01 00:00:00"),
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

        Date dateParse(String dateString) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date date = new Date();
            try {
                date = format.parse(dateString);
            } catch (ParseException e) {
                Toast.makeText(getApplicationContext(), dateString + "did not parsed", Toast.LENGTH_LONG).show();
            }
            return date;
        }
    }
}
