package com.wisebox.gyb;

import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;

public class GetTaskService extends Service {
	boolean isStop = false;
	private WakeLock mWakeLock;

	// 申请设备电源锁
	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "");
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

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		acquireWakeLock();
		new Thread() {// 新建线程，每隔1秒发送一次广播，同时把i放进intent传出
			public void run() {
				while (!isStop) {
					Message msg = new Message();
					String strReturn = GetTaskList();

					msg.obj = strReturn;
					// TaskListActivity.myHandler.sendMessage(msg);

					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public void onDestroy() {

		// Log.i("TAG","Services onDestory");
		releaseWakeLock();
		isStop = true;// 即使service销毁线程也不会停止，所以这里通过设置isStop来停止线程
		super.onDestroy();

	}

	private String GetTaskList() {
		String strResponse = "";
		HttpPost httpRequest = new HttpPost("123");

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
				+ "/appTask.aspx?Method=GetTaskMain&Value=";

		try {
			strURI += URLEncoder.encode(strUploadData, "UTF-8");
			// httpRequest = new HttpPost(URLEncoder.encode(strURI, "UTF-8"));
			httpRequest = new HttpPost(strURI);
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
					return strResponse;
				} else if (strResponse.startsWith("ERROR")) {
					strResponse = strResponse.substring(
							strResponse.indexOf("ERROR:") + 6,
							strResponse.indexOf("ERROREND"));
					return "";
				}
			} else
				new AlertDialog.Builder(this).setTitle("确认").setMessage("123")
						.setPositiveButton("确定", null).show();
		} catch (Exception e) {
			/*
			 * new AlertDialog.Builder(this).setTitle("确认")
			 * .setMessage("获取任务过程出现异常" + e.getMessage())
			 * .setPositiveButton("确定", null).show();
			 */
			strResponse = e.getMessage();
			return "";
		}
		return "";
	}
}
