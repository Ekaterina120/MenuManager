package com.example.menumanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "rrr.db";
    private static final int DATABASE_VERSION = 1;

    // Имена таблиц из вашей БД
    private static final String TABLE_DISH = "Dish";
    private static final String TABLE_INGREDIENT = "Ingredient";
    private static final String TABLE_STOPPED_DISHES = "stopped_dishes";
    private static final String TABLE_MENU = "Menu";
    private static final String TABLE_CATEGORY = "Category";

    // Столбцы
    private static final String COLUMN_DISH_ID = "dish_id";
    private static final String COLUMN_DISH_NAME = "dish_name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_INGREDIENT_ID = "ingredient_id";
    private static final String COLUMN_INGREDIENT_NAME = "ingredient_name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_STOP_DISH_ID = "dish_id";
    private static final String COLUMN_STOP_DISH_NAME = "dish_name";
    private static final String COLUMN_STOP_PRICE = "price";

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Принудительно создаем/обновляем структуру
        try {
            // Создаем таблицу DishIngredient
            String createDishIngredientTable = "CREATE TABLE IF NOT EXISTS DishIngredient (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "dish_id INTEGER," +
                    "ingredient_id INTEGER," +
                    "FOREIGN KEY(dish_id) REFERENCES Dish(dish_id) ON DELETE CASCADE," +
                    "FOREIGN KEY(ingredient_id) REFERENCES Ingredient(ingredient_id) ON DELETE CASCADE)";

            db.execSQL(createDishIngredientTable);
            Log.d(TAG, "✅ Таблица DishIngredient создана в onCreate()");

        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка в onCreate(): " + e.getMessage());
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // При открытии БД проверяем структуру
        forceUpgradeDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Добавляем столбец description, если его нет
        try {
            Cursor cursor = db.rawQuery(
                    "PRAGMA table_info(Dish)",
                    null
            );

            boolean hasDescription = false;
            while (cursor.moveToNext()) {
                String columnName = cursor.getString(1);
                if ("description".equals(columnName)) {
                    hasDescription = true;
                    break;
                }
            }
            cursor.close();

            if (!hasDescription) {
                db.execSQL("ALTER TABLE Dish ADD COLUMN description TEXT");
                Log.d(TAG, "✅ Столбец description добавлен в таблицу Dish");
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при проверке/добавлении столбца description: " + e.getMessage());
        }
    }
    // ==================== СИНХРОНИЗИРОВАННЫЕ МЕТОДЫ ====================

    /**
     * Получить все блюда (синхронизировано)
     */
    public synchronized List<MenuItem> getAllDishes() {
        List<MenuItem> dishList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            String query = "SELECT " + COLUMN_DISH_ID + ", " + COLUMN_DISH_NAME + ", " + COLUMN_PRICE +
                    " FROM " + TABLE_DISH +
                    " ORDER BY " + COLUMN_DISH_NAME;

            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                try {
                    MenuItem dish = new MenuItem();
                    dish.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISH_ID)));
                    dish.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_NAME)));
                    dish.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                    dish.setDescription("");
                    dish.setImagePath(null);
                    dishList.add(dish);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка чтения блюда: " + e.getMessage());
                }
            }

            Log.d(TAG, "Загружено блюд: " + dishList.size());

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getAllDishes: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // НЕ закрываем базу здесь - пусть SQLiteOpenHelper управляет соединением
        }
        return dishList;
    }
// ==================== МЕТОДЫ ДЛЯ ИНГРЕДИЕНТОВ (полный набор) ===================

    /**
     * Обновить количество ингредиента
     */
    public synchronized boolean updateIngredientQuantity(int ingredientId, double newQuantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, newQuantity);

            int result = db.update(TABLE_INGREDIENT, values,
                    COLUMN_INGREDIENT_ID + " = ?",
                    new String[]{String.valueOf(ingredientId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в updateIngredientQuantity: " + e.getMessage());
            return false;
        }
    }

    /**
     * Удалить ингредиент
     */
    public synchronized boolean deleteIngredient(int ingredientId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // Сначала удаляем связи с блюдами (если есть таблица DishIngredient)
            try {
                db.delete("DishIngredient", COLUMN_INGREDIENT_ID + " = ?",
                        new String[]{String.valueOf(ingredientId)});
            } catch (Exception e) {
                Log.d(TAG, "Таблица DishIngredient не найдена или уже удалена");
            }

            // Удаляем сам ингредиент
            int result = db.delete(TABLE_INGREDIENT,
                    COLUMN_INGREDIENT_ID + " = ?",
                    new String[]{String.valueOf(ingredientId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в deleteIngredient: " + e.getMessage());
            return false;
        }
    }
    /**
     * Проверить, есть ли блюдо в стоп-листе (синхронизировано)
     */
    public synchronized boolean isInStopList(int dishId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_STOPPED_DISHES +
                            " WHERE " + COLUMN_STOP_DISH_ID + " = ?",
                    new String[]{String.valueOf(dishId)});
            boolean exists = cursor.getCount() > 0;
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в isInStopList: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Добавить в стоп-лист (синхронизировано)
     */
    public synchronized boolean addToStopList(int dishId, String dishName, double price) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_STOP_DISH_ID, dishId);
            values.put(COLUMN_STOP_DISH_NAME, dishName);
            values.put(COLUMN_STOP_PRICE, price);

            long result = db.insert(TABLE_STOPPED_DISHES, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в addToStopList: " + e.getMessage());
            return false;
        }
    }

    /**
     * Удалить из стоп-листа (синхронизировано)
     */
    public synchronized boolean removeFromStopList(int dishId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int result = db.delete(TABLE_STOPPED_DISHES,
                    COLUMN_STOP_DISH_ID + " = ?",
                    new String[]{String.valueOf(dishId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в removeFromStopList: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получить количество блюд (синхронизировано)
     */
    public synchronized int getDishCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_DISH, null);

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getDishCount: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Получить количество в стоп-листе (синхронизировано)
     */
    public synchronized int getStopListCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_STOPPED_DISHES, null);

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getStopListCount: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Добавить блюдо (синхронизировано)
     */
    public synchronized boolean addDish(String name, double price, int categoryId, int menuId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            int nextId = getNextDishId(db);

            values.put(COLUMN_DISH_ID, nextId);
            values.put(COLUMN_DISH_NAME, name);
            values.put(COLUMN_PRICE, price);

            if (categoryId > 0) {
                values.put("category_id", categoryId);
            }

            if (menuId > 0) {
                values.put("menu_id", menuId);
            }

            long result = db.insert(TABLE_DISH, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в addDish: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверить подключение к БД (синхронизировано)
     */
    public synchronized boolean testConnection() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Простой запрос для проверки соединения
            cursor = db.rawQuery("SELECT 1", null);

            // Если курсор создан и не пустой - соединение работает
            boolean success = cursor != null;

            if (cursor != null) {
                // Можно проверить что курсор имеет данные
                if (cursor.moveToFirst()) {
                    success = true;
                }
            }

            return success;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка тестирования подключения: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // Не закрываем db - пусть SQLiteOpenHelper управляет соединением
        }
    }
    /**
     * Получить ингредиенты для блюда (синхронизировано)
     */
    /**
     * Получить ингредиенты для блюда (исправленная версия)
     */
    public synchronized List<Ingredient> getIngredientsForDish(int dishId) {
        List<Ingredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Проверяем существование таблицы DishIngredient
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='DishIngredient'",
                    null
            );
            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();

            if (!tableExists) {
                Log.d(TAG, "❌ Таблица DishIngredient не существует");
                return ingredients;
            }

            // Получаем ингредиенты через связь DishIngredient
            String query = "SELECT i.* FROM " + TABLE_INGREDIENT + " i " +
                    "INNER JOIN DishIngredient di ON i." + COLUMN_INGREDIENT_ID + " = di.ingredient_id " +
                    "WHERE di.dish_id = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(dishId)});

            while (cursor.moveToNext()) {
                try {
                    Ingredient ingredient = new Ingredient(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                    );
                    ingredients.add(ingredient);
                    Log.d(TAG, "Найден ингредиент: " + ingredient.getName() + " для блюда " + dishId);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка чтения ингредиента: " + e.getMessage());
                }
            }

            Log.d(TAG, "✅ Для блюда " + dishId + " найдено ингредиентов: " + ingredients.size());

        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка в getIngredientsForDish: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ingredients;
    }
    /**
     * Проверить структуру таблицы Dish
     */
    public void checkDishTableStructure() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Получаем информацию о столбцах таблицы Dish
            cursor = db.rawQuery("PRAGMA table_info(Dish)", null);

            Log.d(TAG, "=== Структура таблицы Dish ===");
            while (cursor.moveToNext()) {
                String columnName = cursor.getString(1); // name
                String columnType = cursor.getString(2); // type
                Log.d(TAG, "Столбец: " + columnName + " | Тип: " + columnType);
            }
            Log.d(TAG, "=== Конец структуры ===");

        } catch (Exception e) {
            Log.e(TAG, "Ошибка проверки структуры: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Обновить блюдо (исправленная версия)
     */
    public synchronized boolean updateDish(int dishId, String name, double price, int categoryId, int menuId) {
        SQLiteDatabase db = null;

        try {
            Log.d(TAG, "Обновление блюда ID: " + dishId + ", имя: " + name + ", цена: " + price);

            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("dish_name", name);

            // Преобразуем double в строку для избежания проблем с типами
            values.put("price", String.valueOf(price));

            if (categoryId > 0) {
                values.put("category_id", categoryId);
            }

            if (menuId > 0) {
                values.put("menu_id", menuId);
            }

            // Логируем значения перед обновлением
            Log.d(TAG, "Обновляемые значения: " + values.toString());

            int result = db.update("Dish", values,
                    "dish_id = ?",
                    new String[]{String.valueOf(dishId)});

            Log.d(TAG, "Результат обновления: " + result + " строк изменено");

            return result > 0;

        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка в updateDish: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Простая версия обновления (только имя и цена)
     */
    public synchronized boolean updateDishSimple(int dishId, String name, double price) {
        SQLiteDatabase db = null;

        try {
            Log.d(TAG, "Простое обновление блюда ID: " + dishId + ", имя: " + name + ", цена: " + price);

            db = this.getWritableDatabase();

            // Проверяем существует ли блюдо
            Cursor checkCursor = db.rawQuery(
                    "SELECT dish_id FROM Dish WHERE dish_id = ?",
                    new String[]{String.valueOf(dishId)}
            );

            boolean exists = checkCursor.getCount() > 0;
            checkCursor.close();

            if (!exists) {
                Log.e(TAG, "Блюдо с ID " + dishId + " не найдено!");
                return false;
            }

            // Обновляем имя и цену
            ContentValues values = new ContentValues();
            values.put("dish_name", name);
            values.put("price", price); // Пусть SQLite сам конвертирует тип

            int result = db.update("Dish", values,
                    "dish_id = ?",
                    new String[]{String.valueOf(dishId)});

            if (result > 0) {
                Log.d(TAG, "✅ Блюдо успешно обновлено");
            } else {
                Log.e(TAG, "❌ Блюдо не обновлено (0 строк изменено)");
            }

            return result > 0;

        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка в updateDishSimple: " + e.getMessage(), e);

            // Пробуем альтернативный способ
            try {
                return updateDishAlternative(dishId, name, price);
            } catch (Exception e2) {
                Log.e(TAG, "❌ Альтернативный способ тоже не сработал: " + e2.getMessage());
                return false;
            }
        }
    }

    /**
     * Альтернативный способ обновления (RAW SQL)
     */
    private boolean updateDishAlternative(int dishId, String name, double price) {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();

            // Используем RAW SQL запрос
            String sql = "UPDATE Dish SET dish_name = ?, price = ? WHERE dish_id = ?";
            db.execSQL(sql, new Object[]{name, price, dishId});

            Log.d(TAG, "✅ Блюдо обновлено через RAW SQL");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка в updateDishAlternative: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Удалить блюдо (синхронизировано)
     */
    public synchronized boolean deleteDish(int dishId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // Удаляем из стоп-листа
            db.delete(TABLE_STOPPED_DISHES, COLUMN_STOP_DISH_ID + " = ?",
                    new String[]{String.valueOf(dishId)});

            // Удаляем само блюдо
            int result = db.delete(TABLE_DISH,
                    COLUMN_DISH_ID + " = ?",
                    new String[]{String.valueOf(dishId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в deleteDish: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получить следующий ID (вспомогательный)
     */
    private int getNextDishId(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT MAX(" + COLUMN_DISH_ID + ") FROM " + TABLE_DISH, null);
            int maxId = 1;
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                maxId = cursor.getInt(0) + 1;
            }
            return maxId;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getNextDishId: " + e.getMessage());
            return 1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==================== МЕТОДЫ ДЛЯ ИНГРЕДИЕНТОВ ====================

    public synchronized List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredientList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            String query = "SELECT * FROM " + TABLE_INGREDIENT + " ORDER BY " + COLUMN_INGREDIENT_NAME;

            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                try {
                    Ingredient ingredient = new Ingredient(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                    );
                    ingredientList.add(ingredient);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка чтения ингредиента: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getAllIngredients: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ingredientList;
    }

    public synchronized boolean addIngredient(String name, double quantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            int nextId = getNextIngredientId(db);

            ContentValues values = new ContentValues();
            values.put(COLUMN_INGREDIENT_ID, nextId);
            values.put(COLUMN_INGREDIENT_NAME, name);
            values.put(COLUMN_QUANTITY, quantity);

            long result = db.insert(TABLE_INGREDIENT, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в addIngredient: " + e.getMessage());
            return false;
        }
    }

    private int getNextIngredientId(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT MAX(" + COLUMN_INGREDIENT_ID + ") FROM " + TABLE_INGREDIENT, null);
            int maxId = 1;
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                maxId = cursor.getInt(0) + 1;
            }
            return maxId;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getNextIngredientId: " + e.getMessage());
            return 1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==================== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ====================

    public synchronized List<MenuItem> getAvailableDishes() {
        List<MenuItem> dishList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Блюда, которых нет в стоп-листе
            String query = "SELECT d.* FROM " + TABLE_DISH + " d " +
                    "LEFT JOIN " + TABLE_STOPPED_DISHES + " s ON d." + COLUMN_DISH_ID + " = s." + COLUMN_STOP_DISH_ID + " " +
                    "WHERE s." + COLUMN_STOP_DISH_ID + " IS NULL " +
                    "ORDER BY d." + COLUMN_DISH_NAME;

            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                try {
                    MenuItem dish = new MenuItem();
                    dish.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISH_ID)));
                    dish.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISH_NAME)));
                    dish.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                    dish.setDescription("");
                    dish.setImagePath(null);
                    dishList.add(dish);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка чтения доступного блюда: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getAvailableDishes: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dishList;
    }
    /**
     * Добавить блюдо с ингредиентами
     */
    /**
     * Добавить блюдо с ингредиентами (ИСПРАВЛЕННАЯ ВЕРСИЯ)
     */
    public synchronized boolean addDishWithIngredients(String name, double price, String description, List<Integer> ingredientIds) {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            try {
                // 1. Добавляем блюдо
                int nextId = getNextDishId(db);

                ContentValues dishValues = new ContentValues();
                dishValues.put(COLUMN_DISH_ID, nextId);
                dishValues.put(COLUMN_DISH_NAME, name);
                dishValues.put(COLUMN_PRICE, price);

                // Проверяем, есть ли столбец description
                Cursor cursor = db.rawQuery(
                        "PRAGMA table_info(Dish)",
                        null
                );
                boolean hasDescriptionColumn = false;
                while (cursor.moveToNext()) {
                    if ("description".equals(cursor.getString(1))) {
                        hasDescriptionColumn = true;
                        break;
                    }
                }
                cursor.close();

                if (hasDescriptionColumn) {
                    dishValues.put("description", description);
                    Log.d(TAG, "✅ Столбец description найден, добавляем описание");
                } else {
                    Log.d(TAG, "⚠️ Столбец description не найден, пропускаем");
                }

                long dishResult = db.insert(TABLE_DISH, null, dishValues);

                if (dishResult == -1) {
                    Log.e(TAG, "❌ Ошибка добавления блюда");
                    db.endTransaction();
                    return false;
                }

                Log.d(TAG, "✅ Блюдо добавлено, ID: " + nextId + ", название: " + name);

                // 2. Добавляем связи с ингредиентами (если есть таблица DishIngredient)
                if (ingredientIds != null && !ingredientIds.isEmpty()) {
                    boolean dishIngredientTableExists = false;

                    // Проверяем существование таблицы DishIngredient
                    Cursor tableCursor = db.rawQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name='DishIngredient'",
                            null
                    );
                    dishIngredientTableExists = tableCursor.getCount() > 0;
                    tableCursor.close();

                    if (dishIngredientTableExists) {
                        int addedLinks = 0;
                        for (int ingredientId : ingredientIds) {
                            ContentValues linkValues = new ContentValues();
                            linkValues.put("dish_id", nextId);
                            linkValues.put("ingredient_id", ingredientId);

                            long linkResult = db.insert("DishIngredient", null, linkValues);

                            if (linkResult != -1) {
                                addedLinks++;
                                Log.d(TAG, "✅ Связь добавлена: dish_id=" + nextId + ", ingredient_id=" + ingredientId);
                            } else {
                                Log.e(TAG, "❌ Ошибка добавления связи для ingredientId: " + ingredientId);
                            }
                        }
                        Log.d(TAG, "✅ Добавлено связей: " + addedLinks + " из " + ingredientIds.size());
                    } else {
                        Log.w(TAG, "⚠️ Таблица DishIngredient не существует, связи не добавлены");
                    }
                } else {
                    Log.d(TAG, "ℹ️ Нет ингредиентов для добавления связей");
                }

                db.setTransactionSuccessful();
                Log.d(TAG, "✅ Транзакция успешно завершена для блюда ID: " + nextId);
                return true;

            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка в транзакции addDishWithIngredients: " + e.getMessage(), e);
                return false;
            } finally {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при завершении транзакции: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Общая ошибка в addDishWithIngredients: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Обновить блюдо с ингредиентами
     */
    /**
     * Обновить блюдо с ингредиентами (ИСПРАВЛЕННАЯ ВЕРСИЯ)
     */
    public synchronized boolean updateDishWithIngredients(int dishId, String name, double price, String description, List<Integer> ingredientIds) {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            try {
                // 1. Обновляем блюдо
                ContentValues dishValues = new ContentValues();
                dishValues.put(COLUMN_DISH_NAME, name);
                dishValues.put(COLUMN_PRICE, price);

                // Проверяем, есть ли столбец description
                Cursor cursor = db.rawQuery(
                        "PRAGMA table_info(Dish)",
                        null
                );
                boolean hasDescriptionColumn = false;
                while (cursor.moveToNext()) {
                    if ("description".equals(cursor.getString(1))) {
                        hasDescriptionColumn = true;
                        break;
                    }
                }
                cursor.close();

                if (hasDescriptionColumn) {
                    dishValues.put("description", description);
                    Log.d(TAG, "✅ Столбец description найден, обновляем описание");
                } else {
                    Log.d(TAG, "⚠️ Столбец description не найден, пропускаем");
                }

                int dishResult = db.update(TABLE_DISH, dishValues,
                        COLUMN_DISH_ID + " = ?",
                        new String[]{String.valueOf(dishId)});

                if (dishResult <= 0) {
                    Log.e(TAG, "❌ Не удалось обновить блюдо ID: " + dishId);
                    return false;
                }

                Log.d(TAG, "✅ Блюдо обновлено, ID: " + dishId + ", новое название: " + name);

                // 2. Обновляем связи с ингредиентами (если есть таблица DishIngredient)
                if (ingredientIds != null) {
                    boolean dishIngredientTableExists = false;

                    // Проверяем существование таблицы DishIngredient
                    Cursor tableCursor = db.rawQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name='DishIngredient'",
                            null
                    );
                    dishIngredientTableExists = tableCursor.getCount() > 0;
                    tableCursor.close();

                    if (dishIngredientTableExists) {
                        // Удаляем старые связи
                        int deletedRows = db.delete("DishIngredient", "dish_id = ?",
                                new String[]{String.valueOf(dishId)});
                        Log.d(TAG, "✅ Удалено старых связей: " + deletedRows + " для dish_id: " + dishId);

                        // Добавляем новые связи
                        int addedLinks = 0;
                        for (int ingredientId : ingredientIds) {
                            ContentValues linkValues = new ContentValues();
                            linkValues.put("dish_id", dishId);
                            linkValues.put("ingredient_id", ingredientId);

                            long linkResult = db.insert("DishIngredient", null, linkValues);

                            if (linkResult != -1) {
                                addedLinks++;
                                Log.d(TAG, "✅ Связь добавлена: dish_id=" + dishId + ", ingredient_id=" + ingredientId);
                            } else {
                                Log.e(TAG, "❌ Ошибка добавления связи для ingredientId: " + ingredientId);
                            }
                        }
                        Log.d(TAG, "✅ Добавлено новых связей: " + addedLinks + " из " + ingredientIds.size());
                    } else {
                        Log.w(TAG, "⚠️ Таблица DishIngredient не существует, связи не обновлены");
                    }
                } else {
                    Log.d(TAG, "ℹ️ Нет ингредиентов для обновления связей");
                }

                db.setTransactionSuccessful();
                Log.d(TAG, "✅ Транзакция успешно завершена для обновления блюда ID: " + dishId);
                return true;

            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка в транзакции updateDishWithIngredients: " + e.getMessage(), e);
                return false;
            } finally {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при завершении транзакции: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Общая ошибка в updateDishWithIngredients: " + e.getMessage(), e);
            return false;
        }
    }
    /**
     * Принудительно обновить структуру БД
     */
    public synchronized void forceUpgradeDatabase() {
        SQLiteDatabase db = null;

        try {
            db = this.getWritableDatabase();

            // 1. Добавляем столбец description если его нет
            try {
                db.execSQL("ALTER TABLE Dish ADD COLUMN description TEXT");
                Log.d(TAG, "✅ Столбец description добавлен в таблицу Dish");
            } catch (Exception e) {
                Log.d(TAG, "Столбец description уже существует или ошибка: " + e.getMessage());
            }

            // 2. Создаем таблицу DishIngredient если ее нет
            try {
                String createDishIngredientTable = "CREATE TABLE IF NOT EXISTS DishIngredient (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "dish_id INTEGER," +
                        "ingredient_id INTEGER," +
                        "FOREIGN KEY(dish_id) REFERENCES Dish(dish_id) ON DELETE CASCADE," +
                        "FOREIGN KEY(ingredient_id) REFERENCES Ingredient(ingredient_id) ON DELETE CASCADE)";

                db.execSQL(createDishIngredientTable);
                Log.d(TAG, "✅ Таблица DishIngredient создана/проверена");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка создания таблицы DishIngredient: " + e.getMessage());
            }

            Log.d(TAG, "✅ Структура БД успешно обновлена");

        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка при обновлении структуры БД: " + e.getMessage());
        }
    }
    /**
     * Получить ID ингредиентов для блюда
     */
    public synchronized List<Integer> getIngredientIdsForDish(int dishId) {
        List<Integer> ingredientIds = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            cursor = db.rawQuery(
                    "SELECT ingredient_id FROM DishIngredient WHERE dish_id = ?",
                    new String[]{String.valueOf(dishId)}
            );

            while (cursor.moveToNext()) {
                try {
                    int ingredientId = cursor.getInt(0);
                    ingredientIds.add(ingredientId);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка чтения ingredient_id: " + e.getMessage());
                }
            }

            Log.d(TAG, "Для блюда " + dishId + " найдено ingredientIds: " + ingredientIds.size());

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getIngredientIdsForDish: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ingredientIds;
    }

    public synchronized List<StopListItem> getStopList() {
        List<StopListItem> stopList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            String query = "SELECT * FROM " + TABLE_STOPPED_DISHES + " ORDER BY " + COLUMN_STOP_DISH_NAME;

            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                try {
                    StopListItem item = new StopListItem(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOP_DISH_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STOP_DISH_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STOP_PRICE))
                    );
                    stopList.add(item);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка чтения стоп-листа: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getStopList: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return stopList;
    }
}