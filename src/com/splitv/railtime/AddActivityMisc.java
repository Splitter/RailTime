package com.splitv.railtime;

import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.TimePickerDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TimePicker;
import org.holoeverywhere.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

public class AddActivityMisc extends SherlockFragmentActivity {

	public static class TimePickerFragment extends SherlockDialogFragment
									implements TimePickerDialog.OnTimeSetListener {		

		public Dialog onCreateDialog(Bundle savedInstanceState) {


			//Get Current Date data & set the editText fields accordingly
			final Calendar c = Calendar.getInstance();

			String time = ((AddActivityMisc)getActivity()).getFieldText();
			if(time != null && !time.isEmpty()){
				String[] data = {time.split(":")[0],time.split(":")[1].split("\\s+")[0],time.split(":")[1].split("\\s+")[1]};
				
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
			if(((AddActivityMisc)getActivity()).curOpt=="startActivity"){
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
			((AddActivityMisc)getActivity()).updateTime(hour,minute);
			
		}
	
	}
	
	
	private String _shiftId;
	private View mView;
	private String curOpt ="startActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView=LayoutInflater.inflate(this, R.layout.activity_add_activity_misc);
		//Must set layout before manipulating it
		setContentView(mView);

		Bundle bundle   = getIntent().getExtras();
		_shiftId  = bundle.getString("com.splitv.railtime.shiftId");
		EditText startActivity = (EditText) mView.findViewById(R.id.add_activity_start_time);

		//set onclick
		startActivity.setOnClickListener(new View.OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	//set cur field
	        	curOpt ="startActivity";
	        	//show the time picker dialog(timepickerfragment)
	        	showTimePickerDialog(v);	        	
	        }
	    });		
		
		//Set Date and Add onclick handler to endweek field
		EditText endActivity = (EditText) mView.findViewById(R.id.add_activity_end_time);
	
			//set onclick
		endActivity.setOnClickListener(new View.OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	//set cur field
	        	curOpt ="endActivity";
	        	//show the time picker dialog(timepickerfragment)
	        	showTimePickerDialog(v);	        	
	        }
		});	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem miPrefs = menu.add(R.string.add_rail_save);
		miPrefs.setIcon(R.drawable.ic_save);
	    miPrefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

	    miPrefs.setOnMenuItemClickListener(new OnMenuItemClickListener() {	    	 
            @Override
            public boolean onMenuItemClick(MenuItem item) {

        		EditText field =(EditText)mView.findViewById(R.id.add_activity_start_time);
        		String railStart = (String) field.getText().toString();
            	
            	field =(EditText)mView.findViewById(R.id.add_activity_end_time);
        		String railEnd = (String) field.getText().toString();
        		float rHours = 0;
        		if((railStart != null && !railStart.isEmpty()) && (railEnd != null && !railEnd.isEmpty())){
        			
        			String[] railStartD = {railStart.split(":")[0],railStart.split(":")[1].split("\\s+")[0],railStart.split(":")[1].split("\\s+")[1]};
        			String[] railEndD = {railEnd.split(":")[0],railEnd.split(":")[1].split("\\s+")[0],railEnd.split(":")[1].split("\\s+")[1]};
        			
        			if((railStartD[2].equals("AM") && railEndD[2].equals("AM"))||(railStartD[2].equals("PM") && railEndD[2].equals("PM"))){
        				if(railStartD[0].equals("12")){
        					railStartD[0] = "0";
        				}

        				if(railEndD[0].equals("12")){
        					railEndD[0] = "0";
        				}
        				float hours = (Float.parseFloat(railEndD[0]) -  Float.parseFloat(railStartD[0]))-1;
        				int sMins = 60 - Integer.parseInt(railStartD[1]);
        				int eMins = Integer.parseInt(railEndD[1]);
        				float mins = (float)(sMins+eMins)/60;
        				rHours = hours + mins;    
        			}
        			else if((railStartD[2].equals("AM") && railEndD[2].equals("PM"))||(railStartD[2].equals("PM") && railEndD[2].equals("AM"))){
        				
        				float hours;
        				if(railStartD[0].equals("12")){
        					railStartD[0] = "0";
        				}
        				if(railEndD[0].equals("12")){
        					hours = (Float.parseFloat(railEndD[0]) -  Float.parseFloat(railStartD[0]))-1;
        				}
        				else{
        					hours = ((Float.parseFloat(railEndD[0])+12) - Float.parseFloat(railStartD[0]))-1;
        				}
        				
        				int sMins = 60 - Integer.parseInt(railStartD[1]);
        				int eMins = Integer.parseInt(railEndD[1]);
        				float mins = (float)(sMins+eMins)/60;
        				rHours = hours + mins;
        			}
        		}

        		field =(EditText)mView.findViewById(R.id.additional_notes);
        		String notes = (String) field.getText().toString();
        		if(editTextEmpty(field)){
            		Toast.makeText(AddActivityMisc.this, R.string.add_activity_notes_fail, Toast.LENGTH_SHORT).show();
        			return true;
            	}        		

        		String type = getString(R.string.shift_activity_misc);
        		
        		String timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
        		
        		Float aHours = (float) 0;
        		
        		String activityHours = String.valueOf((rHours > aHours)?rHours:aHours);
        		

        		//Setup DB helper
        		railDBHelper helper = new railDBHelper(AddActivityMisc.this);
        		//get db instance
        		SQLiteDatabase db = helper.getWritableDatabase();     
        		
        		//prepare query
        		String query = "INSERT INTO " + railDBHelper.activityTable + 
        						" ( " + railDBHelper.activityStart +" , " + railDBHelper.activityEnd +
        						" , " + railDBHelper.activityShiftId +" , " + railDBHelper.activityHours +
        						" , " + railDBHelper.activityNotes +" , " + railDBHelper.activityTimestamp +
        						" , " + railDBHelper.activityType  +" ) "+
        						" VALUES ( ? , ? , ? , ? , ? , ? , ? )";
            	
        		try {
        			
        			db.execSQL(query, new String[]{railStart,railEnd,_shiftId,activityHours,notes,timestamp,type});
        			//notify on success
        			Toast.makeText(AddActivityMisc.this, R.string.saved_rail_data, Toast.LENGTH_SHORT).show();
        			
        		} catch (SQLException e) {

        			e.printStackTrace();
        			//notify on failure
        			Toast.makeText(AddActivityMisc.this, R.string.saved_rail_data_failed, Toast.LENGTH_SHORT).show();
        			
        		}    

        		//exit activity
        		AddActivityMisc.this.setResult(RESULT_OK, null);
        		finish();  
				return true; 
            }
	    });
	    
	    
	    
	    
	    

		return true;
	}
	
	
	public String getFieldText(){
		if(curOpt=="startActivity"){
    		EditText start = (EditText) mView.findViewById(R.id.add_activity_start_time);
    		return (String) start.getText().toString();
		}
		else{			
			EditText end = (EditText) mView.findViewById(R.id.add_activity_end_time);
			return  (String) end.getText().toString();
		}
		
	}

	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
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
        
		if(curOpt=="startActivity"){
    		EditText start = (EditText) mView.findViewById(R.id.add_activity_start_time);
    		start.setText(nTime);
		}
		else{			
			EditText end = (EditText) mView.findViewById(R.id.add_activity_end_time);
			end.setText(nTime);
		}	
	}
	//Utility function to show datepicker
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "timePicker");
	}
	
	private boolean editTextEmpty(EditText edit) {
	    boolean empty = true; 

	    if (edit.getText().toString().trim().length() > 0) {
	        empty = false;
	    }
	    return empty;
	}
	

}
