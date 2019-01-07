package com.wisebox.gyb;

		import android.content.BroadcastReceiver;
		import android.content.Context;
		import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(ACTION)) {
			Intent i = new Intent(context, LoginActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}
