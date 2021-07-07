package com.example.vivavida;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.text.LineBreaker;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class SavedActActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private Button Delete;
    private GoogleMap mMap;


    private ActivityDbAdapter dbAdapter;
    private Long mRowId;
    private String route_id;

    private PolylineOptions lineOptions;
    private Polyline lineRoute;
    private List<LatLng> points; //List of points to join lines

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_act);

        // obtiene referencia a los tres views que componen el layout
        TextView type = (TextView) findViewById(R.id.Activity);
        TextView distance = (TextView) findViewById(R.id.Distance);
        TextView time = (TextView) findViewById(R.id.Time);
        TextView kcal = (TextView) findViewById(R.id.Kcal);
        Delete = (Button) findViewById(R.id.delete);

        //creamos el adaptador de la BD y la abrimos
        dbAdapter = new ActivityDbAdapter(this);
        dbAdapter.open();

        // obtiene id de fila de la tabla si se le ha pasado (hemos pulsado una nota para editarla)
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(ActivityDbAdapter.KEY_ROWID1);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(ActivityDbAdapter.KEY_ROWID1) : null;
        }

        //Si el id no es nulo, obtener las estadisticas de la actividad
        if (mRowId != null) {
            Cursor activity = dbAdapter.fetchActivity(mRowId);
            type.setText(activity.getString(
                    activity.getColumnIndexOrThrow(ActivityDbAdapter.KEY_TYPE)));
            distance.setText(activity.getString(
                    activity.getColumnIndexOrThrow(ActivityDbAdapter.KEY_DISTANCE)));
            time.setText(activity.getString(
                    activity.getColumnIndexOrThrow(ActivityDbAdapter.KEY_TIME)));
            kcal.setText(activity.getString(
                    activity.getColumnIndexOrThrow(ActivityDbAdapter.KEY_KCAL)));
            route_id = activity.getString(activity.getColumnIndexOrThrow(ActivityDbAdapter.KEY_ROUTEID));
        }
    }

    //Crear actionbar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        SupportMapFragment mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapsaved));
        mMap.getMapAsync(this);
        Delete.setOnClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    //Manejador de los botones del actionbar
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2.setMessage(R.string.about).setTitle(R.string.about_title);
            AlertDialog About = builder2.show();
            TextView messagev = (TextView) About.findViewById(android.R.id.message);
            messagev.setGravity(Gravity.CENTER);
            messagev.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

            return true;
        }
        //Si sales de la actividad, cerrar BBDD
        if (item.getItemId() == android.R.id.home) {
            dbAdapter.close();
            finish();
            onBackPressed();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Manejador del boton para eliminar actividad
    @Override
    public void onClick(View v) {
        if (v.getId() == (R.id.delete)) {
            dbAdapter.deleteActivity(mRowId);
            dbAdapter.close();
            finish();
        }
    }

    /**
     * Sets GoogleMaps settings to be displayed.
     */
    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        mMap = googleMap;
        LatLng position;

        //Inicializar trazado
        Cursor mCursor = dbAdapter.fetchPoints(route_id);
        lineOptions = new PolylineOptions().width(5).color(this.getResources().getColor(R.color.myorange));
        lineRoute = mMap.addPolyline(lineOptions);
        points = lineRoute.getPoints();

        //Guardar puntos latlng de la BBDD para trazarlos
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                position = new LatLng(Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow(ActivityDbAdapter.KEY_LAT))),
                        Double.parseDouble(mCursor.getString(mCursor.getColumnIndexOrThrow(ActivityDbAdapter.KEY_LNG))));

                points.add(position);
                mCursor.moveToNext();
            } while (!mCursor.isAfterLast());

            //trazar puntos y animar camara
            lineRoute.setPoints(points);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }
    }
}