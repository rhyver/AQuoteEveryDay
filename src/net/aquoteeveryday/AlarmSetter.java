package net.aquoteeveryday;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class AlarmSetter extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = context.getSharedPreferences(Main.PREFS_NAME, 0); 
		Boolean notification = settings.getBoolean(Main.PREFS_NOTIFICATION, true);
		
		// If the notification is activated.
		if (notification) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(System.currentTimeMillis());
	
			// Is it after 7:00 AM. Then set the first alarm to the next day.
			if (calendar.get(Calendar.HOUR_OF_DAY) > 7 & calendar.get(Calendar.MINUTE) > 0) {
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
			}
			
			calendar.set(Calendar.HOUR_OF_DAY, 7);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			
			Intent alarmIntent = new Intent(context, QuoteNotification.class);
			PendingIntent sender = PendingIntent.getBroadcast(context, Main.NOTIFICATION_INTENT, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
			
			Log.i(Main.TAG, "Set the notification alarm to " + calendar.getTimeInMillis());
			Toast.makeText(context, context.getString(R.string.notificationOn), Toast.LENGTH_LONG).show();
		}
	}
}