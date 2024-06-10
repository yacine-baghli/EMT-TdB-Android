package com.enicay.TdBEMT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class Parametre extends AppCompatActivity {

    private NumberPicker tours_max, moyenne, ecart_moyenne;
    private Button button_allemand;
    private Button button_anglais;
    private Button button_arabe;
    private Button button_espagnol;
    private Button button_francais;

    private static final int defval_Nb_tours_max = 11;
    private static final int defval_moyenne_val = 15;
    private static final int defval_ecart_moyenne_val = 4;

    int Nb_tours_max,moyenne_val,ecart_moyenne_val;
    String URL_SERVER;


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametre);

        button_francais = findViewById(R.id.button_francais);
        button_allemand = findViewById(R.id.button_allemand);
        button_anglais = findViewById(R.id.button_anglais);
        button_arabe = findViewById(R.id.button_arabe);
        button_espagnol = findViewById(R.id.button_espagnol);

        button_francais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage("fr");
                startActivity(new Intent(Parametre.this,MainActivity.class));
            }
        });

        button_allemand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage("de");
                startActivity(new Intent(Parametre.this,MainActivity.class));
            }
        });

        button_anglais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage("en");
                startActivity(new Intent(Parametre.this,MainActivity.class));
            }
        });

        button_arabe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage("ar");
                startActivity(new Intent(Parametre.this,MainActivity.class));
            }
        });

        button_espagnol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLanguage("es");
                startActivity(new Intent(Parametre.this,MainActivity.class));
            }
        });



        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
        Nb_tours_max = pref.getInt("Nb_tours_max",defval_Nb_tours_max);
        moyenne_val = pref.getInt("moyenne_val",defval_moyenne_val);
        ecart_moyenne_val = pref.getInt("ecart_moyenne_val",defval_ecart_moyenne_val);

        tours_max = findViewById(R.id.tours_max);

        tours_max.setMinValue(1);
        tours_max.setMaxValue(99);
        tours_max.setValue(Nb_tours_max);
        tours_max.setWrapSelectorWheel(true);

        moyenne = findViewById(R.id.moyenne);

        moyenne.setMinValue(1);
        moyenne.setMaxValue(99);
        moyenne.setValue(moyenne_val);
        moyenne.setWrapSelectorWheel(true);


        ecart_moyenne = findViewById(R.id.ecart_moyenne);

        ecart_moyenne.setMinValue(1);
        ecart_moyenne.setMaxValue(99);
        ecart_moyenne.setValue(ecart_moyenne_val);
        ecart_moyenne.setWrapSelectorWheel(true);

        EditText url_editText = findViewById(R.id.url_editText);

        url_editText.setText("https://embesystems.com/emt/index.php");


        ImageButton info = findViewById(R.id.Info);
        ImageButton home = findViewById(R.id.home);


        info.setOnClickListener(view -> {
            SharedPreferences.Editor editor = pref.edit();

            Nb_tours_max = tours_max.getValue();
            moyenne_val = moyenne.getValue();
            ecart_moyenne_val = ecart_moyenne.getValue();

            URL_SERVER = url_editText.getText().toString();

            editor.putInt("Nb_tours_max",Nb_tours_max);
            editor.putInt("moyenne_val",moyenne_val);
            editor.putInt("ecart_moyenne_val",ecart_moyenne_val);

            editor.putString("URL_SERVER",URL_SERVER);

            editor.commit();
            editor.apply();

            Intent intent = new Intent(Parametre.this,Info.class);
            startActivity(intent);
            finish();
        });



        home.setOnClickListener(view -> {
            SharedPreferences.Editor editor = pref.edit();

            Nb_tours_max = tours_max.getValue();
            moyenne_val = moyenne.getValue();
            ecart_moyenne_val = ecart_moyenne.getValue();

            URL_SERVER = url_editText.getText().toString();

            editor.putInt("Nb_tours_max",Nb_tours_max);
            editor.putInt("moyenne_val",moyenne_val);
            editor.putInt("ecart_moyenne_val",ecart_moyenne_val);

            editor.putString("URL_SERVER",URL_SERVER);


            editor.commit();
            editor.apply();

            Intent intent = new Intent(Parametre.this,MainActivity.class);

            startActivity(intent);
            finish();
        });


    }

    public void onBackPressed() {
        super.onBackPressed();


        EditText url_editText = findViewById(R.id.url_editText);


        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        Nb_tours_max = tours_max.getValue();
        moyenne_val = moyenne.getValue();
        ecart_moyenne_val = ecart_moyenne.getValue();

        URL_SERVER = url_editText.getText().toString();


        editor.putInt("Nb_tours_max",Nb_tours_max);
        editor.putInt("moyenne_val",moyenne_val);
        editor.putInt("ecart_moyenne_val",ecart_moyenne_val);

        editor.putString("URL_SERVER",URL_SERVER);

        editor.commit();
        editor.apply();

        finish();
    }

    public void setLanguage(String languageCode){
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

}