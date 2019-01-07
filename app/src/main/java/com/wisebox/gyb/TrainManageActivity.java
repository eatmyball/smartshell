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
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TrainManageActivity extends Activity implements OnClickListener {
	private EditText tbxBarcode;
	private String strResponse = "";
	private PlayRingThread playThread;
	private ListView lvwMyTrain;
	private List<TrainInfo> lstTrain = new ArrayList<TrainInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train_manage);

		InitControl();

		if (GetTrainList()) {
			if (Split2TrainInfo()) {
				Bind2ListView();
			}
		}
	}

	private void Bind2ListView() {
		// 生成动态数组，加入数据
		ArrayList<HashMap<String, Object>> lstTrainMap = new ArrayList<HashMap<String, Object>>();

		if (lstTrain != null) {
			for (int i = 0; i < lstTrain.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();

				map.put("Item1",
						lstTrain.get(i).m_TrainName + " "
								+ lstTrain.get(i).m_TrainNo + " "
								+ lstTrain.get(i).m_CourseName + "  "
								+ lstTrain.get(i).m_TeacherName);
				map.put("Item2",
						lstTrain.get(i).m_Location + " "
								+ lstTrain.get(i).m_State + " "
								+ lstTrain.get(i).m_ScoreType + "  "
								+ lstTrain.get(i).m_DeptName);
				map.put("Item3", lstTrain.get(i).m_TotalStudentsNo + " "
						+ lstTrain.get(i).m_AttendStudentsNo);
				lstTrainMap.add(map);
			}
		}
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, lstTrainMap,// 数据源
				R.layout.trainlistview,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "Item1", "Item2", "Item3" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.Item1, R.id.Item2, R.id.Item3 });

		// 添加并且显示
		lvwMyTrain.setAdapter(listItemAdapter);

		// 添加点击
		lvwMyTrain.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				try {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();
					bundle.putString("trainno",
							(String) lstTrain.get(arg2).m_TrainNo);

					intent.putExtras(bundle);

					intent.setClass(TrainManageActivity.this,
							TrainScoreActivity.class);
					startActivityForResult(intent, 2);// 请求码

				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});

	}

	private boolean Split2TrainInfo() {
		boolean bReturn = true;
		if (strResponse == null || strResponse == "") {

			return false;
		}

		lstTrain = new ArrayList<TrainInfo>();
		String straTask[] = strResponse.split("\n");
		if (straTask == null || straTask.length == 0) {
			return false;
		}

		int nHave = 0;
		for (int i = 0; i < straTask.length; i++) {
			SplitTrainInfo split = new SplitTrainInfo(straTask[i]);
			TrainInfo ti = split.Split();
			if (ti == null)
				continue;

			lstTrain.add(ti);
		}

		return bReturn;
	}

	// appTrain.aspx?Method=GetTrainMain&Value=
	private boolean GetTrainList() {
		HttpPost httpRequest = new HttpPost("123");
		FileService fs = new FileService(this);
		String strTxt = "";
		try {
			strTxt = fs.read(GlobalInfo.m_TmpFileName);
		} catch (Exception e) {
		}

		if (strTxt != "") {
			String straDetail[] = strTxt.split(GlobalInfo.m_CommandSplitString);
			String strNewCommandText = "";
			for (int i = straDetail.length - 1; i >= 0; i--) {
				httpRequest = new HttpPost(straDetail[i]);
				try {
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpRequest);

					// 若状态码为200 ==> OK
					int nStatus = httpResponse.getStatusLine().getStatusCode();
					if (nStatus == 200) {
						String strTemp = EntityUtils.toString(
								httpResponse.getEntity()).trim();
						if (strTemp.startsWith("SUCCESS")) {
						} else {
							strNewCommandText += straDetail[i]
									+ GlobalInfo.m_CommandSplitString;
						}
					}
				} catch (Exception e) {
					strNewCommandText += straDetail[i]
							+ GlobalInfo.m_CommandSplitString;
				}
			}
			try {
				fs.save(GlobalInfo.m_TmpFileName, strNewCommandText);
			} catch (Exception e) {

			}
		}

		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 登陆人身份
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(" ");//单号
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(GlobalInfo.m_PhoneNumber);//登陆人身份
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(GlobalInfo.m_VerifyCode);
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTrain.aspx?Method=GetTrainMain&Value=" + strUploadData;
		httpRequest = new HttpPost(strURI);

		try {
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

					return true;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(
							strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认")
							.setMessage(strResponse)
							.setPositiveButton("确定", null).show();
					return false;
				}
			} else
				new AlertDialog.Builder(this).setTitle("确认").setMessage("123")
						.setPositiveButton("确定", null).show();
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认")
					.setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		return false;
	}

	private void InitControl() {
		tbxBarcode = (EditText) findViewById(R.id.tm_tbxBarcode);
		tbxBarcode.setMovementMethod(ScrollingMovementMethod.getInstance());
		// tbxBarcode.setInputType(InputType.TYPE_NULL);
		tbxBarcode
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEND
								|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

							return true;
						}
						return false;
					}
				});
		lvwMyTrain = (ListView) findViewById(R.id.lstMyTrain);
		View btnDeal = this.findViewById(R.id.tm_btn_Deal);
		btnDeal.setOnClickListener(this);
		btnDeal.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.patient, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, SelectReasonActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.tm_btn_Deal:

			break;
		}
	}
}
