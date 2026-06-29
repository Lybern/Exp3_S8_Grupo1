package com.minimarket.dto;

public class PublicRegisterRequest {
    private String username;
    private String password;

    public PublicRegisterRequest() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
