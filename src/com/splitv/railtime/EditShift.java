package com.splitv.railtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.DatePickerDialog;
import org.holoeverywhere.app.Dialog;
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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;


public class EditShift extends SherlockFragmentActivity {
	//Fragment for datepicker
		public static class DatePickerFragment extends SherlockDialogFragment
										implements DatePickerDialog.OnDateSetListener {		
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				//Get current value of EditText field
				String[] date = ((EditShift)getActivity()).getCurFieldText().split("\\s+");
				
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
				String[] limits = ((EditShift)getActivity()).getCurLimits();
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

				((EditShift)getActivity()).updateCurFieldText(month, day, year);
				
			}	
		}
	
	
	
	
	
	
	private View mView;
	private static String shiftType;
	private String _id;
	private String _weekId;
	private String shiftDate;
	private String shiftNotes;
	private String startDate;
	private String endDate;
	private railDBHelper helper;
	private String mQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView=LayoutInflater.inflate(this, R.layout.activity_edit_shift);
		//Must set layout before manipulating it
		setContentView(mView);

		Bundle bundle   = getIntent().getExtras();
		_id  = bundle.getString("com.splitv.railtime.editShiftId");
		_weekId  = bundle.getString("com.splitv.railtime.editWeekId");
		//int id = Integer.getInteger(_id);
		
		helper = new railDBHelper(this);
		mQuery="SELECT "+railDBHelper.shiftId+" as _id, "+railDBHelper.shiftDate+", "+railDBHelper.shiftType+", "+
				railDBHelper.shiftNotes+" FROM "+railDBHelper.shiftTable +" WHERE "+railDBHelper.shiftId+" = "+_id;
		
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor dbResult = db.rawQuery(mQuery, null);dbResult.moveToFirst();
		shiftType = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftType));
		shiftDate = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftDate));
		shiftNotes = dbResult.getString(dbResult.getColumnIndex(railDBHelper.shiftNotes));
		
		
		mQuery="SELECT "+railDBHelper.weekId+" as _id, "+railDBHelper.weekEnd+", "+
				railDBHelper.weekStart+" FROM "+railDBHelper.weekTable +" WHERE "+railDBHelper.weekId+" = "+_weekId;
		db = helper.getReadableDatabase();
		dbResult = db.rawQuery(mQuery, null);dbResult.moveToFirst();
		startDate = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekStart));
		endDate = dbResult.getString(dbResult.getColumnIndex(railDBHelper.weekEnd));
		
		
		
		
		Spinner type = (Spinner)mView.findViewById(R.id.EditShiftTypes);
		
		ArrayAdapter myAdap = (ArrayAdapter) type.getAdapter(); //cast to an ArrayAdapter

		int spinnerPosition = myAdap.getPosition(shiftType);

		//set the default according to value
		type.setSelection(spinnerPosition);
		
		
		type.setOnItemSelectedListener(
    	         new OnItemSelectedListener() {
    	              public void onNothingSelected(AdapterView<?> arg0) { }
    	              public void onItemSelected(AdapterView<?> parent, View v,
    	                int position, long id) {
    	                 // Code that does something when the Spinner value changes
    	            	  EditShift.shiftType = (String) parent.getItemAtPosition(position).toString();
    	            	  //Toast.makeText(EditShift.this,shiftType,Toast.LENGTH_LONG).show();
    	              }
    	         });
    	    
		
		
		//Get Current Date data
				//Set Date and Add onclick handler to startweek field
				EditText dateShift = (EditText) mView.findViewById(R.id.shift_date);
					//set date to current date
				dateShift.setText(shiftDate);
					//set onclick
				dateShift.setOnClickListener(new View.OnClickListener(){
			        @Override
			        public void onClick(View v) {
			        	//show the date picker dialog(datepickerfragment)
			        	showDatePickerDialog(v);	        	
			        }
			    });
				

				EditText notesShift = (EditText) mView.findViewById(R.id.additional_notes);
					//set date to current date
				notesShift.setText(shiftNotes);


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
		        		
		        		//Setup DB helper
		        		railDBHelper helper = new railDBHelper(EditShift.this);
		        		//get db instance
		        		SQLiteDatabase db = helper.getWritableDatabase();     
		        		//prepare query

		    			String strFilter = railDBHelper.shiftId+"=" + _id;
		    			ContentValues args = new ContentValues();   
		        		args.put(railDBHelper.shiftType, shiftType);		
		        		args.put(railDBHelper.shiftDate, shiftDate);		
		        		args.put(railDBHelper.shiftNotes, notes);	

		    			if(db.update(railDBHelper.shiftTable, args, strFilter, null)>0){
		        			Toast.makeText(EditShift.this,  R.string.saved_shift_data, Toast.LENGTH_SHORT).show();
		        			
		        		}
		        		else{
		        			Toast.makeText(EditShift.this, R.string.saved_shift_data_failed, Toast.LENGTH_SHORT).show();
		        			
		        		}

		        		//exit activity
		        		EditShift.this.setResult(RESULT_OK, null);
		        		finish();   
		                return true;
		            }
		        });
				return true;
	}
	

	

}
