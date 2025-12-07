package com.example.menumanager;

public class Ingredient {
    private int id;
    private String name;
    private double quantity;
    private String unit;

    public Ingredient() {}

    public Ingredient(int id, String name, double quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = "шт";
    }

    public Ingredient(int id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}