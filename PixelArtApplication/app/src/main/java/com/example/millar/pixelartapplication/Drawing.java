package com.example.millar.pixelartapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import java.lang.Math;
import java.util.ArrayList;

public class Drawing extends AppCompatActivity {
    // Layouts
    ConstraintLayout mainLayout;
    ConstraintLayout Canvas;

    // Views
    SeekBar zoomBar;
    FloatingActionButton colourPickerButton;
    FloatingActionButton saveButton;
    FloatingActionButton toolsButton;
    FloatingActionButton colourSelectorButton;

    boolean toolsVisible;

    // Colour options
    int colour;

    // Drawing objects
    ArrayList drawQueue;
    Pixels pixels;

    // Drawing/panning variables
    long firstTouchTime;
    boolean secondFinger;
    float[] previousCoord;
    int previousDistance;
    int[] lastDrawn;
    boolean saved;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        // Initialize variables
        drawQueue = new ArrayList();
        colour = Color.RED;
        secondFinger = false;
        previousCoord = new float[2];
        lastDrawn = null;
        toolsVisible = false;

        // Find views
        mainLayout = findViewById(R.id.mainLayout);
        Canvas = findViewById(R.id.layout);
        zoomBar = findViewById(R.id.zoom);
        zoomBar.setMax(200);
        colourPickerButton = findViewById(R.id.colourPickerButton);
        saveButton = findViewById(R.id.save);
        toolsButton = findViewById(R.id.Tools);
        colourSelectorButton = findViewById(R.id.colourSelector);

        // Get passed parameters
        Bundle parameters = getIntent().getExtras();
        if(parameters.getBoolean("Loading")) {
            // Loading saved drawing
            saved = true;
            fileName = parameters.getString("FileName");
            pixels = new Pixels(this, fileName);
        } else {
            // Creating new drawing
            pixels = new Pixels(this, parameters.getInt("backgroundColour"), parameters.getInt("width"),
                    parameters.getInt("height"), parameters.getInt("size"));
            saved = false;
        }
        Canvas.addView(pixels);

        // pixels onTouchListener
        pixels.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // First finger down, unsure if user wants to pan or draw
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Take coordinates in case the user wants to pan
                    // This stops the canvas jumping
                    previousCoord[0] = motionEvent.getX();
                    previousCoord[1] = motionEvent.getY();

                    // Take time, after 50 milliseconds drawing will start
                    firstTouchTime = System.currentTimeMillis();
                }
                // A second finger has touched the screen
                else if(motionEvent.getPointerCount() > 1) {
                    secondFinger = true;

                    // Pan
                    if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        // Get finger coordinates
                        float x1 = motionEvent.getX(0);
                        float y1 = motionEvent.getY(0);
                        float x2 = motionEvent.getX(1);
                        float y2 = motionEvent.getY(1);


                        // *** PANNING ***
                        // Work out the distance fingers have moved
                        float xdiff = x1 - previousCoord[0];
                        float ydiff = y1 - previousCoord[1];
                        // Reduce distance to slow down and avoid stuttering
                        xdiff /= 1.5;
                        ydiff /= 1.5;

                        // Move the canvas
                        Canvas.setX(Canvas.getX() + xdiff);
                        Canvas.setY(Canvas.getY() + ydiff);

                        // Set previous coordinates
                        previousCoord[0] = motionEvent.getX();
                        previousCoord[1] = motionEvent.getY();

                        // *** ZOOMING ***
                        // Work out the distance between the fingers
                    }
                }
                // Last finger is taken off the screen
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // If there never was a second finger, the user wanted to place a single square
                    if(!secondFinger) {
                        Integer xcoord = Math.round(motionEvent.getX());
                        Integer ycoord = Math.round(motionEvent.getY());

                        int[] input = {xcoord, ycoord};
                        drawQueue.add(input);
                        changeColors(true);
                    }

                    // Make sure secondFinger is false
                    secondFinger = false;
                }
                // Only one finger on screen and moved
                else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    /*
                       Delay the time before drawing starts to a barely noticeable time
                       This way if both fingers touch the screen within 100 milliseconds of each other
                       panning will start without drawing
                    */
                    if(System.currentTimeMillis() - firstTouchTime > 100 && !secondFinger) {
                        // Get current finger coordinates
                        Integer xcoord = Math.round(motionEvent.getX());
                        Integer ycoord = Math.round(motionEvent.getY());

                        int[] input = {xcoord, ycoord};

                        /*
                            Get historical coordinates as well. These are hidden behind this function
                            for efficiency, we need them however because we need lots of finger locations.
                            It turns out that this is actually not enough, so there is an algorithm
                            later that solves this issue.
                         */

                        for (int i = 0; i < motionEvent.getHistorySize(); i++) {
                            int[] historicalPoint = {Math.round(motionEvent.getHistoricalX(i)), Math.round(motionEvent.getHistoricalY(i))};
                            // Add historical point into the drawQueue to be drawn
                            drawQueue.add(historicalPoint);
                        }

                        // Add current coordinates into the drawQueue to be drawn
                        drawQueue.add(input);
                        changeColors(false);
                    }
                }

                return true;
            }
        });


        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Divide by 100 for the decimal representation of percentage
                float scale = (float) i/100;
                // Add one so that the scale will increase
                scale += 1;
                // Set the canvas scale
                Canvas.setScaleX(scale);
                Canvas.setScaleY(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        colourPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create popup window
                LayoutInflater inflater = (LayoutInflater) Drawing.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View customView = inflater.inflate(R.layout.colour_picker_popup_window, null);
                final PopupWindow popupWindow = new PopupWindow(customView, 1000, 1500, true);

                // Find views
                final ColorPicker picker = customView.findViewById(R.id.picker);
                ImageButton close = customView.findViewById(R.id.close);
                Button submit = customView.findViewById(R.id.submit);
                SVBar svBar = customView.findViewById(R.id.SVBar);

                // Set up SVbar
                picker.addSVBar(svBar);

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Close popup window
                        popupWindow.dismiss();
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Set the colour
                        colour = picker.getColor();

                        // Change colour of the colour picker button
                        colourPickerButton.setBackgroundTintList(ColorStateList.valueOf(colour));
                    }
                });

                // Show popup window
                popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!saved) {
                    // If this is an unsaved drawing, prompt user for the name
                    LayoutInflater inflater = (LayoutInflater) Drawing.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View customView = inflater.inflate(R.layout.save_popup_window, null);
                    final PopupWindow popupWindow = new PopupWindow(customView, 1000, 1500, true);

                    final EditText fileNameEdit = customView.findViewById(R.id.popup_file_name);
                    final Button save = customView.findViewById(R.id.popup_save);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String fileNameText = fileNameEdit.getText().toString();
                            if (!fileNameText.equals("")) {
                                pixels.save(fileNameText);
                                fileName = fileNameText;
                                saved = true;
                                popupWindow.dismiss();
                            }
                        }
                    });

                    popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
                }
                else {
                    // Otherwise no need to get any information from user
                    pixels.save(fileName);
                }
            }
        });

        toolsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolsVisible = !toolsVisible;
                if(toolsVisible) {
                    colourSelectorButton.setVisibility(View.VISIBLE);
                } else {
                    colourSelectorButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void changeColors(boolean fingerUp) {
        // Loop through drawQueue
        for (int i = 0; i < drawQueue.size(); i++) {
            int[] coord = (int[]) drawQueue.get(i);
//            System.out.println("Coord: [" + coord[0] + ", " + coord[1] + "]"
//                    + "\nprevious: [" + previousCoord[0] + ", " + previousCoord[1] + "]");

            System.out.println(i);
            if(lastDrawn != null) {
                // Work out the distance between the current coordinate and the previous coordinate

                int xDist = coord[0] - lastDrawn[0];
                int yDist = coord[1] - lastDrawn[1];

                long distance = Math.round(Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2)));
//                System.out.println("Distance: " + distance + "\nX: " + xDist + "\nY: " + yDist +
//                "\nX^2: " + Math.pow(xDist, 2) + "\nY^2: " + Math.pow(yDist, 2));

                // If this distance is greater than the side of a pixel
                float size = pixels.getSize();
                if(distance > size) {
                    System.out.println("Test");
                    int multiplier = Math.round(distance/size);

                    float x = xDist/multiplier;
                    float y = yDist/multiplier;

                    for(int j=1; j < multiplier+1; j++) {
                        int[] newCoord = {Math.round(lastDrawn[0] + (j*x)), Math.round(lastDrawn[1] + (j*y))};
                        pixels.changePixelColor(newCoord[0], newCoord[1], colour);
                    }
                }
            }

            pixels.changePixelColor(coord[0], coord[1], colour);
            lastDrawn = (int[]) drawQueue.get(i);
        }

        if(fingerUp) {
            lastDrawn = null;
        }
        drawQueue.clear();
    }
}
