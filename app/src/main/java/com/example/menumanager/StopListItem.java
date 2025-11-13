package com.example.menumanager;

public class StopListItem {
    private int id;
    private String dishName;
    private String reason;

    public StopListItem() {
    }

    public StopListItem(int id, String dishName, String reason) {
        this.id = id;
        this.dishName = dishName;
        this.reason = reason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}