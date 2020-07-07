package com.example.nodejschat;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {
    @POST("/login")
Call<LoginResult> eseguiLogin(@Body HashMap<String,String> map); // Effettua una chiamata di tipo http.post attraverso Retrofit2,nel body gli passa i dati dell'utente(string nome,string mail)

    @POST("/signup")
    Call<Void> eseguiRegistrazione(@Body HashMap<String,String> map);
}