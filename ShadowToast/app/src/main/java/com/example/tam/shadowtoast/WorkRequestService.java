package com.example.tam.shadowtoast;

import android.Manifest;
import android.app.IntentService;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;


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

            ArrayList<WorkRequest> workRequestsInAppDb = (ArrayList<WorkRequest>) db.getWorkRequestDao().getAllWorkRequests();

            ArrayList<WorkRequest> workRequestsToSend = new ArrayList<>();
            for (WorkRequest request : workRequestsInAppDb) {
                if (request.isComplete()) {
                    workRequestsToSend.add(request);
                }
            }

            for (WorkRequest requestToSend : workRequestsToSend) {
                final int id = requestToSend.getId();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Заявка №" + id + " синхронизирована", Toast.LENGTH_LONG).show();
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
}
