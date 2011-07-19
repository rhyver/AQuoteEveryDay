package net.aquoteeveryday;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class Message {

	public static void alert(Context context,
		   		 		  	 String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
		       .setCancelable(false)
		       .setNeutralButton("Close", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					//dialog.cancel();
				}
			});
		AlertDialog alert = builder.create();
		
		alert.show();
	}
	
	public static void toast(Context context,
							 String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
