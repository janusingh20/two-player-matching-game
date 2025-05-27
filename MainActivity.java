package com.example.project3_jsingh40;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
     static int GRID_4x3 = 0;
     static int GRID_5x4 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button4x3 = findViewById(R.id.firstButton4x3);
        Button button5x4 = findViewById(R.id.secondButton5x4);
        button4x3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FirstPage.class);
                intent.putExtra("grid", GRID_4x3);
                startActivity(intent);
            }
        });

        button5x4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FirstPage.class);
                intent.putExtra("grid", GRID_5x4);
                startActivity(intent);
            }
        });
    }
}
