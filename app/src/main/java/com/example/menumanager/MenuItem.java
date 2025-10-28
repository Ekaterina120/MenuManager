package com.example.menumanager;

public class MenuItem {
    private int id;
    private String name;
    private int price;
    private String category;

    public MenuItem(int id, String name, int price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
}