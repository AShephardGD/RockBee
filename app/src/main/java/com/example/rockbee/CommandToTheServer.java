package com.example.rockbee;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommandToTheServer {
    @POST("/generateuuid")
    Call<String> generateUUID(@Body String deviceID);
    @POST("/checkuuid")
    Call<Boolean> checkUUID(@Body ArrayList<Object> params);
    @POST("/newroom")
    Call<Boolean> createNewRoom(@Body ArrayList<Object> params);//User creator, String pass
   @POST("/getusers")
    Call<Boolean> songEnded(@Body String UUID);
    @POST("/changepass")
    Call<Boolean> changePassword(@Body ArrayList<Object> params);// String UUID, String password
    @POST("/isroomalive")
    Call<Boolean> isRoomAlive(@Body String UUID);
    @POST("/userpass")
    Call<Boolean> userpass(@Body ArrayList<Object> params);
    @POST("/getinfo")
    Call<String> getInfo(@Body String UUID);
    @POST("/command")
    Call<Integer> command(@Body ArrayList<Object> params);
}
