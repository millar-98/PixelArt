package com.example.millar.pixelartapplication;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by milla on 30/03/2018.
 */

public class Pixels extends View {
    private Context context;
    private int backgroundColour;
    private int width;
    private int height;
    private int size;
    private Bitmap drawingBitmap;

    public Pixels(Context c, int bc, int w, int h, int s) {
        // New drawing
        super(c);
        context = c;

        backgroundColour = bc;
        width = w;
        height = h;
        size = s;

        // Initialize the bitmap
        drawingBitmap = Bitmap.createBitmap(width*size, height*size, Bitmap.Config.ARGB_8888);
        drawingBitmap.eraseColor(backgroundColour);
    }

    public Pixels(Context c, String fileName) {
        // Load saved drawing
        super(c);
        context = c;
        load(fileName);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(drawingBitmap, 0, 0, null);
    }

    @Override
    public boolean performClick() {
        return true;
    }

    public void changePixelColor(int xcoord, int ycoord, int color) {
        // This will find the positions in the top left corner of the pixel
        xcoord /= size;
        ycoord /= size;

        // Stop out of range exceptions occurring
        if(xcoord >= width) {
            xcoord = width-1;
        }
        else if(xcoord < 0) {
            xcoord = 0;
        }
        if(ycoord >= height) {
            ycoord = height-1;
        }
        else if(ycoord < 0) {
            ycoord = 0;
        }

        if(drawingBitmap.getPixel(xcoord*size, ycoord*size) != color) {
            for (int x = xcoord * size; x < xcoord * size + size; x++) {
                for (int y = ycoord * size; y < ycoord * size + size; y++) {
                    drawingBitmap.setPixel(x, y, color);
                }
            }

            // Redraw the screen
            this.invalidate();
        }
    }

    public int getPixelColor(int xcoord, int ycoord) {
        int colour;

        // This will find the positions in the top left corner of the pixel
        xcoord /= size;
        ycoord /= size;

        // Stop out of range exceptions occurring
        if(xcoord >= width) {
            xcoord = width-1;
        }
        else if(xcoord < 0) {
            xcoord = 0;
        }
        if(ycoord >= height) {
            ycoord = height-1;
        }
        else if(ycoord < 0) {
            ycoord = 0;
        }

        colour = drawingBitmap.getPixel(xcoord*size, ycoord*size);

        System.out.println("Colour: " + colour + "\nX: " + xcoord + "\nY: " + ycoord);
        return colour;
    }

    public void save(String fileName) {
        try {
            // Write to the txt file
            File directory = new File(context.getFilesDir(), "Drawings");
            if(!directory.exists()) {
                directory.mkdirs();
            }
            File textFile = new File(directory, fileName + ".txt");
            FileWriter textFileWriter = new FileWriter(textFile);

            textFileWriter.append(Integer.toString(backgroundColour));
            textFileWriter.write(",");
            textFileWriter.write(Integer.toString(width));
            textFileWriter.write(",");
            textFileWriter.write(Integer.toString(height));
            textFileWriter.write(",");
            textFileWriter.write(Integer.toString(size));

            textFileWriter.flush();
            textFileWriter.close();

            // Write to the png file
            File pngFile = new File(directory, fileName + ".png");
            pngFile.createNewFile();
            FileOutputStream pngFileWriter = new FileOutputStream(pngFile);
            drawingBitmap.compress(Bitmap.CompressFormat.PNG, 100, pngFileWriter);

            pngFileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load(String fileName) {
    try {
        // Initialize bitmap
        System.out.println(fileName);
        File f = new File(context.getFilesDir(),"/Drawings/" + fileName + ".png");
        drawingBitmap = BitmapFactory.decodeStream(new FileInputStream(f)).copy(Bitmap.Config.ARGB_8888, true);

        // Get variables
        File textFile = new File(context.getFilesDir() + "/Drawings/" + fileName + ".txt");
        FileInputStream inputStream = new FileInputStream(textFile);
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line = bufferedReader.readLine();
        String[] lineValues = line.split(",");
        for(int i = 0; i < lineValues.length; i++) {
            switch (i) {
                case 0:
                    backgroundColour = Integer.parseInt(lineValues[i]);
                    break;
                case 1:
                    width = Integer.parseInt(lineValues[i]);
                    break;
                case 2:
                    height = Integer.parseInt(lineValues[i]);
                    break;
                case 3:
                    size = Integer.parseInt(lineValues[i]);
                    break;
            }
        }
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
    }

    public int getSize() {
        return size;
    }
}
