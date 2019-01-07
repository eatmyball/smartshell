package com.wisebox.gyb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectReasonActivity extends Activity implements OnClickListener {
	private String strResponse = "";
	private String strReasonType = "";
	private String strSelectedReason = "";
	private List<String> lstReason = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_reason);

		Intent intent = getIntent();
		strReasonType = intent.getStringExtra("reasonType");

		setTitle("请选择" + strReasonType);
		// 绑定Layout里面的ListView
		ListView list = (ListView) findViewById(R.id.sr_lvwData);
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

		if (GetReason()) {
			if (strResponse != null && strResponse.length() > 0) {
				strResponse = strResponse.replaceAll(strReasonType
						+ GlobalInfo.m_SplitString, "");

				String straReason[] = strResponse
						.split(GlobalInfo.m_SplitString);
				for (int i = 0; i < straReason.length; i++) {
					lstReason.add(straReason[i]);
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("reasonview_ItemTitle", straReason[i]);
					listItem.add(map);
				}

				// 生成适配器的Item和动态数组对应的元素
				SimpleAdapter listItemAdapter = new SimpleAdapter(this,
						listItem,// 数据源
						R.layout.reasonview,// ListItem的XML实现
						// 动态数组与ImageItem对应的子项
						new String[] { "reasonview_ItemTitle" },
						// ImageItem的XML文件里面的一个ImageView,两个TextView ID
						new int[] { R.id.reasonview_ItemTitle });

				// 添加并且显示
				list.setAdapter(listItemAdapter);

				// 添加点击
				list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						strSelectedReason = lstReason.get(arg2);
						Intent intent = new Intent();
						intent.putExtra("name", strSelectedReason);// 放入返回值
						setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
						finish();// 结束当前的activity,等于点击返回按钮
					}
				});

			}
		}

		View btnExit = this.findViewById(R.id.sr_btnCancel);
		btnExit.setOnClickListener(this);
	}

	/**
	 * 内部类,为listview添加数据,构成联系人列表
	 * 
	 * @author w
	 * 
	 */
	public class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return lstReason.size();// 返回listview的总长度
		}

		@Override
		public Object getItem(int position) {
			return position;// 返回当前列表的位置
		}

		@Override
		public long getItemId(int position) {
			return position;// 返回当前列表位置
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(SelectReasonActivity.this);
			tv.setTextSize(25);// 设置显示文本的大小,
			tv.setTextColor(Color.RED);// 设置显示文本的颜色
			tv.setText(lstReason.get(position));// 在对应的位置设置联系人数据
			return tv;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_reason, menu);
		return true;
	}

	private boolean GetReason() {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 登陆人身份
		sbUploadData.toString();

		String strURI = "";
		if (strReasonType.equals("取消原因"))
			strURI = "http://" + GlobalInfo.m_ServerIP
					+ "/appSysCode.aspx?Method=GetCancelReason";
		else if (strReasonType.equals("延迟原因"))
			strURI = "http://" + GlobalInfo.m_ServerIP
					+ "/appSysCode.aspx?Method=GetDelayReason";
		else if (strReasonType.equals("非正常完工原因"))
			strURI = "http://" + GlobalInfo.m_ServerIP
					+ "/appSysCode.aspx?Method=GetUNFReason";
		else if (strReasonType.endsWith("NewTaskToLocation"))
			strURI = "http://" + GlobalInfo.m_ServerIP
					+ "/appSysCode.aspx?Method=GetEmergencyTaskToDept&Value="
					+ GlobalInfo.m_VerifyCode;

		HttpPost httpRequest = new HttpPost(strURI);

		// new AlertDialog.Builder(this).setTitle("确认"
		// ).setMessage(strURI).setPositiveButton("确定", null ).show();

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
				if (strResponse.startsWith("SUCCESS")) {
					strResponse = strResponse.substring(
							strResponse.indexOf("SUCCESS:") + 8,
							strResponse.indexOf("SUCCESSEND"));
					if (strResponse.length() == 0) {
						/*
						 * new AlertDialog.Builder(this).setTitle("确认")
						 * .setMessage("没有任务") .setPositiveButton("确定",
						 * null).show();
						 */

						return true;
					}
					return true;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(
							strResponse.indexOf("ERROR:") + 8,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认")
							.setMessage(strResponse)
							.setPositiveButton("确定", null).show();
					return false;
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
		switch (v.getId()) {
		case R.id.sr_btnCancel:
			intent.putExtra("name", "");// 放入返回值
			setResult(0, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
			finish();// 结束当前的activity,等于点击返回按钮
			break;
		}
	}
}
