package com.example.thedietitianfyp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.widget.Toast;

public class DBAdapter {
    /* Variables  */
    private static final String databaseName = "theDietitian";
    private static final int databaseVersion = 76;

    /* Database variables  */
    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    /* Class DbAdapter  */
    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    /* DatabaseHelper */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, databaseName, null, databaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS goal (" +
                        " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " goal_id INTEGER, " +
                        " goal_current_weight INT, " +
                        " goal_target_weight INT, " +
                        " goal_weekly_goal VARCHAR, " +
                        " goal_date DATE," +
                        " goal_goal VARCHAR, " +

                        " goal_calories_bmr INT, " +
                        " goal_calories_with_activity INT, " +
                        " goal_calories_with_diet INT, " +
                        " goal_calories_with_activity_and_diet INT, " +

                        " goal_proteins_bmr INT, " +
                        " goal_carbohydrates_bmr INT, " +
                        " goal_fats_bmr INT, " +

                        " goal_proteins_with_activity INT, " +
                        " goal_carbohydrates_with_activity INT, " +
                        " goal_fats_with_activity INT, " +

                        " goal_proteins_with_activity_and_diet INT, " +
                        " goal_carbohydrates_with_activity_and_diet INT, " +
                        " goal_fats_with_activity_and_diet INT, " +

                        " goal_proteins_with_diet INT, " +
                        " goal_carbohydrates_with_diet INT, " +
                        " goal_fats_with_diet INT) ;");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS food_diary (" +
                    " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " fd_id INTEGER," +
                    " fd_date DATE," +
                    " fd_portion INT," +
                    " fd_food_name VARCHAR," +
                    " fd_calories DOUBLE," +
                    " fd_protein DOUBLE," +
                    " fd_carbohydrates DOUBLE," +
                    " fd_fat DOUBLE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS user (" +
                    " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " user_id INTEGER," +
                    " user_email VARCHAR," +
                    " user_name VARCHAR, " +
                    " user_password VARCHAR," +
                    " user_dob DATE," +
                    " user_gender VARCHAR, " +
                    " user_height INT, " +
                    " user_weight INT," +
                    " user_activity_level INT, " +
                    " user_measurement VARCHAR) ;");

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS food (" +
                        " _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        " food_name VARCHAR," + //1
                        " food_manufacturer_name VARCHAR," + //2
                        " food_description VARCHAR," +//3
                        " food_ingredient VARCHAR," +//4
                        " food_serving_size INT," +//5
                        " food_serving_measurement VARCHAR," +//6
                        " food_calories INT," +//7
                        " food_proteins INT," +//8
                        " food_carbohydrates INT," +//9
                        " food_fat INT," +
                        " food_image);");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop tables
            db.execSQL("DROP TABLE IF EXISTS food");
            db.execSQL("DROP TABLE IF EXISTS food_diary");
            db.execSQL("DROP TABLE IF EXISTS user");
            db.execSQL("DROP TABLE IF EXISTS goal");

            onCreate(db);

            String TAG = "Tag";
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        } // end public void onUpgrade
    } // DatabaseHelper


    /* Open database */
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    /* Close database */
    public void close() {
        DBHelper.close();
    }

    /* Insert data */
    public void insert(String table, String fields, String values) {
        try {
            db.execSQL("INSERT INTO " + table + "(" + fields + ") VALUES (" + values + ")");
        } catch (SQLiteException e) {
            System.out.println("Insert error: " + e.toString());
        }
    }

    /* Count  */
    public int count(String table) {
        Cursor mCount = db.rawQuery("SELECT COUNT(*) FROM " + table + "", null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    /* Quote Smart */
    public String quoteSmart(String value) {
        boolean isNumeric = false;
        try {
            double myDouble = Double.parseDouble(value);
            isNumeric = true;
        } catch (NumberFormatException nfe) {
            //Escapes special characters in a string for use in a sql statement
            if (value != null && value.length() > 0) {
//                value = value.replace("https", "");
//                value = value.replace("://", "");
                value = value.replace("\\", "\\\\");
                value = value.replace(":", "");
                value = value.replace("'", "");
                value = value.replace("(", "");
                value = value.replace("\0", "\\0");
                value = value.replace("\n", "\\n");
                value = value.replace("\r", "\\r");
                value = value.replace("\"", "\\\"");
                value = value.replace("\\x1a", "\\Z");
                value = value.replace("{", "");
            }
        }
        value = "'" + value + "'";

        return value;
    }

    public double quoteSmart(double value) {
        return value;
    }

    public int quoteSmart(int value) {
        return value;
    }

    public long quoteSmart(long value) {
        return value;
    }

    /* quote smart helps stop lethal sql injections */

    /* Query Database */
    public Cursor selectPrimaryKey(String table, String primaryKey, long rowID, String[] fields) throws SQLException {

        Cursor c = db.query(table, fields, primaryKey + "=" + rowID, null, null,
                null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /* Update Database */
    public boolean updateDatabase(String table, String primaryKey, long rowID, String fields, String value) {

        //Remove first and last value of value
        value = value.substring(1, value.length() - 1); //removes ' after running quote smart

        ContentValues args = new ContentValues();
        args.put(fields, value);
        return db.update(table, args, primaryKey + "=" + rowID, null) > 0;
    }

    public boolean updateDatabase(String table, String primaryKey, long rowID, String fields, double value) {
        ContentValues args = new ContentValues();
        args.put(fields, value);
        return db.update(table, args, primaryKey + "=" + rowID, null) > 0;
    }

    public boolean updateDatabase(String table, String primaryKey, long rowID, String fields, int value) {
        ContentValues args = new ContentValues();
        args.put(fields, value);
        return db.update(table, args, primaryKey + "=" + rowID, null) > 0;
    }

    /* Update  */

    public boolean update(String table, String primaryKey, long rowId, String field, String value) {
        // Remove first and last value of value
        value = value.substring(1, value.length()-1); // removes ' after running quote smart

        ContentValues args = new ContentValues();
        args.put(field, value);
        return db.update(table, args, primaryKey + "=" + rowId, null) > 0;
    }
    public boolean update(String table, String primaryKey, long rowId, String field, double value) {
        ContentValues args = new ContentValues();
        args.put(field, value);
        return db.update(table, args, primaryKey + "=" + rowId, null) > 0;
    }
    public boolean update(String table, String primaryKey, long rowId, String field, int value) {
        ContentValues args = new ContentValues();
        args.put(field, value);
        return db.update(table, args, primaryKey + "=" + rowId, null) > 0;
    }
    public boolean update(String table, String primaryKey, long rowID, String fields[], String values[]){


        ContentValues args = new ContentValues();
        int arraySize = fields.length;
        for(int x=0;x<arraySize;x++){
            // Remove first and last value of value
            values[x] = values[x].substring(1, values[x].length()-1); // removes ' after running quote smart

            // Put
            args.put(fields[x], values[x]);
        }

        return db.update(table, args, primaryKey + "=" + rowID, null) > 0;
    }

    /* Select */
    public Cursor select(String table, String[] fields) throws SQLException {

        Cursor mCursor = db.query(table, fields, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // Select All where (String)
    public Cursor select(String table, String[] fields, String whereClause, String whereCondition) throws SQLException {

        Cursor mCursor = db.query(table, fields, whereClause + "=" + whereCondition, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // Select All where (Long)
    public Cursor select(String table, String[] fields, String whereClause, long whereCondition) throws SQLException {
        Cursor mCursor = db.query(table, fields, whereClause + "=" + whereCondition, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // Select with order
    public Cursor select(String table, String[] fields, String whereClause, String whereCondition, String orderBy, String OrderMethod) throws SQLException {

        Cursor mCursor = null;
        if(whereClause.equals("")){
            // no where clause
             mCursor = db.query(table, fields, null, null, null, null, orderBy + " " + OrderMethod, null);
        }
        else {
             mCursor = db.query(table, fields, whereClause + "=" + whereCondition, null, null, null, orderBy + " " + OrderMethod, null);
        }

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /* Delete */
    // Delete a particular record
    public int delete(String table, String primaryKey, long rowID) throws SQLException {
        return db.delete(table, primaryKey + "=" + rowID, null);
    }
}