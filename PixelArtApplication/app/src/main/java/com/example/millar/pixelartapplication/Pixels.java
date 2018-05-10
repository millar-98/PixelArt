package com.example.millar.pixelartapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by milla on 30/03/2018.
 */

public class Pixels extends View {
    private Context context;
    private int width;
    private int height;
    private int size;
    private Bitmap bitmap;
    private int[][] pixels;

    public Pixels(Context c, int w, int h, Point s, int backgroundColour) {
        super(c);
        context = c;

        width = w;
        height = h;
        size = s.x / width;

        bitmap = Bitmap.createBitmap(width*size, height*size, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(backgroundColour);

        pixels = new int[width][height]; // Access pixels[x][y]
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                pixels[x][y] = backgroundColour;
            }
        }
    }

    public Pixels(Context c, String fileName, Point s) {
        super(c);
        context = c;

        load(fileName);

        width = pixels.length;
        height = pixels[0].length;
        size = s.x / width;

        bitmap = Bitmap.createBitmap(width*size, height*size, Bitmap.Config.ARGB_8888);
        loadDrawing();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean performClick() {
        return true;
    }

    public void changePixelColor(int xcoord, int ycoord, int color) {
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

        pixels[xcoord][ycoord] = color;

        if(bitmap.getPixel(xcoord*size, ycoord*size) != color) {
            for (int x = xcoord * size; x < xcoord * size + size; x++) {
                for (int y = ycoord * size; y < ycoord * size + size; y++) {
                    bitmap.setPixel(x, y, color);
                }
            }

            this.invalidate();
        }
    }

    public void loadDrawing() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(bitmap.getPixel(x*size, y*size) != pixels[x][y]) {
                    for (int xPixels = x * size; xPixels < x * size + size; xPixels++) {
                        for (int yPixels = y * size; yPixels < y * size + size; yPixels++) {
                            bitmap.setPixel(xPixels, yPixels, pixels[x][y]);
                        }
                    }

                    this.invalidate();
                }
            }
        }

        this.invalidate();
    }

    public void save(String fileName) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            StringBuffer data = new StringBuffer();

            for(int x = 0; x < pixels.length; x++) {
                for(int y = 0; y < pixels[x].length; y++) {
                    data.append(pixels[x][y]);
                    data.append((y == pixels[x].length-1) ? "": ",");
                }
                data.append((x == pixels.length-1) ? "": "\n");
            }

            outputStream.write(data.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load(String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            ArrayList lines = new ArrayList();
            int count = 0;
            lines.add(reader.readLine());
            while(lines.get(count) != null) {
                lines.add(reader.readLine());
                count++;
            }

            pixels = new int[count][];
            for(int i = 0; i < count; i++) {
                String currentLine = (String) lines.get(i);
                String[] values = currentLine.split(",");

                pixels[i] = new int[values.length];
                for (int j = 0; j < values.length; j++) {
                    pixels[i][j] = Integer.parseInt(values[j]);
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
