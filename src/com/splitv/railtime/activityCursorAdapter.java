package com.splitv.railtime;

import java.text.DecimalFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class activityCursorAdapter extends CursorAdapter {
    private Context mContext;
    public activityCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    	mContext = context;
    }
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		String type = cursor.getString(cursor.getColumnIndex(railDBHelper.activityType));
		if(type.equals(mContext.getString(R.string.shift_activity_rail))){
			RelativeLayout rail = (RelativeLayout) view.findViewById(R.id.shift_activity_view_rail);
			RelativeLayout dz = (RelativeLayout) view.findViewById(R.id.shift_activity_view_dz);
			RelativeLayout misc = (RelativeLayout) view.findViewById(R.id.shift_activity_view_misc);
			rail.setVisibility(View.VISIBLE);
			dz.setVisibility(View.GONE);
			misc.setVisibility(View.GONE);
			TextView field = (TextView) view.findViewById(R.id.units);
			field.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.activityUnits)) + " units");

			field = (TextView) view.findViewById(R.id.loaders);
			field.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.activityLoaders)) + " loaders");

			field = (TextView) view.findViewById(R.id.track);
			field.setText("# "+cursor.getString(cursor.getColumnIndex(railDBHelper.activityTrack)));
			
			field = (TextView) view.findViewById(R.id.hoursRail);

			DecimalFormat newFormat = new DecimalFormat("#.##");
			field.setText(newFormat.format(Double.valueOf(cursor.getString(cursor.getColumnIndex(railDBHelper.activityHours)))));	
			
			
		}
		else if(type.equals(mContext.getString(R.string.shift_activity_dz))){
			RelativeLayout rail = (RelativeLayout) view.findViewById(R.id.shift_activity_view_rail);
			RelativeLayout dz = (RelativeLayout) view.findViewById(R.id.shift_activity_view_dz);
			RelativeLayout misc = (RelativeLayout) view.findViewById(R.id.shift_activity_view_misc);
			rail.setVisibility(View.GONE);
			misc.setVisibility(View.GONE);
			dz.setVisibility(View.VISIBLE);
			

			TextView field = (TextView) view.findViewById(R.id.unitsd);
			field.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.activityUnits)) + " units moved");

			field = (TextView) view.findViewById(R.id.hoursWeekdz);
			DecimalFormat newFormat = new DecimalFormat("#.##");
			field.setText(newFormat.format(Double.valueOf(cursor.getString(cursor.getColumnIndex(railDBHelper.activityHours)))));	
			

		}
		else if(type.equals(mContext.getString(R.string.shift_activity_misc))){
			RelativeLayout rail = (RelativeLayout) view.findViewById(R.id.shift_activity_view_rail);
			RelativeLayout dz = (RelativeLayout) view.findViewById(R.id.shift_activity_view_dz);
			RelativeLayout misc = (RelativeLayout) view.findViewById(R.id.shift_activity_view_misc);
			rail.setVisibility(View.GONE);
			dz.setVisibility(View.GONE);
			misc.setVisibility(View.VISIBLE);
			

			TextView field = (TextView) view.findViewById(R.id.unitsm);
			String text = cursor.getString(cursor.getColumnIndex(railDBHelper.activityNotes));
			if(text.length()>=45){
				text=text.substring(0,45);				
			}
			field.setText(text);

			field = (TextView) view.findViewById(R.id.hoursWeekmisc);
			DecimalFormat newFormat = new DecimalFormat("#.##");
			field.setText(newFormat.format(Double.valueOf(cursor.getString(cursor.getColumnIndex(railDBHelper.activityHours)))));	
			

		}


	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.block_shift_activity, parent, false);
        return retView;
	}

}
