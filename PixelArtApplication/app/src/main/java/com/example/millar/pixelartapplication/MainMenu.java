package com.example.millar.pixelartapplication;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;

public class MainMenu extends AppCompatActivity {
    ConstraintLayout layout;
    ListView drawings;
    drawingsAdapter filesAdapter;

    Button newDrawingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Find views
        layout = findViewById(R.id.MainMenu_layout);
        drawings = findViewById(R.id.Drawings);

        // newDrawingButton functionality
        newDrawingButton = findViewById(R.id.newDrawing);
        newDrawingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), newDrawing.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
            Display all files onResume() so that they will be refreshed if the user navigates
            back to this activity
        */

        /*
            each saved drawing has two files "drawing.png" and "drawing.txt"
            The txt file contains variables (background colour, pixels wide, pixels high, size of each pixel)
            The png file contains the drawing itself
        */
        File filesDir = new File(getFilesDir(), "/Drawings");
        File[] files = filesDir.listFiles();
        for(int i = 0; i < files.length; i++) {
            System.out.println(files[i].getName());
        }
//        String[] files = fileList();

        // Only want to display the file name with no extension and remove duplicates
        String[] filesList;
        if(files != null) {
            filesList = new String[files.length / 2];
            int count = 0;
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i]);
                String file = files[i].getName().split("\\.")[0];
                if (!Arrays.asList(filesList).contains(file)) {
                    filesList[count] = file;
                    count++;
                }
            }
        } else {
            filesList = new String[0];
        }


        // Add files into the list
        filesAdapter = new drawingsAdapter(this, filesList);
        drawings.setAdapter(filesAdapter);
    }
}
