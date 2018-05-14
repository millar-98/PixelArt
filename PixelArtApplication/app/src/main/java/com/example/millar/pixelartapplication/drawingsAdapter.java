package com.example.millar.pixelartapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import java.io.File;

/**
 * Created by milla on 11/05/2018.
 */

public class drawingsAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] files;

    public drawingsAdapter(Context c, String[] f) {
        super(c, 0, f);

        context = c;
        files = f;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;

        if(listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
        }

        final String fileName = files[position];

        // Drawing button
        Button drawing = listItem.findViewById(R.id.item);
        drawing.setText(fileName);
        drawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drawingIntent = new Intent(view.getContext(), Drawing.class);

                // Tell Drawing that it is loading a drawing and give it the file name
                drawingIntent.putExtra("Loading", true);
                drawingIntent.putExtra("FileName", fileName);

                context.startActivity(drawingIntent);
            }
        });

        ImageButton deleteButton = listItem.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dir = context.getFilesDir().getAbsolutePath() + "/Drawings/";

                // Delete both the txt and png files
                File textFile = new File(dir, fileName + ".txt");
                File pngFile = new File(dir, fileName + ".png");
                boolean textFileDeleted = textFile.delete();
                boolean pngFileDeleted = pngFile.delete();

                // Handle the event of a deletion failing
                if(!textFileDeleted || !pngFileDeleted) {
                    System.out.println("Delete was unsuccessful");
                } else {
                    Activity activity = (Activity) context;
                    activity.recreate();
                }
            }
        });

        return listItem;
    }
}
