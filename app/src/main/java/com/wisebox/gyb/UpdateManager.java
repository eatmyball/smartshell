package com.wisebox.gyb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * @author coolszy
 * @date 2012-4-26
 * @blog http://blog.92coding.com
 */

public class UpdateManager {
	private static final int DOWNLOAD = 1;
	private static final int DOWNLOAD_FINISH = 2;
	private String mSavePath;
	private int progress;
	private boolean cancelUpdate = false;

	private Context mContext;
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;

	private String mVersion = "";
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD:
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				try
				{				
				installApk();
				}
				catch(Exception e)
				{
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG)
					.show();
				}
				break;
			default:
				break;
			}
		};
	};

	public UpdateManager(Context context) {
		this.mContext = context;
	}

	/**
	 */
	public void checkUpdate() {
		if (isUpdate()) {
			showNoticeDialog();
		} else {
			Toast.makeText(mContext, R.string.soft_update_no, Toast.LENGTH_LONG)
					.show();
		}
	}

	private String GetServerAPKVersion() {
		String strReturn = "";
		StringBuilder sbUploadData = new StringBuilder(GlobalInfo.m_APKID);

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appSysCode.aspx?Method=GetAPKVersion&Value="
				+ strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			// Èô×´Ì¬ÂëÎª200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				String strResponse2 = EntityUtils.toString(
						httpResponse.getEntity()).trim();
				if (strResponse2.startsWith("SUCCESS")) {
					strResponse2 = strResponse2.substring(
							strResponse2.indexOf("SUCCESS:") + 8,
							strResponse2.indexOf("SUCCESSEND"));
					
					if (strResponse2.length() == 0) {
						return "";
					} else {
						mVersion = strResponse2;
						return strResponse2;
					}
				} else if (strResponse2.startsWith("ERROR")) {
					strResponse2 = strResponse2.substring(
							strResponse2.indexOf("ERROR:") + 6,
							strResponse2.indexOf("ERROREND"));
					new AlertDialog.Builder(mContext).setTitle("È·ÈÏ")
							.setMessage(strResponse2)
							.setPositiveButton("È·¶¨", null).show();
					return "";
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(mContext).setTitle("È·ÈÏ")
					.setMessage("»ñÈ¡·þÎñÆ÷APK°æ±¾¹ý³Ì³öÏÖÒì³£" + e.getMessage())
					.setPositiveButton("È·¶¨", null).show();
			return "";
		}
		return "";
	}

	/**
	 * 
	 * @return
	 */
	private boolean isUpdate() {
		int nLocalVersionCode = getVersionCode(mContext);
		
		String strServerVersionCode = GetServerAPKVersion();
		int nServerVersionCode = Integer.parseInt(strServerVersionCode);
		if (nServerVersionCode > nLocalVersionCode) {
			return true;
		}

		return false;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					"com.wisebox.gyb", 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 */
	private void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_update_title);
		builder.setMessage(R.string.soft_update_info);
		builder.setPositiveButton(R.string.soft_update_updatebtn,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showDownloadDialog();
					}
				});
		builder.setNegativeButton(R.string.soft_update_later,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 */
	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		builder.setNegativeButton(R.string.soft_update_cancel,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		downloadApk();
	}

	/**
	 */
	private void downloadApk() {
		new downloadApkThread().start();
	}

	/**
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// System.setProperty("http.keepAlive", "false");
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					mSavePath = sdpath + "download";

					URL url = new URL(GlobalInfo.m_APKURL);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();

					int length = conn.getContentLength();

					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, "LionTownHTM" + mVersion
							+ ".apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					byte buf[] = new byte[1024];
					do {
						int numread = is.read(buf);
						count += numread;
						progress = (int) (((float) count / length) * 100);
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);
					fos.close();
					is.close();
				}
				mDownloadDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, "LionTownHTM" + mVersion + ".apk");
		if (!apkfile.exists()) {			
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}
