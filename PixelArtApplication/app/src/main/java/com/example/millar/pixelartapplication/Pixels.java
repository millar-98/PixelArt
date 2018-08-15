package com.example.millar.pixelartapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Pixels extends View {
    private Context context;
    private int backgroundColour;
    private int size;
    private int shape; // 0 - Square, 1 - smaller square
    Paint tempColour;

    private int pixelsWidth;
    private int pixelsHeight;
    private int[][] colours;

    // Create new drawing
    public Pixels(Context c, int bc, int w, int h, int s, int sh) {
        super(c);

        context = c;
        backgroundColour = bc;
        pixelsWidth = w;
        pixelsHeight = h;
        size = s;
        shape = sh;

        colours = new int[w][h];
        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                colours[i][j] = backgroundColour;
            }
        }

        tempColour = new Paint();
    }

    // Load drawing
    public Pixels(Context c, String fileName) {
        // Load saved drawing
        super(c);
        context = c;

        tempColour = new Paint();

        load(fileName);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw each pixel
        for(int x = 0; x < pixelsWidth; x++) {
            for(int y = 0; y < pixelsHeight; y++) {
                tempColour.setColor(colours[x][y]); // Set the colour
                int edge = (int) Math.round(size*0.1);
                // Different possible shapes of pixels
                if(shape == 0) {
                    canvas.drawRect(x*size + edge, y*size + edge, x*size + size - edge, y*size + size - edge, tempColour);
                } else if(shape == 1) {
                    canvas.drawCircle(x * size + edge + size/2, y * size + edge + size/2, size/2 - 2*edge, tempColour);
                } else if(shape == 2) {
                    tempColour.setStrokeWidth(edge);
                    canvas.drawLine(x * size, y * size, x * size + size, y * size + size, tempColour);
                } else if(shape == 3) {
                    tempColour.setStrokeWidth(edge);
                    canvas.drawLine(x * size, y * size, x * size + size, y * size + size, tempColour);
                    canvas.drawLine(x * size + size, y * size, x * size, y * size + size, tempColour);
                }
            }
        }
    }

    @Override
    public boolean performClick() {
        return true;
    }

    public int[] convertFingerToPixelCoords(int xcoord, int ycoord) {
        // Divide by size to find index of pixel in colours
        xcoord /= size;
        ycoord /= size;

        // Stop out of range exceptions occurring
        if(xcoord >= pixelsWidth) {
            xcoord = pixelsWidth -1;
        }
        else if(xcoord < 0) {
            xcoord = 0;
        }
        if(ycoord >= pixelsHeight) {
            ycoord = pixelsHeight -1;
        }
        else if(ycoord < 0) {
            ycoord = 0;
        }

        int[] pixelCoords = {xcoord, ycoord};

        return pixelCoords;
    }

    public void changePixelColor(int xcoord, int ycoord, int color, boolean fingerCoords) {
        if(fingerCoords) {
            int[] pixelCoords = convertFingerToPixelCoords(xcoord, ycoord);
            xcoord = pixelCoords[0];
            ycoord = pixelCoords[1];
        }

        colours[xcoord][ycoord] = color;

        this.invalidate();
    }

    public int getPixelColour(int xcoord, int ycoord, boolean fingerCoords) {
        if(fingerCoords) {
            int[] pixelCoords = convertFingerToPixelCoords(xcoord, ycoord);
            xcoord = pixelCoords[0];
            ycoord = pixelCoords[1];
        }

        return colours[xcoord][ycoord];
    }

    private void backgroundColourChanged(int oldBackgroundColour) {
        // Change each pixel with the old background colour to the new background colour
        for(int x = 0; x < pixelsWidth; x++) {
            for(int y = 0; y < pixelsHeight; y++) {
                if(colours[x][y] == oldBackgroundColour) {
                    colours[x][y] = backgroundColour;
                }
            }
        }

        invalidate();
    }

    public void clearCanvas() {
        // Set every pixel to the background colour
        for(int x = 0; x < pixelsWidth; x++) {
            for(int y = 0; y < pixelsHeight; y++) {
                colours[x][y] = backgroundColour;
            }
        }
        this.invalidate();
    }

    public int getSize() {
        return size;
    }
    public int getPixelsWidth() {
        return pixelsWidth;
    }
    public int getPixelsHeight() {
        return pixelsHeight;
    }
    public int getBackgroundColour() {
        return backgroundColour;
    }
    public void setBackgroundColour(int colour) {
        int oldBackgroundColour = backgroundColour;
        backgroundColour = colour;
        backgroundColourChanged(oldBackgroundColour);
    }
    public void setShape(int newShape) {
        shape = newShape;
        invalidate();
    }

    public void save(String fileName) {
        try {
            // Save a text file containing all the information needed to redraw the drawing
            File directory = new File(context.getFilesDir(), "Drawings");
            if(!directory.exists()) {
                directory.mkdirs();
            }
            File textFile = new File(directory, fileName + ".txt");
            FileWriter textFileWriter = new FileWriter(textFile);
            String text = "";

            text += Integer.toString(backgroundColour);
            text += "/";
            text += Integer.toString(pixelsWidth);
            text += "/";
            text += Integer.toString(pixelsHeight);
            text += "/";
            text += Integer.toString(size);
            text += "/";
            text += Integer.toString(shape);
            text += "/";

            for(int x = 0; x < pixelsWidth; x++) {
                for(int y = 0; y < pixelsHeight; y++) {
                    text += Integer.toString(colours[x][y]);
                    if(y != pixelsHeight-1) text += ",";
                }
                if(x != pixelsWidth-1) text += ";";
            }

            textFileWriter.write(text);
            textFileWriter.flush();
            textFileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load(String fileName) {
        try {
            File textFile = new File(context.getFilesDir() + "/Drawings/" + fileName + ".txt");
            FileInputStream inputStream = new FileInputStream(textFile);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String temp = "";

            String line = bufferedReader.readLine();
            String[] lineValues = line.split("/");
            for(int i = 0; i < lineValues.length; i++) {
                switch (i) {
                    case 0:
                        backgroundColour = Integer.parseInt(lineValues[i]);
                        break;
                    case 1:
                        pixelsWidth = Integer.parseInt(lineValues[i]);
                        break;
                    case 2:
                        pixelsHeight = Integer.parseInt(lineValues[i]);
                        break;
                    case 3:
                        size = Integer.parseInt(lineValues[i]);
                        break;
                    case 4:
                        shape = Integer.parseInt(lineValues[i]);
                        break;
                    case 5:
                        temp = lineValues[i];
                        break;
                }
            }

            colours = new int[pixelsWidth][pixelsHeight];

            String[] lines = temp.split(";");
            for(int i = 0; i < lines.length; i++) {
                String[] values = lines[i].split(",");
                for(int j = 0; j < values.length; j++) {
                    System.out.println(values[j]);
                    colours[i][j] = Integer.parseInt(values[j]);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
