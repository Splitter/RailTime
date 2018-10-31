package com.splitv.railtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.DatePickerDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.AdapterView;
import org.holoeverywhere.widget.AdapterView.OnItemSelectedListener;
import org.holoeverywhere.widget.DatePicker;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Spinner;
import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.content.ContentValues;
import android.content.Intent;


public class AddShift extends SherlockFragmentActivity {
	//Fragment for datepicker
		public static class DatePickerFragment extends SherlockDialogFragment
										implements DatePickerDialog.OnDateSetListener {		
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				//Get current value of EditText field
				String[] date = ((AddShift)getActivity()).getCurFieldText().split("\\s+");
				
				//get values to send to datepicker
				int year = Integer.parseInt(date[2]);
				int day = Integer.parseInt(date[1]);
				Calendar cal = Calendar.getInstance();
				try {				
					cal.setTime(new SimpleDateFormat("MMM").parse(date[0]));				
				} catch (ParseException e) {	
					e.printStackTrace();				
				}
				int monthInt = cal.get(Calendar.MONTH);			
				
				// Create a new instance of DatePickerDialog and return it
				DatePickerDialog DPD = new DatePickerDialog(getActivity(), this, year, monthInt, day);
				DatePicker view =  DPD.getDatePicker();
				String[] limits = ((AddShift)getActivity()).getCurLimits();
				String[] min = limits[1].split("\\s+");
				String[] max = limits[0].split("\\s+");
				
				
				int year_calc = Integer.parseInt(min[2]);
				int day_calc = Integer.parseInt(min[1]);
				cal = Calendar.getInstance();
				try {				
					cal.setTime(new SimpleDateFormat("MMM").parse(min[0]));				
				} catch (ParseException e) {	
					e.printStackTrace();				
				}
				int month_calc = cal.get(Calendar.MONTH);	
				cal.set(year_calc,month_calc,day_calc);
				long minDate = cal.getTimeInMillis();
				view.setMinDate(minDate);
				

				year_calc = Integer.parseInt(max[2]);
				day_calc = Integer.parseInt(max[1]);
				try {				
					cal.setTime(new SimpleDateFormat("MMM").parse(max[0]));				
				} catch (ParseException e) {	
					e.printStackTrace();				
				}
				month_calc = cal.get(Calendar.MONTH);	
				cal.set(year_calc,month_calc,day_calc);
				long maxDate = cal.getTimeInMillis();
				view.setMaxDate(maxDate);				
				return DPD;
			}		
			public void onDateSet(DatePicker view, int year, int month, int day) {

				((AddShift)getActivity()).updateCurFieldText(month, day, year);
				
			}	
		}
	
	
	
	
	
	
	private View mView;
	private static String shiftType;
	private String _id;
	private String _weekId;
	private String startDate;
	private String endDate;
	private railDBHelper helper;
	private String mQuery;
	private String _mCount = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView=LayoutInflater.inflate(this, R.layout.activity_add_shift);
		//Must set layout before manipulating it
		setContentView(mView);

		Bundle bundle   = getIntent().getExtras();
		_id  = bundle.getString("com.splitv.railtime.shiftWeekId");
		_mCount  = bundle.getString("com.splitv.railtime.curCount");
		
		//int id = Integer.getInteger(_id);
		
		helper = new railDBHelper(this);
		mQuery="SELECT "+railDBHelper.weekId+" as _id, "+railDBHelper.weekEnd+", "+
				railDBHelper.weekStart+" FROM "+railDBHelper.weekTable +" WHERE "+railDBHelper.weekId+" = "+_id;
		
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor dbResult = db.rawQuery(mQuery, null);dbResult.moveToFirst();
		startDate = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekStart));
		endDate = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekEnd));
		
		
		
		
		
		
		Spinner type = (Spinner)mView.findViewById(R.id.AddShiftTypes);
		type.setOnItemSelectedListener(
    	         new OnItemSelectedListener() {
    	              public void onNothingSelected(AdapterView<?> arg0) { }
    	              public void onItemSelected(AdapterView<?> parent, View v,
    	                int position, long id) {
    	                 // Code that does something when the Spinner value changes
    	            	  AddShift.shiftType = (String) parent.getItemAtPosition(position).toString();
    	            	  //Toast.makeText(AddShift.this,shiftType,Toast.LENGTH_LONG).show();
    	              }
    	         });
    	    
		
		
		//Get Current Date data
				//Set Date and Add onclick handler to startweek field
				EditText shiftDate = (EditText) mView.findViewById(R.id.shift_date);
					//set date to current date
				shiftDate.setText(startDate);
					//set onclick
				shiftDate.setOnClickListener(new View.OnClickListener(){
			        @Override
			        public void onClick(View v) {
			        	//show the date picker dialog(datepickerfragment)
			        	showDatePickerDialog(v);	        	
			        }
			    });


	}
	//Gets the current value of fields
		//used exlusively by datepickerfragment
		public String getCurFieldText(){
			EditText field = (EditText) mView.findViewById(R.id.shift_date);		
			return (String) field.getText().toString();	
		}
		
		public String[] getCurLimits(){		
			return new String[]{endDate,startDate};	
		}
		


		//Sets the current value of fields
		//used exlusively by datepickerfragment
		public void updateCurFieldText(int month, int day, int year){
			
			final Calendar c = Calendar.getInstance();
			c.set(year,month,day);
			SimpleDateFormat month_date = new SimpleDateFormat("MMM");
			String month_name = month_date.format(c.getTime());	
			EditText field = (EditText) mView.findViewById(R.id.shift_date);		
			field.setText(month_name+" "+day+" "+year);	
			
		}
	
		//Show datepicker
		public void showDatePickerDialog(View v) {
		    DialogFragment newFragment = new DatePickerFragment();
		    newFragment.show(getSupportFragmentManager(), "datePicker");
		}
		
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Add 'add week' item to action bar on main screen
				MenuItem miPrefs = menu.add(R.string.add_shift_save);
				miPrefs.setIcon(R.drawable.ic_save);
			    miPrefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				
			    //Start 'AddWeek' Activity when menu item clicked
			    miPrefs.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
		            @Override
		            public boolean onMenuItemClick(MenuItem item) { 
		            	//shiftType
		            	

		        		EditText field =(EditText)mView.findViewById(R.id.shift_date);
		        		String shiftDate = (String) field.getText().toString();

		        		field =(EditText)mView.findViewById(R.id.additional_notes);
		        		String notes = (String) field.getText().toString();
		        		
		        		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddShift.this);
		        		Calendar prefStartC = Calendar.getInstance();
		        		Long prefStart = prefs.getLong("pref_start_time", 0);
		        		//start time string
		        		String time = "5:00 AM";
		        		
		        		if( prefStart!= 0){
		        			prefStartC.setTimeInMillis( prefStart );
		        			int hour = (prefStartC.get(Calendar.HOUR)==0?12:prefStartC.get(Calendar.HOUR));
		        			int minute = prefStartC.get(Calendar.MINUTE);

		        			int pm = prefStartC.get(Calendar.AM_PM);
		        			String AM_PM = "AM";
		        	        if (pm == 1)
		        	        {
		        	            AM_PM = "PM";
		        	        }
		        	        
		        	        time = String.valueOf(hour)+":"+pad(minute)+" "+AM_PM;
		        	        
		        		}
		        		
		        		//timestamp
		        		final Calendar c = Calendar.getInstance();
		        		String timestamp = String.valueOf(c.getTimeInMillis());
		        		//Setup DB helper
		        		railDBHelper helper = new railDBHelper(AddShift.this);
		        		//get db instance
		        		SQLiteDatabase db = helper.getWritableDatabase();     
		        		
		        		try {
		        			  ContentValues values = new ContentValues();
		        			  values.put(railDBHelper.shiftTimestamp, timestamp);
		        			  values.put(railDBHelper.shiftNotes, notes);
		        			  values.put(railDBHelper.shiftStart, time);
		        			  values.put(railDBHelper.shiftEnd, "");
		        			  values.put(railDBHelper. shiftHours, "0");
		        			  values.put(railDBHelper.shiftDate, shiftDate);
		        			  values.put(railDBHelper.shiftType, shiftType);
		        			  values.put(railDBHelper.shiftWeekId, _id);
		        			  long nShiftId = db.insert(railDBHelper.shiftTable , null, values);
		        			  
		        			  if(Integer.valueOf(_mCount)>0){   
		        		        	Intent mIntent = new Intent( AddShift.this, AddActivityDz.class ); 
		        		            mIntent.putExtra( "com.splitv.railtime.shiftId", String.valueOf(nShiftId));
		        		            mIntent.putExtra( "com.splitv.railtime.weekId", _weekId );
		        		            mIntent.putExtra( "com.splitv.railtime.curCount", String.valueOf(_mCount) );
		        		            startActivity(mIntent); 
		  		        		
		        		            //exit activity
		        		            AddShift.this.setResult(RESULT_OK, null);
		        		            finish();   
		        			  }

		        			//notify on success
		        			Toast.makeText(AddShift.this, R.string.saved_shift_data, Toast.LENGTH_SHORT).show();
		        			
		        		} catch (SQLException e) {

		        			e.printStackTrace();
		        			//notify on failure
		        			Toast.makeText(AddShift.this, R.string.saved_shift_data_failed, Toast.LENGTH_SHORT).show();
		        			
		        		}    
		        		
		        		//exit activity
		        		AddShift.this.setResult(RESULT_OK, null);
		        		finish();   
		                return true;
		            }
		        });
				return true;
	}
	

	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}

}
