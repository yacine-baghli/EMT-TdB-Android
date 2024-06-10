package com.enicay.TdBEMT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import okhttp3.WebSocket;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tdb extends AppCompatActivity {

    private static final String TAG = "Tdb";
    private double vitesse_kmh, longitude, latitude, altitude, directiongps;
    private int satelliteCount;
    private String dthgps = "";
    String secondes_esp32 = "", tension_batterie_esp32 = "", courant_batterie_esp32 = "";
    FusedLocationProviderClient fusedLocationProviderClient;
    private int lapCounter = 0;
    private ArrayAdapter<String> releves_adapter;

    String UserName="demo", UserPW="demoemt", VehicleID="2";
    private int vitesse_communication = 9600;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    private boolean color = true;
    private boolean change_color = true;
    TextView output,latitude_txt, longitude_txt, altitude_txt, vitesse_vehicule_txt, nb_sat_txt, vitesse_communication_txt, tension_batterie_esp32_txt, courant_batterie_esp32_txt, energie_esp32_txt;
    private boolean start_race = false;
    private final Handler timerHandler = new Handler();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private TextView text_Data;
    private static final int LOCATION_REQUEST_INTERVAL = 10; // 10 ms
    private static final int VENDOR_ID_ESP32 = 0x1A86;
    private static final int VENDOR_ID_ARDUINO_NANO = 0x0403;
    private static final int VENDOR_ID_ARDUINO_MICRO = 0x2341;
    private static final int VENDOR_ID_ARDUINO_UNO = 0x2341;
    private static final int VENDOR_ID_EQ_ARDUINO_NANO = 0x1A86;
    private static final int VITESSE_COMMUNICATION_ARDUINO = 9600;
    private static final int VITESSE_COMMUNICATION_ESP32 = 115200;
    private String receivedData = "";
    private static final int DISPLAY_UPDATE_INTERVAL_MS = 10;
    private final Handler displayUpdateHandler = new Handler();
    private Runnable displayUpdateRunnable;
    UsbManager usbManager;
    UsbDevice device;
    double energie_esp32 = 0.0;
    UsbDeviceConnection usbConnection;
    UsbSerialDevice serial;
    private Spinner sp_releve;
    private TextView releve,timerTextView;
    private int VendorId=0;
    String trameComplete;
    int cpt = 0;
    private static final String URL_SERVER ="https://embesystems.com/emt/";
    private int vitesse_moyenne = 0;
    int secondes = 0, minutes = 0, heures = 0;
    String tempsSite = "";
    private Handler handler = new Handler();
    private Runnable sendPointRunnable;
    private boolean isSending = false;

//    private static final double AREA_RADIUS = 0.0001; // Todo pour l'incrementation automatique du nombre de tours

//    private static final double max_latitude = 48.2;
//    private static final double min_latitude = 48.1;
//    private static final double max_longitude = 6.1;
//    private static final double min_longitude = 6.1;

//    private boolean isInArea = false;


    @SuppressLint({"MissingInflatedId", "WrongViewCast", "SetTextI18n", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tdb);

        TextView kmh = findViewById(R.id.km_h);
        ViewCompat.setTranslationZ(kmh, 1); //Permet de mettre en premier plan le texte km/h

        releve = findViewById(R.id.releve);
        sp_releve = findViewById(R.id.sp_releve);
        ArrayList<String> InfoID_list = new ArrayList<String>();

        releves_adapter =  new ArrayAdapter<String>( this,android.R.layout.simple_spinner_item, InfoID_list);
        releves_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_releve.setAdapter(releves_adapter);

        UpdateRelevesAdapter();//permet de récupérer les relevves de la base de données du site web de l'EMT

        sp_releve.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String ss = parent.getItemAtPosition(pos).toString();
                releve.setText(ss.substring(0, ss.indexOf(' ')));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });




        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        registerReceiver(broadcastReceiver, filter);


        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
        int Nb_tours_max = pref.getInt("Nb_tours_max", 5);

        TextView lapCounterTextView = findViewById(R.id.lapCounterTextView);
        lapCounterTextView.setText(lapCounter + "/" + Nb_tours_max + " ");

        vitesse_communication_txt = findViewById(R.id.vitesse_communication_txt);
        latitude_txt = findViewById(R.id.latitude_txt);
        longitude_txt = findViewById(R.id.longitude_txt);
        altitude_txt = findViewById(R.id.altitude_txt);
        vitesse_vehicule_txt = findViewById(R.id.vitesse_vehicule);
        nb_sat_txt = findViewById(R.id.nb_sat_txt);

        vitesse_communication_txt.setText(getString(R.string.vitesse_communication_txt) + " " + vitesse_communication);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setUpLocationUpdates();
        getSatelliteCount();
        checkLocationSettings();
        startUsbConnecting();
        startDisplayUpdates();


        ImageButton afficher_color = findViewById(R.id.afficher_color);
        afficher_color.setOnClickListener(view -> {

            if (color) {
                afficher_color.setImageResource(R.drawable.desactiver_color);
                color = !color;
                change_color = true;
            } else {
                afficher_color.setImageResource(R.drawable.afficher_color);
                color = !color;
                change_color = false;
                vitesse_vehicule_txt.setTextColor(ContextCompat.getColor(this, R.color.black));
            }
        });

        ImageButton button_vitesse_communication = findViewById(R.id.button_vitesse_comunnication);
        button_vitesse_communication.setOnClickListener(view -> {

            if (vitesse_communication == VITESSE_COMMUNICATION_ARDUINO) {

                vitesse_communication_txt.setText(getString(R.string.vitesse_communication_txt) + " " + VITESSE_COMMUNICATION_ESP32);
                vitesse_communication = VITESSE_COMMUNICATION_ESP32;
                Toast.makeText(this, "Vitesse de communication changée", Toast.LENGTH_LONG).show();

            } else {

                vitesse_communication_txt.setText(getString(R.string.vitesse_communication_txt) + " " + VITESSE_COMMUNICATION_ARDUINO);
                vitesse_communication = VITESSE_COMMUNICATION_ARDUINO;
                Toast.makeText(this, "Vitesse de communication changée", Toast.LENGTH_LONG).show();

            }

            if (serial != null) {
                serial.close();
            }
            startUsbConnecting();


        });

        ImageButton afficher_coord = findViewById(R.id.afficher_coord);
        output = findViewById(R.id.output);
        tension_batterie_esp32_txt = findViewById(R.id.tension_batterie_esp32_txt);
        courant_batterie_esp32_txt = findViewById(R.id.courant_batterie_esp32_txt);
        energie_esp32_txt = findViewById(R.id.energie_esp32_txt);
        releve = findViewById(R.id.releve);


        afficher_coord.setOnClickListener(view -> {

            if (nb_sat_txt.getVisibility() == View.VISIBLE) {

                button_vitesse_communication.setVisibility(View.INVISIBLE);
                vitesse_communication_txt.setVisibility(View.INVISIBLE);
                nb_sat_txt.setVisibility(View.INVISIBLE);
                latitude_txt.setVisibility(View.INVISIBLE);
                longitude_txt.setVisibility(View.INVISIBLE);
                altitude_txt.setVisibility(View.INVISIBLE);
                text_Data.setVisibility(View.INVISIBLE);
                tension_batterie_esp32_txt.setVisibility(View.INVISIBLE);
                courant_batterie_esp32_txt.setVisibility(View.INVISIBLE);
                energie_esp32_txt.setVisibility(View.INVISIBLE);
                output.setVisibility(View.INVISIBLE);
                releve.setVisibility(View.INVISIBLE);
                sp_releve.setVisibility(View.INVISIBLE);

                afficher_coord.setImageResource(R.drawable.desafficher_coord);

            } else {

                button_vitesse_communication.setVisibility(View.VISIBLE);
                vitesse_communication_txt.setVisibility(View.VISIBLE);
                nb_sat_txt.setVisibility(View.VISIBLE);
                latitude_txt.setVisibility(View.VISIBLE);
                longitude_txt.setVisibility(View.VISIBLE);
                altitude_txt.setVisibility(View.VISIBLE);
                text_Data.setVisibility(View.VISIBLE);
                tension_batterie_esp32_txt.setVisibility(View.VISIBLE);
                courant_batterie_esp32_txt.setVisibility(View.VISIBLE);
                energie_esp32_txt.setVisibility(View.VISIBLE);
                output.setVisibility(View.VISIBLE);
                releve.setVisibility(View.VISIBLE);
                sp_releve.setVisibility(View.VISIBLE);

                afficher_coord.setImageResource(R.drawable.afficher_coord);

            }

        });


        ImageButton start_race_view = findViewById(R.id.start_race);
        start_race_view.setOnClickListener(view -> {
            if (!start_race) {
                startTimer();
            } else {
                resetTimer();
                endTimer();
            }

        });




        Button start_sending = findViewById(R.id.start_sending);

        // Initialiser le Runnable
        sendPointRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSending) {
                    SendPoint();
                    handler.postDelayed(this, 1000); // Refaire apres 1000ms
                }
            }
        };

        start_sending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start_sending.getText().equals("Start sending")) {
                    addReleve();
                    start_sending.setText("Stop sending");
                    isSending = true;
                    handler.post(sendPointRunnable); // Start the Runnable immediately
                } else if (start_sending.getText().equals("Stop sending")) {
                    start_sending.setText("Start sending");
                    isSending = false;
                    handler.removeCallbacks(sendPointRunnable); // Stop le Runnable
                }
            }
        });

        Button tour_plus = findViewById(R.id.tour_plus);
        tour_plus.setOnClickListener(view -> {
            if (lapCounter < Nb_tours_max) {
                lapCounter++;
                lapCounterTextView.setText(lapCounter + "/" + Nb_tours_max + " ");
                changer_texte_dernier_tour();
            }
        });

        Button tour_moins = findViewById(R.id.tour_moins);
        tour_moins.setOnClickListener(view -> {
            if (lapCounter >= 1) {
                lapCounter--;
                lapCounterTextView.setText(lapCounter + "/" + Nb_tours_max + " ");
                changer_texte_dernier_tour();
            }
        });

        if (!start_race) {
            startTimer();
        } else {
            start_race = false;
            endTimer();
            resetTimer();
            start_race_view.setImageResource(R.drawable.start_race);
        }

        text_Data = findViewById(R.id.text_Data);

        startUsbConnecting();



//        Surcharge l'interface utilisateur
//
//        ImageButton resetButton = findViewById(R.id.resetButton);
//        resetButton.setOnClickListener(v -> resetTimer());
//
//        // Initialisez le Runnable pour le timer
//        timerRunnable = new Runnable() {
//            @SuppressLint("DefaultLocale")
//            @Override
//            public void run() {
//                // Incrémente les secondes
//                seconds++;
//
//                // Calcule les heures, minutes et secondes
//                int hours = seconds / 3600;
//                int minutes = (seconds % 3600) / 60;
//                int secs = seconds % 60;
//
//                TextView timerTextView = findViewById(R.id.timerTextView);
//
//                // Met à jour le TextView du timer avec le nouveau temps
//                timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, secs) + " ");
//
//                // Planifie l'exécution de ce Runnable après 1 seconde (1000 ms)
//                timerHandler.postDelayed(this, 1000);
//            }
//        };
//        Button start_sending = findViewById(R.id.start_sending);
////        client = new OkHttpClient();
//        start_sending.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if ((start_sending.getText()).equals("Start sending")){
//                    addReleve();
//                    start_sending.setText("Stop sending");
//                    while((start_sending.getText()).equals("Start sending")){
//                        try {
//                            Thread.sleep(1000);
//                            SendPoint();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } else if((start_sending.getText()).equals("Stop sending")){
////                    fermerConnexionWebSocket(ws,1000,"Vous avez arreté la connexion WebSocket");
//
//                    start_sending.setText("Start sending");
//                }
//            }
//        });

//        Trop encombrant à enleveer de l'activité
//        ImageButton info = findViewById(R.id.Info);
//        info.setOnClickListener(view -> {
//            Intent intent = new Intent(Tdb.this, Info.class);
//            startActivity(intent);
////            finish();
//        });
//        ImageButton parametre = findViewById(R.id.Parametre);
//        parametre.setOnClickListener(view -> {
//            Intent intent = new Intent(Tdb.this, Parametre.class);
//            startActivity(intent);
////            finish();
//        });


    }

    private void startUsbConnecting() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                Integer deviceVendorId = device.getVendorId();
                VendorId = deviceVendorId;
                Log.i("serial", "vendorId: " + deviceVendorId);
                if (deviceVendorId != null && ((deviceVendorId == VENDOR_ID_ESP32) || (deviceVendorId == VENDOR_ID_ARDUINO_NANO) || (deviceVendorId == VENDOR_ID_ARDUINO_MICRO) || (deviceVendorId == VENDOR_ID_ARDUINO_UNO) || (deviceVendorId == VENDOR_ID_EQ_ARDUINO_NANO))) {
                    @SuppressLint("MutableImplicitPendingIntent") PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
                    usbManager.requestPermission(device, intent);
                    keep = false;
                    Log.i(TAG, "Connection successful");
                    Toast.makeText(this, "Connection successful",
                            Toast.LENGTH_LONG).show();
                } else {
                    usbConnection = null;
                    device = null;
                    Log.i(TAG, "Unable to connect");
                    Toast.makeText(this, "Unable to connect",
                            Toast.LENGTH_LONG).show();
                }
                if (!keep) {
                    return;
                }
            }
        } else {
            Toast.makeText(this, "No usb device connected",
                    Toast.LENGTH_LONG).show();
            Log.i(TAG, "No usb device connected");
        }
    }

    private void setUpLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL); // 0.5 seconds
        locationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL); // 0.5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLastLocation() != null) {

                        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
                        int Nb_tours_max = pref.getInt("Nb_tours_max", 5);

                        Location location = locationResult.getLastLocation();

                        double speed = location.getSpeed() * 3.6; // Convert m/s to km/h
                        BigDecimal bd = new BigDecimal(speed);
                        bd = bd.setScale(3, RoundingMode.HALF_UP);
                        vitesse_kmh = bd.doubleValue();
                        //tourRestant = getTourRestant();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        altitude = location.getAltitude();
                        directiongps = location.getBearing();
                        //satelliteCount =  location.getAccuracy();
                        Date date = new Date(location.getTime());
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("CET"));
                        dthgps = formatter.format(date);


                        // todo pour incrementer le nombre de tours automatiquement à ajouter
//                        boolean isInsideArea = location.getLatitude() >= min_latitude && location.getLatitude() <= max_latitude &&
//                                location.getLongitude() >= min_longitude && location.getLongitude() <= max_longitude;
//
//                        // Check if the phone has just entered the area
//                        if (isInsideArea && !isInArea && (lapCounter <= Nb_tours_max)) {
//                            lapCounter++;
//                            isInArea = true;
//
//                            // Update lap counter text
//                            TextView lapCounterTextView = findViewById(R.id.lapCounterTextView);
//                            lapCounterTextView.setText(lapCounter + "/" + Nb_tours_max + " ");
//
//                            // Check if it's the last lap and show the message
//                            changer_texte_dernier_tour();
//
//                        } else if (!isInsideArea) {
//                            isInArea = false;
//                        } else if ((lapCounter == Nb_tours_max) && (isInsideArea) && (!isInArea)) {
//                            isInArea = true;
//                            start_race = false;
//                        }
                        updateLocationText(latitude, longitude, altitude, vitesse_kmh);
                    }
                }
            }, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }




    public void UpdateRelevesAdapter() {
        // populate from server GetReleves
        try {
            String url = URL_SERVER + "getreleves.php";
            String data = "";
            new httpPostRequestReleves(url, data).execute("");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "error");
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                setUpLocationUpdates();
            } else {
                // Permission denied, handle accordingly (e.g., show a message or disable location features)
                Toast.makeText(this, "Location permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }


//    private int getTourRestant() {
//        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
//        int Nb_tours_max = pref.getInt("Nb_tours_max", 5);
//        return Nb_tours_max - lapCounter;
//    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n", "DefaultLocale"})
    private void updateLocationText(double latitude, double longitude, double altitude, double vitesse) {
        latitude_txt.setText(getString(R.string.latitude_txt) + " " + latitude);
        longitude_txt.setText(getString(R.string.longitude_txt) + " " + longitude);
        altitude_txt.setText(getString(R.string.altitude_txt) + " " + altitude);
//        tourRestant_txt.setText(getString(R.string.tourRestant_txt) + " " + tourRestant);


        vitesse_vehicule_txt.setText(String.format("%.2f", vitesse) + " ");

        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
        int vitesse_moyenne = pref.getInt("moyenne_val", 15);
        int ecart_vitesse_moyenne = pref.getInt("ecart_moyenne_val", 4);

        if (change_color) {
            if (vitesse >= vitesse_moyenne - ecart_vitesse_moyenne && vitesse <= vitesse_moyenne + ecart_vitesse_moyenne) {
                vitesse_vehicule_txt.setTextColor(ContextCompat.getColor(this, R.color.green));
            } else if (vitesse < vitesse_moyenne - ecart_vitesse_moyenne) {
                vitesse_vehicule_txt.setTextColor(ContextCompat.getColor(this, R.color.orange));
            } else if (vitesse > vitesse_moyenne + ecart_vitesse_moyenne) {
                vitesse_vehicule_txt.setTextColor(ContextCompat.getColor(this, R.color.red));
            } else {
                vitesse_vehicule_txt.setTextColor(ContextCompat.getColor(this, R.color.violet));
            }
        }
    }


    private void getSatelliteCount() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    satelliteCount = status.getSatelliteCount();
                    nb_sat_txt.setText(getString(R.string.nb_sat) + " " + satelliteCount);
                }
            });
        }
    }

    public void changer_texte_dernier_tour() {
        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
        int Nb_tours_max = pref.getInt("Nb_tours_max", 5);

        TextView texte_dernier_tour = findViewById(R.id.texte_dernier_tour);

        if (lapCounter == Nb_tours_max) {
            texte_dernier_tour.setVisibility(View.VISIBLE);
        } else {
            texte_dernier_tour.setVisibility(View.INVISIBLE);
        }
    }


    private void startTimer() {
        if (!start_race) {
            start_race = true;
            ImageButton start_race_view = findViewById(R.id.start_race);
            start_race_view.setImageResource(R.drawable.end_race);
//            Runnable timerRunnable;
//            timerHandler.postDelayed(timerRunnable, 0); // Start the timer immediately
        }
    }


    @SuppressLint("SetTextI18n")
    private void resetTimer() {
//        seconds = 0;
//        TextView timerTextView = findViewById(R.id.timerTextView);
//        timerTextView.setText("00:00:00");
    }

    private void endTimer() {
        ImageButton start_race_view = findViewById(R.id.start_race);
        start_race = false;
        start_race_view.setImageResource(R.drawable.start_race);
//        timerHandler.removeCallbacks(timerRunnable); // Stop the timer
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (start_race) {
            endTimer(); // Arrêter le timer existant avant de le démarrer à nouveau
            startTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDisplayUpdates();
    }


    private void startDisplayUpdates() {
        displayUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                displayUpdateHandler.postDelayed(this, DISPLAY_UPDATE_INTERVAL_MS);
            }
        };
        displayUpdateHandler.post(displayUpdateRunnable);
    }


    // Update method onReceivedData
    private final UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @SuppressLint({"RestrictedApi", "SetTextI18n"})
        @Override
        public void onReceivedData(byte[] arg0) {

            if(VendorId == VENDOR_ID_ESP32){

                tension_batterie_esp32_txt = findViewById(R.id.tension_batterie_esp32_txt);
                courant_batterie_esp32_txt = findViewById(R.id.courant_batterie_esp32_txt);
                timerTextView = findViewById(R.id.timerTextView);
                energie_esp32_txt = findViewById(R.id.energie_esp32_txt);

//                reception_esp32 =true;
                String data_esp = new String(arg0, StandardCharsets.UTF_8);
                Log.d(TAG, "onReceivedData: " + data_esp);

                // Update receivedData
                receivedData = data_esp;


                // Update UI
                runOnUiThread(() -> {
                    text_Data.setText(data_esp);

                    if (data_esp.contains("$") && data_esp.contains("*")) {
                        int debut = data_esp.indexOf("$");
                        int fin = data_esp.indexOf("*", debut);
                        if (fin != -1) {
                            String extractedStr = data_esp.substring(debut, fin);
                            if (extractedStr.length() >= 16) {
                                tension_batterie_esp32 = extractedStr.substring(1, 6).trim();
                                courant_batterie_esp32 = extractedStr.substring(8, 13).trim();
                                secondes_esp32 = extractedStr.substring(15, 19).trim();

                                try {
                                    // Convertir les valeurs en nombres
                                    double tension = Double.parseDouble(tension_batterie_esp32);
                                    double courant = Double.parseDouble(courant_batterie_esp32);
                                    secondes = Integer.parseInt(secondes_esp32);

                                    // Convertir les secondes en heures, minutes et secondes
                                    heures = secondes / 3600;
                                    minutes = (secondes % 3600) / 60;
                                    secondes %= 60;

                                    // Formater la chaîne de caractères
                                    String temps = String.format("%02d:%02d:%02d", heures, minutes, secondes);

                                    tension_batterie_esp32_txt.setText(tension_batterie_esp32 + " V");
                                    courant_batterie_esp32_txt.setText(courant_batterie_esp32 + " A");
                                    timerTextView.setText(temps);

                                    //todo calculer la puissance ainsi que l'énergie et faire la moyenne glissante :
                                    double puissance = tension * courant;
                                    energie_esp32 = BigDecimal.valueOf(puissance * secondes).setScale(2, RoundingMode.HALF_UP).doubleValue();
                                    energie_esp32_txt.setText(energie_esp32 + " J");
                                } catch (NumberFormatException e) {
                                    // Gérer l'erreur de conversion
                                    Log.e(TAG, "NumberFormatException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
//            }else if((VendorId == VENDOR_ID_ARDUINO_NANO) || (VendorId == VENDOR_ID_ARDUINO_MICRO) || (VendorId == VENDOR_ID_ARDUINO_UNO) || (VendorId == VENDOR_ID_EQ_ARDUINO_NANO)){
            }else{


//                reception_esp32 =false;

                tension_batterie_esp32_txt = findViewById(R.id.tension_batterie_esp32_txt);
                courant_batterie_esp32_txt = findViewById(R.id.courant_batterie_esp32_txt);
                timerTextView = findViewById(R.id.timerTextView);
                energie_esp32_txt = findViewById(R.id.energie_esp32_txt);

                String data = null;
                try {
                    data = new String(arg0, "UTF-8");
                    trameComplete += data;
//                    tvSet(text_Data, trameComplete);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
//                    Log.e(TAG, "Test énergie: " + trameComplete);
                    if ((trameComplete.indexOf("min") >= 0) &&
                            ((trameComplete.indexOf("intensité =") >= 0)||(trameComplete.indexOf("intensite =") >= 0)) &&
                            (trameComplete.indexOf("tension =") >= 0) &&
                            ((trameComplete.indexOf("énergie =") >= 0)||(trameComplete.indexOf("energie =") >= 0)) &&
                            trameComplete.indexOf("J") >= 0) {
                        cpt += 1;
                    }

                    if (cpt == 1) {
                        trameComplete = trameComplete.trim();
                        String[] donnees = trameComplete.split("\n");

                        tempsSite = donnees[0].trim();
                        String tempsEnv = convertirTemps(donnees[0].trim());
                        // Mettre à jour le TextView correspondant pour le temps
                        tvSet(timerTextView, tempsEnv);

                        String[] tension = donnees[1].split(" ");
                        String tensionEnv = tension[2];

                        // Mettre à jour le TextView correspondant pour la tension
                        tvSet(tension_batterie_esp32_txt, tensionEnv + " V");
                        tension_batterie_esp32= tensionEnv;

                        String[] intensite = donnees[2].split(" ");
                        String intensiteEnv = intensite[2];
                        courant_batterie_esp32= intensiteEnv;

                        // Mettre à jour le TextView correspondant pour l'intensité
                        tvSet(courant_batterie_esp32_txt, intensiteEnv + " A");

                        String[] energie = donnees[3].split(" ");
                        String energieEnv = energie[2];
                        energie_esp32 = Double.parseDouble(energieEnv);

                        // Mettre à jour le TextView correspondant pour l'énergie
                        tvSet(energie_esp32_txt, energieEnv + " J");

                        // Réinitialiser la trame complète et le compteur
                        trameComplete = "";
                        cpt = 0;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    };

    private String convertirTemps(String temps) {
//        // Supprimer tous les caractères non numériques
//        String tempsNumerique = temps.replaceAll("\\D", "");
//
        int heures = 0, minutes = 0, secondes = 0;
//
//        // Analyser les chiffres pour obtenir les minutes et les secondes
//        if (tempsNumerique.length() >= 2) {
//            // Si les minutes sont fournies
//            minutes = Integer.parseInt(tempsNumerique.substring(0, tempsNumerique.length() - 2));
//        }
//        if (tempsNumerique.length() >= 4) {
//            // Si les secondes sont fournies
//            String secondesStr = tempsNumerique.substring(tempsNumerique.length() - 2);
//            // Ajouter un zéro à gauche si nécessaire
//            if (secondesStr.length() < 2) {
//                secondesStr = "0" + secondesStr;
//            }
//            secondes = Integer.parseInt(secondesStr);
//        }

        //String data = "7min3s";
        Pattern timePattern = Pattern.compile("(\\d+)min(\\d+)s");
        Matcher timeMatcher = timePattern.matcher(temps);

        if (timeMatcher.find()) {
            minutes = Integer.parseInt(timeMatcher.group(1));
            secondes = Integer.parseInt(timeMatcher.group(2));
            if (minutes >= 60) {
                heures = minutes / 60;
                minutes %= 60;
            }
        }
           // Formater la chaîne au format MM:SS
        return String.format("%02d:%02d:%02d", heures, minutes, secondes);
    }







    private void tvSet(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.setText(ftext);
            }
        });
    }


    private void stopDisplayUpdates() {
        displayUpdateHandler.removeCallbacks(displayUpdateRunnable);
    }


    private void updateUI() {
        text_Data = findViewById(R.id.text_Data);
        runOnUiThread(() -> text_Data.setText(receivedData));
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), ACTION_USB_PERMISSION)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    boolean granted = extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) {
                        usbConnection = usbManager.openDevice(device);
                        serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);
                        if (serial != null) {
                            if (serial.open()) {
                                startTimer();
                                serial.setBaudRate(vitesse_communication);
                                serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                serial.setParity(UsbSerialInterface.PARITY_NONE);
                                serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                serial.read(mCallback);

                            } else {
                                Log.i(TAG, "Port not open");
                            }
                        } else {
                            Log.i(TAG, "Port is null");
                        }
                    } else {
                        Log.i(TAG, "Permission not granted");
                    }
                } else {
                    Log.i(TAG, "Extras are null");
                }
            } else if (Objects.equals(intent.getAction(), UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                startUsbConnecting();
            } else if (Objects.equals(intent.getAction(), UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                serial.close(); //disconnect();
                endTimer();
                resetTimer();
            }
        }
    };


    private void checkLocationSettings() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Tdb.this);
            builder.setMessage(R.string.msg_notif_location)
                    .setCancelable(false)
                    .setPositiveButton(R.string.oui, (dialog, id) -> {
                        // Open location settings
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton(R.string.non, (dialog, id) -> {
                        // Dismiss the dialog
                        dialog.dismiss();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }



    private void SendPoint() {
        String ref = releve.getText().toString();

        if (ref.substring(0,1).equals("1")) return;   // anciens points de 2012 !
        String SQL_PHP = "";
        // todo addpoint via PHP

        try {
            String url = URL_SERVER + "pt_i.php";
//      String data = URLEncoder.encode("ref_releve", "UTF-8")+"="+ ref;
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault(Locale.Category.FORMAT));
            symbols.setDecimalSeparator('.'); // Définir le séparateur décimal comme point
            DecimalFormat df = new DecimalFormat("#.##", symbols); // Utiliser ces symboles dans votre DecimalFormat
            String data = "ref_releve="+ releve.getText().toString();
            data += "&temps="+timerTextView.getText().toString();
            data += "&vitessegps="+ vitesse_kmh;
            data += "&vitessemoy="+ vitesse_moyenne;
            data += "&intensite="+ courant_batterie_esp32;
            data += "&tension="+ tension_batterie_esp32;
            data += "&energie="+ energie_esp32;
            data += "&latitude="+ latitude;
            data += "&longitude="+ longitude;
            data += "&altitude="+ altitude;
            data += "&distance="+ "0";
            data += "&laps="+ lapCounter;
            data += "&directiongps="+ df.format(directiongps);
            data += "&dthgps="+ dthgps;
            data += "&nbrsatgps="+ satelliteCount;
//            data += "&map="+ "0";
            Log.e(TAG, "data: " + data);


            new httpPostRequestAddPoint(url, data).execute("");

            output(data);

//      Toast.makeText(getBaseContext(), response, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "error");
        }
    }


    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                output.setText(output.getText().toString() + "\n\n" + txt);
                output.setText(txt);
            }
        });
    }



    @SuppressLint("StaticFieldLeak")
    class httpPostRequestReleves extends AsyncTask<String, String, String> {
        ProgressDialog pd;
        String url, data;

        public httpPostRequestReleves(String url, String data) {
            this.url = url;
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Tdb.this);
            pd.setMessage("Wait");
            pd.setMax(100);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... name) {
            String response = "";
            BufferedReader reader = null;
            HttpURLConnection conn = null;
            try {
                Log.d("RequestManager", url + " ");
                Log.d("data ", data);
                URL urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                Log.d("post response code", conn.getResponseCode() + " ");
                int responseCode = conn.getResponseCode();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line );
                }

                response = sb.toString();
            } catch (Exception e) {
                Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
            }
            return response;
        }

        protected void onProgressUpdate(String... progress) {
            if (pd != null) pd.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String response) {
            if (pd != null) {
                pd.dismiss();
            }
            Log.d("Recorded : ",  response);
      Toast.makeText(getBaseContext(), "Recorded"
                      + " : " + response
              , Toast.LENGTH_SHORT).show();
            if (response.isEmpty()) return;
            JSONObject obj = null;
            JSONObject jsonRootObject = null;
            JSONArray jsonArray = null;
            String ref_dth_debut;
            ArrayList<String> list = new ArrayList<String>();
            try {
                jsonArray = new JSONArray(response);
                for(int i= 0; i<jsonArray.length(); i++)
                {
                    obj = jsonArray.getJSONObject(i);
                    ref_dth_debut =  obj.optString("REF_DTH_DEBUT");
                    list.add(ref_dth_debut);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            releves_adapter.clear();
            releves_adapter.addAll(list);
            releves_adapter.notifyDataSetChanged();
            int count = sp_releve.getCount();
            if (count!=0) sp_releve.setSelection( count-1);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class httpPostRequestAddPoint extends AsyncTask<String, String, String> {
        ProgressDialog pd;
        String url, data;

        public httpPostRequestAddPoint(String url, String data) {
            this.url = url;
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pd = new ProgressDialog(Tdb.this);
//            pd.setMessage("Wait");
//            pd.setMax(100);
//            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            pd.setCancelable(true);
//            pd.show();
        }

        @Override
        protected String doInBackground(String... name) {
            String response = "";
            BufferedReader reader = null;
            HttpURLConnection conn = null;
            try {
                Log.d("RequestManager", url + " ");
                Log.e("data ", data);
                URL urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                Log.d("post response code", conn.getResponseCode() + " ");
                int responseCode = conn.getResponseCode();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line );
                }

                response = sb.toString();
            } catch (Exception e) {
                Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
            }
            return response;
        }

        protected void onProgressUpdate(String... progress) {
//            if (pd != null) pd.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String name) {
//            if (pd != null) {
//                pd.dismiss();
//            }
//            Toast.makeText(getBaseContext(), "Recorded"+ " : " + name, Toast.LENGTH_SHORT).show();
        }
    }

    public void addReleve() {
        // populate from server GetReleves
        try {
            String url = URL_SERVER + "addreleves.php";
            String data = "UserName="+ URLEncoder.encode(UserName, "UTF-8");
            data += "&UserPW="+ URLEncoder.encode(UserPW, "UTF-8");
            data += "&VehicleID="+ URLEncoder.encode(VehicleID, "UTF-8");
            new httpPostRequestAddPoint(url, data).execute(""); //same call back

            output(data);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error", "error");
        }
    }


}


//TODO Pour le moment, on ne peut pas envoyer les données à l'API via WebSocket mais voici une partie du code qui permet de le faire
//
// On utilise donc le protocole HTTP.
// private final class EchoWebSocketListener extends WebSocketListener {
//    private static final int NORMAL_CLOSURE_STATUS = 1000;
//
//    @Override
//    public void onOpen(WebSocket webSocket, Response response) {
//        output("Connected to server");
//    }
//
//    @Override
//    public void onMessage(WebSocket webSocket, String text) {
//        output("Receiving : " + text);
//        if (start_race) {
//            String texte_a_envoyer;
//            if(reception_esp32){
//                if (!Verif_premier) {
//                    Verif_premier = true;
//                    String texte_debut = "{\"event\":\"debutrun\"}";
//                    webSocket.send(texte_debut);
//                    texte_a_envoyer = "{\"event\":\"dataFromCar\",\"temps\": \"" +
//                            secondes_esp32 + "\" ,\"energie\":" + energie + ",\"tension\":" + Double.parseDouble(tension_batterie_esp32) +
//                            ",\"intensite\":" + Double.parseDouble(courant_batterie_esp32) + ",\"lati\":" + latitude + ",\"long\":" + longitude +
//                            ",\"alti\":" + altitude + ",\"vitesse\":" + vitesse_kmh + ",\"tour\":" + tourRestant +
//                            "}";
//                } else {
//                    texte_a_envoyer = "{\"event\":\"dataFromCar\",\"temps\": \"" +
//                            secondes_esp32 + "\" ,\"energie\":" + energie + ",\"tension\":" + Double.parseDouble(tension_batterie_esp32) +
//                            ",\"intensite\":" + Double.parseDouble(courant_batterie_esp32) + ",\"lati\":" + latitude + ",\"long\":" + longitude +
//                            ",\"alti\":" + altitude + ",\"vitesse\":" + vitesse_kmh + ",\"tour\":" + tourRestant +
//                            "}";
//                }
//            }else{
//                if (!Verif_premier) {
//                    Verif_premier = true;
//                    String texte_debut = "{\"event\":\"debutrun\"}";
//                    webSocket.send(texte_debut);
//                    texte_a_envoyer = "{\"event\":\"dataFromCar\",\"temps\": \"" +
//                            tempsEnv + "\" ,\"energie\":" + energie_arduino + ",\"tension\":" + tension_arduino +
//                            ",\"intensite\":" + intensite_arduino + ",\"lati\":" + latitude + ",\"long\":" + longitude +
//                            ",\"alti\":" + altitude + ",\"vitesse\":" + vitesse_kmh + ",\"tour\":" + tourRestant +
//                            "}";
//                } else {
//                    texte_a_envoyer = "{\"event\":\"dataFromCar\",\"temps\": \"" +
//                            tempsEnv + "\" ,\"energie\":" + energie_arduino + ",\"tension\":" + tension_arduino +
//                            ",\"intensite\":" + intensite_arduino + ",\"lati\":" + latitude + ",\"long\":" + longitude +
//                            ",\"alti\":" + altitude + ",\"vitesse\":" + vitesse_kmh + ",\"tour\":" + tourRestant +
//                            "}";
//                }
//            }
//            webSocket.send(texte_a_envoyer);
//        }
//    }
//
//
//    @Override
//    public void onClosing(WebSocket webSocket, int code, String reason) {
//        webSocket.close(NORMAL_CLOSURE_STATUS, null);
//        output("Closing : " + code + " / " + reason);
//    }
//
//    @Override
//    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
//        output("Error : " + t.getMessage());
//    }
//}
//
//    private void fermerConnexionWebSocket(WebSocket webSocket, int code, String raison) {
//        webSocket.close(code, raison);
//        output(raison);
//    }
//    private void start() {
//        Request request = new Request.Builder().url("ws://emt.polytech-nancy.univ-lorraine.fr:8080/wsapi/?token=L2eEzMs7ZMpNJYCQaECg").build();
//        EchoWebSocketListener listener = new EchoWebSocketListener();
//        ws = client.newWebSocket(request, listener);
//
//        client.dispatcher().executorService().shutdown();
//    }

//    private void SendData() {
//        Button start_sending = findViewById(R.id.start_sending);
//        while ((start_sending.getText()).equals("Stop sending")) {
//            runOnUiThread(() -> {
//                try {
//                    Thread.sleep(1000);
//                    SendPoint();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }




//TODO Cette partie permet d'ajouter des relevés automatiquement en l'ajoutant au thread qui lance le send point (à corriger ne fonctionne pas bien)

//    public void addReleve() {
//        // populate from server GetReleves
////        try {
////            String url = URL_SERVER + "addreleves.php";
////            String data = "UserName="+ URLEncoder.encode(UserName, "UTF-8");
////            data += "&UserPW="+ URLEncoder.encode(UserPW, "UTF-8");
////            data += "&VehicleID="+ URLEncoder.encode(VehicleID, "UTF-8");
////            new httpPostRequestAddPoint(url, data).execute(""); //same call back
//
//        try{
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d("Error", "error");
//        }
//    }




//    private void addReleve() {
//
////        INSERT INTO `releves` VALUES (NEW_REF_RELEVE, DTHGPS, '0000-00-00 00:00:00', '2', '2');
//
//
//        String ref = releve.getText().toString();
//
//        if (ref.substring(0, 1).equals("1")) return;   // Points anciens de 2012 !
//
//        // URL de votre script PHP pour insérer un point
//        String url = URL_SERVER + "pt_i.php";
//
//        try {
//            // Formatter les valeurs numériques avec un séparateur décimal en point
//            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault(Locale.Category.FORMAT));
//            symbols.setDecimalSeparator('.');
//            DecimalFormat df = new DecimalFormat("#.##", symbols);
//
//            // Construire les données à envoyer au script PHP
//            String data = "ref_releve=" + (Integer.parseInt(releve.getText().toString()) + 1);
//            data += "&dthgps=" + dthgps;
//            data += "&dthfin=" + "0000-00-00 00:00:00";
//            data += "&v_id=" + "2";
//            data += "&id_user=" + "2";
//            // Exécuter la requête HTTP POST asynchrone pour envoyer les données au script PHP
//            new httpPostRequestAddPoint(url, data).execute(""); //same call back
//
//            // Afficher les données envoyées dans la sortie
//            output(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d("Error", "error");
//        }
//    }



