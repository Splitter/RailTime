package com.splitv.railtime;

import java.text.DecimalFormat;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class weekCursorAdapter extends CursorAdapter {
    public weekCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
        TextView endWeek = (TextView) view.findViewById(R.id.endWeek);
        endWeek.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.weekEnd)));

        TextView startWeek = (TextView) view.findViewById(R.id.startWeek);
        startWeek.setText(cursor.getString(cursor.getColumnIndex(railDBHelper.weekStart)));

        TextView hoursWeek = (TextView) view.findViewById(R.id.hoursWeek);
		DecimalFormat newFormat = new DecimalFormat("#.##");
		hoursWeek.setText(String.valueOf(Double.valueOf(newFormat.format(cursor.getFloat(cursor.getColumnIndex(railDBHelper.weekHours))))));
	

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.activity_main_entry, parent, false);
        return retView;
	}

}
