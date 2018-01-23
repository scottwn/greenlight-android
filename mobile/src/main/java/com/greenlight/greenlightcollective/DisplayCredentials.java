package com.greenlight.greenlightcollective;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class DisplayCredentials extends AppCompatActivity
{
    private GreenlightProperties properties;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_credentials);
        properties = new GreenlightProperties();
        Uri uri = Uri.fromFile(properties.picture);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(uri);
        TextView textView = (TextView) findViewById(R.id.memberID);
        try
        {
            textView.setText(properties.reader.readLine());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void logout(View view)
    {
        properties.picture.delete();
        Intent login = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(login);
    }
}
