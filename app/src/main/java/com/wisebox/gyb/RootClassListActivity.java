package com.wisebox.gyb;

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
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class RootClassListActivity extends Activity {

	List<RootClassInfo> lstCourse = new ArrayList<RootClassInfo>();
	ListView lvwCourse;
	TextView lblMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_root_class_list);

		lvwCourse = (ListView) this.findViewById(R.id.lvwRootClassListData);
		lblMessage=(TextView)this.findViewById(R.id.lblRootClassListMessage);
		GetCourse();
		BindData2View();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.root_class_list, menu);
		return true;
	}

	private void GetCourse() {
		// 命名空间
		String nameSpace = "http://tempuri.org/";
		// 调用的方法名称
		String methodName = "GetCourseModule";
		// EndPoint
		String endPoint = "http://3b53f1-0.sh.1251226507.clb.myqcloud.com/htmwstest/wstrain.asmx";
		// SOAP Action
		String soapAction = "http://tempuri.org/GetCourseModule";

		// 指定WebService的命名空间和调用的方法名
		SoapObject rpc = new SoapObject(nameSpace, methodName);

		// 设置需调用WebService接口需要传入的两个参数mobileCode、userId
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
				JSONArray resultJsonArray = jsonObject
						.getJSONArray("courseModules");
				for (int i = 0; i < resultJsonArray.length(); i++) {
					String strID = resultJsonArray.getJSONObject(i).getString(
							"moduleId");
					String strName = resultJsonArray.getJSONObject(i)
							.getString("moduleName");
					String strCount = resultJsonArray.getJSONObject(i)
							.getString("courseCount");

					RootClassInfo in=new RootClassInfo();
					in.setCourseCount(strCount);
					in.setModuleId(strID);
					in.setModuleName(strName);
					lstCourse.add(in);
					/*new AlertDialog.Builder(this).setTitle("确认")
							.setMessage(strID + " " + strName + " " + strCount)
							.setPositiveButton("确定", null).show();*/
				}
			}
		} catch (JSONException e) {

		}
		lblMessage.setText("一级课程列表:总数"+lstCourse.size());
	}

	private void BindData2View() {
		ArrayList<HashMap<String, Object>> listItemPerson = new ArrayList<HashMap<String, Object>>();
		
		if (lstCourse != null) {
			for (int i = 0; i < lstCourse.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();

				map.put("lblMessage1", "一级课程ID: "+lstCourse.get(i).getModuleId());
				map.put("lblMessage2", "一级课程名: "+lstCourse.get(i).getModuleName());
				map.put("lblMessage3", "下级课程数: "+lstCourse.get(i).getCourseCount());

				listItemPerson.add(map);
			}
		}
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItemPerson,// 数据源
				R.layout.classlist,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "lblMessage1", "lblMessage2","lblMessage3" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.lblClassListMessage1,
						R.id.lblClassListMessage2,
						R.id.lblClassListMessage3});

		// 添加并且显示
		lvwCourse.setAdapter(listItemAdapter);

		// 添加点击
		lvwCourse.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int nSel = arg2;
				Intent intent = new Intent();

				Bundle bundle = new Bundle();
				bundle.putString("moduleID", lstCourse.get(nSel).getModuleId());				

				intent.putExtras(bundle);
				intent.setClass(RootClassListActivity.this,
						SubClassListActivity.class);

				startActivityForResult(intent, 1);
			}
		});
	}
}
