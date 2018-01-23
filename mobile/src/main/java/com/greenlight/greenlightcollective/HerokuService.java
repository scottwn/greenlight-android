package com.greenlight.greenlightcollective;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface HerokuService
{
    @GET("/resources")
    Call<ResponseBody> getResource(
            @Query("id") String memberID,
            @Query("email") String memberEmail,
            @Query("resource-type") String type
    );

    @Multipart
    @POST("/resources")
    Call<ResponseBody> setPicture(@Part("id") String memberID, @Part MultipartBody.Part
            pictureFile);
}
