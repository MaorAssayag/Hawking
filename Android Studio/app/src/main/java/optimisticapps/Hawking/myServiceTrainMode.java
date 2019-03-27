package optimisticapps.Hawking;
/**
 *   _   _                      _      _
 *  | | | |   __ _  __      __ | | __ (_)  _ __     __ _
 *  | |_| |  / _` | \ \ /\ / / | |/ / | | | '_ \   / _` |
 *  |  _  | | (_| |  \ V  V /  |   <  | | | | | | | (_| |
 *  |_| |_|  \__,_|   \_/\_/   |_|\_\ |_| |_| |_|  \__, |
 *                                                 |___/
 *
 * Hawking - A real-time communication system for deaf and blind people
 *
 *   Hawking was created to help people with struggle to communicate via a braille keyboard.
 *   This app enabling using TTS & STT calibrated with standard braille keyboard.
 *   This project is part of a final computer engineering project in ben-gurion university Israel,
 *   in collaboration with the deaf-blind center in Israel.
 *
 * Creators : Maor Assayag
 *            Refhael Shetrit
 *            Computer Engineer, Ben-gurion University, Israel
 *
 * Supervisors: Prof. Guterman Hugo
 * 		        Dr. Luzzatto Ariel
 *
 * Service (background) for train mode
 */

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import java.util.List;
import java.util.Locale;

public class myServiceTrainMode extends Service {

    public SharedPreferences sharedPref; // SharedPreferences object for all the user settings to be saved on
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Geocoder geocoder;
    private List<Address> addresses;
    private String currentCity = "";
    private Messenger messageHandler;
    private int interval = 10000; // 10s

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        geocoder = new Geocoder(this, Locale.forLanguageTag("he-IL"));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    sendMessage(getString(R.string.location_failed), 2);
                    return;
                }
                // Custom-function : notify the user on city change using geo coder
                for (Location location : locationResult.getLocations()) {
                    getGeocoder(location);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        messageHandler = (Messenger) extras.get("MESSENGER");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(createLocationRequest(),
                    locationCallback,
                    null /* Looper */);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    public void getGeocoder(Location location){
        // Update UI with location data
        // check if GPS avaliable - notify if not
        try{
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            //String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            //String knownName = addresses.get(0).getFeatureName();
            // TEMP : replace with city name
            if (!this.currentCity.equals("" + location.getLatitude())){
                //onMessage(city + location.getLatitude(), 2);
                this.currentCity = "" + location.getLongitude();
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putString("currentCity","" + city);
                prefEditor.apply();
                sendMessage(city, 1);
            }
        }
        catch (Exception e){
            sendMessage(getString(R.string.location_failed), 2);
            //Toast.makeText(this, "enter null geoCoder", Toast.LENGTH_SHORT).show();
        }
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(interval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // TEMP : change to low power
        return locationRequest;
    }

    public void sendMessage(String msg, int state) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("msg1",msg);
        message.setData(bundle);
        if (state == 1){
            message.arg1 = MainActivity.UPDATE_CURRENT_CITY; // case 1 on receiver
        } else{
            message.arg1 = MainActivity.LOCATION_FAILED; // case 1 on receiver
        }
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}