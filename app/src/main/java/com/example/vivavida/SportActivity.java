package com.example.vivavida;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.text.LineBreaker;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import java.text.SimpleDateFormat;

public class SportActivity extends AppCompatActivity
        implements
        View.OnClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    //Permisos de localizacion y variables del mapa
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private Geocoder geocoder;

    //Button START/STOP;
    private Button start;

    //Radio button para seleccionar actividad
    private RadioGroup RadioGroup;
    private RadioButton radio0;
    private RadioButton radio1;
    private RadioButton radio21;
    private RadioButton radio20;

    //Variables de tiempo
    private String postalCode;
    private TextView tem_max;
    private TextView tem_min;
    private ImageView weather;

    //Widget cronometro
    private Chronometer chrono;

    //Variables para trazado de ruta y Polyline
    private PolylineOptions lineOptions;
    private Polyline lineRoute;
    private List<LatLng> points; //List of points to join lines
    private ActivityDbAdapter dbAdapter;
    int route_id;

    //Variables de control, tiempo y calculos de kcal
    private String sport; //Variable to set TextView string
    private long initial; //Variable to set the time the activity starts
    private boolean finish; //Variable to determine if the activity has ended or not
    private double MET; //Variable to calculate kcal burned depending the sport

    //Variables para establecer el tiempo
    private int FirstLocation = 1; //Variable to determine if its the first time that the location is open
    private String max_tem = "N/A"; //Variable to set TextView of maximum temperature
    private String min_tem = "N/A"; //Variable to set TextView of minimum temperature
    private String StringURL;

    /**
     * Creates the Activity elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);

        geocoder = new Geocoder(this, Locale.getDefault());
        start = (Button) findViewById(R.id.buttonStart);
        RadioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        radio0 = (RadioButton) findViewById(R.id.radio0);
        radio1 = (RadioButton) findViewById(R.id.radio1);
        radio21 = (RadioButton) findViewById(R.id.radio21);
        radio20 = (RadioButton) findViewById(R.id.radio20);
        chrono = (Chronometer) findViewById(R.id.Chronometer);
        tem_max = (TextView) findViewById(R.id.tem_max);
        tem_min = (TextView) findViewById(R.id.tem_min);
        weather = (ImageView) findViewById(R.id.weather);
        finish = true;
        dbAdapter = new ActivityDbAdapter(this);

        SharedPreferences Pref = this.getPreferences(MODE_PRIVATE);

        if (Pref.contains("route_id")) {
            route_id = Pref.getInt("route_id", 0);
        } else {
            route_id = 1;
        }
    }

    /**
     * Creates the Activity with the Action bar.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        //Mapa
        SupportMapFragment mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        mMap.getMapAsync(this);

        start.setOnClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles the Action bar items.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.about).setTitle(R.string.about_title);
                AlertDialog About = builder.show();
                TextView msg = (TextView) About.findViewById(android.R.id.message);
                msg.setGravity(Gravity.CENTER);
                msg.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handles the Start/Stop button as well as the Radio Group to select activity.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View v) {

        boolean cont = true; //Variable to check if an option from RadioGroup was selected\

        switch (v.getId()) {
            case R.id.buttonStart:
                if (finish) { //Start Activity
                    if (radio0.isChecked()) {
                        sport = "Running";
                        MET = 11.5;
                    } else if (radio1.isChecked()) {
                        sport = "Hiking";
                        MET = 6;
                    } else if (radio20.isChecked()) {
                        sport = "Cycling";
                        MET = 8.5;
                    } else if (radio21.isChecked()) {
                        sport = "Swimming";
                        MET = 9.5;
                    } else {
                        Toast.makeText(getApplicationContext(), "Please, select one activity", Toast.LENGTH_SHORT).show();
                        cont = false;
                    }

                    RadioGroup.clearCheck(); //Flush the selected option

                    if (cont) { //If an option was selected
                        start.setText("STOP"); //Change Button text to STOP
                        finish = false; //The activity has started, user can't go back to menu

                        //Start chronometer
                        chrono.setBase(SystemClock.elapsedRealtime());
                        chrono.start();

                        //Measure Start time
                        initial = System.currentTimeMillis();
                    }

                } else { //End Activity

                    finish = true; //The activity has finished, user can go back to menu

                    int weight; //Parse to int the weight from preferences of Profile activity
                    int kcal; //Store the apporximate kcal burnt in the activity
                    String dist_s; //Set the distance as dialog text
                    String weight_s; //Store the weight from preferences of Profile activity
                    String kcal_s; //Set the apporximate kcal burnt as dialog text

                    start.setText("START"); //Change Button text to START

                    //End chronometer
                    chrono.setBase(SystemClock.elapsedRealtime());
                    chrono.stop();

                    //Measure End time
                    long fin = System.currentTimeMillis();

                    //Get the weight from preferences of Profile activity
                    SharedPreferences sharedPref = getSharedPreferences("ProfileActivity", MODE_PRIVATE);

                    if (sharedPref.contains("weight")) {
                        weight_s = sharedPref.getString("weight", null); //Get the value as String
                        weight = Integer.parseInt(weight_s); //Parse it to String
                        kcal = kcalBurned(fin - initial, MET, weight); //Calculate the approximate kcal burnt
                        kcal_s = String.valueOf(kcal); //Parse to String the result obtained
                    } else
                        //If the weight was not introduced, display the following string
                        kcal_s = "No weight data registered. Please register it on the Profile Section.";

                    //Convert time to appropriate format: HH:MM:SS
                    String time = miliToTime(initial, fin);

                    //Convert distance to appropriate format: XXXX.XX Km
                    dist_s = String.format("%.2f Km", totalDistance() / 1000);

                    //Create dialog
                    LayoutInflater inflater = getLayoutInflater();
                    View dialoglayout = inflater.inflate(R.layout.newsport, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialoglayout);

                    //Get the layout text elements
                    TextView sport_l = (TextView) dialoglayout.findViewById(R.id.Activity);
                    TextView distance_l = (TextView) dialoglayout.findViewById(R.id.Distance);
                    TextView time_l = (TextView) dialoglayout.findViewById(R.id.Time);
                    TextView kcal_l = (TextView) dialoglayout.findViewById(R.id.Kcal);

                    //Set dialog texts
                    sport_l.setText("Type of activity: " + sport);
                    distance_l.setText("Distance: " + dist_s);
                    time_l.setText("Active time: " + time);
                    kcal_l.setText("Kcal burned: " + kcal_s);

                    //Save stats in the DDBB
                    String date = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.ENGLISH).format(new Date());
                    //DDBB variables

                    dbAdapter.open();
                    Bundle extras = getIntent().getExtras();
                    Long mRowId = extras != null ? extras.getLong(ActivityDbAdapter.KEY_ROWID1) : null;
                    long id = dbAdapter.createActivity(sport, date, dist_s, time, kcal_s, Integer.toString(route_id));
                    if (id > 0) {
                        mRowId = id;
                    }
                    setResult(RESULT_OK);
                    dbAdapter.close();

                    route_id++;
                    SharedPreferences Pref = this.getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = Pref.edit();
                    editor.putInt("route_id", route_id);
                    editor.apply();


                    //Show dialog
                    builder.show();

                    //If points where saved, delete them to reset the trace
                    if (points != null) points.clear();

                    //Restart tracing
                    lineRoute.remove();
                    initializeDraw();
                }
                break;

            default:
                break;
        }
    }

    /**
     * Converts a period of time into the HH:MM:SS format.
     */
    public String miliToTime(long initial, long fin) {
        long milliseconds = fin - initial;
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        return " " + hours + ":" + minutes + ":" + seconds;
    }

    /**
     * Sets the initial configuration of the route trace.
     */
    private void initializeDraw() {
        lineOptions = new PolylineOptions().width(5).color(this.getResources().getColor(R.color.myorange));
        lineRoute = mMap.addPolyline(lineOptions);
    }

    /**
     * Sets GoogleMaps settings to be displayed.
     */
    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        initializeDraw();
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@NonNull Location arg0) {
                LatLng myPosition = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
                //While the activity is tracking, get points and trace a line between them
                if (!finish) {
                    points = lineRoute.getPoints();
                    points.add(myPosition);
                    lineRoute.setPoints(points);

                    dbAdapter.open();
                    dbAdapter.createPoints(Double.toString(arg0.getLatitude()), Double.toString(arg0.getLongitude()), Integer.toString(route_id));
                    setResult(RESULT_OK);
                    dbAdapter.close();
                }

                //Get the postal code from the new location
                if (FirstLocation == 1) { //To obtain only once connection
                    try {
                        List<Address> addresses = geocoder.getFromLocation(arg0.getLatitude(), arg0.getLongitude(), 1);
                        postalCode = addresses.get(0).getPostalCode();
                    } catch (Exception ex) {
                        System.out.println("Error geocoder");
                    }

                    //Checking the conection
                    //Time per community, not province (In this case Madrid centro) -- Ideal: create a DDBB which relates postal codes with AEMET localidad codes
                    StringURL = "https://www.aemet.es/xml/municipios/localidad_" + postalCode.substring(0,2) + "079.xml";

                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        //Try to connect to the URL with a thread
                        new DownloadWebpageTask().execute(StringURL);
                    } else {
                        System.out.println("Error: no connection");
                    }
                    ++FirstLocation;
                }
            }

        });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onBackPressed() {
        if (finish) {
            super.onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), "Please, end the activity before returning to home", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Computes the total distance from the points obtained.
     */
    public float totalDistance() {
        float[] results = {0, 0, 0, 0};
        if (points == null) return 0;
        int size = points.size();
        float distance = 0;
        for (int i = 0; i < size - 1; i++) {
            Location.distanceBetween(points.get(i).latitude, points.get(i).longitude, points.get(i + 1).latitude, points.get(i + 1).longitude, results);
            distance = distance + results[0];
        }
        return distance;
    }

    /**
     * Computes the approximate kcal burnt from the weight, time, and MET
     * Formula obtained in: https://www.verywellfit.com/how-many-calories-you-burn-during-exercise-4111064
     */
    public int kcalBurned(long time, double MET, int weight) {

        return (int) ((time / 60000) * MET * 3.5 * weight / 200);
    }

    /**
     * Create a threat for the URL connection and set the weather's values
     */
    private class DownloadWebpageTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... urls) {
            URL url = null;
            BufferedInputStream in = null;
            Weather temp = new Weather();
            //In case we can't connect to the URL
            temp.setMinTemp(min_tem);
            temp.setMaxTemp(max_tem);
            try {
                url = new URL(urls[0]);
            } catch (Exception ex) {
                System.out.println("Malformed URL");
            }
            //Opening the connection
            try {
                if (url != null) {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            if (in != null) {
                try {
                    //Process the xml post by the URL
                    temp = parse(in);
                } catch (Exception e) {
                    System.out.println("Exception");
                    return temp;
                }
            }

            return temp;
        }

        @Override
        protected void onPreExecute() {

        }

        //Update the user interface according to the results of the xml
        @Override
        protected void onPostExecute(Weather result) {

            //Codes of the OpenData of AEMET
            String Sunny = "11n 11";
            String Raining = "64n 64 63 63n 62n 62 61n 61 54n 54 53n 53 52n 52 51n 51 46n 45n 44n 44 43n 43  23 23n 24 24n 25n 25 26 26n";
            String Snowing = "74n 74 73n 73 72n 72 71n 71 36 36n 35 35n 34 34n 33 33n";
            String Cloudy = "12n 13 13n 14 14n 15 16 16n 17";
            if (Sunny.contains(result.getWeather())) { //It is sunny
                weather.setImageResource(R.drawable.ic_baseline_wb_sunny_24);
                weather.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.myyellow));
            } else if (Raining.contains(result.getWeather())) { //It is raining
                weather.setImageResource(R.drawable.ic_baseline_umbrella_24);
                weather.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mywhite));
            } else if (Snowing.contains(result.getWeather())) { //It is snowing
                weather.setImageResource(R.drawable.ic_baseline_ac_snow_24);
                weather.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mylightblue));
            } else if (Cloudy.contains(result.getWeather())) { //It is cloudy
                weather.setImageResource(R.drawable.ic_baseline_cloud_queue_24);
                weather.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mylightblue));
            } else {
                weather.setImageResource(R.drawable.ic_baseline_help_outline_24);
                weather.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mywhite));
            }

            tem_min.setText(result.getMinTemp());
            tem_max.setText(result.getMaxTemp());
        }

        private String ns = null;// To compere the tags of the xml

        /**
         * Read the xml file
         */
        public Weather parse(BufferedInputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readXML(parser);
            } finally {
                in.close();
            }
        }


        // Read root and search prediction
        private Weather readXML(XmlPullParser parser) throws XmlPullParserException, IOException {
            Weather entries = new Weather();
            parser.require(XmlPullParser.START_TAG, ns, "root");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;

                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("prediccion")) {
                    entries = readPredicion(parser);
                } else {
                    skip(parser);
                }
            }
            return entries;
        }

        // Read prediction and search date
        private Weather readPredicion(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "prediccion");
            String name;
            int times = 0;// To read only the first day (actual date)
            Weather w = new Weather();

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                name = parser.getName();

                if (name.equals("dia") && times == 0) {
                    w = readDate(parser);
                    times++;
                } else {
                    skip(parser);
                }
            }

            return w;
        }

        //Read the actual day and search temperature and the weather
        private Weather readDate(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "dia");
            String name;
            int times = 0;
            Weather w = new Weather();
            Temperature temp;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) continue;
                name = parser.getName();
                if (name.equals("estado_cielo") && times == 0) {
                    times++;
                    String wea = readWeather(parser);
                    w.setWeather(wea);

                } else if (name.equals("temperatura")) {

                    temp = readTemp(parser);
                    w.setMinTemp(temp.getMinTemp_());
                    w.setMaxTemp(temp.getMaxTemp_());
                } else {
                    skip(parser);
                }
            }

            return w;
        }


        // Read temperature and search min and max
        private Temperature readTemp(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "temperatura");
            String name;
            Temperature temp = new Temperature();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                name = parser.getName();

                if (name.equals("maxima")) {
                    String max = readMax(parser);
                    temp.setMaxTemp_(max);
                } else if (name.equals("minima")) {
                    String min = readMin(parser);
                    temp.setMinTemp_(min);
                } else {
                    skip(parser);
                }
            }

            return temp;
        }


        //Read the content of the flags once they are founded
        private String readWeather(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "estado_cielo");
            String weather = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "estado_cielo");
            return weather;
        }

        private String readMax(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "maxima");
            String max = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "maxima");
            return max;
        }

        private String readMin(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "minima");
            String min = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "minima");
            return min;
        }

        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        //Skips tags the parser isn't interested in
        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }
}

// Class to set the temperature
class Temperature {
    private String min_temp;
    private String max_temp;

    public Temperature() {
        this.min_temp = "";
        this.max_temp = "";

    }

    public void setMinTemp_(String min_temp) {
        this.min_temp = min_temp;
    }

    public void setMaxTemp_(String max_temp) {
        this.max_temp = max_temp;
    }

    public String getMinTemp_() {
        return this.min_temp;
    }

    public String getMaxTemp_() {
        return this.max_temp;
    }

}

// Class to set the weather
class Weather {
    private String weather;
    private final Temperature temperature;

    public Weather() {
        this.weather = "";
        this.temperature = new Temperature();


    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setMinTemp(String min_temp) {
        this.temperature.setMinTemp_(min_temp);
    }

    public void setMaxTemp(String max_temp) {
        this.temperature.setMaxTemp_(max_temp);
    }

    public String getWeather() {
        return weather;
    }

    public String getMinTemp() {
        return this.temperature.getMinTemp_();
    }

    public String getMaxTemp() {
        return this.temperature.getMaxTemp_();
    }

}






