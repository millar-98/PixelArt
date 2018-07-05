package com.example.millar.pixelartapplication;

import android.content.Intent;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

public class newDrawing extends AppCompatActivity {
    ConstraintLayout layout;
    Button createButton;
    TextView dimensions;
    ColorPicker colorPicker;
    SVBar svBar;

    int width;
    int height;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_drawing);

        width = 64;
        height = 64;

        // Find all views
        layout = findViewById(R.id.newDrawingLayout);
        createButton = findViewById(R.id.create);
        dimensions = findViewById(R.id.dimensions);
        colorPicker = findViewById(R.id.picker2);
        svBar = findViewById(R.id.SVBar2);

        colorPicker.addSVBar(svBar);

        dimensions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) newDrawing.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View customView = inflater.inflate(R.layout.dimensions_popup_window, null);
                final PopupWindow popupWindow = new PopupWindow(customView, 1300, 1000, true);

                // TODO: Make this in a for loop somehow!!
                Button[] buttons = new Button[6];
                buttons[0] = customView.findViewById(R.id.b8);
                buttons[1] = customView.findViewById(R.id.b16);
                buttons[2] = customView.findViewById(R.id.b32);
                buttons[3] = customView.findViewById(R.id.b64);
                buttons[4] = customView.findViewById(R.id.b128);

                buttons[0].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        width = 8;
                        height = 8;

                        String dimensionsText = width + " X " + height;
                        dimensions.setText(dimensionsText);
                    }
                });
                buttons[1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        width = 16;
                        height = 16;

                        String dimensionsText = width + " X " + height;
                        dimensions.setText(dimensionsText);
                    }
                });
                buttons[2].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        width = 32;
                        height = 32;

                        String dimensionsText = width + " X " + height;
                        dimensions.setText(dimensionsText);
                    }
                });
                buttons[3].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        width = 64;
                        height = 64;

                        String dimensionsText = width + " X " + height;
                        dimensions.setText(dimensionsText);
                    }
                });
                buttons[4].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        width = 128;
                        height = 128;

                        String dimensionsText = width + " X " + height;
                        dimensions.setText(dimensionsText);
                    }
                });

                popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });


        // createButton functionality
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drawingIntent = new Intent(view.getContext(), Drawing.class);

                // Calculations
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int size_int = size.x / width;
                System.out.println(size.x + " " + size_int);
                int backgroundColour = colorPicker.getColor();

                // Pass parameters
                drawingIntent.putExtra("Loading", false);
                drawingIntent.putExtra("backgroundColour", backgroundColour);
                drawingIntent.putExtra("width", width);
                drawingIntent.putExtra("height", height);
                drawingIntent.putExtra("size", size_int);

                startActivity(drawingIntent);
            }
        });
    }
}
