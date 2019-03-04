package com.wisebox.gyb;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wisebox.gyb.AccessServer.OnReceiveDataListener;
import com.wisebox.gyb.utils.ParamsBuildUtils;
import com.wisebox.gyb.utils.XmlParseUtil;
import com.wisebox.gyb.utils.gsonObj.DeptMacJson;
import com.wisebox.gyb.utils.gsonObj.MacAddrJson;
import com.wisebox.gyb.utils.gsonObj.UserInfoJson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import javax.xml.validation.Validator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressLint("ShowToast")
public class LoginActivity extends Activity implements OnClickListener,
		OnReceiveDataListener {
	private static final String ACTION_YTO_SWITCH_STATUSBAR = "com.yto.action.STATUSBAR_SWITCH_STATE";// 下拉栏
	private static final String ACTION_YTO_SWITCH_HOME_KEY = "com.yto.action.HOMEKEY_SWITCH_STATE";// HOME键
	private static final String EXTRA_ENABLE = "enable";
	private ProgressDialog progressDialog = null;
	private static final int MESSAGETYPE_SUCCESS = 0x0001;
	private static final int MESSAGETYPE_ERROR = 0x0002;
	private static final int MESSAGETYPE_EXCEPTION = 0x0003;
	private EditText tbxAccount, tbxPassword;
	private CheckBox cbxAutoLogin;
	private String strResponse, strPhone, strPassword, strAccount;

	// private AccessServer pr=new AccessServer();

	/**
	 * Called when the activity is first created.
	 */
	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// 为了解决网络异常
		GlobalInfo.AuthenicateAccessInternet();

		// 下拉栏
		Intent intentStatusBar = new Intent(ACTION_YTO_SWITCH_STATUSBAR);
		intentStatusBar.putExtra(EXTRA_ENABLE, false);
		sendBroadcast(intentStatusBar);
		// home键
		Intent intentHome = new Intent(ACTION_YTO_SWITCH_HOME_KEY);
		intentHome.putExtra(EXTRA_ENABLE, false);
		sendBroadcast(intentHome);

		// pr.setOnReceiveDataListener(this);

		tbxAccount = (EditText) findViewById(R.id.tbxAccount);
		tbxPassword = (EditText) findViewById(R.id.tbxPassword);
		cbxAutoLogin = (CheckBox) this.findViewById(R.id.chkSaveAccountPwd);
		tbxAccount.setMovementMethod(ScrollingMovementMethod.getInstance());

		View btnLogin = this.findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(this);

		View btnExit = this.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(this);

		CheckBox cb = (CheckBox) this.findViewById(R.id.chkSaveAccountPwd);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					GlobalInfo.setEditTextReadOnly(tbxAccount, false);
					GlobalInfo.setEditTextReadOnly(tbxPassword, false);
				} else {
					GlobalInfo.setEditTextReadOnly(tbxAccount, true);
					GlobalInfo.setEditTextReadOnly(tbxPassword, true);
				}
			}
		});

		SharedPreferences sharedPre = getSharedPreferences("config",
				MODE_PRIVATE);
		String username = sharedPre.getString("username", "");
		String password = sharedPre.getString("password", "");
		String strSave = sharedPre.getString("saveinfo", "");

		tbxAccount.setText(username);
		tbxAccount.requestFocus();
		tbxAccount.setSelection(tbxAccount.getText().length());
		if (strSave.equals("1")) {
			tbxPassword.setText(password);

			cb.setChecked(true);
			Toast.makeText(this, "开启自动登录", Toast.LENGTH_SHORT);
			onLogin();
		}
	}

	/*
	 * // HOME
	 *
	 * @Override public void onAttachedToWindow() {
	 * this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	 * super.onAttachedToWindow(); }
	 *
	 * // 下拉
	 *
	 * @Override public void onWindowFocusChanged(boolean hasFocus) {
	 * super.onWindowFocusChanged(hasFocus); try { Object service =
	 * getSystemService("statusbar"); Class<?> statusbarManager =
	 * Class.forName("android.app.StatusBarManager"); Method test =
	 * statusbarManager.getMethod("collapse"); test.invoke(service); } catch
	 * (Exception ex) { ex.printStackTrace(); } }
	 */

	public String testReadAll(String strName) {
		// uri = content://com.android.contacts/contacts
		String strLocalPhoneList = "";
		Uri uri = Uri.parse("content://com.android.contacts/contacts"); // 访问raw_contacts表
		ContentResolver resolver = this.getBaseContext().getContentResolver();
		Cursor cursor = resolver.query(uri, new String[]{Data._ID}, null,
				null, null); // 获得_id属性
		while (cursor.moveToNext()) {
			StringBuilder buf = new StringBuilder();
			int id = cursor.getInt(0);// 获得id并且在data中寻找数据
			buf.append("id=" + id);
			uri = Uri.parse("content://com.android.contacts/contacts/" + id
					+ "/data"); // 如果要获得data表中某个id对应的数据，则URI为content://com.android.contacts/contacts/#/data
			Cursor cursor2 = resolver.query(uri, new String[]{Data.DATA1,
					Data.MIMETYPE}, null, null, null); // data1存储各个记录的总数据，mimetype存放记录的类型，如电话、email等
			String strN = "", strP = "";
			while (cursor2.moveToNext()) {
				String data = cursor2
						.getString(cursor2.getColumnIndex("data1"));

				if (cursor2.getString(cursor2.getColumnIndex("mimetype"))
						.equals("vnd.android.cursor.item/name")) { // 如果是名字
					strN = data;
				} else if (cursor2
						.getString(cursor2.getColumnIndex("mimetype")).equals(
								"vnd.android.cursor.item/phone_v2")) {
					strP = data;
				}
			}
			if (strN.equals(strName)) {
				strLocalPhoneList = strP;
				break;
			}
		}
		return strLocalPhoneList;
	}

	/**
	 * 使用SharedPreferences保存用户登录信息
	 *
	 * @param context
	 * @param username
	 * @param password
	 */
	public static void saveLoginInfo(Context context, String username,
									 String password, String strSaved) {
		// 获取SharedPreferences对象
		SharedPreferences sharedPre = context.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		// 获取Editor对象
		Editor editor = sharedPre.edit();
		// 设置参数
		editor.putString("username", username);
		editor.putString("password", password);
		editor.putString("saveinfo", strSaved);
		// 提交
		editor.commit();
	}

	private void onLogin() {

//		try {
//			strPhone = testReadAll("0本机");
//		} catch (Exception exp1) {
//			new AlertDialog.Builder(LoginActivity.this).setTitle("确认")
//					.setMessage("异常:" + exp1.getMessage()).setPositiveButton("确定", null).show();
//		}
//		if (strPhone.length() == 0) {
//			strPhone = "";
//		}else {
//			strPhone = strPhone.replace(" ", "");
//		}
		strPhone = "";
		GlobalInfo.m_LoginAccount = ((EditText) LoginActivity.this
				.findViewById(R.id.tbxAccount)).getText().toString();
		strPassword = ((EditText) LoginActivity.this
				.findViewById(R.id.tbxPassword)).getText().toString();

		if (GlobalInfo.m_LoginAccount.length() <= 0) {
			Toast.makeText(getApplicationContext(), "请输入工号", Toast.LENGTH_SHORT)
					.show();
			tbxAccount.requestFocus();
			return;
		}
		if (strPassword.length() <= 0) {
			Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT)
					.show();
			tbxPassword.requestFocus();
			return;
		}
		progressDialog = ProgressDialog.show(LoginActivity.this, "登录", "正在登录中,请稍候！");
		StringBuilder sbUploadData = new StringBuilder("");
		sbUploadData.append(GlobalInfo.m_LoginAccount);
		sbUploadData.append(GlobalInfo.m_SplitString);
		sbUploadData.append(strPassword);
		sbUploadData.append(GlobalInfo.m_SplitString);
		sbUploadData.append(strPhone);
		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appLogin.aspx?Method=Login&Value=" + strUploadData;
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().url(strURI).get().build();
			okHttpClient.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					if(progressDialog !=null) {
						progressDialog.dismiss();
					}
				}

				@Override
				public void onResponse(Call call, Response response) {

					try {
						Thread.sleep(500);
						if(progressDialog !=null) {
							progressDialog.dismiss();
						}
						if (response.isSuccessful()) {
							strResponse = response.body().string().trim();
							if (strResponse.startsWith("SUCCESS:")) {
								strResponse = strResponse.substring(
										strResponse.indexOf("SUCCESS:") + 8,
										strResponse.indexOf("SUCCESSEND"));
								String stra[] = strResponse.split(GlobalInfo.m_SplitString);
								GlobalInfo.m_PropID = stra[0];
								GlobalInfo.m_RoleCode = stra[1];
								GlobalInfo.m_RoleName = stra[2];
								GlobalInfo.m_PersonName = stra[3];
								GlobalInfo.m_DeptName = stra[4];
								GlobalInfo.m_DeptID = stra[5];
								GlobalInfo.m_VerifyCode = stra[6];
								GlobalInfo.m_DeptCode = GlobalInfo.m_VerifyCode;
								GlobalInfo.m_OnOffDutyState = stra[7];

								if (GlobalInfo.m_RoleCode.toLowerCase().equals(
										"hosschedule")) {
									// GetPhoneList();
								}

								FileService fs = new FileService(LoginActivity.this);
								try {
									fs.save(GlobalInfo.m_UserInfoFileName,
											GlobalInfo.m_LoginAccount
													+ GlobalInfo.m_SplitString
													+ GlobalInfo.m_PersonName);
								} catch (Exception exp1) {

								}
								//获取医院蓝牙设备地址
								getHospitalMac();
								String strSaved = "0";
								if (cbxAutoLogin.isChecked()) {
									strSaved = "1";
								}

								saveLoginInfo(LoginActivity.this,
										GlobalInfo.m_LoginAccount, strPassword, strSaved);
								handler.sendEmptyMessage(MESSAGETYPE_SUCCESS);
							} else {
								strResponse = strResponse.substring(
										strResponse.indexOf("ERROR:") + 8,
										strResponse.indexOf("ERROREND"));
								Message msg = new Message();
								msg.what = MESSAGETYPE_ERROR;
								Bundle data = new Bundle();
								data.putString("msg",strResponse);
								msg.setData(data);
								handler.sendMessage(msg);
							}
						} else {
							handler.sendEmptyMessage(MESSAGETYPE_EXCEPTION);
						}
					} catch (IOException exception) {
						Message msg = new Message();
						msg.what = MESSAGETYPE_ERROR;
						Bundle data = new Bundle();
						data.putString("msg",exception.getMessage());
						msg.setData(data);
						handler.sendMessage(msg);
					} catch (JSONException exception) {
						Message msg = new Message();
						msg.what = MESSAGETYPE_ERROR;
						Bundle data = new Bundle();
						data.putString("msg",exception.getMessage());
						msg.setData(data);
						handler.sendMessage(msg);
					}catch (Exception exception) {
						Message msg = new Message();
						msg.what = MESSAGETYPE_ERROR;
						Bundle data = new Bundle();
						data.putString("msg",exception.getMessage());
						msg.setData(data);
						handler.sendMessage(msg);
					}
				}
			});
	}


	public void testDelete(String strName) throws Exception {
		// 根据姓名求id
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = this.getBaseContext().getContentResolver();
		Cursor cursor = resolver.query(uri, new String[] { Data._ID },
				"display_name=?", new String[] { strName }, null);
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
		Cursor cursor = resolver.query(uri, new String[] { Data._ID }, null,
				null, null); // 获得_id属性
		while (cursor.moveToNext()) {
			StringBuilder buf = new StringBuilder();
			int id = cursor.getInt(0);// 获得id并且在data中寻找数据
			buf.append("id=" + id);
			uri = Uri.parse("content://com.android.contacts/contacts/" + id
					+ "/data"); // 如果要获得data表中某个id对应的数据，则URI为content://com.android.contacts/contacts/#/data
			Cursor cursor2 = resolver.query(uri, new String[] { Data.DATA1,
					Data.MIMETYPE }, null, null, null); // data1存储各个记录的总数据，mimetype存放记录的类型，如电话、email等
			while (cursor2.moveToNext()) {
				String data = cursor2
						.getString(cursor2.getColumnIndex("data1"));
				if (cursor2.getString(cursor2.getColumnIndex("mimetype"))
						.equals("vnd.android.cursor.item/name")) { // 如果是名字
					buf.append(",name=" + data);
					if (!data.contains("其他") && !data.contains("0本机")) {
						try {
							testDelete(data);
						} catch (Exception exp) {

						}
					}
				}
			}
		}
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
		StringBuilder sbUploadData = new StringBuilder(
				GlobalInfo.m_LoginAccount);

		String strUploadData = sbUploadData.toString();

		String strURI = "http://" + GlobalInfo.m_ServerIP
				+ "/appSysCode.aspx?Method=GetPhoneList&Value=" + strUploadData;
		HttpPost httpRequest = new HttpPost(strURI);

		try {
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			// 若状态码为200 ==> OK
			int nStatus = httpResponse.getStatusLine().getStatusCode();
			if (nStatus == 200) {
				String strResponse2 = EntityUtils.toString(
						httpResponse.getEntity()).trim();
				if (strResponse2.startsWith("SUCCESS")) {
					strResponse2 = strResponse2.substring(
							strResponse2.indexOf("SUCCESS:") + 8,
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
							new AlertDialog.Builder(this).setTitle("确认")
									.setMessage("更新成功")
									.setPositiveButton("确定", null).show();
					}
					return true;
				} else if (strResponse2.startsWith("ERROR")) {
					strResponse2 = strResponse2.substring(
							strResponse2.indexOf("ERROR:") + 6,
							strResponse2.indexOf("ERROREND"));
					new AlertDialog.Builder(this).setTitle("确认")
							.setMessage(strResponse2)
							.setPositiveButton("确定", null).show();
					return false;
				}
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this).setTitle("确认")
					.setMessage("获取任务过程出现异常" + e.getMessage())
					.setPositiveButton("确定", null).show();
			return false;
		}finally {
			progressDialog.dismiss(); // 关闭进度条
		}
		return false;
	}

	// 添加联系人，使用事务
	public void testAddContact(String strName, String strPhone)
			throws Exception {
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		ContentResolver resolver = getBaseContext().getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
				.withValue("account_name", null).build();
		operations.add(op1);

		uri = Uri.parse("content://com.android.contacts/data");
		ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/name")
				.withValue("data2", strName).build();
		operations.add(op2);

		ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
				.withValueBackReference("raw_contact_id", 0)
				.withValue("mimetype", "vnd.android.cursor.item/phone_v2")
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLogin:
			onLogin();
			break;
		case R.id.btnExit:
			String str = tbxPassword.getText().toString();
			if (str.equals("52581051"))
				System.exit(0);
			else {
			}
			break;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MESSAGETYPE_SUCCESS:
				// 刷新UI，显示数据，并关闭进度条
				if(progressDialog!=null)
				progressDialog.dismiss(); // 关闭进度条

				Toast.makeText(getApplicationContext(), strPhone + " 登录成功",
							Toast.LENGTH_SHORT).show();
				Intent i = new Intent(LoginActivity.this,
							TaskListActivity.class);
				startActivity(i);
				break;
			case MESSAGETYPE_ERROR:
				if(progressDialog!=null)
					progressDialog.dismiss(); // 关闭进度条
				new AlertDialog.Builder(LoginActivity.this).setTitle("确认")
						.setMessage("错误:"+message.getData().getString("msg")).setPositiveButton("确定", null).show();
				break;
			case MESSAGETYPE_EXCEPTION:
				if(progressDialog!=null)
					progressDialog.dismiss(); // 关闭进度条
				new AlertDialog.Builder(LoginActivity.this).setTitle("确认")
						.setMessage("异常").setPositiveButton("确定", null).show();
				break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;

		return super.onKeyDown(keyCode, event);// return false;
	}

	@Override
	public void onReceiveData(String strResponse, int StatusCode,
			String strRequest) {
		if (StatusCode == 200) {

			if (strResponse.startsWith("SUCCESS:")) {
				strResponse = strResponse.substring(
						strResponse.indexOf("SUCCESS:") + 8,
						strResponse.indexOf("SUCCESSEND"));
				String stra[] = strResponse.split(GlobalInfo.m_SplitString);
				GlobalInfo.m_PropID = stra[0];
				GlobalInfo.m_RoleCode = stra[1];
				GlobalInfo.m_RoleName = stra[2];
				GlobalInfo.m_PersonName = stra[3];
				GlobalInfo.m_DeptName = stra[4];
				GlobalInfo.m_DeptID = stra[5];
				GlobalInfo.m_VerifyCode = stra[6];
				GlobalInfo.m_OnOffDutyState = stra[7];

				if (GlobalInfo.m_RoleCode.toLowerCase().equals("hosschedule"))
					GetPhoneList();

				FileService fs = new FileService(this);
				try {
					fs.save(GlobalInfo.m_UserInfoFileName,
							GlobalInfo.m_LoginAccount
									+ GlobalInfo.m_SplitString
									+ GlobalInfo.m_PersonName);
				} catch (Exception exp) {

				}
				CheckBox cb = (CheckBox) this
						.findViewById(R.id.chkSaveAccountPwd);
				String strSaved = "0";
				if (cb.isChecked()) {
					strSaved = "1";
				}

				saveLoginInfo(LoginActivity.this, GlobalInfo.m_LoginAccount,
						"1", strSaved);

				Toast.makeText(getApplicationContext(),
						GlobalInfo.m_LoginAccount + " 登录成功", Toast.LENGTH_SHORT)
						.show();
				Intent i = new Intent(this, TaskListActivity.class);
				startActivity(i);
			} else if (strResponse.startsWith("ERROR")) {
				strResponse = strResponse.substring(
						strResponse.indexOf("ERROR:") + 6,
						strResponse.indexOf("ERROREND"));
				new AlertDialog.Builder(this).setTitle("确认")
						.setMessage(strResponse).setPositiveButton("确定", null)
						.show();
			}
		} else {
			new AlertDialog.Builder(this).setTitle("确认").setMessage("错误")
					.setPositiveButton("确定", null).show();
		}
	}

	private void getHospitalMac() throws IOException, JSONException {
		OkHttpClient client = new OkHttpClient();
		String url = GlobalInfo.NEW_API_URL + "wsformicromsg.asmx?wsdl";
		JSONObject obj = new JSONObject();
		obj.put("HospitalCode", GlobalInfo.m_DeptCode);
		RequestBody body = RequestBody.create(MediaType.get("text/xml"), ParamsBuildUtils.bodyBuild("GetMacAddressByHospitalCode",obj.toString()));
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		Response response = client.newCall(request).execute();
		if(response.isSuccessful()) {
			String result = response.body().string();
			String json = XmlParseUtil.getSoapResult(result, "GetMacAddressByHospitalCode");
			Log.w("SOAP Result", "GetMacAddressByHospitalCode："+json);
			if(!TextUtils.isEmpty(json)) {
				Gson gson = new Gson();
				MacAddrJson macData = gson.fromJson(json, MacAddrJson.class);
				for(DeptMacJson item: macData.dsData.Table) {
					DeptMacJson mac = new DeptMacJson();
					mac.PropName = item.PropName;
					mac.Mac = item.Mac;
					mac.PropCode = item.PropCode;
					mac.PropID = item.PropID;
					MyApp.addMacItem(mac);
				}
			}
		}
	}
}