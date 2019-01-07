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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class TaskList2Activity extends Activity implements OnClickListener {

	private List<String> lstBillNo = new ArrayList<String>();
	private List<String> lstTargetType = new ArrayList<String>();
	private List<String> lstFromLocationCode = new ArrayList<String>();
	private List<String> lstToLocationCode = new ArrayList<String>();
	private List<String> lstFromLocation = new ArrayList<String>();
	private List<String> lstToLocation = new ArrayList<String>();
	private List<String> lstPatientNo = new ArrayList<String>();
	private List<String> lstState = new ArrayList<String>();
	private List<String> lstETA = new ArrayList<String>();
	private List<String> lstString1 = new ArrayList<String>();
	private List<String> lstFromSickBed = new ArrayList<String>();
	private String strResponse = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list2);

		// 绑定“处理”按钮的时间
		View btnReturn = this.findViewById(R.id.tl_btn_Return);
		btnReturn.setOnClickListener(this);

		Intent intent = getIntent();
		strResponse = intent.getStringExtra("response");
		setTitle("请选择任务 使用人：" + GlobalInfo.m_PersonName);
		// 绑定Layout里面的ListView
		ListView list = (ListView) findViewById(R.id.tl_lvwTasklist);

		// 生成动态数组，加入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

		if (strResponse != null && strResponse.length() > 0) {
			String straTask[] = strResponse.split("\n");
			lstBillNo = new ArrayList<String>();
			lstTargetType = new ArrayList<String>();
			lstFromLocationCode = new ArrayList<String>();
			lstToLocationCode = new ArrayList<String>();
			lstFromLocation = new ArrayList<String>();
			lstToLocation = new ArrayList<String>();
			lstPatientNo = new ArrayList<String>();
			lstState = new ArrayList<String>();
			lstETA = new ArrayList<String>();
			lstString1 = new ArrayList<String>();
			lstFromSickBed = new ArrayList<String>();
			for (int i = 0; i < straTask.length; i++) {
				SplitTaskInfo split = new SplitTaskInfo(straTask[i]);
				TaskInfo ti = split.Split();

				HashMap<String, Object> map = new HashMap<String, Object>();
				if (ti.m_TargetType.equals("病人"))
					map.put("ItemImage", R.drawable.bingren);
				else if (ti.m_TargetType.equals("标本"))
					map.put("ItemImage", R.drawable.biaoben);
				else if (ti.m_TargetType.equals("物品"))
					map.put("ItemImage", R.drawable.wupin);
				else if (ti.m_TargetType.equals("文件"))
					map.put("ItemImage", R.drawable.wenjian);
				else
					map.put("ItemImage", R.drawable.qita);
				String strTitle=ti.m_String3 + " "
						+ ti.m_FromSickbed + "床->";
				if(ti.m_String6.length()>0)
				{
					strTitle+=ti.m_String6 + "  "
							+ ti.m_String1;
				}
				else
				{
					strTitle+=ti.m_ToLocation + "  "
							+ ti.m_String1;
				}
				map.put("ItemTitle",strTitle);
								
				map.put("ItemText", ti.m_TargetType + " " + ti.m_PatientName
						+ " " + ti.m_BillType + " " + ti.m_EmergencyLevel + " "
						+ ti.m_PatientBirthday + " " + ti.m_Note + " "
						+ ti.m_State);
				map.put("ItemBillNo", ti.m_BillNo);
				listItem.add(map);

				lstBillNo.add(ti.m_BillNo);
				lstTargetType.add(ti.m_TargetType);
				lstState.add(ti.m_State);
				lstFromLocationCode.add(ti.m_FromLocationCode);
				lstToLocationCode.add(ti.m_ToLocationCode);
				lstFromLocation.add(ti.m_FromLocation);
				lstToLocation.add(ti.m_ToLocation);
				lstPatientNo.add(ti.m_PatientNo);
				lstETA.add(ti.m_PatientBirthday);
				lstString1.add(ti.m_String1);
				lstFromSickBed.add(ti.m_FromSickbed);
			}
		}

		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.tasklistview,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "ItemImage", "ItemTitle", "ItemText",
						"ItemBillNo" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.ItemImage, R.id.ItemTitle, R.id.ItemText,
						R.id.ItemBillNo });

		// 添加并且显示
		list.setAdapter(listItemAdapter);

		// 添加点击
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (lstState.get(arg2).equals("新建")
						|| lstState.get(arg2).equals("已接受")) {

					ConfirmAcceptTaskActivity dlg = new ConfirmAcceptTaskActivity(
							TaskList2Activity.this, "确认开始该任务吗：|起点："
									+ lstFromLocation.get(arg2) + "|病床："
									+ lstFromSickBed.get(arg2) + "|终点："
									+ lstToLocation.get(arg2) + "|时间："
									+ lstETA.get(arg2) + "|类型："
									+ lstTargetType.get(arg2) + "|工具："
									+ lstString1.get(arg2), "确定", "取消");

					int nReturn = dlg.showDialog();
					if (nReturn == 0)// 接受该任务则添加到任务列表中
					{
						GlobalInfo.UpdateLastLocation(
								lstFromLocationCode.get(arg2),
								lstToLocationCode.get(arg2));
						AcceptOrRefuseTask(lstBillNo.get(arg2), "START"
								+ GlobalInfo.m_LoginAccount
								+ GlobalInfo.m_PersonName);
						Intent intent = new Intent();
						intent.putExtra("returnvalue", lstBillNo.get(arg2));// 放入返回值
						intent.putExtra("fromlocatoncode",
								lstFromLocationCode.get(arg2));// 放入返回值
						intent.putExtra("targettype", lstTargetType.get(arg2));// 放入返回值
						setResult(2, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
						finish();// 结束当前的activity,等于点击返回按钮
					} else if (nReturn == 1) { // 拒绝任务则继续下一个任务的处理
						// finish();
					}
					// playThread.StopAlarmRing();
				}
			}
		});
	}

	private void AcceptOrRefuseTask(String strBillNo, String strState) {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(strBillNo);// 单号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(GlobalInfo.m_RoleCode);// 登陆人身份
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strState);// 分隔符
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(GlobalInfo.m_VerifyCode);
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		// new AlertDialog.Builder(this).setTitle("确认"
		// ).setMessage(strURI).setPositiveButton("确定", null ).show();
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
					/*
					 * new AlertDialog.Builder(this).setTitle("确认")
					 * .setMessage("成功").setPositiveButton("确定", null) .show();
					 * // finish(); return;
					 */
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(
							strResponse.indexOf("ERROR:") + 8,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认")
							.setMessage(strResponse)
							.setPositiveButton("确定", null).show();
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认")
					.setMessage("接受或拒绝任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return;
		}
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public synchronized void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void OnStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_list, menu);
		return true;
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tl_btn_Return:
			Intent intent = new Intent();
			intent.putExtra("returnvalue", "");// 放入返回值
			setResult(2, intent);// 放入回传的值,并添加一个Code,方便区分返回的数据
			finish();// 结束当前的activity,等于点击返回按钮
			break;
		}
	}
}
