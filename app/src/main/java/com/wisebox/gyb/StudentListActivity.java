package com.wisebox.gyb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class StudentListActivity extends Activity implements OnClickListener {
	private EditText tbxBarcode;
	List<StudentInfo> lstData = new ArrayList<StudentInfo>();
	ListView lvwCourse;
	String strCourseID, strCourseName, strStudentCount, strTrainDate;
	TextView tbxCode, tbxName, tbxTrainDate, tbxStudentCount;
	Button btnStart, btnEnd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_list);

		lvwCourse = (ListView) this.findViewById(R.id.skkq_lvwPerson);

		ImageView ivNewStudent = (ImageView) this
				.findViewById(R.id.skkq_NewStudent);
		ivNewStudent.setOnClickListener(this);
		tbxCode = (TextView) this.findViewById(R.id.sk_tbxKeCi);
		tbxName = (TextView) this.findViewById(R.id.sk_tbxKeCheng);
		tbxTrainDate = (TextView) this.findViewById(R.id.sk_tbxTrainDate);
		tbxStudentCount = (TextView) this.findViewById(R.id.sk_tbxZongShu);
		tbxBarcode = (EditText) findViewById(R.id.sk_tbxBarcode);
		tbxBarcode.setOnKeyListener(onKey);
		tbxBarcode.setMovementMethod(ScrollingMovementMethod.getInstance());
		tbxBarcode.setInputType(InputType.TYPE_NULL);

		btnStart = (Button) this.findViewById(R.id.sk_btnKaiShiShangKe);
		btnEnd = (Button) this.findViewById(R.id.sk_btnWanChengShangKe);
		btnStart.setOnClickListener(this);
		btnEnd.setOnClickListener(this);

		Intent intent = getIntent();
		strCourseID = intent.getStringExtra("code");
		strCourseName = intent.getStringExtra("name");
		strStudentCount = intent.getStringExtra("studentCount");
		strTrainDate = intent.getStringExtra("trainDate");
		tbxName.setText("课程：" + strCourseName);
		tbxTrainDate.setText("日期：" + strTrainDate);
		tbxStudentCount.setText("学生总数：" + strStudentCount);

		GetStudent();
		BindData2View();
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
		String strBarcode = tbxBarcode.getText().toString();
		if (strBarcode == null || strBarcode.length() == 0)
			return;
		if (strBarcode.length() > 10)
			strBarcode = strBarcode.substring(0, 10);
		
		tbxBarcode.setText("");

		if (strBarcode.startsWith(GlobalInfo.m_LoginAccount.substring(0, 5))) {
			Boolean bHave = false;
			for (int i = 0; i < lstData.size(); i++) {
				if (strBarcode.equals(lstData.get(i).getCode())) {
					bHave = true;
					if (lstData.get(i).getSignDate().length() > 0) {
						new AlertDialog.Builder(this).setTitle("确认")
								.setMessage("工号" + strBarcode + "已经签到，无需再次签到")
								.setPositiveButton("确定", null).show();
						break;
					} else {
						SimpleDateFormat sDateFormat = new SimpleDateFormat(
								"yyyy-MM-dd    hh:mm:ss");
						String date = sDateFormat.format(new java.util.Date());
						lstData.get(i).setSignDate(date);
						lstData.get(i).setOldNewFlag("new");
					}
				}
			}
			if (bHave) {
				BindData2View();
			} else {
				new AlertDialog.Builder(this)
						.setTitle("确认")
						.setMessage(
								"工号" + strBarcode
										+ "不在课程人员名单中，如欲添加，请点击人员名单右上方+图标。")
						.setPositiveButton("确定", null).show();
			}
		} else {
			new AlertDialog.Builder(this).setTitle("确认")
					.setMessage("工号" + strBarcode + "非本项目工号，请重新扫描有效工号")
					.setPositiveButton("确定", null).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.student_list, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.skkq_NewStudent:
			Intent intent = new Intent();
			intent.setClass(StudentListActivity.this,
					PersonInHospitalListActivity.class);
			startActivityForResult(intent, 1);
			break;
		case R.id.sk_btnKaiShiShangKe:
			GetCourseCode();
			break;
		case R.id.sk_btnWanChengShangKe:
			break;
		}
	}

	private void GetCourseCode() {
		// 命名空间
		String nameSpace = "http://tempuri.org/";
		// 调用的方法名称
		String methodName = "GetTrainRecordId";
		// EndPoint
		String endPoint = "http://3b53f1-0.sh.1251226507.clb.myqcloud.com/htmwstest/wstrain.asmx";
		// SOAP Action
		String soapAction = "http://tempuri.org/GetTrainRecordId";

		// 指定WebService的命名空间和调用的方法名
		SoapObject rpc = new SoapObject(nameSpace, methodName);

		// 设置需调用WebService接口需要传入的两个参数mobileCode、userId

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd    hh:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		rpc.addProperty("courseId", strCourseID);
		rpc.addProperty("appCurrentTime", date);
		rpc.addProperty("userCode", GlobalInfo.m_LoginAccount);

		// 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);

		envelope.bodyOut = rpc;
		// 设置是否调用的是dotNet开发的WebService
		envelope.dotNet = true;
		// 等价于envelope.bodyOut = rpc;
		envelope.setOutputSoapObject(rpc);

		HttpTransportSE transport = new HttpTransportSE(endPoint);
		try {
			// 调用WebService
			transport.call(soapAction, envelope);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 获取返回的数据
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		String result = object.getProperty(0).toString();

		/*new AlertDialog.Builder(this).setTitle("确认").setMessage(result)
				.setPositiveButton("确定", null).show();*/

		try {
			JSONObject jsonObject = new JSONObject(result);
			String strResult = jsonObject.getString("success");
			if ("true".equals(strResult)) {
				String strCourseCode = jsonObject.getString("recordId");

				tbxCode.setText("课次：" + strCourseCode);
			}
		} catch (JSONException e) {

		}
	}

	private void GetStudent() {
		// 命名空间
		String nameSpace = "http://tempuri.org/";
		// 调用的方法名称
		String methodName = "GetUserByCourseId";
		// EndPoint
		String endPoint = "http://3b53f1-0.sh.1251226507.clb.myqcloud.com/htmwstest/wstrain.asmx";
		// SOAP Action
		String soapAction = "http://tempuri.org/GetUserByCourseId";

		// 指定WebService的命名空间和调用的方法名
		SoapObject rpc = new SoapObject(nameSpace, methodName);

		// 设置需调用WebService接口需要传入的两个参数mobileCode、userId

		rpc.addProperty("courseId", strCourseID);
		rpc.addProperty("userCode", GlobalInfo.m_LoginAccount);

		// 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);

		envelope.bodyOut = rpc;
		// 设置是否调用的是dotNet开发的WebService
		envelope.dotNet = true;
		// 等价于envelope.bodyOut = rpc;
		envelope.setOutputSoapObject(rpc);

		HttpTransportSE transport = new HttpTransportSE(endPoint);
		try {
			// 调用WebService
			transport.call(soapAction, envelope);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 获取返回的数据
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		String result = object.getProperty(0).toString();

		/*new AlertDialog.Builder(this).setTitle("确认").setMessage(result)
				.setPositiveButton("确定", null).show();*/

		try {
			JSONObject jsonObject = new JSONObject(result);
			String strResult = jsonObject.getString("success");
			if ("true".equals(strResult)) {
				JSONArray resultJsonArray = jsonObject.getJSONArray("users");
				for (int i = 0; i < resultJsonArray.length(); i++) {
					String strID = resultJsonArray.getJSONObject(i).getString(
							"propId");
					String strName = resultJsonArray.getJSONObject(i)
							.getString("propName");
					String strDate = resultJsonArray.getJSONObject(i)
							.getString("signinTime");

					StudentInfo in = new StudentInfo();

					in.setCode(strID);
					in.setName(strName);
					in.setSignDate(strDate);
					in.setOldNewFlag("old");
					lstData.add(in);
					/*
					 * new AlertDialog.Builder(this).setTitle("确认")
					 * .setMessage(strID + " " + strName + " " + strCount)
					 * .setPositiveButton("确定", null).show();
					 */
				}
			}
		} catch (JSONException e) {

		}
		// lblMessage.setText("二级课程列表:总数"+lstCourse.size());
	}

	private void BindData2View() {
		ArrayList<HashMap<String, Object>> listItemPerson = new ArrayList<HashMap<String, Object>>();

		if (lstData != null) {
			for (int i = 0; i < lstData.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();

				map.put("lblMessage1", "人员ID: " + lstData.get(i).getCode()
						+ " " + "人员姓名: " + lstData.get(i).getName());
				map.put("lblMessage2", "签到日期: " + lstData.get(i).getSignDate());
				// map.put("lblMessage3",
				// "签到日期: "+lstData.get(i).getSignDate());

				listItemPerson.add(map);
			}
		}
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItemPerson,// 数据源
				R.layout.twomessagelist,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "lblMessage1", "lblMessage2" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.lblClassListMessage1,
						R.id.lblClassListMessage2 });

		// 添加并且显示
		lvwCourse.setAdapter(listItemAdapter);

		// 添加点击
		lvwCourse.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// int nSel = arg2;
				// Intent intent = new Intent();

				// Bundle bundle = new Bundle();
				// bundle.putString("code", lstData.get(nSel).getCode());
				// bundle.putString("name", lstData.get(nSel).getCode());

				// intent.putExtras(bundle);
				// intent.setClass(SubClassListActivity.this,
				// StudentListActivity.class);

				// startActivityForResult(intent, 1);
			}
		});
	}

}
