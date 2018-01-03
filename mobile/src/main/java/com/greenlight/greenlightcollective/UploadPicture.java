package com.greenlight.greenlightcollective;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.greenlight.greenlightcollective.MainActivity.EXTRA_ID;


public class UploadPicture extends AppCompatActivity {

    private Uri outputFileUri;
    private String memberID;

    private static final int SELECT_PICTURE = 1;

    public void openImageIntent(View v)
    {
        // Determine Uri of camera image to save.
        File picture = new File(getApplicationContext().getFilesDir(), "picture.jpg");
        outputFileUri = Uri.fromFile(picture);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/jpg");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, SELECT_PICTURE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File outputFile = new File(outputFileUri.getPath());

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if(!isCamera)
                {
                    //write to picture.jpg
                    //File originalFile = new File(data.getData().getPath());
                    try
                    {
                        ContentResolver resolver = getApplicationContext().getContentResolver();
                        InputStream originalFile  = resolver.openInputStream(data.getData());
                        FileOutputStream stream = new FileOutputStream(outputFile);
                        IOUtils.copy(originalFile,stream);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                //upload the picture to the Green Light DB
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://greenlight-courses.herokuapp.com")
                        .build();
                final HerokuService service = retrofit.create(HerokuService.class);
                Call<ResponseBody> call = service.setPicture(memberID,outputFile);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try
                        {
                            System.out.println(response.body().string());
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

                //start the credentials activity
                Intent credentials = new Intent(getApplicationContext(),
                        DisplayCredentials.class);
                credentials.putExtra(EXTRA_ID,memberID);
                startActivity(credentials);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_picture);
        Intent intent = getIntent();
        memberID = intent.getStringExtra(EXTRA_ID);
    }
    /*
    private void getPicture()
    {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/jpeg");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String pickTitle = "You can choose a picture from your phone or take a new picture";
        Intent chooserIntent = Intent.createChooser(pickIntent,pickTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {takePhotoIntent});
        startActivityForResult(chooserIntent,SELECT_PICTURE);
    }
    */
}
