package com.wisebox.gyb;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenuActivity extends Activity implements OnClickListener {
	private Button btnQianDao, btnExit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		btnQianDao = (Button) this.findViewById(R.id.btnMainMenuQianDao);
		btnExit = (Button) this.findViewById(R.id.btnMainMenuExit);
		btnQianDao.setOnClickListener(this);
		btnExit.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btnMainMenuQianDao:
			Intent i1 = new Intent(this, RootClassListActivity.class);
			startActivity(i1);
			break;
		case R.id.btnMainMenuExit:
			System.exit(0); // 退出app的标准写法
			break;
		}
	}

}
