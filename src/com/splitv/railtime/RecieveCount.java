package com.splitv.railtime;



import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

public class RecieveCount  extends SherlockListActivity  {
	
	private railDBHelper helper;//Keep copy of sql helper handy
	private weekCursorAdapter weekAdapter; //keep copy of adapter so we can refresh listview
	private shiftCursorAdapter shiftAdapter; //keep copy of adapter so we can refresh listview
	private activityCursorAdapter activityAdapter; //keep copy of adapter so we can refresh listview
	private ListView mainList;//keep a copy of main listview 
	private String mQuery;//Since query never changes keep a copy
	private View mView;//Since view never changes keep a copy
	private boolean weekDone = false;
	private int newCount = 0;
	String weekId;
	private Button newShiftBut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		//Inflate proper view
		mView=LayoutInflater.inflate(this, R.layout.import_main);
		//Must set layout before manipulating it
		setContentView(mView);

		//Hide 'add a new work week' hint because we already have weeks to display
		newShiftBut = (Button) mView.findViewById(R.id.AddNewShift);
		newShiftBut.setText(R.string.recieveCountNewWeek);
		newShiftBut.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent( RecieveCount.this, AddWeek.class ); 
                mIntent.putExtra( "com.splitv.railtime.curCount", String.valueOf(newCount) );
                startActivity(mIntent);
                finish();						
			}
		});	
		TextView txt = (TextView) mView.findViewById(R.id.AddHintText);	
		txt.setText(R.string.recieveCountChooseWeek);
		

	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	    	String nText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    	newCount = Integer.valueOf(nText);
	    }
		
		
		
		//Set all constants needed to refresh/build list
		helper = new railDBHelper(this);
		mainList = getListView();
		//get rid of gap in list
		mainList.setDividerHeight(0);		
		
		//query to be used when populating listview
		mQuery="SELECT "+railDBHelper.weekId+" as _id, "+railDBHelper.weekEnd+", "+
						railDBHelper.weekHours+", "+railDBHelper.weekStart+" FROM "+railDBHelper.weekTable +" ORDER BY "+railDBHelper.weekTimestamp+" DESC";
		//build list view
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor dbResult = db.rawQuery(mQuery, null);
		weekAdapter = new weekCursorAdapter(RecieveCount.this, dbResult, CursorAdapter.NO_SELECTION);
		mainList.setAdapter(weekAdapter);	
		
	}

//Start ViewWeek activity when list item clicked
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if(!weekDone){
			TextView txt = (TextView) mView.findViewById(R.id.AddHintText);	
			txt.setText(R.string.recieveCountChooseShift);
		
		//Get ID for item clicked
			Cursor cur = (Cursor)mainList.getAdapter().getItem(position);
			weekId = cur.getString(cur.getColumnIndex("_id"));
		
		//Save secondary query since it never changes
			mQuery="SELECT "+railDBHelper.shiftId+" as _id, "+railDBHelper.shiftType+", "+
						railDBHelper.shiftHours+", "+railDBHelper.shiftDate+" FROM "+railDBHelper.shiftTable +" WHERE "+railDBHelper.shiftWeekId+" = "+weekId+" ORDER BY "+railDBHelper.shiftTimestamp+" DESC";
				

		//Now populate shift listview in lower portion of screen with data

			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor dbResult = db.rawQuery(mQuery, null);
			if(dbResult.getCount()<=0){

        		Toast.makeText(getApplicationContext(), "Work week does not contain any shifts to add count to...",
    	        													Toast.LENGTH_LONG).show();

            	Intent mIntent = new Intent( RecieveCount.this, AddShift.class ); 
                mIntent.putExtra( "com.splitv.railtime.shiftWeekId", weekId );
                mIntent.putExtra( "com.splitv.railtime.curCount", String.valueOf(newCount) );
                startActivity(mIntent);
                finish();
			}
			else{
				weekDone = true;

				newShiftBut.setText(R.string.recieveCountNewShift);
				newShiftBut.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent mIntent = new Intent( RecieveCount.this, AddShift.class ); 
		                mIntent.putExtra( "com.splitv.railtime.shiftWeekId", weekId );
		                mIntent.putExtra( "com.splitv.railtime.curCount", String.valueOf(newCount) );
		                startActivity(mIntent);
		                finish();						
					}
				});	
				
				shiftAdapter = new shiftCursorAdapter(RecieveCount.this, dbResult, CursorAdapter.NO_SELECTION);
				mainList.setAdapter(shiftAdapter);				
			}
			
		}
		else{

    		Cursor cur = (Cursor)mainList.getAdapter().getItem(position);
    		String shiftId = cur.getString(cur.getColumnIndex("_id"));
        	Intent mIntent = new Intent( RecieveCount.this, AddActivityDz.class ); 
            mIntent.putExtra( "com.splitv.railtime.shiftId", shiftId );
            mIntent.putExtra( "com.splitv.railtime.weekId", weekId );
            mIntent.putExtra( "com.splitv.railtime.curCount", String.valueOf(newCount) );
            startActivity(mIntent);
            finish();
        	
		}
	}

}
