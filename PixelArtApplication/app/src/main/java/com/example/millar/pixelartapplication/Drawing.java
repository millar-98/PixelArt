package com.example.millar.pixelartapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

    // Colour options
    int colour;

    // Drawing objects
    ArrayList drawQueue;
    Pixels pixels;

    // Drawing/panning variables
    long firstTouchTime;
    boolean secondFinger;
    float[] previousCoord;
    int[] lastDrawn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        // Initialize variables
        drawQueue = new ArrayList();
        colour = Color.RED;
        secondFinger = false;
        previousCoord = new float[2];
        lastDrawn = null;

        // Get passed parameters
        Bundle parameters = getIntent().getExtras();
        final int width = parameters.getInt("Width");
        final int height = parameters.getInt("Height");

        // Find views
        mainLayout = findViewById(R.id.mainLayout);
        Canvas = findViewById(R.id.layout);
        zoomBar = findViewById(R.id.zoom);
        zoomBar.setMax(200);
        colourPickerButton = findViewById(R.id.colourPickerButton);

        // Work out screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Create pixels
        pixels = new Pixels(this, width, height, size);
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
                        float x = motionEvent.getX(0);
                        float y = motionEvent.getY(0);

                        // Work out the distance fingers have moved
                        float xdiff = x - previousCoord[0];
                        float ydiff = y - previousCoord[1];
                        // Reduce distance to slow down and avoid stuttering
                        xdiff /= 1.5;
                        ydiff /= 1.5;

                        // Move the canvas
                        Canvas.setX(Canvas.getX() + xdiff);
                        Canvas.setY(Canvas.getY() + ydiff);

                        // Set previous coordinates
                        previousCoord[0] = motionEvent.getX();
                        previousCoord[1] = motionEvent.getY();
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
                final View customView = inflater.inflate(R.layout.popup_window, null);
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
    }

    public void changeColors(boolean fingerUp) {
//        ArrayList newQueue = (ArrayList) drawQueue.clone();

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
//                float a = Math.round(pixels.getSize()/2);
                float a = pixels.getSize();
                if(distance > a) {
                    System.out.println("Test");
                    int multiplier = Math.round(distance/a);

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

    public void takeTime() {
        firstTouchTime = System.currentTimeMillis();
    }
    public void toggleSecondFinger() {
        secondFinger = !secondFinger;
    }
}