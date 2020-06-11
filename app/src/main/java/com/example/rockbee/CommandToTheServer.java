package com.example.rockbee;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CommandToTheServer {
    @POST("/generateuuid")
    Call<String> generateUUID(@Body String deviceID);
    @POST("/checkuuid")
    Call<Boolean> checkUUID(@Body ArrayList<Object> params);
    @POST("/newroom")
    Call<Boolean> createNewRoom(@Body ArrayList<Object> params);//User creator, String pass
    @POST("/checkingsong")
    Call<Boolean> checkingSongInTheRoom(@Body ArrayList<Object> params);
    @POST("/checkingroom")
    Call<Boolean> checkingRoom(@Body ArrayList<Object> params);
}
