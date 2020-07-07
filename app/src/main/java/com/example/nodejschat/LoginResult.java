package com.example.nodejschat;

import com.google.gson.annotations.SerializedName;

public class LoginResult {
    @SerializedName("name") // Comunica al gson-converter che questa variabile contieni dati con chiave Name
    private String name;
    @SerializedName("email") // Comunica al gson-converter che questa variabile contieni dati con chiave Email
    private String password;

    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }

}
