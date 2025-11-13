package com.example.menumanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "menu.db";
    private static final int DATABASE_VERSION = 1;
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createTables(SQLiteDatabase db) {
        String CREATE_MENU_TABLE = "CREATE TABLE IF NOT EXISTS Menu (" +
                "menu_id INTEGER PRIMARY KEY," +
                "menu_name TEXT," +
                "creation_date TEXT)";

        String CREATE_DISH_TABLE = "CREATE TABLE IF NOT EXISTS Dish (" +
                "dish_id INTEGER PRIMARY KEY," +
                "menu_id INTEGER," +
                "dish_name TEXT," +
                "price REAL)";

        String CREATE_INGREDIENT_TABLE = "CREATE TABLE IF NOT EXISTS Ingredient (" +
                "ingredient_id INTEGER PRIMARY KEY," +
                "ingredient_name TEXT," +
                "quantity REAL)";

        String CREATE_DISH_INGREDIENT_TABLE = "CREATE TABLE IF NOT EXISTS DishIngredient (" +
                "dish_id INTEGER," +
                "ingredient_id INTEGER," +
                "PRIMARY KEY(dish_id, ingredient_id))";

        String CREATE_STOPPED_DISHES_TABLE = "CREATE TABLE IF NOT EXISTS stopped_dishes (" +
                "dish_id INTEGER PRIMARY KEY," +
                "dish_name TEXT," +
                "price REAL)";

        db.execSQL(CREATE_MENU_TABLE);
        db.execSQL(CREATE_DISH_TABLE);
        db.execSQL(CREATE_INGREDIENT_TABLE);
        db.execSQL(CREATE_DISH_INGREDIENT_TABLE);
        db.execSQL(CREATE_STOPPED_DISHES_TABLE);

        insertTestData(db);
    }

    private void insertTestData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        // Добавляем тестовые блюда в стоп-лист
        values.put("dish_id", 1);
        values.put("dish_name", "Тирамису");
        values.put("price", 160.0);
        db.insertWithOnConflict("stopped_dishes", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 2);
        values.put("dish_name", "Стейк");
        values.put("price", 450.0);
        db.insertWithOnConflict("stopped_dishes", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        // Добавляем тестовые блюда в основное меню
        values.clear();
        values.put("dish_id", 3);
        values.put("dish_name", "Сырники");
        values.put("price", 120.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 4);
        values.put("dish_name", "Омлет");
        values.put("price", 130.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 5);
        values.put("dish_name", "Солянка");
        values.put("price", 220.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 6);
        values.put("dish_name", "Борщ");
        values.put("price", 200.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 7);
        values.put("dish_name", "Рис с овощами");
        values.put("price", 150.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 8);
        values.put("dish_name", "Рыба на пару");
        values.put("price", 210.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 9);
        values.put("dish_name", "Оливье");
        values.put("price", 100.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        values.clear();
        values.put("dish_id", 10);
        values.put("dish_name", "Цезарь");
        values.put("price", 180.0);
        values.put("menu_id", (Integer) null);
        db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Получаем только доступные блюда (не в стоп-листе)
    public List<MenuItem> getAvailableDishes() {
        List<MenuItem> dishList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT dish_id, dish_name, price FROM Dish WHERE menu_id IS NULL", null);

        if (cursor.moveToFirst()) {
            do {
                MenuItem dish = new MenuItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        (int)cursor.getDouble(2),
                        "Основное меню"
                );
                dishList.add(dish);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dishList;
    }

    // Получаем все блюда (для обратной совместимости)
    public List<MenuItem> getAllDishes() {
        return getAvailableDishes();
    }

    public List<StopListItem> getStopList() {
        List<StopListItem> stopList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT dish_id, dish_name, price FROM stopped_dishes", null);

        if (cursor.moveToFirst()) {
            do {
                StopListItem item = new StopListItem(
                        cursor.getInt(0),
                        cursor.getString(1),
                        "В стоп-листе"
                );
                stopList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stopList;
    }

    // Добавляем в стоп-лист и скрываем из основного меню
    public boolean addToStopListAndHide(int dishId, String dishName, double price) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Добавляем в стоп-лист
        ContentValues stopValues = new ContentValues();
        stopValues.put("dish_id", dishId);
        stopValues.put("dish_name", dishName);
        stopValues.put("price", price);

        long stopResult = db.insertWithOnConflict("stopped_dishes", null, stopValues, SQLiteDatabase.CONFLICT_REPLACE);

        // Помечаем блюдо как скрытое (menu_id = -1)
        ContentValues dishValues = new ContentValues();
        dishValues.put("menu_id", -1);

        int dishResult = db.update("Dish", dishValues, "dish_id = ?", new String[]{String.valueOf(dishId)});

        return stopResult != -1 && dishResult > 0;
    }

    // Убираем из стоп-листа и показываем в основном меню
    public boolean removeFromStopListAndShow(int dishId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Удаляем из стоп-листа
        int stopResult = db.delete("stopped_dishes", "dish_id = ?", new String[]{String.valueOf(dishId)});

        // Возвращаем блюдо в основное меню (menu_id = NULL)
        ContentValues dishValues = new ContentValues();
        dishValues.put("menu_id", (Integer) null);

        int dishResult = db.update("Dish", dishValues, "dish_id = ?", new String[]{String.valueOf(dishId)});

        return stopResult > 0 && dishResult > 0;
    }

    // Старые методы для обратной совместимости
    public boolean addToStopList(int dishId, String dishName, double price) {
        return addToStopListAndHide(dishId, dishName, price);
    }

    public boolean removeFromStopList(int dishId) {
        return removeFromStopListAndShow(dishId);
    }

    public boolean isInStopList(int dishId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM stopped_dishes WHERE dish_id = ?",
                new String[]{String.valueOf(dishId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean addDish(String name, double price, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dish_name", name);
        values.put("price", price);
        values.put("menu_id", (Integer) null);

        Cursor cursor = db.rawQuery("SELECT MAX(dish_id) FROM Dish", null);
        int newDishId = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            newDishId = cursor.getInt(0) + 1;
        }
        cursor.close();

        values.put("dish_id", newDishId);

        long result = db.insert("Dish", null, values);
        return result != -1;
    }

    public boolean updateDish(int dishId, String name, double price, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dish_name", name);
        values.put("price", price);

        int result = db.update("Dish", values, "dish_id = ?", new String[]{String.valueOf(dishId)});
        return result > 0;
    }

    public boolean deleteDish(int dishId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Удаляем из основной таблицы
        int result = db.delete("Dish", "dish_id = ?", new String[]{String.valueOf(dishId)});

        // Также удаляем из стоп-листа если есть
        db.delete("stopped_dishes", "dish_id = ?", new String[]{String.valueOf(dishId)});

        return result > 0;
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredientList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ingredient_id, ingredient_name, quantity FROM Ingredient", null);

        if (cursor.moveToFirst()) {
            do {
                Ingredient ingredient = new Ingredient(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2)
                );
                ingredientList.add(ingredient);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredientList;
    }

    public boolean addDishToMenu(int dishId, String dishName, double price) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("dish_id", dishId);
        values.put("dish_name", dishName);
        values.put("price", price);
        values.put("menu_id", (Integer) null);

        long result = db.insertWithOnConflict("Dish", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public double getPriceFromStopList(int dishId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT price FROM stopped_dishes WHERE dish_id = ?", new String[]{String.valueOf(dishId)});

        double price = 0;
        if (cursor.moveToFirst()) {
            price = cursor.getDouble(0);
        }
        cursor.close();
        return price;
    }
    // В класс DatabaseHelper добавим следующие методы:

    // Обновление количества ингредиента
    public boolean updateIngredientQuantity(int ingredientId, double newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", newQuantity);

        int result = db.update("Ingredient", values, "ingredient_id = ?",
                new String[]{String.valueOf(ingredientId)});
        return result > 0;
    }

    // Добавление нового ингредиента
    public boolean addIngredient(String name, double quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ingredient_name", name);
        values.put("quantity", quantity);

        // Получаем максимальный ingredient_id
        Cursor cursor = db.rawQuery("SELECT MAX(ingredient_id) FROM Ingredient", null);
        int newIngredientId = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            newIngredientId = cursor.getInt(0) + 1;
        }
        cursor.close();

        values.put("ingredient_id", newIngredientId);

        long result = db.insert("Ingredient", null, values);
        return result != -1;
    }

    // Удаление ингредиента
    public boolean deleteIngredient(int ingredientId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Сначала удаляем связи с блюдами
        db.delete("DishIngredient", "ingredient_id = ?",
                new String[]{String.valueOf(ingredientId)});

        // Затем удаляем сам ингредиент
        int result = db.delete("Ingredient", "ingredient_id = ?",
                new String[]{String.valueOf(ingredientId)});
        return result > 0;
    }

    // Получение ингредиентов с фильтрацией по низкому количеству
    public List<Ingredient> getLowQuantityIngredients(double threshold) {
        List<Ingredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ingredient_id, ingredient_name, quantity FROM Ingredient WHERE quantity < ?",
                new String[]{String.valueOf(threshold)});

        if (cursor.moveToFirst()) {
            do {
                Ingredient ingredient = new Ingredient(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2)
                );
                ingredients.add(ingredient);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredients;
    }

    // Получение блюд, которые используют ингредиент
    public List<String> getDishesUsingIngredient(int ingredientId) {
        List<String> dishNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT d.dish_name FROM Dish d " +
                "JOIN DishIngredient di ON d.dish_id = di.dish_id " +
                "WHERE di.ingredient_id = ? AND d.menu_id IS NULL";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(ingredientId)});

        if (cursor.moveToFirst()) {
            do {
                dishNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dishNames;
    }
}