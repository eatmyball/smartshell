package com.wisebox.gyb;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MyDialogActivity extends Activity {

	private Button btnOK;
	private Button btnCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_dialog);

		btnOK = (Button) this.findViewById(R.id.md_btnOk);
		btnCancel = (Button) this.findViewById(R.id.md_btnCancel);
		btnOK.setOnClickListener(new OKClickListener());
		btnCancel.setOnClickListener(new CancelClickListener());

		TextView lblMessage = (TextView) this.findViewById(R.id.md_message);

		Intent intent = getIntent();
		String strMessage = intent.getStringExtra("message");

		lblMessage.setText(strMessage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_dialog, menu);
		return true;
	}

	class OKClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("returnvalue", "ok");// 放入返回值
			setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
			finish();// 结束当前的activity,等于点击返回按钮
		}
	}

	class CancelClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("returnvalue", "cancel");// 放入返回值
			setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
			finish();// 结束当前的activity,等于点击返回按钮
		}

	}
}
