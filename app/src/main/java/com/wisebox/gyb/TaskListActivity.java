package com.wisebox.gyb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.google.gson.JsonObject;
import com.wisebox.gyb.BLE.BLEUtils;
import com.wisebox.gyb.BLE.BleTimeTask;
import com.wisebox.gyb.ScreenObserver.ScreenStateListener;
import com.wisebox.gyb.utils.ParamsBuildUtils;
import com.wisebox.gyb.utils.gsonObj.DeptMacJson;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.provider.ContactsContract.Data;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressLint({ "ShowToast", "DefaultLocale", "HandlerLeak" })
public class TaskListActivity extends Activity implements OnClickListener {
	private EditText tbxBarcode;
	// private TextView lblUserInfo;
	private ListView lvwExecuting, lvwAccepted;
	private ScreenObserver mScreenObserver;
	private List<TaskInfo> lstTaskInfo = new ArrayList<TaskInfo>();
	private String strResponse = "";
	private PlayRingThread playThread;
	private boolean bReceiveNewTask = true;
	private int nTryConnectTimes = 0;
	private WakeLock mWakeLock;
	private boolean bInSplit2TaskInfoFunction = false;
	private PowerManager pm;
	Button btnViewExecuting, btnViewAccepted, btnScan,btnNewDrudTask;
	private int REQUEST_CODE_SCAN = 299;

	private TaskListAdapter adapterAccepted;
	private TaskListAdapter adapterExecuting;
	/**
	 * 当前音量
	 */
	private int currentVolume;
	/**
	 * 控制音量的对象
	 */
	public AudioManager mAudioManager;
	/**
	 * 系统最大音量
	 */
	private int maxVolume;
	/**
	 * 确保关闭程序后，停止线程
	 */
	private boolean isDestroy;

	private boolean mIsSupportBle = false;

	private BleTimeTask myBleTask;

	private String lastMac = "";
	private long lastReportTime; //上次上报蓝牙的时间
	private long REPORT_INTERVAL = 2*60*1000; //同一个蓝牙2分钟更新一次
	private List<BleDevice> sortCollection = new ArrayList<>();

	// 申请设备电源锁
	@SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
		if (null == mWakeLock) {
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	// 释放设备电源锁
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	// private HomeWatcherReceiver mHomeWatcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);

		/*
		 * mHomeWatcher = new HomeWatcherReceiver(this);
		 * mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
		 * 
		 * @Override public void onHomePressed() { Log.e(TAG, "onHomePressed");
		 * Toast.makeText(getApplicationContext(), "onHomePressed",
		 * Toast.LENGTH_SHORT).show(); }
		 * 
		 * @Override public void onHomeLongPressed() { Log.e(TAG,
		 * "onHomeLongPressed"); Toast.makeText(getApplicationContext(),
		 * "onHomeLongPressed", Toast.LENGTH_SHORT).show(); } });
		 * mHomeWatcher.startWatch();
		 */

		if (GlobalInfo.m_LoginAccount == null || GlobalInfo.m_LoginAccount.equals("")) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("登陆人身份丢失，请重新登录").setPositiveButton("确定", null)
					.show();

			System.exit(0);
		}

		playThread = new PlayRingThread(this);
		mScreenObserver = new ScreenObserver(this);
		pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mScreenObserver.requestScreenStateUpdate(new ScreenStateListener() {
			@Override
			public void onScreenOn() {
				playThread.StopAlarmRing();
			}

			@Override
			public void onScreenOff() {
			}
		});

		GlobalInfo.GetBarcodeSplitInfo();
		InitControl();

		if (GetTaskList()) {
			if (Split2TaskInfo()) {
				Bind2ListView();
			}
		}
		//初始化蓝牙
		BLEUtils.init();
		mIsSupportBle = BLEUtils.getInstance().isSupportBle();
		if(mIsSupportBle) {
			BLEUtils.getInstance().enableBluetooth();
		}
		// 启动定时刷新任务的进程
		new Thread(new ThreadShow()).start();
		//蓝牙定时器任务
		myBleTask = new BleTimeTask(20000, new TimerTask() {
			@Override
			public void run() {
				bleTaskHandler.sendEmptyMessage(999);
			}
		});
		myBleTask.start();

		acquireWakeLock();
		isDestroy = false;
		// 获得AudioManager对象
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);// 音乐音量,如果要监听铃声音量变化，则改为AudioManager.STREAM_RING
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		OnOffDuty("ON");
		lvwExecuting.setVisibility(View.VISIBLE);
		lvwAccepted.setVisibility(View.GONE);
		btnViewExecuting.setEnabled(false);
		btnViewAccepted.setEnabled(true);
		btnViewAccepted.setBackgroundColor(Color.parseColor("#006f67"));
		btnViewExecuting.setBackgroundColor(Color.GRAY);
	}

	private void InitControl() {
		// 绑定“处理”按钮的事件
		View btnDeal = this.findViewById(R.id.pt_btn_Deal);
		btnDeal.setOnClickListener(this);

		View btnGetTask = this.findViewById(R.id.pt_btn_GetTask);
		btnGetTask.setOnClickListener(this);

		btnNewDrudTask=(Button)findViewById(R.id.btnNewDrugTask);
		btnNewDrudTask.setOnClickListener(this);
		btnViewExecuting = (Button) findViewById(R.id.pt_btn_ViewExecuting);
		btnViewExecuting.setOnClickListener(this);

		btnScan = (Button) findViewById(R.id.pt_btn_Scan);
		btnScan.setOnClickListener(this);

		btnViewAccepted = (Button) findViewById(R.id.pt_btn_ViewAccepted);
		btnViewAccepted.setOnClickListener(this);
		setTitle("待执行任务列表 使用人：" + GlobalInfo.m_PersonName + " 状态 :" + GlobalInfo.m_OnOffDutyState + " "
				+ GlobalInfo.m_OnOffLineState);

		/*
		 * lblUserInfo = (TextView) this.findViewById(R.id.UserInfo);
		 * lblUserInfo.setText("用户名:" + GlobalInfo.m_PersonName + " 状态 :" +
		 * GlobalInfo.m_OnOffDutyState + " " + GlobalInfo.m_OnOffLineState);
		 */

		tbxBarcode = (EditText) findViewById(R.id.pt_tbxBarcode);
		// tbxBarcode.setMovementMethod(ScrollingMovementMethod.getInstance());
		// tbxBarcode.setInputType(InputType.TYPE_NULL);

		/*
		 * tbxBarcode .setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() { public boolean
		 * onEditorAction(TextView v, int actionId, KeyEvent event) {
		 * //tbxBarcode
		 * .setText(Integer.toString(actionId)+" "+Integer.toString(event
		 * .getKeyCode())+" "+Integer.toString(KeyEvent.KEYCODE_ENTER)); if
		 * (actionId == EditorInfo.IME_ACTION_SEND || (event != null &&
		 * event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
		 * tbxBarcode.setText(Integer
		 * .toString(actionId)+" "+Integer.toString(event
		 * .getKeyCode())+" "+Integer.toString(KeyEvent.KEYCODE_ENTER));
		 * DealBarcode(); return true; } return false; } });
		 */
		tbxBarcode.setOnKeyListener(onKey);

		/*
		 * tbxBarcode.setOnKeyListener(new OnKeyListener() {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
		 * if (KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() ==
		 * KeyEvent.ACTION_DOWN) { DealBarcode(); return true; } return false; }
		 * }); tbxBarcode.clearFocus();
		 */
		setTitle("待执行任务列表 使用人：" + GlobalInfo.m_PersonName);
		// 绑定Layout里面的ListView
		lvwExecuting = (ListView) findViewById(R.id.tasklist_lvwExecutingTask);
		lvwAccepted = (ListView) findViewById(R.id.tasklist_lvwAcceptedTask);
		/*
		 * if (GlobalInfo.b_SupportBluetooth) { ViewGroup.LayoutParams params =
		 * lvwExecuting.getLayoutParams(); params.height = 200;
		 * lvwExecuting.setLayoutParams(params);
		 * 
		 * ViewGroup.LayoutParams params2 = lvwAccepted.getLayoutParams();
		 * params2.height = 200; lvwAccepted.setLayoutParams(params); }
		 */

		/*
		 * TelephonyManager telMgr= (TelephonyManager)
		 * getSystemService(TELEPHONY_SERVICE); String strMessage=""; if
		 * (telMgr.getSimState() == telMgr.SIM_STATE_READY) { strMessage="良好"; }
		 * else if (telMgr.getSimState() == telMgr.SIM_STATE_ABSENT) {
		 * strMessage="无SIM卡"; } else { strMessage="SIM卡被锁定或未知的状态"; }
		 * 
		 * new AlertDialog.Builder(this).setTitle("确认")
		 * .setMessage(strMessage+telMgr.getDeviceId()).setPositiveButton("确定",
		 * null) .show();
		 */

	}

	OnKeyListener onKey = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {

			// TODO Auto-generated method stub

			if (keyCode == KeyEvent.KEYCODE_ENTER) {

				DealBarcode();
				// InputMethodManager imm =
				// (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

				// if(imm.isActive()){

				// imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0
				// );

				// }

				return true;

			}

			return false;

		}

	};

	private void GetTask() {
		if (GetTaskListByHand()) {
			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);
		} else {
			Message msg = new Message();
			msg.what = 2;
			handler.sendMessage(msg);
		}
	}

	private boolean GetTaskListByHand() {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 登陆人身份
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=GetTaskMain&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				nTryConnectTimes = 0;
				strResponse = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse.startsWith("SUCCESS")) {
					Toast.makeText(getApplicationContext(), "成功刷新任务", Toast.LENGTH_SHORT).show();
					strResponse = strResponse.substring(strResponse.indexOf("SUCCESS:") + 8,
							strResponse.indexOf("SUCCESSEND"));
					return true;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse).setPositiveButton("确定", null)
							.show();
					return false;
				}
			} else {
				new AlertDialog.Builder(this).setTitle("确认").setMessage("返回了代表错误的状态" + nStatus)
						.setPositiveButton("确定", null).show();
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();

			nTryConnectTimes++;
			return false;
		}
		return false;
	}

	private boolean GetTaskList() {
		HttpPost httpRequest = new HttpPost("123");
		FileService fs = new FileService(this);
		String strTxt = "";
		try {
			strTxt = fs.read(GlobalInfo.m_TmpFileName);

		} catch (Exception e) {
		}

		// new
		// AlertDialog.Builder(this).setTitle("确认").setMessage("文件内容："+strTxt)
		// .setPositiveButton("确定", null).show();

		if (strTxt != null && strTxt.length() > 0) {
			String straDetail[] = strTxt.split(GlobalInfo.m_CommandSplitString);
			String strNewCommandText = "";
			new AlertDialog.Builder(this).setTitle("确认").setMessage("需发送的命令的个数：" + straDetail.length)
					.setPositiveButton("确定", null).show();
			for (int i = straDetail.length - 1; i >= 0; i--) {
				new AlertDialog.Builder(this).setTitle("确认").setMessage("第" + i + "命令:" + straDetail[i])
						.setPositiveButton("确定", null).show();
				httpRequest = new HttpPost(straDetail[i]);
				try {
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

					// 若状态码为200 ==> OK
					int nStatus = httpResponse.getStatusLine().getStatusCode();
					if (nStatus == 200) {
						String strTemp = EntityUtils.toString(httpResponse.getEntity()).trim();
						if (strTemp.startsWith("SUCCESS")) {
						} else {
							new AlertDialog.Builder(this).setTitle("确认").setMessage("发送第" + i + "命令返回状态：" + nStatus)
									.setPositiveButton("确定", null).show();
							strNewCommandText += straDetail[i] + GlobalInfo.m_CommandSplitString;
						}
					}
				} catch (Exception e) {
					new AlertDialog.Builder(this).setTitle("确认").setMessage("发送第" + i + "命令出现异常：" + e.getMessage())
							.setPositiveButton("确定", null).show();
					strNewCommandText += straDetail[i] + GlobalInfo.m_CommandSplitString;
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

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=GetTaskMain&Value=" + strUploadData;
		httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				nTryConnectTimes = 0;
				strResponse = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse.startsWith("SUCCESS")) {
					strResponse = strResponse.substring(strResponse.indexOf("SUCCESS:") + 8,
							strResponse.indexOf("SUCCESSEND"));
					return true;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse).setPositiveButton("确定", null)
							.show();
					return false;
				}
			} else {
				new AlertDialog.Builder(this).setTitle("确认").setMessage("返回了代表错误的状态" + nStatus)
						.setPositiveButton("确定", null).show();
			}
		} catch (Exception e) {
			/*
			 * new AlertDialog.Builder(this).setTitle("确认")
			 * .setMessage("获取任务过程出现异常" + e.getMessage())
			 * .setPositiveButton("确定", null).show();
			 */
			nTryConnectTimes++;
			return false;
		}
		return false;
	}

	private boolean Split2TaskInfo() {
		if (bInSplit2TaskInfoFunction)
			return false;

		bInSplit2TaskInfoFunction = true;
		boolean bReturn = true;
		if (strResponse == null || strResponse == "") {
			bInSplit2TaskInfoFunction = false;
			return false;
		}

		lstTaskInfo = new ArrayList<TaskInfo>();
		String straTask[] = strResponse.split("\n");
		if (straTask == null || straTask.length == 0) {
			bInSplit2TaskInfoFunction = false;
			return false;
		}

		int nHave = 0;
		for (int i = straTask.length - 1; i >= 0; i--) {
			SplitTaskInfo split = new SplitTaskInfo(straTask[i]);
			TaskInfo ti = split.Split();
			if (ti == null)
				continue;

			/*
			 * new
			 * AlertDialog.Builder(this).setTitle("确认").setMessage(ti.m_BillNo
			 * +"---"+ti.m_State+"---"+ti.m_String6) .setPositiveButton("确定",
			 * null).show();
			 */
			if (ti.m_State.equals("派工")) {
				if (nHave == 0)
					nHave = 1;
				if (ti.m_EmergencyLevel.equals("一级"))
					nHave = 2;
				/*
				 * bReceiveNewTask = false; ConfirmAcceptTaskActivity dlg = new
				 * ConfirmAcceptTaskActivity( this, "确认接受该任务吗：|起点：" +
				 * ti.m_FromLocation + "|床位：" + ti.m_FromSickbed + "|终点：" +
				 * ti.m_ToLocation + "|时间：" + ti.m_PatientBirthday + "|类型：" +
				 * ti.m_TargetType + "|工具：" + ti.m_String1);
				 * 
				 * int nReturn = dlg.showDialog(); if (nReturn == 0)//
				 * 接受该任务则添加到任务列表中 { bReceiveNewTask = true;
				 * AcceptOrRefuseTask(ti.m_BillNo, "ACCEPT"); ti.m_State =
				 * "已接受"; } else if (nReturn == 1) { // 拒绝任务则继续下一个任务的处理
				 * bReceiveNewTask = true; AcceptOrRefuseTask(ti.m_BillNo,
				 * "REFUSE"); playThread.StopAlarmRing(); continue; }
				 * playThread.StopAlarmRing();
				 */
				AcceptOrRefuseTask(ti.m_BillNo, "ACCEPT");
				ti.m_State = "已接受";

				lstTaskInfo.add(ti);
			} else
				lstTaskInfo.add(ti);
		}

		if (nHave > 0) {
			Vibrator vibrator;
			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			// long[] pattern = { 100, 400, 100, 400 }; // 停止 开启 停止 开启
			vibrator.vibrate(1000);
			if (playThread != null)
				playThread.StopAlarmRing();
			playThread = new PlayRingThread(this);
			playThread.strType = "new";
			if (nHave == 2)
				playThread.strType = "emergency";

			playThread.bLooping = false;
			boolean screen = pm.isScreenOn();
			if (!screen)
				playThread.bLooping = true;

			playThread.start();
		}
		bInSplit2TaskInfoFunction = false;
		return bReturn;
	}

	private void Bind2ListView() {
		// 生成动态数组，加入数据
		int nAcceptedFirstIndex=lvwAccepted.getFirstVisiblePosition();
		int nExecutedFirstIndex=lvwExecuting.getFirstVisiblePosition();
		final List<TaskInfo> lstExecuting = new ArrayList<TaskInfo>();
		final List<TaskInfo> lstAccepted = new ArrayList<TaskInfo>();
		for(int i=0;i<lstTaskInfo.size();i++) {
			if(lstTaskInfo.get(i).m_State.equals("执行中"))
				lstExecuting.add(lstTaskInfo.get(i));
			else
				lstAccepted.add(lstTaskInfo.get(i));
		}

		adapterAccepted=new TaskListAdapter(getApplicationContext(),lstAccepted);
		lvwAccepted.setAdapter(adapterAccepted);

		adapterExecuting=new TaskListAdapter(getApplicationContext(),lstExecuting);
		lvwExecuting.setAdapter(adapterExecuting);

		btnViewExecuting.setText("执行中(" + lstExecuting.size() + ")");
		btnViewAccepted.setText("已接受(" + lstAccepted.size() + ")");
		// 添加点击
		lvwExecuting.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				try {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();
					bundle.putString("billNo", (String) lstExecuting.get(arg2).m_BillNo);
					bundle.putString("fromLocationCode", lstExecuting.get(arg2).m_FromLocationCode);
					bundle.putString("autostart", "false");
					intent.putExtras(bundle);

					if (lstExecuting.get(arg2).m_TargetType.equals("病人")) {
						intent.setClass(TaskListActivity.this, PatientActivity.class);
						// startActivityForResult(intent, 1);// 请求码
						startActivity(intent);// 请求码
					} else {
						intent.setClass(TaskListActivity.this, OtherTransferActivity.class);
						// startActivityForResult(intent, 2);// 请求码
						startActivity(intent);
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});

		// 添加点击
		lvwAccepted.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				try {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();
					bundle.putString("billNo", (String) lstAccepted.get(arg2).m_BillNo);
					bundle.putString("fromLocationCode", lstAccepted.get(arg2).m_FromLocationCode);
					bundle.putString("autostart", "false");
					intent.putExtras(bundle);

					if (lstAccepted.get(arg2).m_TargetType.equals("病人")) {
						intent.setClass(TaskListActivity.this, PatientActivity.class);
						// startActivityForResult(intent, 1);// 请求码
						startActivity(intent);// 请求码
					} else {
						intent.setClass(TaskListActivity.this, OtherTransferActivity.class);
						// startActivityForResult(intent, 2);// 请求码
						startActivity(intent);// 请求码
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});
		lvwAccepted.setSelection(nAcceptedFirstIndex);
		lvwExecuting.setSelection(nExecutedFirstIndex);
	}

	private boolean GetTaskList2() {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append("");// 登陆人身份
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		try {
			sbUploadData.append(URLEncoder.encode(tbxBarcode.getText().toString(), "UTF-8"));// 起点科室
		} catch (Exception exp) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("编码时出现异常:" + exp.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append("NEW");// 状态
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 单号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(GlobalInfo.m_VerifyCode);// 校验码
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=GetTaskMain2&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				String strResponse2 = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse2.startsWith("SUCCESS")) {
					strResponse2 = strResponse2.substring(strResponse2.indexOf("SUCCESS:") + 8,
							strResponse2.indexOf("SUCCESSEND"));
					if (strResponse2.length() == 0) {
						return true;
					} else {
						if (playThread != null)
							playThread.StopAlarmRing();
						playThread = new PlayRingThread(TaskListActivity.this);
						playThread.strType = "select";

						playThread.start();

						Intent intent = new Intent();

						Bundle bundle = new Bundle();
						bundle.putString("response", strResponse2);
						intent.putExtras(bundle);

						intent.setClass(TaskListActivity.this, TaskList2Activity.class);
						startActivity(intent);
					}
					return true;
				} else if (strResponse2.startsWith("ERROR")) {
					strResponse2 = strResponse2.substring(strResponse2.indexOf("ERROR:") + 6,
							strResponse2.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse2).setPositiveButton("确定", null)
							.show();
					return false;
				}
			} else {
				new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程返回错误的状态" + nStatus)
						.setPositiveButton("确定", null).show();
				return false;
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		return false;
	}

	@Override
	/**
	 * 当跳转的activity(被激活的activity)使用完毕,销毁的时候调用该方法
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SCAN) {
			if (data != null) {
				String content = data.getStringExtra("codedContent");
				tbxBarcode.setText(content);
				DealBarcode();
			}
		} else {
			if (data != null) {
				if (GetTaskList()) {
					if (Split2TaskInfo()) {
						Bind2ListView();
					}
				}
			}
		}
	}

	private boolean GetTaskList3(String strBarcode) {
		StringBuilder sbUploadData = new StringBuilder("");

		try {
			sbUploadData.append(strBarcode);// 起点科室
		} catch (Exception exp) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("编码时出现异常:" + exp.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		String strUploadData = sbUploadData.toString();
		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=GetTaskMain3&Value=" + strUploadData;

		HttpPost httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				String strResponse2 = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse2.startsWith("SUCCESS")) {
					strResponse2 = strResponse2.substring(strResponse2.indexOf("SUCCESS:") + 8,
							strResponse2.indexOf("SUCCESSEND"));
					if (strResponse2.length() == 0) {
						return true;
					} else {
						if (playThread != null)
							playThread.StopAlarmRing();
						playThread = new PlayRingThread(TaskListActivity.this);

						String straTask[] = strResponse2.split("\n");

						if (straTask.length == 1) {
							SplitTaskInfo split = new SplitTaskInfo(straTask[0]);
							TaskInfo ti = split.Split();
							GlobalInfo.UpdateLastLocation(ti.m_FromLocationCode, ti.m_ToLocationCode);
							AcceptOrRefuseTask(ti.m_BillNo,
									"START" + GlobalInfo.m_LoginAccount + GlobalInfo.m_PersonName);
							playThread.strType = "start";
							playThread.start();

							if (GetTaskList()) {
								if (Split2TaskInfo()) {
									Bind2ListView();
								}
							}

							return true;
						}

						playThread.strType = "select";

						playThread.start();

						Intent intent = new Intent();

						Bundle bundle = new Bundle();
						bundle.putString("response", strResponse2);
						intent.putExtras(bundle);

						intent.setClass(TaskListActivity.this, TaskList2Activity.class);
						startActivityForResult(intent, 1);
					}
					return true;
				} else if (strResponse2.startsWith("ERROR")) {
					strResponse2 = strResponse2.substring(strResponse2.indexOf("ERROR:") + 6,
							strResponse2.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse2).setPositiveButton("确定", null)
							.show();
					return false;
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		return false;
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

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		// new AlertDialog.Builder(this).setTitle("确认"
		// ).setMessage(strURI).setPositiveButton("确定", null ).show();
		String strResponse = "";
		try {
			// httpRequest.setEntity(new UrlEncodedFormEntity(params,
			// HTTP.UTF_8));
			// 发出HTTP request, 取得HTTP response
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity()).trim();
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
					strResponse = strResponse.substring(strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse).setPositiveButton("确定", null)
							.show();
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("接受或拒绝任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return;
		}
	}

	public void onDestroy() {
		super.onDestroy();
		//蓝牙定时任务停止
		myBleTask.stop();
		BLEUtils.getInstance().destroy();
		releaseWakeLock();
		isDestroy = true;
		// throw new NullPointerException();
	}

	public synchronized void onResume() {
		super.onResume();

		tbxBarcode.requestFocus();
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

	private void ChangeState(String strBillNo, String strReason, String strState) {
		boolean bHaveException = false;
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(strBillNo);// 单号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 人员
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strReason);// 登陆人身份
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strState);// 分隔符
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=UpdateTask&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		String strResponse = "";
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse.startsWith("SUCCESS")) {
					return;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse).setPositiveButton("确定", null)
							.show();
				}
			}
		} catch (Exception e) {
			if (!strState.startsWith("END") && !strState.startsWith("START")) {
				new AlertDialog.Builder(this).setTitle("确认").setMessage("改变任务状态过程出现异常" + strState + e.getMessage())
						.setPositiveButton("确定", null).show();
				return;
			} else
				bHaveException = true;
		}
		if (bHaveException) {
			FileService fs = new FileService(this);
			try {
				fs.append(GlobalInfo.m_TmpFileName, strURI + GlobalInfo.m_CommandSplitString);
			} catch (Exception ee) {
				new AlertDialog.Builder(this).setTitle("确认").setMessage(ee.getMessage()).setPositiveButton("确定", null)
						.show();
			}
		}
	}

	// 扫描打卡。上班或下班时用
	// 如果打卡成功，再改变显示的人员上班/未上班的状态
	private void OnOffDuty(String strOnOff) {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);// 工号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strOnOff);// 上班或下班的标识

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=OnOffDuty&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		String strResponse = "";
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse.startsWith("SUCCESS")) {
					/*
					 * new AlertDialog.Builder(this).setTitle("确认")
					 * .setMessage("打卡成功").setPositiveButton("确定", null)
					 * .show();
					 */
					if (strOnOff.endsWith("ON"))
						GlobalInfo.m_OnOffDutyState = "上班";
					else if (strOnOff.endsWith("OFF"))
						GlobalInfo.m_OnOffDutyState = "未上班";
					setTitle("待执行任务列表 使用人：" + GlobalInfo.m_PersonName + " 状态 :" + GlobalInfo.m_OnOffDutyState + " "
							+ GlobalInfo.m_OnOffLineState);
					/*
					 * lblUserInfo.setText("用户名:" + GlobalInfo.m_PersonName +
					 * " 状态 :" + GlobalInfo.m_OnOffDutyState + " " +
					 * GlobalInfo.m_OnOffLineState);
					 */
					return;
				} else if (strResponse.startsWith("ERROR")) {
					if (playThread != null)
						playThread.StopAlarmRing();
					playThread = new PlayRingThread(this);
					playThread.strType = "dakatishi";

					playThread.start();
					strResponse = strResponse.substring(strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse).setPositiveButton("确定", null)
							.show();
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return;
		}
	}

	// 添加联系人，使用事务
	public void testAddContact(String strName, String strPhone) throws Exception {
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = getBaseContext().getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri).withValue("account_name", null).build();
		operations.add(op1);

		uri = Uri.parse("content://com.android.contacts/data");
		ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/name")
				.withValue("data2", strName).build();
		operations.add(op2);

		ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/phone_v2")
				.withValue("data1", strPhone).withValue("data2", "2").build();
		operations.add(op3);

		/*
		 * ContentProviderOperation op4 =
		 * ContentProviderOperation.newInsert(uri)
		 * .withValueBackReference("raw_contact_id", 0) .withValue("mimetype",
		 * "vnd.android.cursor.item/email_v2") .withValue("data1",
		 * "asdfasfad@163.com") .withValue("data2", "2") .build();
		 * operations.add(op4);
		 */

		resolver.applyBatch("com.android.contacts", operations);
	}

	public void testDelete(String strName) throws Exception {
		// 根据姓名求id
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = this.getBaseContext().getContentResolver();
		Cursor cursor = resolver.query(uri, new String[] { Data._ID }, "display_name=?", new String[] { strName },
				null);
		if (cursor.moveToFirst()) {
			int id = cursor.getInt(0);
			// 根据id删除data中的相应数据
			resolver.delete(uri, "display_name=?", new String[] { strName });
			uri = Uri.parse("content://com.android.contacts/data");
			resolver.delete(uri, "raw_contact_id=?", new String[] { id + "" });
		}
	}

	public void testReadAll() {
		// uri = content://com.android.contacts/contacts
		Uri uri = Uri.parse("content://com.android.contacts/contacts"); // 访问raw_contacts表
		ContentResolver resolver = this.getBaseContext().getContentResolver();
		Cursor cursor = resolver.query(uri, new String[] { Data._ID }, null, null, null); // 获得_id属性
		while (cursor.moveToNext()) {
			StringBuilder buf = new StringBuilder();
			int id = cursor.getInt(0);// 获得id并且在data中寻找数据
			buf.append("id=" + id);
			uri = Uri.parse("content://com.android.contacts/contacts/" + id + "/data"); // 如果要获得data表中某个id对应的数据，则URI为content://com.android.contacts/contacts/#/data
			Cursor cursor2 = resolver.query(uri, new String[] { Data.DATA1, Data.MIMETYPE }, null, null, null); // data1存储各个记录的总数据，mimetype存放记录的类型，如电话、email等
			while (cursor2.moveToNext()) {
				String data = cursor2.getString(cursor2.getColumnIndex("data1"));
				if (cursor2.getString(cursor2.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/name")) { // 如果是名字
					buf.append(",name=" + data);
					if (!data.contains("其他") && !data.contains("本机")) {
						try {
							testDelete(data);
						} catch (Exception exp) {

						}
					}
				}
			}
		}
	}

	public String readSDFile(String fileName) throws IOException {

		File file = new File(fileName);

		FileInputStream fis = new FileInputStream(file);

		int length = fis.available();

		byte[] buffer = new byte[length];
		fis.read(buffer);

		String res = EncodingUtils.getString(buffer, "UTF-8");

		fis.close();
		return res;
	}

	// 写文件
	public void writeSDFile(String fileName, String write_str) throws IOException {

		File file = new File(fileName);

		FileOutputStream fos = new FileOutputStream(file);

		byte[] bytes = write_str.getBytes();

		fos.write(bytes);

		fos.close();
	}

	private boolean UpdateAPK() {
		boolean bReturn = true;
		// downLoadFile(GlobalInfo.m_APKURL);
		UpdateManager manager = new UpdateManager(TaskListActivity.this);
		manager.checkUpdate();
		return bReturn;
	}

	private boolean GetPhoneList() {
		/*
		 * try { writeSDFile("123.txt","87654321"); } catch(Exception exp1) {
		 * new AlertDialog.Builder(this).setTitle("确认")
		 * .setMessage("write"+exp1.getMessage()) .setPositiveButton("确定",
		 * null).show(); StringBuilder sbUploadData = new
		 * StringBuilder(GlobalInfo.m_LoginAccount); }
		 */

		/*
		 * String str=""; try { str=readSDFile("phonenumber.txt"); }
		 * catch(Exception exp2) { new AlertDialog.Builder(this).setTitle("确认")
		 * .setMessage("read"+exp2.getMessage()) .setPositiveButton("确定",
		 * null).show(); StringBuilder sbUploadData = new
		 * StringBuilder(GlobalInfo.m_LoginAccount); } new
		 * AlertDialog.Builder(this).setTitle("确认") .setMessage(str)
		 * .setPositiveButton("确定", null).show();
		 */
		StringBuilder sbUploadData = new StringBuilder(GlobalInfo.m_LoginAccount);

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appSysCode.aspx?Method=GetPhoneList&Value="
				+ strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				String strResponse2 = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse2.startsWith("SUCCESS")) {
					strResponse2 = strResponse2.substring(strResponse2.indexOf("SUCCESS:") + 8,
							strResponse2.indexOf("SUCCESSEND"));
					if (strResponse2.length() == 0) {
						return true;
					} else {
						testReadAll();
						String[] stra = strResponse2.split("fgf");

						boolean bHaveError = false;
						for (int nnn = 0; nnn < stra.length; nnn += 2) {
							try {
								testAddContact(stra[nnn], stra[nnn + 1]);
							} catch (Exception exp) {
								bHaveError = true;
							}
						}
						if (!bHaveError)
							new AlertDialog.Builder(this).setTitle("确认").setMessage("更新成功")
									.setPositiveButton("确定", null).show();
					}
					return true;
				} else if (strResponse2.startsWith("ERROR")) {
					strResponse2 = strResponse2.substring(strResponse2.indexOf("ERROR:") + 6,
							strResponse2.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse2).setPositiveButton("确定", null)
							.show();
					return false;
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}
		return false;
	}

	public static void saveLoginInfo(Context context, String username, String password, String strSaved) {
		// 获取SharedPreferences对象
		SharedPreferences sharedPre = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		// 获取Editor对象
		Editor editor = sharedPre.edit();
		// 设置参数
		editor.putString("username", username);
		editor.putString("password", password);
		editor.putString("saveinfo", strSaved);
		// 提交
		editor.commit();
	}

	protected File downLoadFile(String httpUrl) {
		// TODO Auto-generated method stub
		final String fileName = "updata.apk";
		File tmpFile = new File("/sdcard/update");
		if (!tmpFile.exists()) {
			tmpFile.mkdir();
		}
		final File file = new File("/sdcard/update/" + fileName);

		try {
			URL url = new URL(httpUrl);
			try {
				new AlertDialog.Builder(this).setTitle("确认").setMessage(httpUrl).setPositiveButton("确定", null).show();
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				// conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setUseCaches(false);
				conn.setInstanceFollowRedirects(true);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				conn.setChunkedStreamingMode(5);
				conn.connect();
				InputStream is = conn.getInputStream();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buf = new byte[256];
				conn.connect();
				double count = 0;
				if (conn.getResponseCode() >= 400) {
					Toast.makeText(TaskListActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
				} else {
					while (count <= 100) {
						if (is != null) {
							int numRead = is.read(buf);
							if (numRead <= 0) {
								break;
							} else {
								fos.write(buf, 0, numRead);
							}

						} else {
							break;
						}

					}
				}

				conn.disconnect();
				fos.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block

				new AlertDialog.Builder(this).setTitle("确认").setMessage("IO:" + e.getMessage())
						.setPositiveButton("确定", null).show();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block

			new AlertDialog.Builder(this).setTitle("确认").setMessage("Mal:" + e.getMessage())
					.setPositiveButton("确定", null).show();
		}

		return file;
	}

	private void RecordPatientNo(String strBillNo, String strPatientNo) {
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(strBillNo);// 单号
		sbUploadData.append(GlobalInfo.m_SplitString);// 分隔符
		sbUploadData.append(strPatientNo);// 人员

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP + "/appTask.aspx?Method=RecordPatientNo&Value="
				+ strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		String strResponse = "";
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				strResponse = EntityUtils.toString(httpResponse.getEntity()).trim();
				if (strResponse.startsWith("SUCCESS")) {
					Toast.makeText(this, "记录病人编码成功", Toast.LENGTH_LONG);
					return;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(strResponse.indexOf("ERROR:") + 8,
							strResponse.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认").setMessage(strResponse).setPositiveButton("确定", null)
							.show();
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return;
		}
	}

	private void DealBarcode() {
		String strTaskToLocationCode = "";
		boolean bHave = false;
		int nCount = 0, nSel = -1;
		// 先判断是否有终点码为扫描到的条码，且状态为执行中的任务。如果有，则直接使这些任务完工。

		String strBarcode = tbxBarcode.getText().toString().replace(" ", "");

		/*
		 * new AlertDialog.Builder(this).setTitle("确认")
		 * .setMessage("扫描了条码:"+strBarcode) .setPositiveButton("确定",
		 * null).show();
		 */

		if (strBarcode == null || strBarcode.length() == 0)
			return;

		if (strBarcode.length() < 5) {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("条码长度太短").setPositiveButton("确定", null).show();
			return;
		}

		if (strBarcode.startsWith(GlobalInfo.m_LoginAccount.substring(0, 5)) || strBarcode.equals("LIONTOWN")
				|| strBarcode.equals("CONTACT") || strBarcode.equals("UPDATE")) {
		} else {
			if (strBarcode.length() > 18)
				strBarcode = strBarcode.substring(10, 18);
			else {
				if (GlobalInfo.m_VerifyCode.equals("03023")) {
					if (strBarcode.length() > (GlobalInfo.m_nSplitLeft + GlobalInfo.m_nSplitRight))
						strBarcode = strBarcode.substring(0, 7);// (GlobalInfo.m_nSplitLeft,strBarcode.length()-GlobalInfo.m_nSplitRight);
				}

			}
		}
		strBarcode = strBarcode.replace("REG", "");

		tbxBarcode.setText("");
		if (strBarcode.equals(GlobalInfo.m_AdministratorBarcode)) {
			FileService fs = new FileService(this);
			try {
				fs.save(GlobalInfo.m_UserInfoFileName, "");
			} catch (Exception e) {
			}

			saveLoginInfo(TaskListActivity.this, GlobalInfo.m_LoginAccount, "", "0");

			System.exit(0);
		}
		if (strBarcode.equals("CONTACT")) {
			try {
				GetPhoneList();
				return;
			} catch (Exception expContact) {
				new AlertDialog.Builder(this).setTitle("确认").setMessage(expContact.getMessage())
						.setPositiveButton("确定", null).show();
			}
		}
		if (strBarcode.toUpperCase().equals("UPDATE")) {
			try {
				UpdateAPK();
				return;
			} catch (Exception expUpdate) {
				new AlertDialog.Builder(this).setTitle("确认").setMessage(expUpdate.getMessage())
						.setPositiveButton("确定", null).show();
			}
		}
		String strTemp = GlobalInfo.m_LoginAccount.substring(0, 5) + "2ON";
		if (strTemp.equals(strBarcode.toUpperCase())) {
			OnOffDuty("ON");
			return;
		} else if ((GlobalInfo.m_LoginAccount.substring(0, 5) + "2OFF").equals(strBarcode.toUpperCase())) {
			OnOffDuty("OFF");
			return;
		} /*
			 * else if ("12345678".equals(strBarcode.toUpperCase())) { Intent i
			 * = new Intent(this, TrainManageActivity.class); startActivity(i);
			 * return; }
			 */

		if (lstTaskInfo != null) {
			String strTempBarcode = strBarcode;
			if (strTempBarcode.length() > 18)
				strTempBarcode = strTempBarcode.substring(10, 18);
			for (int i = lstTaskInfo.size() - 1; i >= 0; i--) {
				String str1 = lstTaskInfo.get(i).m_PatientNo;
				if (str1.equals(strTempBarcode) && lstTaskInfo.get(i).m_State.equals("已接受")
						&& lstTaskInfo.get(i).m_TargetType.endsWith("病人")) {

					/*
					 * new AlertDialog.Builder(this).setTitle("确认")
					 * .setMessage("扫描了条码:"+strBarcode+"。根据病人编码开始任务")
					 * .setPositiveButton("确定", null).show();
					 */

					ChangeState(lstTaskInfo.get(i).m_BillNo, "", "START");
					bHave = true;
					strTaskToLocationCode = lstTaskInfo.get(i).m_ToLocationCode;
					// lstTaskInfo.get(i).m_State="执行中";
					RecordPatientNo(lstTaskInfo.get(i).m_BillNo, strTempBarcode);
				}
			}
		}
		if (bHave) {
			GlobalInfo.UpdateLastLocation(strBarcode, strTaskToLocationCode);
			Toast.makeText(this, Integer.toString(lstTaskInfo.size()), Toast.LENGTH_LONG);
			// Bind2ListView();
			if (playThread != null)
				playThread.StopAlarmRing();
			playThread = new PlayRingThread(TaskListActivity.this);
			playThread.strType = "start";
			playThread.start();

			if (GetTaskList()) {
				if (Split2TaskInfo()) {
					Bind2ListView();
				}
			}
			return;
		}

		bHave = false;
		if (lstTaskInfo != null) {
			for (int i = lstTaskInfo.size() - 1; i >= 0; i--) {
				String str1 = lstTaskInfo.get(i).m_ToLocationCode;
				if (str1.contains(strBarcode) && lstTaskInfo.get(i).m_State.equals("执行中")
						&& lstTaskInfo.get(i).m_TargetType.endsWith("病人")) {

					/*
					 * new AlertDialog.Builder(this).setTitle("确认")
					 * .setMessage("扫描了条码:"+strBarcode+"。根据终点科室编码结束任务")
					 * .setPositiveButton("确定", null).show();
					 */

					ChangeState(lstTaskInfo.get(i).m_BillNo, "", "END" + strBarcode);
					bHave = true;
					strTaskToLocationCode = lstTaskInfo.get(i).m_ToLocationCode;
					lstTaskInfo.remove(i);
				}
			}
		}
		if (bHave) {
			GlobalInfo.UpdateLastLocation(strBarcode, strTaskToLocationCode);
			Toast.makeText(this, Integer.toString(lstTaskInfo.size()), Toast.LENGTH_LONG);
			Bind2ListView();
			if (playThread != null)
				playThread.StopAlarmRing();
			playThread = new PlayRingThread(TaskListActivity.this);
			playThread.strType = "finish";
			playThread.start();

			if (GetTaskList()) {
				if (Split2TaskInfo()) {
					Bind2ListView();
				}
			}
			return;
		}

		// 再判断是否有起点为扫描到的条码，如果有且只有一个，则直接使该任务变为执行中，跳转到该任务的界面
		// 如果有多个，则提示请选择任务
		if (lstTaskInfo != null) {
			for (int i = 0; i < lstTaskInfo.size(); i++) {
				String str1 = lstTaskInfo.get(i).m_FromLocationCode;
				if (str1.contains(strBarcode) && lstTaskInfo.get(i).m_State.equals("已接受")) {
					nCount++;
					nSel = i;
					strTaskToLocationCode = lstTaskInfo.get(i).m_ToLocationCode;
				}
			}
		}

		if (nCount == 1) {
			GlobalInfo.UpdateLastLocation(strBarcode, strTaskToLocationCode);
			Intent intent = new Intent();

			Bundle bundle = new Bundle();
			bundle.putString("billNo", lstTaskInfo.get(nSel).m_BillNo);
			bundle.putString("fromLocationCode", strBarcode);
			bundle.putString("autostart", "true");
			intent.putExtras(bundle);

			if (playThread != null)
				playThread.StopAlarmRing();
			playThread = new PlayRingThread(TaskListActivity.this);

			playThread.strType = "start";

			playThread.start();

			/*
			 * new AlertDialog.Builder(this).setTitle("确认")
			 * .setMessage("扫描了条码:"+strBarcode+"。根据起点科室条码开始任务")
			 * .setPositiveButton("确定", null).show();
			 * 
			 * new AlertDialog.Builder(this).setTitle("确认")
			 * .setMessage("扫描了条码:"+strBarcode+"。开始任务") .setPositiveButton("确定",
			 * null).show();
			 */

			ChangeState(lstTaskInfo.get(nSel).m_BillNo, strBarcode, "START");
			lstTaskInfo.get(nSel).m_State = "执行中";
			Bind2ListView();

			if (lstTaskInfo.get(nSel).m_TargetType.equals("病人")) {
				intent.setClass(TaskListActivity.this, PatientActivity.class);
				startActivityForResult(intent, 1);// 请求码
			} else {
				intent.setClass(TaskListActivity.this, OtherTransferActivity.class);
				startActivityForResult(intent, 2);// 请求码
			}
		} else if (strBarcode.startsWith(GlobalInfo.m_LoginAccount.substring(0, 5))) {
			GlobalInfo.UpdateLastLocation(strBarcode, "");
			// 从服务器得到起点为扫描到的条码，并且状态为新建的任务，显示在新的界面中
			tbxBarcode.setText(strBarcode);
			GetTaskList2();
			tbxBarcode.setText("");
		} else {
			// 从服务器得到起点为扫描到的条码，并且状态为新建的任务，显示在新的界面中

			// tbxBarcode.setText(strBarcode);
			GetTaskList3(strBarcode);
			// tbxBarcode.setText("");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			playThread.StopAlarmRing();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.pt_btn_Deal:
				DealBarcode();
				break;
			case R.id.pt_btn_GetTask:
				GetTask();
				break;
			case R.id.pt_btn_ViewAccepted:
				lvwExecuting.setVisibility(View.GONE);
				lvwAccepted.setVisibility(View.VISIBLE);
				btnViewExecuting.setEnabled(true);
				btnViewAccepted.setEnabled(false);
				btnViewExecuting.setBackgroundColor(Color.parseColor("#006f67"));
				btnViewAccepted.setBackgroundColor(Color.GRAY);
				break;
			case R.id.pt_btn_ViewExecuting:
				lvwExecuting.setVisibility(View.VISIBLE);
				lvwAccepted.setVisibility(View.GONE);
				btnViewExecuting.setEnabled(false);
				btnViewAccepted.setEnabled(true);
				btnViewAccepted.setBackgroundColor(Color.parseColor("#006f67"));
				btnViewExecuting.setBackgroundColor(Color.GRAY);
				break;
			case R.id.pt_btn_Scan:
				//替换为新的二维码构建工具
				startActivityForResult(new Intent(TaskListActivity.this, CaptureActivity.class), REQUEST_CODE_SCAN);
				break;
			case R.id.btnNewDrugTask:
				startActivityForResult(new Intent(TaskListActivity.this, NewDrugTaskActivity.class), REQUEST_CODE_SCAN);
				break;
		}
	}

	/*
	 * @Override public void onAttachedToWindow() { super.onAttachedToWindow();
	 * this.getWindow().setType(
	 * WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG); }
	 */

	/*
	 * Runnable mDisableHomeKeyRunnable = new Runnable() {
	 * 
	 * @Override public void run() { disableHomeKey(); } }; Handler mHandler =
	 * new Handler();
	 * 
	 * public void disableHomeKey() {
	 * this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); }
	 */

	// handler类接收数据
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				GlobalInfo.m_OnOffLineState = "在线";
				setTitle("待执行任务列表 使用人：" + GlobalInfo.m_PersonName + " 状态 :" + GlobalInfo.m_OnOffDutyState + " "
						+ GlobalInfo.m_OnOffLineState);

				/*
				 * lblUserInfo.setText("用户名:" + GlobalInfo.m_PersonName +
				 * " 状态 :" + GlobalInfo.m_OnOffDutyState + " " +
				 * GlobalInfo.m_OnOffLineState);
				 */

				if (Split2TaskInfo()) {
					Bind2ListView();
				}
			} else if (msg.what == 2) {
				if (nTryConnectTimes == 3) {
					nTryConnectTimes = 0;
					GlobalInfo.m_OnOffLineState = "离线";
					setTitle("待执行任务列表 使用人：" + GlobalInfo.m_PersonName + " 状态 :" + GlobalInfo.m_OnOffDutyState + " "
							+ GlobalInfo.m_OnOffLineState);
					/*
					 * lblUserInfo.setText("用户名:" + GlobalInfo.m_PersonName +
					 * " 状态 :" + GlobalInfo.m_OnOffDutyState + " " +
					 * GlobalInfo.m_OnOffLineState);
					 */
				}
			}
		};
	};

	class ThreadShow implements Runnable {
		@Override
		public void run() {
			while (!isDestroy) {
				try {
					Thread.sleep(30000);
					// 从服务器得到任务列表
					if (bReceiveNewTask) {
						if (GetTaskList()) {
							Message msg = new Message();
							msg.what = 1;
							handler.sendMessage(msg);
						} else {
							Message msg = new Message();
							msg.what = 2;
							handler.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * 监听音量按键的线程
	 */
	private Thread volumeChangeThread;

	/**
	 * 持续监听音量变化 说明： 当前音量改变时，将音量值重置为最大值减2
	 */
	public void onVolumeChangeListener() {
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumeChangeThread = new Thread() {
			public void run() {
				while (!isDestroy) {
					int count = 0;
					boolean isDerease = false;
					// 监听的时间间隔
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						System.out.println("error in onVolumeChangeListener Thread.sleep(20) " + e.getMessage());
					}

					if (currentVolume < mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
						count++;
						currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						// 设置音量等于
						// maxVolume-2的原因是：当音量值是最大值和最小值时，按音量加或减没有改变，所以每次都设置为固定的值。
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 2,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						playThread.StopAlarmRing();
					}
					if (currentVolume > mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
						count++;
						currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 2,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						if (count == 1) {
							isDerease = true;
						}
						playThread.StopAlarmRing();

					}

					if (count == 2) {
						System.out.println("按下了音量+");

					} else if (isDerease) {
						System.out.println("按下了音量-");
					}

				}
			};
		};
		volumeChangeThread.start();
	}

	//扫描蓝牙设备，发现指定设备后报告到系统后台
	private void scanBleandReport() {
		BLEUtils.scan(new BleScanCallback() {
			@Override
			public void onScanFinished(List<BleDevice> scanResultList) {
				Log.w("BLE SCAN", "Ble Scan stoped "+getCurrentTime("HH:mm:ss"));

				if(sortCollection.size() > 0) {
				    //对结果按照信号强度排序
					Collections.sort(sortCollection, (ble1, ble2)->ble1.getRssi()>=ble2.getRssi()? -1:1);
					for(BleDevice item: sortCollection){
						Log.w("BLE SCAN", "Ble sorted mac:"+ item.getMac()+" rssi:"+item.getRssi());
					}
					BleDevice result = sortCollection.get(0);
					long current = System.currentTimeMillis();
					//如果蓝牙和上次上传的蓝牙相同且时间小于2分钟，则不上报数据
					if(result.getMac().equals(lastMac)&&((current - lastReportTime)<REPORT_INTERVAL)) {
						return;
					}else {
						for (DeptMacJson item:MyApp.getHospitalMacList()) {
							String itemMac = item.Mac;
							if(itemMac.equals(result.getMac())) {
								//记录该条数据
								Log.w("BLE SCAN", "Ble report 科室:"+ item.PropName+" mac:"+item.Mac);
								reportMac(item);
							}
						}
					}
				}

			}

			@Override
			public void onScanStarted(boolean success) {
				Log.w("BLE SCAN", "Ble Scan started "+getCurrentTime("HH:mm:ss"));
				if(sortCollection!=null){
					sortCollection.clear();
				}
			}

			@Override
			public void onScanning(BleDevice bleDevice) {
				Log.w("BLE SCAN", "Ble discovered mac"+bleDevice.getMac()+" rssi:"+bleDevice.getRssi() +" "+getCurrentTime("HH:mm:ss"));
				String mac = bleDevice.getMac();
				for (DeptMacJson item:MyApp.getHospitalMacList()) {
					String itemMac = item.Mac;
					//上报已匹配的蓝牙地址，且蓝牙信息不能为上次已经上报过的。
					if(itemMac.equals(mac)) {
						Log.w("BLE SCAN", "BLE mapped:"+mac);
						sortCollection.add(bleDevice);
					}
				}
			}
		});
	}
	//上报mac数据
	private void reportMac(DeptMacJson macJson){
		try {
			OkHttpClient client = new OkHttpClient();
			String url = GlobalInfo.NEW_API_URL + "wsformicromsg.asmx?wsdl";
			JSONObject obj = new JSONObject();
			obj.put("TransferCode", GlobalInfo.m_LoginAccount);
			obj.put("DeptCode", macJson.PropCode);
			obj.put("DeptName", macJson.PropName);
			obj.put("MacAddress", macJson.Mac);
			RequestBody body = RequestBody.create(MediaType.get("text/xml"), ParamsBuildUtils.bodyBuild("NewDeptBtMac",obj.toString()));
			Request request = new Request.Builder()
					.url(url)
					.post(body)
					.build();
			Response response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				Log.w("REPORT","MAC info commit success!!");
				//记录最后一次上报过的蓝牙地址
				lastMac = macJson.Mac;
				lastReportTime = System.currentTimeMillis();
			}
		}catch (JSONException exception) {

		}catch (IOException exception) {

		}
	}

	private Handler bleTaskHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 999) {
				scanBleandReport();
			}
		}
	};

	private String getCurrentTime(String pattern) {
		String result = "";
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		result = formatter.format(currentTime);
		return result;
	}
}
