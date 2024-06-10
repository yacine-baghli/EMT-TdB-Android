package com.enicay.TdBEMT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

//    private Button info,parametre,TdB,staff;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton info = findViewById(R.id.info);
        ImageButton parametre = findViewById(R.id.parametre);
        ImageButton TdB = findViewById(R.id.TdB);
        ImageButton staff = findViewById(R.id.staff);



        info.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,Info.class);
            startActivity(intent);
            //finish(); On n'a pas besoin de finish la main activity elle restera toujours actif en fond
        });

        parametre.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,Parametre.class);
            startActivity(intent);
            //finish(); On n'a pas besoin de finish la main activity elle restera toujours actif en fond
        });

        TdB.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,Tdb.class);
            startActivity(intent);
            //finish(); On n'a pas besoin de finish la main activity elle restera toujours actif en fond
        });

        staff.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Staff.class);
            startActivity(intent);
            //finish(); On n'a pas besoin de finish la main activity elle restera toujours actif en fond
        });


    }
}