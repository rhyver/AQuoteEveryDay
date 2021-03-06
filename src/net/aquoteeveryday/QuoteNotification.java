package net.aquoteeveryday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class QuoteNotification extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(Main.TAG, "Start Notification");
		
		String notificationService = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(notificationService);
		
		int icon = R.drawable.notification;
		long now = System.currentTimeMillis();
		String title = context.getString(R.string.notificationTitle);
		String text = context.getString(R.string.notificationText);
		
		Intent notificationIntent = new Intent(context, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		Notification notification = new Notification(icon, text, now);
		notification.setLatestEventInfo(context, title, text, contentIntent);

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(1, notification);
	}
}