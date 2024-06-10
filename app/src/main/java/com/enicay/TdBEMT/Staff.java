package com.enicay.TdBEMT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

public class Staff extends AppCompatActivity {

    WebView EMT_site;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_staff);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW);
//        getSupportActionBar().hide();

        SharedPreferences pref = getSharedPreferences("priv_settings", Context.MODE_PRIVATE);
//        int Nb_tours_max = pref.getInt("Nb_tours_max", 5);
        String URL_SERVER = pref.getString("URL_SERVER","https://embesystems.com/emt/index.php");

        TextView EMTWebSitetxt = findViewById(R.id.EMTWebSitetxt);
        EMTWebSitetxt.setText("WebView from the web site :\n"+URL_SERVER);

        EMT_site = findViewById(R.id.EMTWebSite);
        EMT_site.getSettings().setJavaScriptEnabled(true);
        EMT_site.setWebViewClient(new WebViewClient());
        EMT_site.loadUrl(URL_SERVER);

        ImageButton parametre = findViewById(R.id.Parametre);
        parametre.setOnClickListener(view -> {
            Intent intent = new Intent(Staff.this,Parametre.class);
            startActivity(intent);
            finish();
        });

        ImageButton info = findViewById(R.id.Info);
        info.setOnClickListener(view -> {
            Intent intent = new Intent(Staff.this,Info.class);
            startActivity(intent);
            finish();
        });

    }
}