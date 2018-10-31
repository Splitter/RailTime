package com.splitv.railtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.DatePickerDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.DatePicker;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.splitv.railtime.railDBHelper;

public class AddWeek extends SherlockFragmentActivity {

	//Fragment for datepicker
	public static class DatePickerFragment extends SherlockDialogFragment
									implements DatePickerDialog.OnDateSetListener {		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			//Get current value of EditText field
			String[] date = ((AddWeek)getActivity()).getCurFieldText().split("\\s+");
			
			//get values to send to datepicker
			int year = Integer.parseInt(date[2]);
			int day = Integer.parseInt(date[1]);
			//Convert Month string to integer value
			Calendar cal = Calendar.getInstance();
			try {				
				cal.setTime(new SimpleDateFormat("MMM").parse(date[0]));				
			} catch (ParseException e) {	
				e.printStackTrace();				
			}
			int monthInt = cal.get(Calendar.MONTH);			
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, monthInt, day);
		}		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Call Main Activity to update EditText fields of form
			((AddWeek)getActivity()).updateCurFieldText(month, day, year);
		}	
	}
		
	//The Edittext with current focus
	private String curOpt ="startWeek";
	//Copy of the main view
	private View mView;
	private String _mCount = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView=LayoutInflater.inflate(this, R.layout.activity_add_week);
		//Must set layout before manipulating it
		setContentView(mView);

		Bundle bundle   = getIntent().getExtras();
		_mCount  = bundle.getString("com.splitv.railtime.curCount");
		
		//Get Current Date data & set the editText fields accordingly
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int day = c.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat month_date = new SimpleDateFormat("MMM");
		String month_name = month_date.format(c.getTime());
		

		//Set Date and Add onclick handler to startweek field
		EditText startWeek = (EditText) mView.findViewById(R.id.startweek);
			//set date to current date
		startWeek.setText(month_name+" "+day+" "+year);
			//set onclick
		startWeek.setOnClickListener(new View.OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	//set cur field
	        	curOpt ="startWeek";
	        	//show the date picker dialog(datepickerfragment)
	        	showDatePickerDialog(v);	        	
	        }
	    });
		
		

		//Make EndWeek field 6 working days later then what we set startweek to
			//reset calendar to 12am so we can add 6 days and Always get a 7 day work week
		c.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
		c.set(Calendar.MINUTE, 0);                 // set minute in hour
		c.set(Calendar.SECOND, 0);                 // set second in minute
		c.set(Calendar.MILLISECOND, 0);            // set millisecond in second		
		//Get current value in milliseconds(12am today)
			//Add 6 days(in milliseconds) to get our 7 day work week
		long start_mil = c.getTimeInMillis();			
		long end_mil = start_mil+518400000;
		//set calender to calculated value
		c.setTimeInMillis(end_mil);
		//Get values from our newly adjusted calendar
		year = c.get(Calendar.YEAR);
		day = c.get(Calendar.DAY_OF_MONTH);
		month_date = new SimpleDateFormat("MMM");
		month_name = month_date.format(c.getTime());
		
		//Set Date and Add onclick handler to endweek field
		EditText endWeek = (EditText) mView.findViewById(R.id.endweek);
			//set date to current date + 7 days
		//day+=6;
		endWeek.setText(month_name+" "+day+" "+year);
			//set onclick
		endWeek.setOnClickListener(new View.OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	//set cur field
	        	curOpt ="endWeek";
	        	//show the date picker dialog(datepickerfragment)
	        	showDatePickerDialog(v);	        	
	        }
	    });		
	}

	//Gets the current value of fields
	//used exlusively by datepickerfragment
	public String getCurFieldText(){
		EditText field;
		if(curOpt=="startWeek"){
			field = (EditText) mView.findViewById(R.id.startweek);		
		}
		else{
			field = (EditText) mView.findViewById(R.id.endweek);	
		}
		return (String) field.getText().toString();	
	}
	

	//Universal function to set EditText date fields
		//This ensures both fields are always within 6 days(7 working days) of each other
	public void updateCurFieldText(int month, int day, int year){

		final Calendar start = Calendar.getInstance();
		final Calendar end = Calendar.getInstance();
		//Always make sure Start/End week values are within 7 working days of each other 
		if(curOpt=="startWeek"){
			start.set(year,month,day); 
			start.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
			start.set(Calendar.MINUTE, 0);                 // set minute in hour
			start.set(Calendar.SECOND, 0);                 // set second in minute
			start.set(Calendar.MILLISECOND, 0);            // set millisecond in second			
			long start_mil = start.getTimeInMillis();			
			long end_mil = start_mil+518400000;
			end.setTimeInMillis(end_mil);
		}
		else{
			end.set(year,month,day); 
			end.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
			end.set(Calendar.MINUTE, 0);                 // set minute in hour
			end.set(Calendar.SECOND, 0);                 // set second in minute
			end.set(Calendar.MILLISECOND, 0);            // set millisecond in second			
			long end_mil = end.getTimeInMillis();			
			long start_mil = end_mil-518400000;
			start.setTimeInMillis(start_mil);
		}
		//Get start date values
		int start_year = start.get(Calendar.YEAR);
		int start_day = start.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat month_date = new SimpleDateFormat("MMM");
		String start_month = month_date.format(start.getTime());
		//Get end date values
		int end_year = end.get(Calendar.YEAR);
		int end_day = end.get(Calendar.DAY_OF_MONTH);
		month_date = new SimpleDateFormat("MMM");
		String end_month = month_date.format(end.getTime());
		//get handles to EditText fields
		EditText sWeek =  (EditText) mView.findViewById(R.id.startweek);
		EditText eWeek = (EditText) mView.findViewById(R.id.endweek);
		//set the values of both text fields everytime one is adjusted
			//to keep them within 7 day work week
		sWeek.setText(start_month+" "+start_day+" "+start_year);
		eWeek.setText(end_month+" "+end_day+" "+end_year);
	}
	
	//Utility function to show datepicker
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Add 'add week' item to action bar on main screen
		MenuItem miPrefs = menu.add(R.string.save_week);
		miPrefs.setIcon(R.drawable.ic_save);
	    miPrefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
	    //Start 'AddWeek' Activity when menu item clicked
	    miPrefs.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) { 
            	
            	//Get all data to store in DB
        		EditText field =(EditText)mView.findViewById(R.id.startweek);
        		String startWeek = (String) field.getText().toString();
        		field =(EditText)mView.findViewById(R.id.endweek);
        		String endWeek = (String) field.getText().toString();
        		field =(EditText)mView.findViewById(R.id.additional_notes);
        		String notes = (String) field.getText().toString();        		
        		final Calendar c = Calendar.getInstance();        		
        		String timestamp = String.valueOf(c.getTimeInMillis());

        		//Setup DB helper
        		railDBHelper helper = new railDBHelper(AddWeek.this);
        		//get db instance
        		SQLiteDatabase db = helper.getWritableDatabase(); 


  			  ContentValues values = new ContentValues();
  			  values.put(railDBHelper.weekTimestamp, timestamp);
  			  values.put(railDBHelper.weekNotes, notes);
  			  values.put(railDBHelper.weekStart, startWeek);
  			  values.put(railDBHelper.weekEnd, endWeek);
  			  values.put(railDBHelper.weekHours, "0");
        		//attempt db insert
        		try {
        			long mWeekId = db.insert(railDBHelper.weekTable , null, values);
        			//notify on success
        			Toast.makeText(AddWeek.this, R.string.saved_week_data, Toast.LENGTH_SHORT).show();
        			if(Integer.valueOf(_mCount)>0){
        				Intent mIntent = new Intent( AddWeek.this, AddShift.class ); 
                        mIntent.putExtra( "com.splitv.railtime.shiftWeekId", String.valueOf(mWeekId) );
                        mIntent.putExtra( "com.splitv.railtime.curCount", String.valueOf(_mCount) );
                        startActivity(mIntent);
                        finish();						
        			}
        		} catch (SQLException e) {
        			e.printStackTrace();
        			//notify on failure
        			Toast.makeText(AddWeek.this, R.string.saved_week_data_failed, Toast.LENGTH_SHORT).show();
        		}     	 
        		//exit activity
        		finish();   
                return true;
            }
        });
		return true;
	}
}
