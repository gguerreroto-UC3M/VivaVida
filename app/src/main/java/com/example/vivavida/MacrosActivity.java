package com.example.vivavida;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;


public class MacrosActivity extends AppCompatActivity implements View.OnClickListener {

    //Obtener todas los objetos del layout

    //Valoes para mostrar progreso de las macros
    private TextView carb_por;
    private TextView proteins_por;
    private TextView calories_por;
    private TextView fats_por;

    //Valores para el dialogo tras escaneo
    private TextView carbs_scan;
    private TextView proteins_scan;
    private TextView kcal_scan;
    private TextView fats_scan;

    //Barras de progreso para cada macro
    private ProgressBar progressBar0;
    private ProgressBar progressBar1;
    private ProgressBar progressBar2;
    private ProgressBar progressBar3;

    //Botones para editar el objetivo de macros
    private ImageButton button_carb;
    private ImageButton button_proteins;
    private ImageButton button_calories;
    private ImageButton button_fats;

    //Boton para escanear producto
    private Button button_scan;

    //Radio group para selecionar macro manualmente
    private RadioGroup radioGroup;
    private RadioButton radio0;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;

    //Slider para ingresar la cantidad de macro manualmente
    private SeekBar seekBar;

    //Valor correspondeinte al slider
    private TextView textView_quantity;

    //Variables para introducir la cantidad del producto escaneado
    private double quantity = 0;
    private EditText quantityPer;
    private Button Done2; //Boton del dialogo tras escaneo

    //Items del dialogo para editar el objetivo de macro
    private EditText goal;
    private Button Done;

    //Objetivos editables para cada macro
    private int maxCarb;
    private int maxProteins;
    private int maxFats;
    private int maxCalories;

    //Cantidad actual de cada macro
    private int ActualCarb;
    private int ActualProteins;
    private int ActualCalories;
    private int ActualFats;

    //'1' para guardar preferencias
    private int savepref = 0;

    //Variables para inidcar macro completada
    private int comp1 = 0;
    private int comp2 = 0;
    private int comp3 = 0;
    private int comp4 = 0;

    //Codigo del producto escaneado
    private String productCode = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macros);

        //Inicializar todos los elementos de los distintos layouts
        button_carb = (ImageButton) findViewById(R.id.button_carbs);
        button_proteins = (ImageButton) findViewById(R.id.button_proteins);
        button_calories = findViewById(R.id.button_calories);
        button_fats = (ImageButton) findViewById(R.id.button_fats);
        button_scan = findViewById(R.id.button_scan);

        button_carb.setOnClickListener(this);
        button_proteins.setOnClickListener(this);
        button_calories.setOnClickListener(this);
        button_fats.setOnClickListener(this);
        button_scan.setOnClickListener(view -> scan());

        radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        radio0 = (RadioButton) findViewById(R.id.radio0);
        radio1 = (RadioButton) findViewById(R.id.radio1);
        radio2 = (RadioButton) findViewById(R.id.radio2);
        radio3 = (RadioButton) findViewById(R.id.radio3);

        carb_por = (TextView) findViewById(R.id.carbs_por);
        proteins_por = (TextView) findViewById(R.id.proteins_por);
        calories_por = (TextView) findViewById(R.id.calories_por);
        fats_por = (TextView) findViewById(R.id.fats_por);

        textView_quantity = (TextView) findViewById(R.id.textView_quantity);

        progressBar0 = (ProgressBar) findViewById(R.id.progressBar0);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        //Cargar preferencias
        loadPreferences();

        //Eliminar preferencias cada 24h. Para comprobar que funciona, reducir el tiempo a 30000 segundos
        new CountDownTimer(30000, 1000) { //86400000 seconds in 24h
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(getApplicationContext(), "La cuenta llega a 0 en: " + millisUntilFinished / 1000, Toast.LENGTH_SHORT).show();
            }

            public void onFinish() {
                //Una vez terminado el temporizador, eliminar preferencias
                deletePreferences();

            }
        }.start();

        //Funciones para manejar el slider
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantity = progress;
                textView_quantity.setText("" + progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //Crear actionbar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Manejador para los botones de editar objetivo de macro y escaneo, y mostrar su dialogo
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button_carbs:
                showDialoge(0);
                break;

            case R.id.button_proteins:
                showDialoge(1);
                break;

            case R.id.button_calories:
                showDialoge(2);
                break;

            case R.id.button_fats:
                showDialoge(3);
                break;
            case R.id.button_scan:
                scan();
                break;
            default:
                break;
        }
    }

    //Crear dialogo para editar macro, establecer el ojetivo introducido y actualizar las barras de progreso
    public void showDialoge(int macro) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialogo_macros, null);
        Done = (Button) view.findViewById(R.id.Done);
        goal = (EditText) view.findViewById(R.id.goal);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        Done.setOnClickListener(v -> {
            try {
                switch (macro) {
                    case 0:
                        maxCarb = Integer.parseInt(goal.getText().toString().trim());
                        break;
                    case 1:
                        maxProteins = Integer.parseInt(goal.getText().toString().trim());
                        break;
                    case 2:
                        maxCalories = Integer.parseInt(goal.getText().toString().trim());
                        break;

                    default:
                        maxFats = Integer.parseInt(goal.getText().toString().trim());
                        break;
                }

                progressBar0.setProgress((ActualCarb * 100) / maxCarb);
                carb_por.setText(" " + ActualCarb + "/" + maxCarb);
                progressBar1.setProgress((ActualProteins * 100) / maxProteins);
                proteins_por.setText(" " + ActualProteins + "/" + maxProteins);
                progressBar2.setProgress((ActualFats * 100) / maxFats);
                fats_por.setText(" " + ActualFats + "/" + maxFats);
                progressBar3.setProgress((ActualCalories * 100) / maxCalories);
                calories_por.setText(" " + ActualCalories + "/" + maxCalories);

            } catch (NumberFormatException nfe) {
                Toast.makeText(getApplicationContext(), " Please enter a numeric value  ", Toast.LENGTH_SHORT).show();

            }
            //Guardar en preferencias
            savePreferences();
            dialog.dismiss();
        });
    }


    //Metodos para la gestion de las preferencias
    private void savePreferences() {
        SharedPreferences myPreferences = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putInt("CARBS", maxCarb);
        myEditor.putInt("PROTEINS", maxProteins);
        myEditor.putInt("FATS", maxFats);
        myEditor.putInt("CALORIES", maxCalories);
        myEditor.putInt("A_CARBS", ActualCarb);
        myEditor.putInt("A_PROTEINS", ActualProteins);
        myEditor.putInt("A_FATS", ActualFats);
        myEditor.putInt("A_CALORIES", ActualCalories);
        myEditor.apply();
    }

    private void loadPreferences() {
        SharedPreferences myPreferences = this.getPreferences(MODE_PRIVATE);

        maxCarb = myPreferences.getInt("CARBS", 500);
        maxProteins = myPreferences.getInt("PROTEINS", 500);
        maxFats = myPreferences.getInt("FATS", 500);
        maxCalories = myPreferences.getInt("CALORIES", 500);
        ActualCarb = myPreferences.getInt("A_CARBS", 0);
        ActualProteins = myPreferences.getInt("A_PROTEINS", 0);
        ActualFats = myPreferences.getInt("A_FATS", 0);
        ActualCalories = myPreferences.getInt("A_CALORIES", 0);
        //Actualizar cada vez que se cargen las preferencias
        progressBar0.setProgress((ActualCarb * 100) / maxCarb);
        carb_por.setText(" " + ActualCarb + "/" + maxCarb);
        progressBar1.setProgress((ActualProteins * 100) / maxProteins);
        proteins_por.setText(" " + ActualProteins + "/" + maxProteins);
        progressBar2.setProgress((ActualFats * 100) / maxFats);
        fats_por.setText(" " + ActualFats + "/" + maxFats);
        progressBar3.setProgress((ActualCalories * 100) / maxCalories);
        calories_por.setText(" " + ActualCalories + "/" + maxCalories);
    }

    private void deletePreferences() {
        SharedPreferences myPreferences = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putInt("A_CARBS", 0);
        myEditor.putInt("A_PROTEINS", 0);
        myEditor.putInt("A_FATS", 0);
        myEditor.putInt("A_CALORIES", 0);
        myEditor.apply();
    }


    //Funcion que añade la macro especificada por el usuario, actualiza el valor de progreso y la barra de progreso para cada macro
    public void addMacro(View view) {
        if (R.id.button_add == view.getId()) {
            if (radio0.isChecked()) {
                if (quantity != 0) {
                    if (comp1 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Carbs " + quantity, Toast.LENGTH_SHORT).show();
                        ActualCarb += quantity;
                        if (ActualCarb >= maxCarb) {
                            ActualCarb = maxCarb;
                            progressBar0.setProgress(100);
                            carb_por.setText(" " + ActualCarb + "/" + maxCarb);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                            comp1 = 1;
                        } else {
                            progressBar0.setProgress((ActualCarb * 100) / maxCarb);
                            carb_por.setText(" " + ActualCarb + "/" + maxCarb);
                        }
                        savepref = 1;
                        radio0.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please, enter a quantity", Toast.LENGTH_SHORT).show();
                }
            } else if (radio1.isChecked()) {
                if (quantity != 0) {
                    if (comp2 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Proteins " + quantity, Toast.LENGTH_SHORT).show();
                        ActualProteins += quantity;
                        if (ActualProteins >= maxProteins) {
                            ActualProteins = maxProteins;
                            progressBar1.setProgress(100);
                            proteins_por.setText(" " + ActualProteins + "/" + maxProteins);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                            comp2 = 1;
                        } else {
                            progressBar1.setProgress((ActualProteins * 100) / maxProteins);
                            proteins_por.setText(" " + ActualProteins + "/" + maxProteins);
                        }
                        savepref = 1;
                        radio1.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please, enter a quantity", Toast.LENGTH_SHORT).show();
                }
            } else if (radio2.isChecked()) {
                if (quantity != 0) {
                    if (comp3 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Fats " + quantity, Toast.LENGTH_SHORT).show();
                        ActualFats += quantity;
                        if (ActualFats >= maxFats) {
                            comp3 = 1;
                            ActualFats = maxFats;
                            progressBar2.setProgress(100);
                            fats_por.setText(" " + ActualFats + "/" + maxFats);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar2.setProgress((ActualFats * 100) / maxFats);
                            fats_por.setText(" " + ActualFats + "/" + maxFats);
                        }
                        savepref = 1;
                        radio2.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please, enter a quantity", Toast.LENGTH_SHORT).show();
                }
            } else if (radio3.isChecked()) {
                if (quantity != 0) {
                    if (comp4 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Kcal " + quantity, Toast.LENGTH_SHORT).show();
                        ActualCalories += quantity;
                        if (ActualCalories >= maxCalories) {
                            ActualCalories = maxCalories;
                            progressBar3.setProgress(100);
                            calories_por.setText(" " + ActualCalories + "/" + maxCalories);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                            comp4 = 1;
                        } else {
                            progressBar3.setProgress((ActualCalories * 100) / maxCalories);
                            calories_por.setText(" " + ActualCalories + "/" + maxCalories);
                        }
                        savepref = 1;
                        radio3.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please, enter a quantity", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please, select one Macro", Toast.LENGTH_SHORT).show();
            }
            radioGroup.clearCheck(); //Borrar la opcion seleccionada
        }
        if (savepref == 1) {
            //Guardar en preferencias
            savePreferences();
            savepref = 0;
        }
    }

    //Metodo para iniciar actividad de escaneo
    public void scan() {
        IntentIntegrator intent = new IntentIntegrator(this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.setPrompt("scan code");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.setOrientationLocked(false);
        intent.setCaptureActivity(ScanningActivity.class);
        intent.initiateScan();

    }

    //Resultado de la actividad de escaneo: el codigo del producto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //button_scan.setText(result.getContents().toString().trim());
            productCode = result.getContents();
            new searchProduct().execute(productCode);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        //

    }

    //Metodo en segundo plano para obtener las macros a partir del codigo del producto, accediendo al servidor de OpenFoodFacts
    public class searchProduct extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            final String barcode = strings[0];
            String result = null;
            try {
                final String jsonStr = Jsoup.connect(
                        "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json")
                        .ignoreContentType(true)
                        .execute()
                        .body();

                final JSONObject jsonObj = new JSONObject(jsonStr);
                if (jsonObj.has("product")) {
                    JSONObject productNode = jsonObj.getJSONObject("product");
                    if (productNode.has("nutriments")) {
                        JSONObject nutriments = productNode.getJSONObject("nutriments");
                        result = String.valueOf(nutriments.getDouble("carbohydrates_100g"));
                        result = result + "-" + String.valueOf(nutriments.getDouble("proteins_100g"));
                        result = result + "-" + String.valueOf(nutriments.getDouble("fat_100g"));
                        result = result + "-" + String.valueOf(nutriments.getDouble("energy-kcal_100g"));

                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        //Mostrar dialogo con las macros, si se ha devuelto null quiere decir que el producto no esta disponible en OpenFoodFacts
        @Override
        protected void onPostExecute(@Nullable String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(getApplicationContext(), "Sorry, there is no data available for the scanned product.", Toast.LENGTH_LONG).show();
            } else {
                String[] parts = result.split("-");
                showProductDialogue(parts);
            }

        }
    }

    //Crear dialogo para mostrat macros del producto escaneado e introducir su cantidad
    public void showProductDialogue(String[] parts) {

        LayoutInflater inflater2 = getLayoutInflater();
        View view = inflater2.inflate(R.layout.dialog_scan_product, null);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setCancelable(false);
        builder2.setView(view);

        carbs_scan = view.findViewById(R.id.CarbsValue);
        proteins_scan = view.findViewById(R.id.ProteinsValue);
        fats_scan = view.findViewById(R.id.FatsValue);
        kcal_scan = view.findViewById(R.id.kcalValue);
        Done2 = (Button) view.findViewById(R.id.Done2);
        quantityPer = (EditText) view.findViewById(R.id.prodQuantity);

        carbs_scan.setText(parts[0]);
        proteins_scan.setText(parts[1]);
        fats_scan.setText(parts[2]);
        kcal_scan.setText(parts[3]);
        AlertDialog dialog2 = builder2.create();
        dialog2.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog2.show();

        //Cuando se manda la cantidad de producto escaneado, volver a actualizar los valores de progreso y barras de progreso en funcion de las macros del producto y su cantidad
        Done2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    quantity = Double.parseDouble(parts[0]);
                    if (comp1 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Carbs " + quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100, Toast.LENGTH_SHORT).show();
                        ActualCarb += quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100;
                        if (ActualCarb >= maxCarb) {
                            ActualCarb = maxCarb;
                            progressBar0.setProgress(100);
                            carb_por.setText(" " + ActualCarb + "/" + maxCarb);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                            comp1 = 1;
                        } else {
                            progressBar0.setProgress((ActualCarb * 100) / maxCarb);
                            carb_por.setText(" " + ActualCarb + "/" + maxCarb);
                        }
                        savepref = 1;
                        radio0.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }

                    quantity = Double.parseDouble(parts[1]);
                    if (comp2 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Proteins " + quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100, Toast.LENGTH_SHORT).show();
                        ActualProteins += (quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100);
                        if (ActualProteins >= maxProteins) {
                            ActualProteins = maxProteins;
                            progressBar1.setProgress(100);
                            proteins_por.setText(" " + ActualProteins + "/" + maxProteins);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                            comp2 = 1;
                        } else {
                            progressBar1.setProgress((ActualProteins * 100) / maxProteins);
                            proteins_por.setText(" " + ActualProteins + "/" + maxProteins);
                        }
                        savepref = 1;
                        radio1.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }

                    quantity = Double.parseDouble(parts[2]);
                    if (comp3 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Fats " + (quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100), Toast.LENGTH_SHORT).show();
                        ActualFats += (quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100);
                        if (ActualFats >= maxFats) {
                            comp3 = 1;
                            ActualFats = maxFats;
                            progressBar2.setProgress(100);
                            fats_por.setText(" " + ActualFats + "/" + maxFats);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar2.setProgress((ActualFats * 100) / maxFats);
                            fats_por.setText(" " + ActualFats + "/" + maxFats);
                        }
                        savepref = 1;
                        radio2.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }

                    quantity = Double.parseDouble(parts[3]);
                    if (comp4 == 0) {
                        Toast.makeText(getApplicationContext(), "Adding to Kcal " + (quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100), Toast.LENGTH_SHORT).show();
                        ActualCalories += (quantity * Integer.parseInt(quantityPer.getText().toString().trim()) / 100);
                        if (ActualCalories >= maxCalories) {
                            ActualCalories = maxCalories;
                            progressBar3.setProgress(100);
                            calories_por.setText(" " + ActualCalories + "/" + maxCalories);
                            Toast.makeText(getApplicationContext(), "Congratulations, you complete your macro  ", Toast.LENGTH_SHORT).show();
                            comp4 = 1;
                        } else {
                            progressBar3.setProgress((ActualCalories * 100) / maxCalories);
                            calories_por.setText(" " + ActualCalories + "/" + maxCalories);
                        }
                        savepref = 1;
                        radio3.setChecked(false);
                        seekBar.setProgress(0);
                    } else {
                        Toast.makeText(getApplicationContext(), "You have already completed your macro. Please select another one  ", Toast.LENGTH_SHORT).show();
                    }

                    quantity = 0;

                } catch (NumberFormatException nfe) {
                    Toast.makeText(getApplicationContext(), " Please enter a numeric value  ", Toast.LENGTH_SHORT).show();
                }

                //Guardar en preferencias
                savePreferences();
                dialog2.cancel();
            }
        });
    }

    //Manejador de los botones de la actionbar
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder2.setMessage(R.string.about).setTitle(R.string.about_title);
                androidx.appcompat.app.AlertDialog About = builder2.show();
                TextView messagev = (TextView) About.findViewById(android.R.id.message);
                messagev.setGravity(Gravity.CENTER);
                messagev.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}