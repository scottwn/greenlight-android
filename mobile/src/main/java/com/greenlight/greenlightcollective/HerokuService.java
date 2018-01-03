package com.greenlight.greenlightcollective;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HerokuService {
    @GET("/resources")
    Call<ResponseBody> getPicture(@Query("id") String memberID, @Query("email") String memberEmail);

    @POST("/resources")
    Call<ResponseBody> setPicture(@Field("id") String memberID, @Field("picture") File pictureFile);
}
