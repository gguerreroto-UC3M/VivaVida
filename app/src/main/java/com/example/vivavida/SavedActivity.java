package com.example.vivavida;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SavedActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityDbAdapter dbAdapter;
    private ListView m_listview;
    private Button delAllBut;

    //Codigo inclue partes del codigo NotepadBD proporcionada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        delAllBut = (Button) findViewById(R.id.deleteAll);

        //creamos el adaptador de la BD y la abrimos
        dbAdapter = new ActivityDbAdapter(this);
        dbAdapter.open();

        // Creamos un listview que va a contener el título de todas las notas y
        // en el que cuando pulsemos sobre un título lancemos una actividad de editar
        // la nota con el id correspondiente
        m_listview = (ListView) findViewById(R.id.id_list_view);
        m_listview.setOnItemClickListener(
                (arg0, view, position, id) -> {
                    Intent i = new Intent(view.getContext(), SavedActActivity.class);
                    i.putExtra(ActivityDbAdapter.KEY_ROWID1, id);
                    startActivity(i);
                }
        );
        // rellenamos el listview con el tipo de actividad y fecha de todas las actividades en la BD
        fillData();
    }

    //Crear action bar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        delAllBut.setOnClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    //Manejador para borrar todas las actividades de las BBDD y su dialogo de confirmacion
    public void onClick(View v) {
        if (v.getId() == (R.id.deleteAll)) {
            dbAdapter = new ActivityDbAdapter(this);
            dbAdapter.open();

            LayoutInflater inflater = getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.deleteall_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(SavedActivity.this);
            builder.setView(dialoglayout);

            Button yes = (Button) dialoglayout.findViewById(R.id.yes);
            Button no = (Button) dialoglayout.findViewById(R.id.no);

            final AlertDialog ad = builder.show();

            yes.setOnClickListener(v1 -> {
                dbAdapter.deleteAllActivities();
                ad.cancel();
                fillData();
            });

            no.setOnClickListener(v12 -> ad.cancel());
        }
    }

    private void fillData() {
        Cursor activityCursor = dbAdapter.fetchAllActivities();

        // Creamos un array con los campos que queremos mostrar en el listview
        String[] from = new String[]{ActivityDbAdapter.KEY_TYPE, ActivityDbAdapter.KEY_DATE};

        // array con los campos que queremos ligar a los campos del array de la línea anterior (en este caso sólo text1)
        int[] to = new int[]{R.id.text1, R.id.text2};

        // Creamos un SimpleCursorAdapter y lo asignamos al listview para mostrarlo
        SimpleCursorAdapter activities =
                new SimpleCursorAdapter(this, R.layout.activity_row, activityCursor, from, to, 0);
        m_listview.setAdapter(activities);
    }

    //Manejador de los botones del actionbar
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("NonConstantResourceId")
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        dbAdapter.open();
        fillData();
    }

    @Override
    protected void onStop(){
        super.onStop();
        dbAdapter.close();
    }
}