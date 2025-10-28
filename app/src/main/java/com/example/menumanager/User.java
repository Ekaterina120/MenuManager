package com.example.menumanager;

public class User {
    private int id;
    private String username;
    private String role;
    private String password;

    public User(int id, String username, String role, String password) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.password = password;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
}