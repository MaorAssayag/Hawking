package optimisticapps.Hawking;
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

/**
 *   _   _                      _      _
 *  | | | |   __ _  __      __ | | __ (_)  _ __     __ _
 *  | |_| |  / _` | \ \ /\ / / | |/ / | | | '_ \   / _` |
 *  |  _  | | (_| |  \ V  V /  |   <  | | | | | | | (_| |
 *  |_| |_|  \__,_|   \_/\_/   |_|\_\ |_| |_| |_|  \__, |
 *                                                 |___/   ;)
 *
 * Hawking - A real-time communication system for deaf and blind people
 *
 *   Hawking was created to help people with struggle to communicate via a braille keyboard.
 *   This app enabling using TTS & STT calibrated with standard braille keyboard.
 *   This project is part of a final computer engineering project in ben-gurion university Israel,
 *   in collaboration with the deaf-blind center in Israel.
 *
 * @author Maor Assayag
 *         Computer Engineer, Ben-gurion University, Israel
 *
 * @author Refhael Shetrit
 *         Computer Engineer, Ben-gurion University, Israel
 *
 * Supervisors: Prof. Guterman Hugo
 * 		        Dr. Luzzatto Ariel
 *
 * @version 1.0
 *
 * Service (background) for train mode
 */

public class myServiceTrainMode extends Service {

    /**
     * General
     */
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

    /**
     * onCreate()
     *
     * The first method that will be called on creation of this Service.
     * Initialize Geocoder object used to decode Latitude & Longitude from LocationManager
     * to Address, City name etc.
     *
     * Initialize a callback to the LocationService which will be called on new update to the location.
     */
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

    /**
     * onStartCommand(Intent, int, int)
     *
     * On start of the Service. Initialize a message handler for communication with the Main
     * Activity . If the required permission isnt available do not start the Service.
     *
     * @param intent - override
     * @param flags - override
     * @param startId - override
     * @return override
     */
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

    /**
     * onDestroy()
     *
     * When the Service stopped run the method stopLocationUpdates (stop the request
     * to get location updates)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    /**
     * getGeocoder(Location)
     *
     * The backbone of this class. After decoding the Location object update the current City
     * in the SharedPreferences object, and sendMessage to Main Activity to notify the user
     * (send message & vibrate).
     *
     * @param location - updated location object including all the required information
     */
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

    /**
     * createLocationRequest()
     *
     * Set the LocationRequest parameters with @interval time between requested updates(high
     * interval = less battery consumption), the required accuracy (we need to differentiate
     * cities so low accuracy is fine).
     *
     * @return LocationRequest object with the required parameters
     */
    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(interval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // TEMP : change to low power
        return locationRequest;
    }

    /**
     * sendMessage(String, state)
     *
     * Communicate with the Main Activity using message handler.
     * The Service can send messages with specific state and string, for e.g. update the
     * current city and failed messages.
     *
     * @param msg - the string to be sent to Main Activity
     * @param state - the type of the message
     */
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

    /**
     * stopLocationUpdates()
     * Stop location request from the device, used to stop this Service operation.
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}