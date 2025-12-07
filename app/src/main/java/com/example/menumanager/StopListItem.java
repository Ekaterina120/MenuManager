package com.example.menumanager;

public class StopListItem {
    private int id;
    private String name;
    private double price;
    private String stopDate;

    public StopListItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stopDate = "";
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStopDate() { return stopDate; }
    public void setStopDate(String stopDate) { this.stopDate = stopDate; }

    public String getStatus() { return "В стоп-листе"; }
}