package com.splitv.railtime;

import java.text.DecimalFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class shiftCursorAdapter extends CursorAdapter {
    public shiftCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
        TextView shiftDate = (TextView) view.findViewById(R.id.shiftDate);
        shiftDate.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.shiftDate)));

        TextView shiftType = (TextView) view.findViewById(R.id.shiftType);
        shiftType.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.shiftType)));

        TextView hoursWeek = (TextView) view.findViewById(R.id.hoursWeek);
        
		DecimalFormat newFormat = new DecimalFormat("#.##");
		hoursWeek.setText(newFormat.format(Double.valueOf(cursor.getString(cursor.getColumnIndex(railDBHelper.shiftHours)))));	
		

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.activity_view_week_entry, parent, false);
        return retView;
	}

}
