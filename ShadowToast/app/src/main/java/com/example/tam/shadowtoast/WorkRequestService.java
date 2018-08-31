package com.example.tam.shadowtoast;

import android.Manifest;
import android.app.IntentService;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class WorkRequestService extends IntentService {

    Location currentLocation;
    LocationManager locationManager;

    public WorkRequestService() {
        super("dbUpdate");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Сервис запущен", Toast.LENGTH_SHORT).show();
                }
            });

            AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "workrequestsdb").build();

            if (isOnline()) {
                //SEND DATA TO DB
                ArrayList<WorkRequest> workRequestsInAppDb = (ArrayList<WorkRequest>) db.getWorkRequestDao().getAllWorkRequests();
                if (workRequestsInAppDb != null) {
                    for (WorkRequest request : workRequestsInAppDb) {
                        if (request.isComplete()) {
                            final int id = request.getId();

                            try {
                                URL url = new URL(getString(R.string.server_query_url));
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setConnectTimeout(5000);
                                connection.setReadTimeout(3000);
                                connection.setDoOutput(true);//POST

                                Uri.Builder builder = new Uri.Builder()
                                        .appendQueryParameter("id", Integer.toString(request.getId()));

                                String query = builder.build().getEncodedQuery();

                                OutputStream os = connection.getOutputStream();
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                writer.write(query);
                                writer.flush();
                                writer.close();
                                os.close();
                                connection.connect();

                                String response = null;
                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    InputStream input = connection.getInputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                                    StringBuilder result = new StringBuilder();
                                    String line;

                                    while ((line = reader.readLine()) != null) {
                                        result.append(line);
                                    }
                                    response = result.toString();
                                }

                                JSONObject jResponse = new JSONObject(response);
                                boolean error = jResponse.getBoolean("error");
                                if (!error) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Заявка №" + id + " синхронизирована", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (final Exception e) {
                                Log.d("WRService", e.getMessage());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Ошибка синхронизации заявки №" + id, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }
                }
            }else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_LONG).show();
                    }
                });
            }

            //LOCATION
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                changeCurrentLocationIfBetter(networkLocation);
                changeCurrentLocationIfBetter(gpsLocation);

                if (currentLocation != null) {
                    final String latitude = String.format("Широта: %.6f", currentLocation.getLatitude());
                    final String longitude = String.format("Долгота: %.6f", currentLocation.getLongitude());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Текущие координаты:" +
                                            "\n" + latitude +
                                            "\n" + longitude, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Координаты не определены", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Нет разрешений для геолокации", Toast.LENGTH_SHORT).show();
                    }
                });
            }
          /*PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Alarm отменён сервисом", Toast.LENGTH_SHORT).show();
                }
            });*/
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

    public boolean isOnline() {
        try {
            Socket socket = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            socket.connect(sockaddr, 1500);
            socket.close();
            return true;
        } catch (IOException e) { return false; }
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
