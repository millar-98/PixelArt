package com.example.millar.pixelartapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 * Created by milla on 30/03/2018.
 */

public class Pixels extends View {
    private Context context;
    private int backgroundColour;
    private int pixelsWidth;
    private int pixelsHeight;
    private int size;
    private int shape; // 0 - Square, 1 - smaller square
    private Bitmap drawingBitmap;
    public boolean showGrid;

    public Pixels(Context c, int bc, int w, int h, int s) {
        // New drawing
        super(c);
        context = c;

        backgroundColour = bc;
        pixelsWidth = w;
        pixelsHeight = h;
        size = s;
        showGrid = false;
        shape = 1;

        // Initialize the bitmap
        drawingBitmap = Bitmap.createBitmap(pixelsWidth *size, pixelsHeight *size, Bitmap.Config.ARGB_8888);
        drawingBitmap.eraseColor(backgroundColour);
    }

    public Pixels(Context c, String fileName) {
        // Load saved drawing
        super(c);
        context = c;
        showGrid = false;

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

    public int[] convertFingerToPixelCoords(int xcoord, int ycoord) {
        // This will find the positions in the top left corner of the pixel
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

        if(getPixelColour(xcoord, ycoord, false) != color) {
            if (shape == 0) {
                int xCount = 0;
                for (int x = xcoord * size; x < xcoord * size + size; x++) {
                    int yCount = 0;
                    for (int y = ycoord * size; y < ycoord * size + size; y++) {
                        if (showGrid) {
//                        if(xCount != 0 && xCount != size-1 && yCount != 0 && yCount != size-1) {
                            if (xCount != 0 && yCount != 0) {
                                drawingBitmap.setPixel(x, y, color);
                            }
                        } else {
                            drawingBitmap.setPixel(x, y, color);
                        }
                        yCount++;
                    }
                    xCount++;
                }
            } else if(shape == 1) {
                int edge = (int) Math.round(size*0.05);

                for (int x = xcoord * size + edge; x < xcoord * size + size - edge; x++) {
                    for (int y = ycoord * size + edge; y < ycoord * size + size - edge; y++) {
                        drawingBitmap.setPixel(x, y, color);
                    }
                }
            } else if(shape == 2) {
            }

            // Redraw the screen
            this.invalidate();
        }
    }

    public int getPixelColour(int xcoord, int ycoord, boolean fingerCoords) {
        if(fingerCoords) {
            int[] pixelCoords = convertFingerToPixelCoords(xcoord, ycoord);
            xcoord = pixelCoords[0];
            ycoord = pixelCoords[1];
        }

        return drawingBitmap.getPixel(xcoord*size + 2, ycoord*size + 2);
    }

    public void clearCanvas() {
        drawingBitmap.eraseColor(backgroundColour);
        this.invalidate();
    }

    private void backgroundColourChanged(int oldBackgroundColour) {
        for(int x = 0; x < pixelsWidth; x++) {
            for(int y = 0; y < pixelsHeight; y++) {
                if(getPixelColour(x, y, false) == oldBackgroundColour) {
                    // Ignore shape for pixels that are the background colour
                    for(int i = x * size; i < x * size + size; i++) {
                        for(int j = y * size; j < y * size + size; j++) {
                            drawingBitmap.setPixel(i, j, backgroundColour);
                        }
                    }
                } else {
                    if(shape == 1) {
                        int edge = (int) Math.round(size*0.05);

                        for(int i = x * size; i < x * size + edge; i++) {
                            for(int j = y * size; j < y * size + size; j++) {
                                drawingBitmap.setPixel(i, j, backgroundColour);
                            }
                        }

                        for(int i = x * size + size - 1; i > x * size + size - edge - 1; i--) {
                            for(int j = y * size; j < y * size + size; j++) {
                                drawingBitmap.setPixel(i, j, backgroundColour);
                            }
                        }

                        for(int i = y * size; i < y * size + edge; i++) {
                            for(int j = x * size; j < x * size + size; j++) {
                                drawingBitmap.setPixel(j, i, backgroundColour);
                            }
                        }

                        for(int i = y * size + size - 1; i > y * size + size - edge - 1; i--) {
                            for(int j = x * size; j < x * size + size; j++) {
                                drawingBitmap.setPixel(j, i, backgroundColour);
                            }
                        }
                    }
                }
            }
        }
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
            textFileWriter.write(Integer.toString(pixelsWidth));
            textFileWriter.write(",");
            textFileWriter.write(Integer.toString(pixelsHeight));
            textFileWriter.write(",");
            textFileWriter.write(Integer.toString(size));
            textFileWriter.write(",");
            textFileWriter.write(Integer.toString(shape));

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
}
