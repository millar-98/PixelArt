package com.example.millar.pixelartapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by milla on 30/03/2018.
 */

public class Pixels extends View {
    private int width;
    private int height;
    private int size;
    private Bitmap bitmap;

    public Pixels(Context context, int w, int h, Point s) {
        super(context);

        width = w;
        height = h;
        size = s.x / width;

        bitmap = Bitmap.createBitmap(width*size, height*size, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.BLACK);
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

        // Stop out of range exceptions occuring
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


        if(bitmap.getPixel(xcoord*size, ycoord*size) != color) {
            for (int x = xcoord * size; x < xcoord * size + size; x++) {
                for (int y = ycoord * size; y < ycoord * size + size; y++) {
                    bitmap.setPixel(x, y, color);
                }
            }

            this.invalidate();
        }
    }

    public void changeColours(ArrayList queue, int colour) {
        for (int i = 0; i < queue.size(); i++) {
            int[] coord = (int[]) queue.get(i);
            changePixelColor(coord[0], coord[1], colour);
        }

        this.invalidate();
    }

    public int getSize() {
        return size;
    }
}
