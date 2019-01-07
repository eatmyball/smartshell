package com.wisebox.gyb;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ScoreAStudentActivity extends Activity implements OnClickListener {
	private TextView tbxBarcode, tbxPersonNo, tbxPersonName;
	private EditText tbxScore;
	private Button btnDeal, btnSave;
	private String strPersonName, strPersonNo, strScore, strTrainNo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score_astudent);

		InitControl();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.score_astudent, menu);
		return true;
	}

	private void InitControl() {
		tbxPersonNo = (TextView) findViewById(R.id.ss_PersonNo);
		tbxPersonName = (TextView) findViewById(R.id.ss_PersonName);
		tbxScore = (EditText) findViewById(R.id.ss_tbxScore);
		tbxBarcode = (EditText) findViewById(R.id.ss_tbxBarcode);

		btnDeal = (Button) findViewById(R.id.ss_btn_Deal);
		btnSave = (Button) findViewById(R.id.ss_btn_Save);

		btnDeal.setOnClickListener(this);
		btnSave.setOnClickListener(this);

		Intent intent = getIntent();
		strPersonName = intent.getStringExtra("PersonName");
		strPersonNo = intent.getStringExtra("PersonNo");
		strScore = intent.getStringExtra("Score");
		strTrainNo = intent.getStringExtra("TrainNo");

		tbxPersonNo.setText(strPersonNo);
		tbxPersonName.setText(strPersonName);
		tbxScore.setText(strScore);
	}

	private boolean Save() {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(strTrainNo);// 单号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(tbxPersonNo.getText().toString());// 人员
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append("123");// 登陆人身份
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(tbxScore.getText().toString());// 分隔符
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(GlobalInfo.m_VerifyCode);
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTrain.aspx?Method=UpdateStudentScore&Value="
				+ strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		new AlertDialog.Builder(this).setTitle("确认").setMessage(strURI)
				.setPositiveButton("确定", null).show();
		String strResponse = "";
		try {
			new ArrayList<NameValuePair>();

			// httpRequest.setEntity(new UrlEncodedFormEntity(params,
			// HTTP.UTF_8));
			// 发出HTTP request, 取得HTTP response
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity())
						.trim();
				// new AlertDialog.Builder(this).setTitle("确认"
				// ).setMessage(strResponse).setPositiveButton("确定",
				// null ).show();
				if (strResponse.startsWith("SUCCESS")) {
					String strName = strResponse.substring(
							strResponse.indexOf("SUCCESS") + 7,
							strResponse.indexOf("SUCCESSEND"));
					tbxPersonName.setText(strName);
					return true;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(
							strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认")
							.setMessage(strResponse)
							.setPositiveButton("确定", null).show();
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认")
					.setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.ss_btn_Deal:
			break;
		case R.id.ss_btn_Save:
			if (Save()) {
				intent.putExtra("message", tbxPersonNo.getText().toString()
						+ GlobalInfo.m_SplitString
						+ tbxPersonName.getText().toString()
						+ GlobalInfo.m_SplitString
						+ tbxScore.getText().toString()
						+ GlobalInfo.m_SplitString);// 放入返回值
				setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
				finish();// 结束当前的activity,等于点击返回按钮
			}
			break;
		}
	}
}
