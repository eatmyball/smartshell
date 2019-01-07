package com.wisebox.gyb;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewSampleTaskActivity extends Activity implements OnClickListener {
	private Button btnSelect, btnSave;
	private EditText tbxBarcode, tbxFromLocationCode, tbxToLocationCode;
	private TextView tbxBarcodeQuantity;
	private List<String> lstBarcode = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_sample_task);

		InitControl();
	}

	private void InitControl() {
		setTitle("新建紧急任务");
		tbxBarcode = (EditText) findViewById(R.id.nst_tbxBarcode);
		tbxBarcode.setMovementMethod(ScrollingMovementMethod.getInstance());
		tbxBarcode.setInputType(InputType.TYPE_NULL);
		tbxBarcode.setOnKeyListener(onKey);
		/*tbxBarcode
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEND
								|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							DealBarcode();
							return true;
						}
						return false;
					}
				});*/
		tbxFromLocationCode = (EditText) findViewById(R.id.nst_FromLocationCode);
		tbxToLocationCode = (EditText) findViewById(R.id.nst_ToLocationCode);
		tbxBarcodeQuantity = (TextView) this
				.findViewById(R.id.nst_tbxBarcodeQuantity);
		GlobalInfo.setEditTextReadOnly(tbxFromLocationCode, false);
		GlobalInfo.setEditTextReadOnly(tbxToLocationCode, false);
		btnSelect = (Button) findViewById(R.id.nst_btn_Select);
		btnSelect.setOnClickListener(this);
		btnSave = (Button) findViewById(R.id.nst_btn_Save);
		btnSave.setOnClickListener(this);
	}

	OnKeyListener onKey = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				DealBarcode();
				return true;
			}
			return false;
		}
	};
	
	private void DealBarcode() {
		String strBarcode = tbxBarcode.getText().toString().trim();
		tbxBarcode.setText("");
		if (strBarcode == null || strBarcode.equals(""))
			return;

		if (strBarcode.startsWith(GlobalInfo.m_VerifyCode)) {
			tbxFromLocationCode.setText(strBarcode);
			return;
		}

		for (int i = 0; i < lstBarcode.size(); i++) {
			if (lstBarcode.get(i).equals(strBarcode)) {
				Toast.makeText(this, "已经扫描该条码,不能再次扫描" + strBarcode,
						Toast.LENGTH_SHORT);
				return;
			}
		}

		lstBarcode.add(strBarcode);

		tbxBarcodeQuantity.setText(Integer.toString(lstBarcode.size()));
	}

	private void Save() {
		if (tbxFromLocationCode.getText().length() == 0) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("起点不允许为空")
					.setPositiveButton("确定", null).show();
			tbxBarcode.requestFocus();
			return;
		}
		/*if (tbxToLocationCode.getText().length() == 0) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("终点不允许为空")
					.setPositiveButton("确定", null).show();
			btnSelect.requestFocus();
			return;
		}*/
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 单号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append("");// 人员
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(tbxFromLocationCode.getText());// 登陆人身份
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append("123");// 分隔符
		//sbUploadData.append(tbxToLocationCode.getText());// 分隔符
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append("BIAOBEN");// 分隔符
		if (lstBarcode.size() > 0)
			sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		for (int i = 0; i < lstBarcode.size(); i++) {
			sbUploadData.append(lstBarcode.get(i));// 分隔符
			if (i < lstBarcode.size() - 1)
				sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		}
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTask.aspx?Method=NewTask&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		// new AlertDialog.Builder(this).setTitle("确认"
		// ).setMessage(strURI).setPositiveButton("确定", null
		// ).show();
		String strResponse = "";
		try {
			new ArrayList<NameValuePair>();

			// httpRequest.setEntity(new
			// UrlEncodedFormEntity(params,
			// HTTP.UTF_8));
			// 发出HTTP request, 取得HTTP response
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity())
						.trim();
				if (strResponse.startsWith("SUCCESS")) {
					finish();
					return;
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
					.setMessage("结束任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			btnSave.setEnabled(true);
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_sample_task, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nst_btn_Select:
			Intent intent = new Intent(this, SelectReasonActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("reasonType", "NewTaskToLocation");
			intent.putExtras(bundle);
			startActivityForResult(intent, 1);// 请求码
			break;
		case R.id.nst_btn_Save:
			btnSave.setEnabled(false);
			Save();
		}
	}

	@Override
	/**
	 * 当跳转的activity(被激活的activity)使用完毕,销毁的时候调用该方法
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			String name = data.getStringExtra("name");
			if (name != null && name.length() > 0) {
				if (requestCode == 1) {// 返回了取消原因
					tbxToLocationCode.setText(name);
				}
			}
		}
	}
}
