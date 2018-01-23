package com.greenlight.greenlightcollective;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "com.greenlight.greenlightcollective.ID";
    private Intent credentials, promptIntent;
    private File id, picture;
    private String idString, emailString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t2 = (TextView) findViewById(R.id.signUpView);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
        credentials = new Intent(getApplicationContext(), DisplayCredentials.class);
        promptIntent = new Intent(getApplicationContext(), UploadPicture.class);
        id = new File(getApplicationContext().getFilesDir(),"id.txt");
        picture = new File(getApplicationContext().getFilesDir(),"picture.jpg");
        if(picture.exists())
        {
            startActivity(credentials);
        }
    }

    private void persistDataToActivity(Intent intent)
    {
        //Write ID to file.
        try {
            FileOutputStream stream = new FileOutputStream(id);
            stream.write(idString.getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        startActivity(intent);
    }

    public void login(View view)
    {
        EditText greenlightId = (EditText) findViewById(R.id.idText);
        EditText greenlightEmail = (EditText) findViewById(R.id.emailText);
        idString = greenlightId.getText().toString();
        emailString = greenlightEmail.getText().toString();
        System.out.println("login with " + idString + " " + emailString);
        TextView error = (TextView) findViewById(R.id.errorView);
        checkLogin(idString,emailString,error);
    }

    private void checkLogin(final String memberID, String memberEmail, final TextView error)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://greenlight-courses.herokuapp.com")
                .build();
        final HerokuService service = retrofit.create(HerokuService.class);
        Call<ResponseBody> call = service.getResource(memberID,memberEmail,"picture");
        System.out.println("calling service");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
                try
                {
                    ResponseBody body = response.body();
                    String responseType = body.contentType().type();
                    String responseString = null;
                    if(responseType.equals("text"))
                    {
                        responseString = body.string();
                    }
                    //These are print statements for debugging.
                    System.out.println("call");
                    System.out.println(call.request().toString());
                    System.out.println("request");
                    System.out.println(response.raw().toString());
                    System.out.println(responseType);
                    System.out.println(body.contentLength());
                    if(responseType.equals("text") && responseString.length() == 0)
                    {
                        System.out.println("null response");
                        //Valid login but no picture, prompt user to upload picture.
                        error.setText("Looks like you don't have a picture yet. Let's upload one.");
                        persistDataToActivity(promptIntent);
                    }
                    else if(responseType.equals("text"))
                    {
                        System.out.println(responseString);
                        //This is an error, display it on the login screen.
                        error.setText(responseString);
                    }
                    else
                    {
                        System.out.println("picture exists");
                        error.setText("Success!");
                        //Save the picture locally.
                        FileOutputStream stream = new FileOutputStream(picture);
                        stream.write(body.bytes());
                        stream.close();
                        persistDataToActivity(credentials);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    //Also display on login screen.
                    error.setText(e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                t.printStackTrace();
            }
        });
    }
}
