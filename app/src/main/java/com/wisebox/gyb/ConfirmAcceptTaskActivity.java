package com.wisebox.gyb;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class ConfirmAcceptTaskActivity extends Dialog {

	int dialogResult;
	Handler mHandler;

	public ConfirmAcceptTaskActivity(Activity context, String strMessage) {
		super(context);
		setOwnerActivity(context);
		onCreate();

		setTitle("请确认");

		TextView promptLbl = (TextView) findViewById(R.id.cd_lblMessage);
		promptLbl.setTextColor(android.graphics.Color.BLACK);
		promptLbl.setText(strMessage.replace("|", "\n"));

	}

	public ConfirmAcceptTaskActivity(Activity context, String strMessage,
			String strButtonName1, String strButtonName2) {
		super(context);
		setOwnerActivity(context);
		onCreate();
		TextView promptLbl = (TextView) findViewById(R.id.cd_lblMessage);
		promptLbl.setTextColor(android.graphics.Color.BLACK);
		promptLbl.setText(strMessage.replace("|", "\n"));

		if (strButtonName1.length() > 0) {
			Button btnOK = (Button) this.findViewById(R.id.cd_btnAccept);
			btnOK.setText(strButtonName1);
		}
		if (strButtonName2.length() > 0) {
			Button btnCancel = (Button) this.findViewById(R.id.cd_btnRefuse);
			btnCancel.setText(strButtonName2);
		}
	}

	public int getDialogResult() {
		return dialogResult;
	}

	public void setDialogResult(int dialogResult) {
		this.dialogResult = dialogResult;
	}

	/** Called when the activity is first created. */

	public void onCreate() {
		setContentView(R.layout.activity_confirm_accept_task);
		findViewById(R.id.cd_btnRefuse).setOnClickListener(
				new android.view.View.OnClickListener() {

					@Override
					public void onClick(View paramView) {
						endDialog(1);
					}
				});
		findViewById(R.id.cd_btnAccept).setOnClickListener(
				new android.view.View.OnClickListener() {

					@Override
					public void onClick(View paramView) {
						endDialog(0);
					}
				});
	}

	public void endDialog(int result) {
		dismiss();
		setDialogResult(result);
		Message m = mHandler.obtainMessage();
		mHandler.sendMessage(m);
	}

	public int showDialog() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message mesg) {
				// process incoming messages here
				// super.handleMessage(msg);
				throw new RuntimeException();
			}
		};
		super.show();
		try {
			Looper.getMainLooper().loop();
		} catch (RuntimeException e2) {
		}
		return dialogResult;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME
				|| keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return false;
	}
}
