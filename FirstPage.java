package com.example.project3_jsingh40;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class FirstPage extends AppCompatActivity {

    String FIREBASE_URL = "https://matchinggame-b675a-default-rtdb.firebaseio.com/";
    int gridSize;
    DatabaseReference db;
    EditText getGameID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        Intent i = getIntent();
        if (i != null) {
            if (i.hasExtra("grid")) {
                gridSize = i.getIntExtra("grid", MainActivity.GRID_4x3);
            }
        }
        else {
            gridSize = MainActivity.GRID_4x3;
        }
        FirebaseDatabase fd = FirebaseDatabase.getInstance(FIREBASE_URL);
        db = fd.getReference("matchinggames");

        Button createButton = findViewById(R.id.createButton);
        Button joinButton = findViewById(R.id.joinButton);
        getGameID = findViewById(R.id.getGameID);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGameButton();
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGameButton();
            }
        });

    }

    private void createGameButton() {
        String id = db.push().getKey();
        if (id == null)
            return;
        DatabaseReference dbref = db.child(id);
        dbref.child("grid").setValue(gridSize);
        dbref.child("gameboard").setValue(new ArrayList<>());
        dbref.child("playerturn").setValue("p1");
        Intent i;
        if (gridSize == MainActivity.GRID_5x4) {
            i = new Intent(this, MainActivity3.class);
        } else {
            i = new Intent(this, MainActivity2.class);
        }
        i.putExtra("getId", id);
        i.putExtra("playerId", "p1");
        startActivity(i);
    }


    private void joinGameButton() {
        String temp = getGameID.getText().toString();
        String id = temp.trim();
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Please input a game ID to join game.", Toast.LENGTH_SHORT).show();
            return;
        }
        getGridSize(id);
        Intent i;
        if (gridSize == MainActivity.GRID_5x4) {
            i = new Intent(this, MainActivity3.class);
        } else {
            i = new Intent(this, MainActivity2.class);
        }
        i.putExtra("getId", id);
        i.putExtra("playerId", "player2");
        startActivity(i);
    }


    private void getGridSize(String id) {
        DatabaseReference dbref = db.child(id).child("grid");
        dbref.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot ds) {
                        Integer getVal = ds.getValue(Integer.class);
                        if (getVal == null) {
                            Toast.makeText(FirstPage.this, "Wrong ID", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        gridSize = getVal;
                    }
                });
    }
}
