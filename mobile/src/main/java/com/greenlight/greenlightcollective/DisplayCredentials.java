package com.greenlight.greenlightcollective;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import static com.greenlight.greenlightcollective.MainActivity.EXTRA_ID;

public class DisplayCredentials extends AppCompatActivity {

    public void logout(View view)
    {
        File picture = new File(getApplicationContext().getFilesDir(), "picture.jpg");
        picture.delete();
        Intent login = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_credentials);
        Intent intent = getIntent();
        File picture = new File(getApplicationContext().getFilesDir(), "picture.jpg");
        Uri uri = Uri.fromFile(picture);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(uri);
        TextView textView = (TextView) findViewById(R.id.memberID);
        textView.setText(intent.getStringExtra(EXTRA_ID));
    }
}
