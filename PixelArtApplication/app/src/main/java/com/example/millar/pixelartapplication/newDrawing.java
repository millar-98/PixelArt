package com.example.millar.pixelartapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class newDrawing extends AppCompatActivity {
    Bitmap drawingBitmap;

    Button createButton;
    EditText width;
    EditText height;
    ColorPicker colorPicker;
    SVBar svBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_drawing);

        // Find all views
        createButton = findViewById(R.id.create);
        width = findViewById(R.id.width);
        height = findViewById(R.id.height);
        colorPicker = findViewById(R.id.picker2);
        svBar = findViewById(R.id.SVBar2);

        colorPicker.addSVBar(svBar);


        // createButton functionality
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drawingIntent = new Intent(view.getContext(), Drawing.class);

                // Calculations
                int width_int = (!width.getText().toString().equals("")) ? Integer.parseInt(width.getText().toString()) : 0;
                int height_int = (!height.getText().toString().equals("")) ? Integer.parseInt(height.getText().toString()) : 0;
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int size_int = size.x / width_int;
                int backgroundColour = colorPicker.getColor();

                // Pass parameters
                drawingIntent.putExtra("Loading", false);
                drawingIntent.putExtra("backgroundColour", backgroundColour);
                drawingIntent.putExtra("width", width_int);
                drawingIntent.putExtra("height", height_int);
                drawingIntent.putExtra("size", size_int);

                startActivity(drawingIntent);
            }
        });
    }
}
