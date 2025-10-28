package com.example.menumanager;

public class StopListItem {
    private int id;
    private String dishName;
    private String reason;

    public StopListItem(int id, String dishName, String reason) {
        this.id = id;
        this.dishName = dishName;
        this.reason = reason;
    }

    public int getId() { return id; }
    public String getDishName() { return dishName; }
    public String getReason() { return reason; }
}