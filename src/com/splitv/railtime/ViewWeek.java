package com.splitv.railtime;

import java.text.DecimalFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class ViewWeek extends SherlockListActivity {

	private railDBHelper helper;//Keep copy of sql helper handy
	private shiftCursorAdapter shiftAdapter; //keep copy of adapter so we can refresh listview
	private ListView mainList;//keep a copy of main listview 
	private String mQueryWeek;//Since query never changes keep a copy
	private String mQueryShift;//Since query never changes keep a copy
	private View mView;//Since view never changes keep a copy
	private String _id;//ID for the currently viewed week
	public TextView mWeekHoursT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		//Inflate proper view
		mView=LayoutInflater.inflate(this, R.layout.activity_view_week);
		//Must set layout before manipulating it
		setContentView(mView);

		//Set all constants needed to refresh/build list
		mainList = getListView();
		mainList.setDividerHeight(0);//get rid of gap in list
		helper = new railDBHelper(this);
		
		RelativeLayout InfoArea =(RelativeLayout)findViewById(R.id.ViewWeekDataOuter);
		InfoArea.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            	 Intent mIntent = new Intent(ViewWeek.this,
                		 EditWeek.class); 
                mIntent.putExtra("com.splitv.railtime.editWeekId",_id); 
                startActivity(mIntent);
            }
        });
		
		//Get the ID for Currently viewed Week
		Bundle bundle   = getIntent().getExtras();
		_id  = bundle.getString("com.splitv.railtime.viewWeekId");

		//Save main query since it never changes
		mQueryWeek="SELECT "+railDBHelper.weekId+" as _id, "+railDBHelper.weekEnd+", "+
				railDBHelper.weekHours+", "+railDBHelper.weekStart+", "+railDBHelper.weekHours+
				", "+railDBHelper.weekNotes+" FROM "+railDBHelper.weekTable +" WHERE "+railDBHelper.weekId+" = "+_id;
		
		//Save secondary query since it never changes
		mQueryShift="SELECT "+railDBHelper.shiftId+" as _id, "+railDBHelper.shiftType+", "+
				railDBHelper.shiftHours+", "+railDBHelper.shiftDate+" FROM "+railDBHelper.shiftTable +" WHERE "+railDBHelper.shiftWeekId+" = "+_id+" ORDER BY "+railDBHelper.shiftTimestamp+" DESC";
		
		//build view for first time
		buildList(false);
		
	}

	//Helper function to refresh main shift listview
		//set rebuild to false when building list for first time
		//set rebuild to true after initial first build
	public void buildList(boolean rebuild){
		//Query week table for information shown on top of screen
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor dbResult = db.rawQuery(mQueryWeek, null);
		//Parse out data shown on top of screen
		dbResult.moveToFirst();
		String weekStart = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekStart));
		String weekEnd = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekEnd));
		Float weekHours = dbResult.getFloat(dbResult.getColumnIndex(railDBHelper.weekHours));
		String weekNotes = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekNotes));
		String title = weekStart+" - "+weekEnd;
		getSupportActionBar().setTitle(title);
		if(weekNotes == null || weekNotes.isEmpty()){
			weekNotes="n/a";
		}
		//Show the proper information in top of screen
		TextView WeekDatesT = (TextView) mView.findViewById(R.id.WeekDates);
		WeekDatesT.setText(title);
		TextView WeekNotesT = (TextView) mView.findViewById(R.id.WeekNotes);
		WeekNotesT.setText(weekNotes);
		mWeekHoursT = (TextView) mView.findViewById(R.id.WeekHours);
		DecimalFormat newFormat = new DecimalFormat("#.##");
		mWeekHoursT.setText(String.valueOf(Double.valueOf(newFormat.format(weekHours))+" hours"));
		
		new CalculateHours(mWeekHoursT).execute();

		//Now populate shift listview in lower portion of screen with data
		dbResult = db.rawQuery(mQueryShift, null);
		if( dbResult.getCount() > 0 ){
			//Hide 'add a new work shift' hint because we already have shifts to display
			ImageView img = (ImageView) mView.findViewById(R.id.AddHintImage);
			img.setVisibility(View.GONE);	
			TextView txt = (TextView) mView.findViewById(R.id.AddHintText);	
			txt.setVisibility(View.GONE);		
		}
		else{
			//Show 'add a new work shift' hint because there are no existing work shifts
			ImageView img = (ImageView) mView.findViewById(R.id.AddHintImage);
			img.setVisibility(View.VISIBLE);	
			TextView txt = (TextView) mView.findViewById(R.id.AddHintText);	
			txt.setVisibility(View.VISIBLE);		
		}
		//Either rebuild or build for the first time
		if(rebuild){
			shiftAdapter.changeCursor(dbResult);
		}
		else{
			shiftAdapter = new shiftCursorAdapter(ViewWeek.this, dbResult, CursorAdapter.NO_SELECTION);
			mainList.setAdapter(shiftAdapter);			
		}
	}
	
	
	private class CalculateHours extends AsyncTask<Void, Integer, Void>{
		private float mTotalHours = 0;
		private TextView mView;
		
		public CalculateHours( TextView Hours ){
			
			mView = Hours;
		}
		@Override
		protected void onPostExecute(Void result) {
			DecimalFormat newFormat = new DecimalFormat("#.##");
			mView.setText(String.valueOf(Double.valueOf(newFormat.format(mTotalHours))+" hours"));
		}
		  
		@Override
		protected Void doInBackground(Void... params) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor dbResult = db.rawQuery(mQueryShift, null);
			dbResult.moveToFirst();
	        while (dbResult.isAfterLast() == false) {
	    		float shiftHours = dbResult.getFloat(dbResult.getColumnIndex(railDBHelper.shiftHours));
	            mTotalHours += shiftHours;		    		
	    		dbResult.moveToNext();
	        }
    		String strFilter = railDBHelper.weekId+"=" + _id;
    		ContentValues args = new ContentValues();
    		args.put(railDBHelper.weekHours, String.valueOf(mTotalHours));
    		db.update(railDBHelper.weekTable, args, strFilter, null);
			return null;
		}
	}

	//Make sure we are rebuilding list everytime this activity gets focus
	public void onResume(){
		super.onResume();
		buildList(true);
	}


	//Start ViewShift activity when list item clicked
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Get shiftID of item clicked
		Cursor cur = (Cursor)mainList.getAdapter().getItem(position);
		String shiftId = cur.getString(cur.getColumnIndex("_id"));
		//Start ViewShift Activity
			//Pass the id's for shift & week to new activity
        Intent mIntent = new Intent(ViewWeek.this,
                ViewShift.class); 
        mIntent.putExtra("com.splitv.railtime.shiftWeekId",_id); 
        mIntent.putExtra("com.splitv.railtime.shiftShiftId",shiftId); 
        //start activity
        startActivity(mIntent);
	}
	    
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Add 'add shift' item to action bar on main screen
		MenuItem mItem = menu.add(R.string.title_activity_add_shift);
		mItem.setIcon(R.drawable.ic_add);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
	    //Start 'AddShift' Activity when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
                Intent mIntent = new Intent(ViewWeek.this,
                        AddShift.class); 
                //Pass week ID to activity
                mIntent.putExtra( "com.splitv.railtime.curCount", "0" );
                mIntent.putExtra("com.splitv.railtime.shiftWeekId",_id); 
                startActivity(mIntent);
                return true;
            }
        });

		//Add menu dropdown button
        SubMenu subMenu1 = menu.addSubMenu(R.string.menu);
        MenuItem subMenu1Item = subMenu1.getItem();
        subMenu1Item.setIcon(R.drawable.ic_menu);
        //force it to always show
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		//Add link to EditWeek Activty on dropdown
		mItem = subMenu1.add(R.string.edit_week);
		mItem.setIcon(R.drawable.ic_edit);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
                Intent mIntent = new Intent(ViewWeek.this,
                		 EditWeek.class); 
                mIntent.putExtra("com.splitv.railtime.editWeekId",_id); 
                startActivity(mIntent);
                return true;
            }
        });
		
		//Add 'delete week' item to dropdown menu
		mItem = subMenu1.add(R.string.delete_week);
		mItem.setIcon(R.drawable.ic_trash);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//Delete week when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) {      	
            	//Show a confirmation dialog before deleting week
            	AlertDialog.Builder builder = new AlertDialog.Builder(
                        ViewWeek.this);
            	builder.setCancelable(true);
                builder.setTitle(R.string.delete_week);
                builder.setMessage(R.string.delete_week_dialog_message);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {                            	
                            	//Setup DB helper
                        		helper = new railDBHelper(ViewWeek.this);
                        		//get db instance
                        		SQLiteDatabase db = helper.getWritableDatabase();     
                        		//prepare querys
                        			//Delete week AND all shifts linked to week
                        		String query = "DELETE FROM " + railDBHelper.weekTable + 
                        						" WHERE " + railDBHelper.weekId +" ="+_id;
                        		String query2 = "DELETE FROM "+railDBHelper.shiftTable+
                        						" WHERE "+railDBHelper.shiftWeekId+" = "+_id;
                        		try {
                        			db.execSQL(query);
                        			db.execSQL(query2);
                        			//notify on success
                        			Toast.makeText(ViewWeek.this, R.string.delete_week_success, Toast.LENGTH_SHORT).show();
                        			dialog.dismiss();
                            		ViewWeek.this.setResult(RESULT_OK, null);
                            		finish();
                        		} catch (SQLException e) {
                        			e.printStackTrace();
                        			//notify on failure
                        			Toast.makeText(ViewWeek.this, R.string.delete_week_failed, Toast.LENGTH_SHORT).show();
                        			dialog.dismiss();
                        		}
                            }
                        });
                builder.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            	//Do NOTHING but dismiss dialog if no is clicked
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();            	
                return true;
            }
        });

		
		
		
		
		//Add link to Preferences Screen on dropdown
		mItem = subMenu1.add(R.string.menu_settings);
		mItem.setIcon(R.drawable.ic_settings);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//Start 'Preferences' Activity when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
                Intent mIntent = new Intent(ViewWeek.this,
                		 PreferencesActivity.class); 
                startActivity(mIntent);
                return true;
            }
        });
		return true;
	}
}
