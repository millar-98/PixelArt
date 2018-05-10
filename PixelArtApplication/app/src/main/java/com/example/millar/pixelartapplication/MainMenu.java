package com.example.millar.pixelartapplication;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainMenu extends AppCompatActivity {
    ConstraintLayout layout;
    ListView drawings;
    ArrayAdapter filesAdapter;

    Button newDrawingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        layout = findViewById(R.id.MainMenu_layout);
        drawings = findViewById(R.id.Drawings);

        String[] files = fileList();
        filesAdapter = new ArrayAdapter<String>(this, R.layout.list_view, R.id.item, files);
        drawings.setAdapter(filesAdapter);

        drawings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String drawing = adapterView.getItemAtPosition(i).toString();

                Intent drawingIntent = new Intent(view.getContext(), Drawing.class);

                drawingIntent.putExtra("Loading", true);
                drawingIntent.putExtra("FileName", drawing);

                startActivity(drawingIntent);
            }
        });
        // newDrawingButton functionality
        newDrawingButton = findViewById(R.id.newDrawing);
        newDrawingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), newDrawing.class));
            }
        });
    }
}
