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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class UploadPicture extends AppCompatActivity
{
    private Uri outputFileUri;
    private static final int SELECT_PICTURE = 1;
    private GreenlightProperties properties;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_picture);
        properties = new GreenlightProperties();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        System.out.println("activity result");
        if(resultCode == RESULT_OK)
        {
            if(requestCode == SELECT_PICTURE)
            {
                final boolean isCamera;
                if(data == null)
                {
                    isCamera = true;
                }
                else
                {
                    final String action = data.getAction();
                    if(action == null)
                    {
                        isCamera = false;
                    }
                    else
                    {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                if(!isCamera)
                {
                    //Write to picture.jpg.
                    try
                    {
                        ContentResolver resolver = getApplicationContext().getContentResolver();
                        InputStream originalFile = resolver.openInputStream(data.getData());
                        FileOutputStream stream = new FileOutputStream(properties.picture);
                        IOUtils.copy(originalFile, stream);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                try
                {
                    //upload the picture to the Green Light DB
                    Retrofit retrofit = new Retrofit.Builder()
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .baseUrl("https://greenlight-courses.herokuapp.com")
                            .build();
                    final HerokuService service = retrofit.create(HerokuService.class);
                    MultipartBody.Part uploadFile = MultipartBody.Part.createFormData(
                            "picture",
                            properties.picture.getName(),
                            RequestBody.create(MediaType.parse("image/*"), properties.picture)
                    );
                    Call<ResponseBody> call = service.setPicture(properties.reader.readLine(),
                            uploadFile);
                    call.enqueue(new Callback<ResponseBody>()
                    {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody>
                                response)
                        {
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
                        public void onFailure(Call<ResponseBody> call, Throwable t)
                        {
                            t.printStackTrace();
                        }
                    });
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                startActivity(properties.credentials);
            }
        }
    }

    public void openImageIntent(View v)
    {
        outputFileUri = Uri.fromFile(properties.picture);
        //Create camera intent.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam)
        {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        //Create document chooser.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/jpg");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        //Add the document options to the chooser.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        //Add the camera options to the chooser.
        chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()])
        );
        startActivityForResult(chooserIntent, SELECT_PICTURE);
    }
}
