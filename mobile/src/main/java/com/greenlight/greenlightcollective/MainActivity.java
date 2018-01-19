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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t2 = (TextView) findViewById(R.id.signUpView);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void login(View view)
    {
        EditText greenlightId = (EditText) findViewById(R.id.idText);
        EditText greenlightEmail = (EditText) findViewById(R.id.emailText);
        String idString = greenlightId.getText().toString();
        String emailString = greenlightEmail.getText().toString();
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
                    System.out.println("call");
                    System.out.println(call.request().toString());
                    System.out.println("request");
                    System.out.println(response.raw().toString());
                    System.out.println(responseType);
                    System.out.println(body.contentLength());
                    if(responseType.equals("text") && responseString.length() == 0)
                    {
                        System.out.println("null response");
                        //valid login but no picture, prompt user to upload picture
                        error.setText("Looks like you don't have a picture yet. Let's upload one.");
                        Intent promptIntent = new Intent(getApplicationContext(),
                                UploadPicture.class);
                        promptIntent.putExtra(EXTRA_ID, memberID);
                        startActivity(promptIntent);
                    }
                    else if(responseType.equals("text"))
                    {
                        System.out.println(responseString);
                        //this is an error, display it on the login screen
                        error.setText(responseString);
                    }
                    else
                    {
                        System.out.println("picture exists");
                        //picture exists, show credentials
                        error.setText("Success!");
                        File picture = new File(getApplicationContext().getFilesDir(),
                                "picture.jpg");
                        FileOutputStream stream = new FileOutputStream(picture);
                        stream.write(body.bytes());
                        stream.close();
                        Intent credentials = new Intent(getApplicationContext(),
                                DisplayCredentials.class);
                        credentials.putExtra(EXTRA_ID,memberID);
                        startActivity(credentials);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    //also display on login screen
                    error.setText(e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                t.printStackTrace();
                //also display on login screen
            }
        });
    }
}
