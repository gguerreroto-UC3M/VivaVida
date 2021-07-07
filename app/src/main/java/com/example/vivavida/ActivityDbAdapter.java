package com.example.vivavida;
/*
 * Based in code by Google with Apache License, Version 2.0
 *
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Clase adaptadora que nos va a facilitar el uso de la BD
public class ActivityDbAdapter {
    private static final String TAG = "VIVAVIDA: DbAdapter"; // Usado en los mensajes de Log

    //Nombre de la base de datos, tablas (dos, una para la activdad y otra para los puntos lat lng) y versión
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE1 = "activities";
    private static final String DATABASE_TABLE2 = "points";
    private static final int DATABASE_VERSION = 2;

    //campos de la tabla de la base de datos de actividades
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_TIME = "time";
    public static final String KEY_KCAL = "kcal";
    public static final String KEY_ROWID1 = "_id";
    public static final String KEY_ROUTEID = "route";

    //campos de la tabla de la base de datos de puntos
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_ROWID2 = "_id";

    //comparten KEY_ROUTEID


    // Sentencia SQL para crear las tablas de las bases de datos
    private static final String DATABASE_CREATE1 = "create table " + DATABASE_TABLE1 + " (" +
            KEY_ROWID1 + " integer primary key autoincrement, " +
            KEY_TYPE + " text not null, " +
            KEY_DATE + " text not null, " +
            KEY_DISTANCE + " text not null, " +
            KEY_TIME + " text not null, " +
            KEY_KCAL + " text not null, " +
            KEY_ROUTEID + " text not null);";

    private static final String DATABASE_CREATE2 = "create table " + DATABASE_TABLE2 + " (" +
            KEY_ROWID2 + " integer primary key autoincrement, " +
            KEY_LAT + " text not null, " +
            KEY_LNG + " text not null, " +
            KEY_ROUTEID + " text not null);";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE1);
            db.execSQL(DATABASE_CREATE2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE1);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE2);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */

    public ActivityDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ActivityDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new activity using the parametes provided. If it is
     * successfully created return the new rowId for that activity, otherwise return
     * a -1 to indicate failure.
     *
     * @param type     the type of the activity
     * @param date     the date of the activity
     * @param distance the distance of the activity
     * @param time     the time of the activity
     * @param kcal     the kcal burnt of the activity
     * @param route_id the id linked to the activity
     * @return rowId or -1 if failed
     */
    public long createActivity(String type, String date, String distance, String time, String kcal, String route_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_DISTANCE, distance);
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_KCAL, kcal);
        initialValues.put(KEY_ROUTEID, route_id);

        return mDb.insert(DATABASE_TABLE1, null, initialValues);
    }

    /**
     * Create a new LatLng point using the parameters provided. If the point is
     * successfully created return the new rowId for that point, otherwise return
     * a -1 to indicate failure.
     *
     * @param lat      the latitude of the point
     * @param lng      the longitude of the point
     * @param route_id the id linked to the activity
     * @return rowId or -1 if failed
     */
    public long createPoints(String lat, String lng, String route_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LAT, lat);
        initialValues.put(KEY_LNG, lng);
        initialValues.put(KEY_ROUTEID, route_id);

        return mDb.insert(DATABASE_TABLE2, null, initialValues);
    }

    /**
     * Delete the activity and associated points with the given rowId
     *
     * @param rowId id of the activity to delete
     */
    public void deleteActivity(long rowId) {
        Cursor mCursor =
                fetchActivity(rowId);

        String route_id = mCursor.getString(mCursor.getColumnIndexOrThrow(KEY_ROUTEID));

        mDb.delete(DATABASE_TABLE1, KEY_ROWID1 + "=" + rowId, null);
        mDb.delete(DATABASE_TABLE2, KEY_ROUTEID + "=" + route_id, null);

        //Delete both the activity and all points associated to that activity route
    }

    /**
     * Delete all the activities and associated points
     */
    public void deleteAllActivities() {
        mDb.execSQL("delete from " + DATABASE_TABLE1);
        mDb.execSQL("delete from " + DATABASE_TABLE2);
    }

    /**
     * Return a Cursor over the list of all activities in the database
     *
     * @return Cursor over all activities
     */
    public Cursor fetchAllActivities() {

        return mDb.query(DATABASE_TABLE1, new String[]{KEY_ROWID1, KEY_TYPE,
                KEY_DATE, KEY_DISTANCE, KEY_TIME, KEY_KCAL, KEY_ROUTEID}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the activity that matches the given rowId
     *
     * @param rowId id of activity to retrieve
     * @return Cursor positioned to matching activity, if found
     * @throws SQLException if activity could not be found/retrieved
     */
    public Cursor fetchActivity(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE1, new String[]{KEY_ROWID1, KEY_TYPE,
                                KEY_DATE, KEY_DISTANCE, KEY_TIME, KEY_KCAL, KEY_ROUTEID}, KEY_ROWID1 + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Return a Cursor positioned at the activity that matches the given rowId
     *
     * @param route_id id of the route to retrieve the points
     * @return Cursor positioned to matching points, if found
     * @throws SQLException if points could not be found/retrieved
     */
    public Cursor fetchPoints(String route_id) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE2, new String[]{KEY_ROWID2, KEY_LAT,
                                KEY_LNG, KEY_ROUTEID}, KEY_ROUTEID + "=" + route_id, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the activity using the details provided. The activity to be updated is
     * specified using the rowId, and it is altered to use the argument
     * values passed in
     *
     * @param rowId    id of note to update
     * @param type     the type of the activity to set to
     * @param date     the date of the activity to set to
     * @param distance the distance of the activity to set to
     * @param time     the time of the activity to set to
     * @param kcal     the kcal burnt of the activity to set to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateActivity(long rowId, String type, String date, String distance, String time, String kcal, String route_id) {
        ContentValues args = new ContentValues();
        args.put(KEY_TYPE, type);
        args.put(KEY_DATE, date);
        args.put(KEY_DISTANCE, distance);
        args.put(KEY_TIME, time);
        args.put(KEY_KCAL, kcal);
        args.put(KEY_ROUTEID, route_id);

        return mDb.update(DATABASE_TABLE1, args, KEY_ROWID1 + "=" + rowId, null) > 0;
    }
}
