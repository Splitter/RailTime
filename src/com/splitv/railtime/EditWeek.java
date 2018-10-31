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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.splitv.railtime.railDBHelper;

public class EditWeek extends SherlockFragmentActivity {

	//Fragment for datepicker
	public static class DatePickerFragment extends SherlockDialogFragment
									implements DatePickerDialog.OnDateSetListener {		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			//Get current value of EditText field
			String[] date = ((EditWeek)getActivity()).getCurFieldText().split("\\s+");
			
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
			((EditWeek)getActivity()).updateCurFieldText(month, day, year);
		}	
	}

	
	private String curOpt ="startWeek";//The Edittext with current focus	
	private View mView;//Save instance of main view
	private railDBHelper helper;
	private String _id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView=LayoutInflater.inflate(this, R.layout.activity_add_week);
		//Must set layout before manipulating it
		setContentView(mView);
		helper = new railDBHelper(this);
		Bundle bundle   = getIntent().getExtras();
		_id  = bundle.getString("com.splitv.railtime.editWeekId");
		
		//Get data to populate fields with
		String mQuery="SELECT "+railDBHelper.weekId+" as _id, "+railDBHelper.weekEnd+", "+
				railDBHelper.weekHours+", "+railDBHelper.weekStart+", "+railDBHelper.weekHours+
				", "+railDBHelper.weekNotes+" FROM "+railDBHelper.weekTable +" WHERE "+railDBHelper.weekId+" = "+_id;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor dbResult = db.rawQuery(mQuery, null);
		
		//parse the data
		dbResult.moveToFirst();
		String weekStart = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekStart));
		String weekEnd = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekEnd));
		String weekNotes = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekNotes));
		//ONLY set the notes if there is a value, so the 'hint' is not overwritten
		if(weekNotes != null && !weekNotes.isEmpty()){
			EditText additionalNotes = (EditText) mView.findViewById(R.id.additional_notes);
			additionalNotes.setText(weekNotes);
		}
		
		
		//Set Date and Add onclick handler to startweek field
		EditText startWeek = (EditText) mView.findViewById(R.id.startweek);
			//set date to current date
		startWeek.setText(weekStart);
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
		
		//Set Date and Add onclick handler to endweek field
		EditText endWeek = (EditText) mView.findViewById(R.id.endweek);
			//set date to current date + 7 days
		//day+=6;
		endWeek.setText(weekEnd);
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
	

	//Sets the current value of fields
		//Make sure dates are within 7 working days of each other
		//used exlusively by datepickerfragment
	public void updateCurFieldText(int month, int day, int year){

		final Calendar start = Calendar.getInstance();
		final Calendar end = Calendar.getInstance();
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
		
		int start_year = start.get(Calendar.YEAR);
		int start_day = start.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat month_date = new SimpleDateFormat("MMM");
		String start_month = month_date.format(start.getTime());
		
		int end_year = end.get(Calendar.YEAR);
		int end_day = end.get(Calendar.DAY_OF_MONTH);
		month_date = new SimpleDateFormat("MMM");
		String end_month = month_date.format(end.getTime());
		
		EditText sWeek =  (EditText) mView.findViewById(R.id.startweek);
		EditText eWeek = (EditText) mView.findViewById(R.id.endweek);

		sWeek.setText(start_month+" "+start_day+" "+start_year);
		eWeek.setText(end_month+" "+end_day+" "+end_year);
	}
	
	//Show datepicker
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Add 'add week' item to action bar on main screen
		MenuItem miPrefs = menu.add(R.string.save_week);
		miPrefs.setIcon(R.drawable.ic_save);
	    miPrefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
	    //Start 'EditWeek' Activity when menu item clicked
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
        		
        		
        		//Setup DB helper
        		railDBHelper helper = new railDBHelper(EditWeek.this);
        		//get db instance
        		SQLiteDatabase db = helper.getWritableDatabase();     
        		//prepare query
        		String strFilter = railDBHelper.weekId+"=" + _id;
        		ContentValues args = new ContentValues();
        		args.put(railDBHelper.weekNotes, notes);
        		args.put(railDBHelper.weekStart, startWeek);
        		args.put(railDBHelper.weekEnd, endWeek);
        		
        		
        		if(db.update(railDBHelper.weekTable, args, strFilter, null)>0){
        			Toast.makeText(EditWeek.this, R.string.saved_week_data, Toast.LENGTH_SHORT).show();
        			
        		}
        		else{
        			Toast.makeText(EditWeek.this, R.string.saved_week_data_failed, Toast.LENGTH_SHORT).show();
        			
        		}
        		
        		//exit activity
        		finish();   
                return true;
            }
        });
		return true;
	}
}
