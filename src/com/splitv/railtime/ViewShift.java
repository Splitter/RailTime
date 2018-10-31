package com.splitv.railtime;

import java.text.DecimalFormat;
import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.TimePicker;
import org.holoeverywhere.widget.Toast;
import org.holoeverywhere.app.TimePickerDialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.CursorAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;




public class ViewShift extends SherlockFragmentActivity implements OnItemClickListener{
	

	public static class TimePickerFragment extends SherlockDialogFragment
									implements TimePickerDialog.OnTimeSetListener {		

		public Dialog onCreateDialog(Bundle savedInstanceState) {


			//Get Current Date data & set the editText fields accordingly
			final Calendar c = Calendar.getInstance();

			String time = ((ViewShift)getActivity()).getFieldText();
			if(time != null && !time.isEmpty()){
				String[] data = {time.split(":")[0],time.split(":")[1].split("\\s+")[0],time.split(":")[1].split("\\s+")[1]};
				
				//Toast.makeText(((ViewShift)getActivity()), data[0]+" : "+data[1]+" "+data[2], Toast.LENGTH_SHORT).show();
				if(data[0].equals("12")){
					data[0]="0";
				}
				c.set(Calendar.HOUR, Integer.parseInt(data[0]));
				c.set(Calendar.MINUTE, Integer.parseInt(data[1]));
				if(data[2].equals("PM")){
					c.set(Calendar.AM_PM, 1);
				}
				else{
					c.set(Calendar.AM_PM, 0);
				}
				
			}
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minutes = c.get(Calendar.MINUTE);
			TimePickerDialog picker = new TimePickerDialog(getActivity(), this,hour,minutes,false);
			String title = "";
			if(((ViewShift)getActivity()).curOpt=="startShift"){
				title = "Set Start Time";				
			}
			else{
				title = "Set End Time";
			}
			
			picker.setTitle(title);
			return picker;
		}

		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			// TODO Auto-generated method stub
			((ViewShift)getActivity()).updateTime(hour,minute);
			
		}
	
	}
	
	private railDBHelper helper;//Keep copy of sql helper handy
	private activityCursorAdapter activityAdapter; //keep copy of adapter so we can refresh listview
	private ListView mainList;//keep a copy of main listview 
	private String mQueryWeek;//Since query never changes keep a copy
	private String mQueryShift;//Since query never changes keep a copy
	private String mQueryActivities;//Since query never changes keep a copy
	private View mView;//Since view never changes keep a copy
	private String _shiftId;//ID for the currently viewed shift
	private String _weekId;//ID for the currently viewed week
	private String curOpt ="startShift";
	TextView curHours;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		//Inflate proper view
		mView=LayoutInflater.inflate(this, R.layout.activity_view_shift);
		//Must set layout before manipulating it
		setContentView(mView);
		

		//Set all constants needed to refresh/build list
		curHours = (TextView) mView.findViewById(R.id.ShiftTotalHours);
		mainList = (ListView)mView.findViewById(R.id.rail_list);
		mainList.setDividerHeight(0);//get rid of gap in list
		
		mainList.setOnItemClickListener(this);
		
		helper = new railDBHelper(this);
		

		//Get the ID for Currently viewed Shift/Week
		Bundle bundle   = getIntent().getExtras();
		_shiftId  = bundle.getString("com.splitv.railtime.shiftShiftId");
		_weekId  = bundle.getString("com.splitv.railtime.shiftWeekId");
		mQueryShift="SELECT "+railDBHelper.shiftId+" as _id, "+railDBHelper.shiftType+", "+
				railDBHelper.shiftHours+", "+railDBHelper.shiftDate+", "+railDBHelper.shiftNotes+", "+
				railDBHelper.shiftStart+", "+railDBHelper.shiftEnd+
				" FROM "+railDBHelper.shiftTable +" WHERE "+railDBHelper.shiftId+" = "+_shiftId+" ORDER BY "+railDBHelper.shiftTimestamp+" DESC";

		mQueryActivities="SElECT "+railDBHelper.activityId+" as _id, "+railDBHelper.activityStart+", "+railDBHelper.activityEnd+", "+
				railDBHelper.activityHours+", "+railDBHelper.activityLoaders+", "+railDBHelper.activityNotes+", "+railDBHelper.activityShiftId+", "+
				railDBHelper.activityTrack+", "+railDBHelper.activityType+", "+railDBHelper.activityUnits+", "+railDBHelper.activityTimestamp+
				" FROM "+railDBHelper.activityTable+" WHERE "+railDBHelper.activityShiftId+" = "+_shiftId+" ORDER BY "+railDBHelper.activityTimestamp+" DESC";
		//Set Date and Add onclick handler to endweek field
		EditText startShift = (EditText) mView.findViewById(R.id.starttime);

			//set onclick
		startShift.setOnClickListener(new View.OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	//set cur field
	        	curOpt ="startShift";
	        	//show the time picker dialog(timepickerfragment)
	        	showTimePickerDialog(v);	        	
	        }
	    });		
		
		//Set Date and Add onclick handler to endweek field
		EditText endShift = (EditText) mView.findViewById(R.id.endtime);

			//set onclick
		endShift.setOnClickListener(new View.OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	//set cur field
	        	curOpt ="endShift";
	        	//show the time picker dialog(timepickerfragment)
	        	showTimePickerDialog(v);	        	
	        }
	    });		

		//build view for first time
		buildList(false);
		
	}

	@Override
	public void onItemClick(AdapterView<?> l, View v, int position, long id) {
		//Get shiftID of item clicked
		Cursor cur = (Cursor)mainList.getAdapter().getItem(position);
		String _id = cur.getString(cur.getColumnIndex("_id"));

		String type = cur.getString(cur.getColumnIndex(railDBHelper.activityType));
		if(type.equals(ViewShift.this.getString(R.string.shift_activity_rail))){
	        Intent mIntent = new Intent(ViewShift.this,
	                EditActivityRail.class); 
	        mIntent.putExtra("com.splitv.railtime.activityId",_id); 
	        mIntent.putExtra("com.splitv.railtime.activityShiftId",_shiftId); 
	        //start activity
	        startActivity(mIntent);			
		}
		else if(type.equals(ViewShift.this.getString(R.string.shift_activity_dz))){
	        Intent mIntent = new Intent(ViewShift.this,
	                EditActivityDz.class); 
	        mIntent.putExtra("com.splitv.railtime.activityId",_id); 
	        mIntent.putExtra("com.splitv.railtime.activityShiftId",_shiftId); 
	        //start activity
	        startActivity(mIntent);			
		}
		else if(type.equals(ViewShift.this.getString(R.string.shift_activity_misc))){
	        Intent mIntent = new Intent(ViewShift.this,
	                EditActivityMisc.class); 
	        mIntent.putExtra("com.splitv.railtime.activityId",_id); 
	        mIntent.putExtra("com.splitv.railtime.activityShiftId",_shiftId); 
	        //start activity
	        startActivity(mIntent);			
		}
		
		
	}
	
	public String getFieldText(){
		if(curOpt=="startShift"){
    		EditText start = (EditText) mView.findViewById(R.id.starttime);
    		return (String) start.getText().toString();
		}
		else{			
			EditText end = (EditText) mView.findViewById(R.id.endtime);
			return  (String) end.getText().toString();
		}
		
	}

	private class CalculateHours extends AsyncTask<Void, Integer, Float>{
		private float mTotalHours = 0;
		private TextView mView;
		
		public CalculateHours( ){
		}
		@Override
		protected void onPostExecute(Float result) {	

			DecimalFormat newFormat = new DecimalFormat("#.##");
			curHours.setText(String.valueOf(Double.valueOf(newFormat.format(result))));

		}

		@Override
		protected Float doInBackground(Void... params) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor dbResult = db.rawQuery(mQueryActivities, null);
			dbResult.moveToFirst();
	        while (dbResult.isAfterLast() == false) {
	    		float shiftHours = dbResult.getFloat(dbResult.getColumnIndex(railDBHelper.activityHours));
	            mTotalHours += shiftHours;		    		
	    		dbResult.moveToNext();
	        }
	        updateHoursDB(mTotalHours);
			return Float.valueOf(mTotalHours);
		}
	}

	public void updateTime(int hour, int minutes){	

		//Get Current Date data & set the editText fields accordingly
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		
		int nHour = (c.get(Calendar.HOUR)==0?12:c.get(Calendar.HOUR));
		
		int pm = c.get(Calendar.AM_PM);
		String nMinutes = pad(minutes);
		String AM_PM = "AM";
        if (pm == 1)
        {
            AM_PM = "PM";
        }
        
        String nTime = Integer.toString(nHour)+":"+nMinutes+" "+AM_PM;
        
		//get db instance
		SQLiteDatabase db = helper.getWritableDatabase();     
		//prepare query
		String strFilter = railDBHelper.shiftId+"=" + _shiftId;
		ContentValues args = new ContentValues();
		if(curOpt=="startShift"){
    		EditText start = (EditText) mView.findViewById(R.id.starttime);
    		start.setText(nTime);
    		args.put(railDBHelper.shiftStart, nTime);
		}
		else{			
			EditText end = (EditText) mView.findViewById(R.id.endtime);
			end.setText(nTime);
    		args.put(railDBHelper.shiftEnd, nTime);
		}
		db.update(railDBHelper.shiftTable, args, strFilter, null);
        
		new CalculateHours().execute();		
	}

	
	public void updateHoursDB(float mHours){	


		SQLiteDatabase db = helper.getWritableDatabase();  

		
		Cursor dbResult = db.rawQuery(mQueryShift, null);	
		//Parse out start/end time data
		dbResult.moveToFirst();
		String shiftStart = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftStart));
		String shiftEnd = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftEnd));
		//make sure both fields are set so we can calculate hours
		if((shiftStart != null && !shiftStart.isEmpty()) && (shiftEnd != null && !shiftEnd.isEmpty())){


			//parse out data for start/end at hour/minute/am_pm
			String[] shiftStartD = {shiftStart.split(":")[0],shiftStart.split(":")[1].split("\\s+")[0],shiftStart.split(":")[1].split("\\s+")[1]};
			String[] shiftEndD = {shiftEnd.split(":")[0],shiftEnd.split(":")[1].split("\\s+")[0],shiftEnd.split(":")[1].split("\\s+")[1]};
			//Toast.makeText(ViewShift.this,shiftStartD[2]+"-"+shiftEndD[2], Toast.LENGTH_SHORT).show();

			//get db instance
			db = helper.getWritableDatabase();     
			//prepare query
			String strFilter = railDBHelper.shiftId+"=" + _shiftId;
			ContentValues args = new ContentValues();
			
			if((shiftStartD[2].equals("AM") && shiftEndD[2].equals("AM"))||(shiftStartD[2].equals("PM") && shiftEndD[2].equals("PM"))){
				//Toast.makeText(ViewShift.this, "AMAM", Toast.LENGTH_SHORT).show();

				if(shiftStartD[0].equals("12")){
					shiftStartD[0] = "0";
				}

				if(shiftEndD[0].equals("12")){
					shiftEndD[0] = "0";
				}
				float hours = (Float.parseFloat(shiftEndD[0]) -  Float.parseFloat(shiftStartD[0]))-1;
				int sMins = 60 - Integer.parseInt(shiftStartD[1]);
				int eMins = Integer.parseInt(shiftEndD[1]);
				float mins = (float)(sMins+eMins)/60;
				hours = hours + mins;
				hours = (mHours>hours)?mHours:hours;
        		args.put(railDBHelper.shiftHours, hours);			
				
			}
			else if((shiftStartD[2].equals("AM") && shiftEndD[2].equals("PM"))||(shiftStartD[2].equals("PM") && shiftEndD[2].equals("AM"))){
				//Toast.makeText(ViewShift.this, "AMPM", Toast.LENGTH_SHORT).show();
				float hours;
				if(shiftStartD[0].equals("12")){
					shiftStartD[0] = "0";
				}
				if(shiftEndD[0].equals("12")){
					hours = (Float.parseFloat(shiftEndD[0]) -  Float.parseFloat(shiftStartD[0]))-1;
				}
				else{
					hours = ((Float.parseFloat(shiftEndD[0])+12) - Float.parseFloat(shiftStartD[0]))-1;
				}
				
				int sMins = 60 - Integer.parseInt(shiftStartD[1]);
				int eMins = Integer.parseInt(shiftEndD[1]);
				float mins = (float)(sMins+eMins)/60;
				hours = hours + mins;
				hours = (mHours>hours)?mHours:hours;
        		
        		args.put(railDBHelper.shiftHours, hours);
        		
			}
			else{
    		
        		args.put(railDBHelper.shiftHours, (mHours>0)?mHours:0);	
			}
			
			
			
			db.update(railDBHelper.shiftTable, args, strFilter, null);
			
		}else{

			db = helper.getWritableDatabase();     
			//prepare query
			String strFilter = railDBHelper.shiftId+"=" + _shiftId;
			ContentValues args = new ContentValues();
    		args.put(railDBHelper.shiftHours, (mHours>0)?mHours:0);	
    		db.update(railDBHelper.shiftTable, args, strFilter, null);
    		
		}
	}


	
	
	//Helper function to refresh main shift listview
		//set rebuild to false when building list for first time
		//set rebuild to true after initial first build
	public void buildList(boolean rebuild){

		//Query week table for information shown on top of screen
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor dbResult = db.rawQuery(mQueryShift, null);		

		//Parse out data shown on top of screen
		dbResult.moveToFirst();
		String shiftDate = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftDate));
		String shiftType = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftType));
		String shiftNotes = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftNotes));
		String shiftStart = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftStart));
		String shiftEnd = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftEnd));
		String shiftHours = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftHours));


		getSupportActionBar().setTitle(shiftDate);
		getSupportActionBar().setSubtitle(shiftType);

		if(shiftNotes == null || shiftNotes.isEmpty()){
			shiftNotes="n/a";
		}
		TextView notes = (TextView) mView.findViewById(R.id.WeekNotes);
		notes.setText(shiftNotes);
		
		EditText start = (EditText) mView.findViewById(R.id.starttime);
		start.setText(shiftStart);
		
		EditText end = (EditText) mView.findViewById(R.id.endtime);
		end.setText(shiftEnd);
		
		
		//Now populate shift listview in lower portion of screen with data
				dbResult = db.rawQuery(mQueryActivities, null);
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
					activityAdapter.changeCursor(dbResult);
				}
				else{
					activityAdapter = new activityCursorAdapter(ViewShift.this, dbResult, CursorAdapter.NO_SELECTION);
					mainList.setAdapter(activityAdapter);			
				}
		
	}

	//Make sure we are rebuilding list everytime this activity gets focus
	public void onResume(){
		super.onResume();
		buildList(true);
		new CalculateHours().execute();	
	}
	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}

	//Utility function to show datepicker
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "timePicker");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		


		//Add activity dropdown button
        SubMenu activities = menu.addSubMenu(R.string.add_activity);
        MenuItem activities_item = activities.getItem();
        activities_item.setIcon(R.drawable.ic_add);
        //force it to always show
        activities_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        

        //Add link to Rail Track Activty on dropdown
        MenuItem mItem = activities.add(R.string.title_activity_add_railtrack);
		mItem.setIcon(R.drawable.ic_train);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
	          @Override
	          public boolean onMenuItemClick(MenuItem item) { 
	              Intent mIntent = new Intent(ViewShift.this,
	              		 AddActivityRail.class); 
	              mIntent.putExtra("com.splitv.railtime.shiftId",_shiftId);
	              startActivity(mIntent);
	              return true;
	          }
	      });
		
        //Add link to DropZone Activty on dropdown
        mItem = activities.add(R.string.title_activity_add_dropzone);
		mItem.setIcon(R.drawable.ic_car);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
	          @Override
	          public boolean onMenuItemClick(MenuItem item) { 
	              Intent mIntent = new Intent(ViewShift.this,
	              		 AddActivityDz.class); 
	              mIntent.putExtra("com.splitv.railtime.shiftId",_shiftId);
	              mIntent.putExtra("com.splitv.railtime.curCount","0");
	              startActivity(mIntent);
	              return true;
	          }
	      });
		
		
        //Add link to misc Activty on dropdown
        mItem = activities.add(R.string.title_activity_add_misc);
		mItem.setIcon(R.drawable.ic_misc);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
	          @Override
	          public boolean onMenuItemClick(MenuItem item) { 
	              Intent mIntent = new Intent(ViewShift.this,
	              		 AddActivityMisc.class); 
	              mIntent.putExtra("com.splitv.railtime.shiftId",_shiftId);
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
        
        //Add link to EditShift Activty on dropdown
        mItem = subMenu1.add(R.string.edit_shift);
		mItem.setIcon(R.drawable.ic_edit);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
	          @Override
	          public boolean onMenuItemClick(MenuItem item) { 
	              Intent mIntent = new Intent(ViewShift.this,
	              		 EditShift.class); 
	              mIntent.putExtra("com.splitv.railtime.editShiftId",_shiftId); 
	              mIntent.putExtra("com.splitv.railtime.editWeekId",_weekId); 
	              startActivity(mIntent);
	              return true;
	          }
	      });
		
		
		//Add 'delete week' item to dropdown menu
		mItem = subMenu1.add(R.string.delete_shift);
		mItem.setIcon(R.drawable.ic_trash);
		mItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//Delete week when menu item clicked
		//Delete week when menu item clicked
		mItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) {      	
            	//Show a confirmation dialog before deleting week
            	AlertDialog.Builder builder = new AlertDialog.Builder(
                        ViewShift.this);
            	builder.setCancelable(true);
                builder.setTitle(R.string.delete_shift);
                builder.setMessage(R.string.delete_shift_dialog_message);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {                            	
                            	//Setup DB helper
                        		helper = new railDBHelper(ViewShift.this);
                        		//get db instance
                        		SQLiteDatabase db = helper.getWritableDatabase();     
                        		//prepare querys
                        			//Delete week AND all shifts linked to week
                        		String query = "DELETE FROM "+railDBHelper.shiftTable+
                        						" WHERE "+railDBHelper.shiftId+" = "+_shiftId;
                        		String query2 = "DELETE FROM "+railDBHelper.activityTable+
                						" WHERE "+railDBHelper.activityShiftId+" = "+_shiftId;
                        		try {
                        			db.execSQL(query);
                        			db.execSQL(query2);
                        			//notify on success
                        			Toast.makeText(ViewShift.this, R.string.delete_shift_success, Toast.LENGTH_SHORT).show();
                        			dialog.dismiss();
                        			ViewShift.this.setResult(RESULT_OK, null);
                            		finish();
                        		} catch (SQLException e) {
                        			e.printStackTrace();
                        			//notify on failure
                        			Toast.makeText(ViewShift.this, R.string.delete_shift_failed, Toast.LENGTH_SHORT).show();
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
                Intent mIntent = new Intent(ViewShift.this,
                		 PreferencesActivity.class); 
                startActivity(mIntent);
                return true;
            }
        });
				
				
				
				

		
		return true;
	}

}
