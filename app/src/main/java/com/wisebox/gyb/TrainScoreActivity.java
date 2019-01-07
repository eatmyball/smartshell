package com.wisebox.gyb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TrainScoreActivity extends Activity implements OnClickListener {
	private EditText tbxBarcode;
	private TextView lvwTrain;
	private String strResponse = "", strTrainNo = "";
	private PlayRingThread playThread;
	private ListView lvwStudent;
	private TrainInfo trainInfo = new TrainInfo();
	private List<StudentInfo> lstStudent = new ArrayList<StudentInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_train_score);

		InitControl();

		if (GetTrainList()) {
			Bind2ListView();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.train_score, menu);
		return true;
	}

	private void Bind2ListView() {
		lvwTrain.setText(trainInfo.m_TrainName + " " + trainInfo.m_CourseName
				+ " " + trainInfo.m_TeacherName + " " + trainInfo.m_Location);
		// 生成动态数组，加入数据
		ArrayList<HashMap<String, Object>> lstTrainMap = new ArrayList<HashMap<String, Object>>();

		if (lstStudent != null) {
			for (int i = 0; i < lstStudent.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();

				/*map.put("StudentItem1",
						lstStudent.get(i).m_PersonNo + " "
								+ lstStudent.get(i).m_PersonName + " "
								+ lstStudent.get(i).m_Score);*/

				lstTrainMap.add(map);
			}
		}
		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, lstTrainMap,// 数据源
				R.layout.studentlist,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "StudentItem1" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.StudentItem1 });

		// 添加并且显示
		lvwStudent.setAdapter(listItemAdapter);

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

		Intent intent = getIntent();
		strTrainNo = intent.getStringExtra("trainno");
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(strTrainNo);// 登陆人身份
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(" ");//单号
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(GlobalInfo.m_PhoneNumber);//登陆人身份
		// sbUploadData.append(GlobalInfo.m_SplitString);//分隔符
		// sbUploadData.append(GlobalInfo.m_VerifyCode);
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTrain.aspx?Method=GetATrain&Value=" + strUploadData;
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

					SplitTrainInfo split = new SplitTrainInfo(strResponse);
					trainInfo = split.Split();

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

		strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appTrain.aspx?Method=GetATrainStudentList&Value="
				+ strUploadData;
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
					String stra[] = strResponse.split("\n");

					for (int i = 0; i < stra.length; i++) {
						SplitStudentInfo split = new SplitStudentInfo(stra[i]);
						lstStudent.add(split.Split());
					}

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
		return true;
	}

	private void InitControl() {
		tbxBarcode = (EditText) findViewById(R.id.ts_tbxBarcode);
		lvwTrain = (TextView) findViewById(R.id.ts_TrainInfo);
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
		lvwStudent = (ListView) findViewById(R.id.lstStudent);
		View btnDeal = this.findViewById(R.id.ts_btn_Deal);
		btnDeal.setOnClickListener(this);
		btnDeal.requestFocus();
	}

	private void DealBarcode() {
		String strBarcode = tbxBarcode.getText().toString();
		tbxBarcode.setText("");
		if (strBarcode == null || strBarcode.length() == 0)
			return;

		boolean bHave = false;
		String strPersonNo = strBarcode, strPersonName = "", strScore = "";
		for (int i = 0; i < lstStudent.size(); i++) {
			/*if (lstStudent.get(i).m_PersonNo.equals(strBarcode)) {
				strPersonNo = lstStudent.get(i).m_PersonNo;
				strPersonName = lstStudent.get(i).m_PersonName;
				strScore = lstStudent.get(i).m_Score;
				bHave = true;
				break;
			}*/
		}

		if (!bHave) {
			/*
			 * new AlertDialog.Builder(this).setTitle("确认")
			 * .setMessage("本次培训没有这个人，请注意添加") .setPositiveButton("确定",
			 * null).show();
			 */
		}

		Intent intent = new Intent();

		Bundle bundle = new Bundle();
		bundle.putString("PersonNo", strPersonNo);
		bundle.putString("PersonName", strPersonName);
		bundle.putString("Score", strScore);
		bundle.putString("TrainNo", strTrainNo);

		intent.putExtras(bundle);

		intent.setClass(TrainScoreActivity.this, ScoreAStudentActivity.class);
		startActivityForResult(intent, 2);// 请求码
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, SelectReasonActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.ts_btn_Deal:
			DealBarcode();
			break;
		}
	}

	@Override
	/**
	 * 当跳转的activity(被激活的activity)使用完毕,销毁的时候调用该方法
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			String name = data.getStringExtra("message");
			if (name != null && name.length() > 0) {
				/*String[] stra = name.split(GlobalInfo.m_SplitString);
				boolean bHave = false;
				for (int i = 0; i < lstStudent.size(); i++) {
					if (lstStudent.get(i).m_PersonNo.equals(stra[0])) {
						bHave = true;
						lstStudent.get(i).m_Score = stra[2];
					}
				}
				if (!bHave) {
					StudentInfo si = new StudentInfo();
					si.m_PersonName = stra[1];
					si.m_PersonNo = stra[0];
					si.m_Score = stra[2];
					lstStudent.add(si);
				}*/
			}

			Bind2ListView();
		}
	}
}
