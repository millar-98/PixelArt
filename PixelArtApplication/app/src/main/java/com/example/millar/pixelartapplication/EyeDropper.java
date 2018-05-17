package com.example.millar.pixelartapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by milla on 17/05/2018.
 */

public class EyeDropper extends View {
    private Paint borderPaint;
    private Paint paint;
    private int[] coordinates;

    public EyeDropper(Context context, int colour) {
        super(context);

        borderPaint = new Paint();
        borderPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));

        paint = new Paint();
        paint.setColor(colour);

        coordinates = new int[2];
        coordinates[0] = 200;
        coordinates[1] = 200;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(coordinates[0], coordinates[1], 130, borderPaint);
        canvas.drawCircle(coordinates[0], coordinates[1], 120, paint);
    }

    @Override
    public boolean performClick() {
        return true;
    }

    public void redraw(int colour, int[] newCoordinates) {
        paint.setColor(colour);
        coordinates = newCoordinates;
        this.invalidate();
    }
}
