package com.greenlight.greenlightcollective;

import android.app.Application;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by scottneagle on 1/23/18.
 */

class GreenlightProperties extends Application
{
    public Intent credentials, promptIntent;
    public File id, picture;
    public BufferedReader reader;

    public GreenlightProperties()
    {
        credentials = new Intent(getApplicationContext(), DisplayCredentials.class);
        promptIntent = new Intent(getApplicationContext(), UploadPicture.class);
        id = new File(getApplicationContext().getFilesDir(), "id.txt");
        picture = new File(getApplicationContext().getFilesDir(), "picture.jpg");
        try
        {
            reader = new BufferedReader(new FileReader(id));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
