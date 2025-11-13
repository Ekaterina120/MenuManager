package com.example.menumanager;

public class Ingredient {
    private int id;
    private String name;
    private double quantity;

    public Ingredient(int id, String name, double quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }

    // Добавляем сеттеры
    public void setName(String name) { this.name = name; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}