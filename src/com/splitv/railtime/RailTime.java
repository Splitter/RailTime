package com.splitv.railtime;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;

import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class RailTime extends SherlockListActivity {

		private railDBHelper helper;//Keep copy of sql helper handy
		private weekCursorAdapter weekAdapter; //keep copy of adapter so we can refresh listview
		private ListView mainList;//keep a copy of main listview 
		private String mQuery;//Since query never changes keep a copy
		private View mView;//Since view never changes keep a copy
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		//Inflate proper view
		mView=LayoutInflater.inflate(this, R.layout.activity_main);
		//Must set layout before manipulating it
		setContentView(mView);

		//Set all constants needed to refresh/build list
		helper = new railDBHelper(this);
		mainList = getListView();
		//get rid of gap in list
		mainList.setDividerHeight(0);
		
		//query to be used when populating listview
		mQuery="SELECT "+railDBHelper.weekId+" as _id, "+railDBHelper.weekEnd+", "+
				railDBHelper.weekHours+", "+railDBHelper.weekStart+" FROM "+railDBHelper.weekTable +" ORDER BY "+railDBHelper.weekTimestamp+" DESC";
		//build list view
		buildList(false);			
	}
	
	//Helper function to refresh main week listview
		//set rebuild to false when building list for first time
		//set rebuild to true after initial first build
	public void buildList(boolean rebuild){
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor dbResult = db.rawQuery(mQuery, null);
			if( dbResult.getCount() > 0 ){
				//Hide 'add a new work week' hint because we already have weeks to display
				ImageView img = (ImageView) mView.findViewById(R.id.AddHintImage);
				img.setVisibility(View.GONE);	
				TextView txt = (TextView) mView.findViewById(R.id.AddHintText);	
				txt.setVisibility(View.GONE);			
			
			}
			else{
				//Show 'add a new work week' hint because there are no existing work weeks
				ImageView img = (ImageView) mView.findViewById(R.id.AddHintImage);
				img.setVisibility(View.VISIBLE);	
				TextView txt = (TextView) mView.findViewById(R.id.AddHintText);	
				txt.setVisibility(View.VISIBLE);		
			}
			if(rebuild){
				weekAdapter.changeCursor(dbResult);
			}
			else{
				weekAdapter = new weekCursorAdapter(RailTime.this, dbResult, CursorAdapter.NO_SELECTION);
				mainList.setAdapter(weekAdapter);
			}
	}
	
	//Make sure we are rebuilding list everytime this activity gets focus
	public void onResume(){
		super.onResume();
		buildList(true);
	}
	
    //Start ViewWeek activity when list item clicked
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Get ID for item clicked
		Cursor cur = (Cursor)mainList.getAdapter().getItem(position);
		String weekId = cur.getString(cur.getColumnIndex("_id"));
		
		//Start ViewWeek Activity
			//Pass the id for week to new activity
		Intent mIntent = new Intent(RailTime.this,
                ViewWeek.class); 
        mIntent.putExtra("com.splitv.railtime.viewWeekId",weekId); 
        startActivity(mIntent);
        
	}
    
	
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Add 'add week' item to action bar on main screen
		
		MenuItem mItem = menu.add(R.string.add_week);
		mItem.setIcon(R.drawable.ic_add);
		//force it to always show
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    //Start 'AddWeek' Activity when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
                Intent mIntent = new Intent(RailTime.this,
                        AddWeek.class); 
                mIntent.putExtra( "com.splitv.railtime.curCount", "0" );
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

		//Add link to Preferences Screen on dropdown
		mItem = subMenu1.add(R.string.menu_settings);
		mItem.setIcon(R.drawable.ic_settings);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//Start 'Preferences' Activity when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
                Intent mIntent = new Intent(RailTime.this,
                		 PreferencesActivity.class); 
                startActivity(mIntent);
                return true;
            }
        });
		
		//Add Link to delete all saved data
			//Used when debugging app
			//TODO:Remove before final build
		mItem = subMenu1.add(R.string.delete_all);
		mItem.setIcon(R.drawable.ic_trash);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//Delete everything when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) {          	
            	//Show a confirmation dialog before deleting everything
            	AlertDialog.Builder builder = new AlertDialog.Builder(
                        RailTime.this);
            	builder.setCancelable(true);
                builder.setTitle(R.string.delete_all);
                builder.setMessage(R.string.delete_all_dialog_message);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {                            	
                            	//Setup DB helper
                        		helper = new railDBHelper(RailTime.this);
                        		//get db instance
                        		SQLiteDatabase db = helper.getWritableDatabase();  
                        		try {
                        			//Just call onUpgrade to make it easy
                        			helper.onUpgrade(db, 1,2);
                        			//notify on success
                        			Toast.makeText(RailTime.this, R.string.delete_all_success, Toast.LENGTH_SHORT).show();
                        			//remove dialog and close db connection
                        			dialog.dismiss();
                        			//rebuild list
                        			buildList(false);
                        		
                        		} catch (SQLException e) {
                        			e.printStackTrace();
                        			//notify on failure
                        			Toast.makeText(RailTime.this, R.string.delete_all_failed, Toast.LENGTH_SHORT).show();
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
	    
		return true;
	}

}
