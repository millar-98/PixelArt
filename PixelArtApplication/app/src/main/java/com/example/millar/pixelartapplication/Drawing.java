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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.Math;
import java.util.ArrayList;

public class Drawing extends AppCompatActivity {
    // Layouts
    ConstraintLayout mainLayout;
    ConstraintLayout Canvas;

    // Views
    FloatingActionButton colourPickerButton;
    FloatingActionButton saveButton;
    FloatingActionButton toolsButton;
    FloatingActionButton clearCanvasButton;
    FloatingActionButton showGridButton;
    FloatingActionButton fillToolButton;
    FloatingActionButton cancelEyeDropper;
    TextView clearCanvasText;

    boolean toolsVisible;

    // Drawing
    ArrayList drawQueue;
    Pixels pixels;
    int colour;

    // Tools
    boolean canDraw;
    boolean eyeDropperEnabled;
    boolean useOldColour;
    int oldColour;
    int newColour;
    boolean fillToolEnabled;
    int[] initialCoord_SLtool;
    EyeDropper eyeDropper; // Graphic to show what colour your finger is over

    long firstTouchTime;
    boolean secondFinger; // True when a second finger touches the screen
    float[] previousCoord_panning; // The previous finger coordinate, used for panning

    double previousDistance; // Saves the previous distance between two fingers for pinch zooming
    boolean scale; // True when a previous distance has been recorded

    // Records the last drawn pixel. Set to null once the finger is lifted up. This helps fix
    // A drawing issue where the changeColours algorithm wasn't filling in between two pixels
    int[] lastDrawn;

    // Saving
    boolean saved;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        // Initialize variables
        canDraw = true;
        eyeDropperEnabled = false;
        fillToolEnabled = false;
        initialCoord_SLtool = new int[2];
        drawQueue = new ArrayList();
        colour = Color.BLACK;
        secondFinger = false;
        previousCoord_panning = new float[2];
        lastDrawn = null;
        toolsVisible = false;
        scale = false;

        // Find views
        mainLayout = findViewById(R.id.mainLayout);
        Canvas = findViewById(R.id.layout);
        colourPickerButton = findViewById(R.id.colourPickerButton);
        saveButton = findViewById(R.id.save);
        toolsButton = findViewById(R.id.Tools);
        clearCanvasButton = findViewById(R.id.clearCanvas);
        clearCanvasText = findViewById(R.id.clearCanvasText);
        fillToolButton = findViewById(R.id.fillTool);
        showGridButton = findViewById(R.id.showGrid);
        cancelEyeDropper = findViewById(R.id.cancelEyeDropper);

        eyeDropper = new EyeDropper(this, colour);
        mainLayout.addView(eyeDropper);
        eyeDropper.setVisibility(View.INVISIBLE);

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
                if(canDraw) {
                    // First finger down, unsure if user wants to pan or draw
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        // Take coordinates in case the user wants to pan
                        // This stops the canvas jumping
                        previousCoord_panning[0] = motionEvent.getX();
                        previousCoord_panning[1] = motionEvent.getY();

                        // Take time, after 50 milliseconds drawing will start
                        firstTouchTime = System.currentTimeMillis();
                    }
                    // A second finger has touched the screen
                    else if (motionEvent.getPointerCount() > 1) {
                        secondFinger = true;

                        // Pan
                        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                            // Get finger coordinates
                            float x1 = motionEvent.getX(0);
                            float y1 = motionEvent.getY(0);
                            float x2 = motionEvent.getX(1);
                            float y2 = motionEvent.getY(1);


                            // *** PANNING ***
                            // Work out the distance fingers have moved
                            float xdiff = x1 - previousCoord_panning[0];
                            float ydiff = y1 - previousCoord_panning[1];
                            // Reduce distance to slow down and avoid stuttering
                            xdiff /= 1.5;
                            ydiff /= 1.5;

                            // Move the canvas
                            Canvas.setX(Canvas.getX() + xdiff);
                            Canvas.setY(Canvas.getY() + ydiff);

                            // Set previous coordinates
                            previousCoord_panning[0] = motionEvent.getX();
                            previousCoord_panning[1] = motionEvent.getY();

                            // *** ZOOMING ***
                            // Work out the distance between the fingers
                            double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                            if (scale) {
                                if(Canvas.getScaleX() >= 1) {
                                    double difference = distance - previousDistance;
                                    System.out.println(difference);
                                    double scaleChange = difference / 1000;
                                    System.out.println(scaleChange);
                                    Canvas.setScaleX(Canvas.getScaleX() + (float) scaleChange);
                                    Canvas.setScaleY(Canvas.getScaleY() + (float) scaleChange);
                                }

                                if(Canvas.getScaleX() < 1) {
                                    Canvas.setScaleX(1.0f);
                                    Canvas.setScaleY(1.0f);
                                }
                            } else {
                                scale = true;
                            }
                            previousDistance = distance;
                        }
                    }
                    // Last finger is taken off the screen
                    else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        // If there never was a second finger, the user wanted to place a single square
                        if (!secondFinger) {
                            Integer xcoord = Math.round(motionEvent.getX());
                            Integer ycoord = Math.round(motionEvent.getY());

                            int[] input = {xcoord, ycoord};
                            drawQueue.add(input);
                            changeColors(true);
                        }

                        // Make sure secondFinger is false
                        secondFinger = false;
                        scale = false;
                    }
                    // Only one finger on screen and moved
                    else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    /*
                       Delay the time before drawing starts to a barely noticeable time
                       This way if both fingers touch the screen within 100 milliseconds of each other
                       panning will start without drawing
                    */
                        if (System.currentTimeMillis() - firstTouchTime > 100 && !secondFinger) {
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
                }
                // Eyedropper tool selected
                else if(eyeDropperEnabled) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        // Show the eyedropper graphic
                        int[] screenCoordinates = {Math.round(motionEvent.getRawX()), Math.round(motionEvent.getRawY()) - 300};
                        int[] coordinates = {Math.round(motionEvent.getX()), Math.round(motionEvent.getY())};

                        oldColour = colour;
                        useOldColour = true;

                        newColour = pixels.getPixelColour(coordinates[0], coordinates[1], true);
                        eyeDropper.redraw(newColour, screenCoordinates);
                        eyeDropper.setVisibility(View.VISIBLE);
                        cancelEyeDropper.setVisibility(View.INVISIBLE);
                    }
                    else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        // Update the eyedropper graphic
                        int[] screenCoordinates = {Math.round(motionEvent.getRawX()), Math.round(motionEvent.getRawY()) - 300};
                        int[] coordinates = {Math.round(motionEvent.getX()), Math.round(motionEvent.getY())};
                        newColour = pixels.getPixelColour(coordinates[0], coordinates[1], true);
                        eyeDropper.redraw(newColour, screenCoordinates);
                    }
                    else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        // Update the eyedropper graphic and return back to drawing
                        int[] screenCoordinates = {Math.round(motionEvent.getRawX()), Math.round(motionEvent.getRawY()) - 300};
                        int[] coordinates = {Math.round(motionEvent.getX()), Math.round(motionEvent.getY())};

                        newColour = pixels.getPixelColour(coordinates[0], coordinates[1], true);
                        eyeDropper.redraw(newColour, screenCoordinates);
                        eyeDropper.setVisibility(View.INVISIBLE);

                        canDraw = true;
                        eyeDropperEnabled = false;
                        colourPickerButton.callOnClick();
                    }
                }
                else if(fillToolEnabled) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        int[] coordinates = {Math.round(motionEvent.getX()), Math.round(motionEvent.getY())};
                        fill(pixels.convertFingerToPixelCoords(coordinates[0], coordinates[1]), pixels.getPixelColour(coordinates[0], coordinates[1], true));

                        fillToolButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                        canDraw = true;
                        fillToolEnabled = false;
                    }
                }

                return true;
            }
        });


        colourPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fillToolEnabled) {
                    fillToolEnabled = false;
                    canDraw = true;
                    fillToolButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }

                // Create popup window
                LayoutInflater inflater = (LayoutInflater) Drawing.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View customView = inflater.inflate(R.layout.colour_picker_popup_window, null);
                final PopupWindow popupWindow = new PopupWindow(customView, 1000, 1500, true);

                // Find views
                final ColorPicker picker = customView.findViewById(R.id.picker);
                FloatingActionButton submit = customView.findViewById(R.id.submit);
                FloatingActionButton eyeDropper = customView.findViewById(R.id.eyeDropper);
                SVBar svBar = customView.findViewById(R.id.SVBar);

                // Set up SVbar
                picker.addSVBar(svBar);

                if(useOldColour) {
                    picker.setOldCenterColor(oldColour);
                    picker.setNewCenterColor(newColour);
                    picker.setColor(newColour);
                    useOldColour = false;
                } else {
                    picker.setOldCenterColor(colour);
                    picker.setNewCenterColor(colour);
                    picker.setColor(colour);
                }

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Set the colour
                        colour = picker.getColor();
                        popupWindow.dismiss();
                    }
                });

                eyeDropper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        canDraw = false;
                        eyeDropperEnabled = true;
                        popupWindow.dismiss();

                        cancelEyeDropper.setVisibility(View.VISIBLE);
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
                    clearCanvasButton.setVisibility(View.VISIBLE);
                    clearCanvasText.setVisibility(View.VISIBLE);
                    fillToolButton.setVisibility(View.VISIBLE);
                    showGridButton.setVisibility(View.VISIBLE);

                    toolsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                } else {
                    clearCanvasButton.setVisibility(View.INVISIBLE);
                    clearCanvasText.setVisibility(View.INVISIBLE);
                    fillToolButton.setVisibility(View.INVISIBLE);
                    showGridButton.setVisibility(View.INVISIBLE);

                    toolsButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }
            }
        });

        clearCanvasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pixels.clearCanvas();
            }
        });

        fillToolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillToolEnabled = !fillToolEnabled;
                canDraw = !canDraw;
                if(fillToolEnabled) {
                    fillToolButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                } else {
                    fillToolButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }
            }
        });

        showGridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pixels.showGrid) {
                    pixels.removeGrid();
                    showGridButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                } else {
                    pixels.drawGrid();
                    showGridButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                }
            }
        });

        cancelEyeDropper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canDraw = true;
                eyeDropperEnabled = false;

                cancelEyeDropper.setVisibility(View.INVISIBLE);
                colourPickerButton.callOnClick();
            }
        });
    }

    public void changeColors(boolean fingerUp) {
        // Loop through drawQueue
        for (int i = 0; i < drawQueue.size(); i++) {
            int[] coord = (int[]) drawQueue.get(i);

            if(lastDrawn != null) {
                // Work out the distance between the current coordinate and the previous coordinate

                int xDist = coord[0] - lastDrawn[0];
                int yDist = coord[1] - lastDrawn[1];

                long distance = Math.round(Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2)));

                // If this distance is greater than the side of a pixel
                float size = pixels.getSize();
                if(distance > size) {
                    int multiplier = Math.round(distance/size);

                    float x = xDist/multiplier;
                    float y = yDist/multiplier;

                    for(int j=1; j < multiplier+1; j++) {

                        int[] newCoord = {Math.round(lastDrawn[0] + (j*x)), Math.round(lastDrawn[1] + (j*y))};
                        pixels.changePixelColor(newCoord[0], newCoord[1], colour, true);
                    }
                }
            }

            pixels.changePixelColor(coord[0], coord[1], colour, true);
            lastDrawn = coord;
        }

        if(fingerUp) {
            lastDrawn = null;
        }
        drawQueue.clear();
    }

    // Fill tool method
    public void fill(int[] coordinates, int originalColour) {
        int x = coordinates[0];
        int y = coordinates[1];
        // If the pixels colour is the same as the original colour change the pixels colour to the new
        // colour and recursively call adjacent pixels.
        if(pixels.getPixelColour(x, y, false) == originalColour) {
            pixels.changePixelColor(x, y, colour, false);

            int[][] surrounding = {{x+1, y}, {x-1, y}, {x, y+1}, {x, y-1}};
            for(int i = 0; i < surrounding.length; i++) {
                // If the adjacent pixel is in bounds recursively call with that pixel
                if(surrounding[i][0] < pixels.getPixelsWidth() && surrounding[i][1] < pixels.getPixelsHeight()
                        && surrounding[i][0] >= 0 && surrounding[i][1] >= 0) {
                    fill(surrounding[i], originalColour);
                }
            }
        }
    }
}
