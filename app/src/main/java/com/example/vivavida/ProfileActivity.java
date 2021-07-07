package com.example.vivavida;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnDismissListener {

    //Elementos del layout
    private String currentPhotoPath;
    private String currentPhotoURI;
    private ImageButton ProfilePic;
    private EditText NameEdit;
    private EditText HeightEdit;
    private EditText WeightEdit;

    //Codigo para los permisos de camara
    private final int CAMERA_ENABLE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ProfilePic = (ImageButton) findViewById(R.id.ProfilePic);
        ProfilePic.setOnClickListener(this);

        //Obtener preferencias
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);

        //Poner foto de perfil segun si existe, y si es tomada con la camara o desde galeria
        if (!(sharedPref.contains("PhotoPath") || sharedPref.contains("PhotoURI"))) {
            ProfilePic.setImageResource(R.drawable.ic_baseline_account_circle_24);
        } else if (sharedPref.contains("PhotoURI")) {
            Uri image = Uri.parse(sharedPref.getString("PhotoURI", null));
            ProfilePic.setImageURI(image);
        } else {
            File f = new File(sharedPref.getString("PhotoPath", null));
            Uri contentUri = Uri.fromFile(f);
            ProfilePic.setImageURI(contentUri);
        }

        //Crear spinner con las opciones de genero
        final String[] options = new String[]{"Man", "Woman", "Other"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, options);
        final Spinner selectOpts = (Spinner) findViewById(R.id.select_genre);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        selectOpts.setAdapter(adapter);

        //Guardar posicion del spinner en preferencias
        if (sharedPref.contains("item")) {
            int pos = adapter.getPosition(sharedPref.getString("item", null));
            selectOpts.setSelection(pos);
        }

        //Manejador spinner
        selectOpts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String spinnerValue = selectOpts.getSelectedItem().toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("item", spinnerValue);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Editar nombre, y cargar si existe en preferencias
        NameEdit = (EditText) findViewById(R.id.edit_name);
        if (sharedPref.contains("name")) {
            NameEdit.setText(sharedPref.getString("name", null));
        }
        NameEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("name", v.getText().toString());
                editor.apply();
            }
            return false;
        });

        //Editar altura, y cargar si existe en preferencias
        HeightEdit = (EditText) findViewById(R.id.edit_height);
        if (sharedPref.contains("height")) {
            HeightEdit.setText(sharedPref.getString("height", null));
        }
        HeightEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("height", v.getText().toString());
                editor.apply();
            }
            return false;
        });

        //Editar altura, y cargar si existe en preferencias
        WeightEdit = (EditText) findViewById(R.id.edit_weight);
        if (sharedPref.contains("weight")) {
            WeightEdit.setText(sharedPref.getString("weight", null));
        }
        WeightEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("weight", v.getText().toString());
                editor.apply();
            }
            return false;
        });

        //Comprobar permisos de camara, y si no hay, solicitarlos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_ENABLE);
        }
    }

    //Crear actionbar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Manejador para la foto de perfil
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ProfilePic) {
            //Cread dialogo para elegir o tomar foto de perfil o desde la galeria
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.profile_pic).setItems(R.array.picoptions, (dialog, which) -> {
                switch (which) {
                    case 0:
                        takePhoto(); //Tomar foto
                        break;
                    case 1:
                        getPhoto();//Galeria
                        break;
                }
            });
            builder.show();
        }
    }

    //Manejador tras volver o de tomar la foto o escogerla de la galeria
    protected void onActivityResult(int requestCode, int resultCode, Intent
            imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    // Creamos colecciÃ³n de preferencias
                    SharedPreferences Prefs = this.getPreferences(MODE_PRIVATE);

                    // Obtenemos un editor de preferencias
                    SharedPreferences.Editor editor = Prefs.edit();

                    // Guardamos el valor de la preferencia
                    editor.putString("PhotoPath", currentPhotoPath);

                    if (Prefs.contains("PhotoURI")) {
                        editor.remove("PhotoURI");
                    }
                    editor.apply();
                    File f = new File(Prefs.getString("PhotoPath", null));
                    Uri contentUri = Uri.fromFile(f);
                    ProfilePic.setImageURI(contentUri);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri contentUri = imageReturnedIntent.getData();
                    currentPhotoURI = contentUri.toString();
                    SharedPreferences Prefs = this.getPreferences(MODE_PRIVATE);

                    // Obtenemos un editor de preferencias
                    SharedPreferences.Editor editor = Prefs.edit();

                    // Guardamos el valor de la preferencia
                    editor.putString("PhotoURI", currentPhotoURI);
                    if (Prefs.contains("PhotoPath")) {
                        editor.remove("PhotoPath");
                    }
                    editor.apply();
                    ProfilePic.setImageURI(contentUri);
                }
        }
    }

    //Metodo para tomar foto. Origen: https://developer.android.com/training/camera/photobasics
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 0);
            }
        }
    }

    //Metodo para escoger foto de la galeria. Origen: https://developer.android.com/training/camera/photobasics
    private void getPhoto() {
        Intent pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, 1);

    }

    //Meetodo para crear fichero de imagen a patir de la foto tomada. Origen: https://developer.android.com/training/camera/photobasics
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
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

    //Manejador para la respuesta despues de preguntar por permiso de camara
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CAMERA_ENABLE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.CAMERA)) {
            // Enable the my location layer if the permission has been granted.
        } else {
            // Display the missing permission error dialog when the fragments resume.
            PermissionDeniedDialog
                    .newInstance(true).show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    //clase para manejar el dialogo de permiso de camara
    public static class PermissionDeniedDialog extends DialogFragment {

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private boolean mFinishActivity = false;

        /**
         * Creates a new instance of this dialog and optionally finishes the calling Activity
         * when the 'Ok' button is clicked.
         */
        public static PermissionDeniedDialog newInstance(boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);

            PermissionDeniedDialog dialog = new PermissionDeniedDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mFinishActivity = getArguments().getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new android.app.AlertDialog.Builder(getActivity())
                    .setMessage(R.string.camera_permission_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mFinishActivity) {
                Toast.makeText(getActivity(), R.string.permission_required_toast2,
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }
}

