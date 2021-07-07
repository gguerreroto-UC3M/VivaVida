package com.example.vivavida;

import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Elementos del layout
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button1:
                Intent sport = new Intent(this, SportActivity.class);
                startActivity(sport);
                break;

            case R.id.button2:
                Intent Macros = new Intent(this, MacrosActivity.class);
                startActivity(Macros);
                break;

            case R.id.button3:
                Intent Profile = new Intent(this, ProfileActivity.class);
                startActivity(Profile);
                break;

            case R.id.button4:
                Intent Saved = new Intent(this, SavedActivity.class);
                startActivity(Saved);
                break;

            default:
                break;
        }
    }

    //Crear actionbar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Manejador de los botones de la actionbar
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage(R.string.about).setTitle(R.string.about_title);
                AlertDialog About = builder2.show();
                TextView messagev = (TextView) About.findViewById(android.R.id.message);
                messagev.setGravity(Gravity.CENTER);
                messagev.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
