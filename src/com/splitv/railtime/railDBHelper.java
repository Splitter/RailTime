package com.splitv.railtime;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class railDBHelper extends SQLiteOpenHelper {
	
	//Database Name
	static final String mDB="railTime";
	
	//Week table
	static final String weekTable="weekTable";
	static final String weekId="weekId";
	static final String weekStart="weekStart";
	static final String weekEnd="weekEnd";
	static final String weekHours="weekHours";
	static final String weekNotes="weekNotes";
	static final String weekTimestamp="weekTimestamp";
	
	//Shift table
	static final String shiftTable="shiftTable";
	static final String shiftId="shiftId";
	static final String shiftWeekId="shiftWeekId";
	static final String shiftType="shiftType";
	static final String shiftDate="shiftDate";
	static final String shiftNotes="shiftNotes";
	static final String shiftStart="shiftStart";
	static final String shiftEnd="shiftEnd";
	static final String shiftHours="shiftHours";
	static final String shiftTimestamp="shiftTimestamp";
	
	//Activity table
		//General
	static final String activityTable="activityTable";
	static final String activityId="activityId";
	static final String activityShiftId="activityShiftId";
	static final String activityHours="activityHours";
	static final String activityNotes="weekNotes";
	static final String activityTimestamp="activityTimestamp";
	static final String activityType="activityType";
	static final String activityStart="activityStart";
	static final String activityEnd="activityEnd";
		//Rail & DZ
	static final String activityTrack="activityTrack";
	static final String activityUnits="activityUnits";
	static final String activityLoaders="activityLoaders";
	
	
	
	
	public railDBHelper(Context context) {
		
		super(context, mDB, null,111); 

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		String query = "CREATE TABLE IF NOT EXISTS "+weekTable+" ("+weekId+"  INTEGER PRIMARY KEY , "+
				weekStart+" TEXT , "+weekEnd+" TEXT , "+weekHours+" INTEGER, "+weekNotes+" TEXT, "+weekTimestamp+" INTEGER )";
		db.execSQL(query);
		
		query = "CREATE TABLE IF NOT EXISTS "+shiftTable+" ("+shiftId+"  INTEGER PRIMARY KEY , "+shiftWeekId+" INTEGER , "+shiftType+" TEXT , "+shiftDate+" TEXT,"+
				shiftStart+" TEXT , "+shiftEnd+" TEXT , "+shiftHours+" INTEGER, "+shiftNotes+" TEXT, "+shiftTimestamp+" INTEGER )";
		db.execSQL(query);

		query = "CREATE TABLE IF NOT EXISTS "+activityTable+" ("+activityId+"  INTEGER PRIMARY KEY , "+activityShiftId+" INTEGER , "+activityType+" TEXT , "+
				activityTrack+" TEXT,"+activityUnits+" TEXT,"+activityLoaders+" TEXT,"+activityStart+" TEXT , "+activityEnd+" TEXT , "+
				activityHours+" INTEGER, "+activityNotes+" TEXT, "+activityTimestamp+" INTEGER )";
		db.execSQL(query);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS "+weekTable);
		db.execSQL("DROP TABLE IF EXISTS "+shiftTable);
		db.execSQL("DROP TABLE IF EXISTS "+activityTable);
		onCreate(db);
		
	}

}
