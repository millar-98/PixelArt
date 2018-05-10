package com.example.millar.pixelartapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class newDrawing extends AppCompatActivity {
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
                Intent drawing = new Intent(view.getContext(), Drawing.class);

                int width_int = (!width.getText().toString().equals("")) ? Integer.parseInt(width.getText().toString()) : 0;
                int height_int = (!height.getText().toString().equals("")) ? Integer.parseInt(height.getText().toString()) : 0;

                drawing.putExtra("Loading", false);
                drawing.putExtra("Width", width_int);
                drawing.putExtra("Height", height_int);
                drawing.putExtra("backgroundColour", colorPicker.getColor());

                startActivity(drawing);
            }
        });
    }
}
