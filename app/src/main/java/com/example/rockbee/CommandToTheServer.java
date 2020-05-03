package com.example.rockbee;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CommandToTheServer {
    @POST("/generateuuid")
    Call<String> generateUUID();
    @POST("/checkuuid")
    Call<Boolean> checkUUID(@Body String UUID);
    @POST("/newroom")
    Call<Boolean> createNewRoom(@Body ArrayList<Object> params);//User creator, String pass
    @POST("/connecttotheroom")
    Call<Integer> connectToTheRoom(@Body ArrayList<Object> params);//String UUID, User user, String pass
    @POST("/addsong")
    Call<Boolean> addToTheQueue(@Body ArrayList<Object> params);//String UUID, File song
    @POST("/disconnect")
    Call<Boolean> disconnect(@Body ArrayList<Object> params);// String UUID, User user
    @POST("/getusers")
    Call<ArrayList<User>> getUsers(@Body String UUID);
    @POST("/getsongs")
    Call<ArrayList<File>> getSongs(@Body String UUID);
    @POST("/songended")
    Call<Boolean> songEnded(@Body String UUID);
    @POST("/changepass")
    Call<Boolean> changePassword(@Body ArrayList<Object> params);// String UUID, String password
    @POST("/closetheroom")
    Call<Boolean> closeTheRoom(@Body String UUID);
    @POST("/isroomalive")
    Call<Boolean> isRoomAlive(@Body String UUID);
    @POST("/userpass")
    Call<Boolean> userpass(@Body ArrayList<Object> params);
    @POST("/getinfo")
    Call<String> getInfo(@Body String UUID);
}
