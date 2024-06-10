package com.enicay.TdBEMT;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Info extends AppCompatActivity {


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

//        View text_info = findViewById(R.id.text_info);
//        text_info.setMo(new ScrollingMovementMethod());

        ImageButton parametre = findViewById(R.id.Parametre);
        ImageButton home = findViewById(R.id.home);
//        Button suivant = findViewById(R.id.suivant);

//        suivant.setOnClickListener(view -> {
//            Intent intent = new Intent(Info.this,Info2.class);
//            startActivity(intent);
//            finish();
//        });

        parametre.setOnClickListener(view -> {
            Intent intent = new Intent(Info.this,Parametre.class);
            startActivity(intent);
            finish();
        });

        home.setOnClickListener(view -> {
            Intent intent = new Intent(Info.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
    }


}